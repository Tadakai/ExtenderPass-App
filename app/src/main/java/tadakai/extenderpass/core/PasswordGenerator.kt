package tadakai.extenderpass.core

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil

object PasswordGenerator {

    // =========================================================================
    // FIXED CONSTANTS — changing ANY of these breaks backward compatibility
    // =========================================================================

    /** Public, immutable salt */
    private val SALT: ByteArray = "extenderpass_fixed_salt_v5".toByteArray(Charsets.UTF_8)

    /** PBKDF2 iteration count. */
    private const val ITERATIONS = 300_000

    /** Maximum allowed output length (safety cap). */
    const val MAX_LENGTH = 10_000

    /** Minimum allowed output length. */
    const val MIN_LENGTH = 1

    /** Default output length. */
    const val DEFAULT_LENGTH = 20

    /** Suffix appended when a deterministic fallback is needed (extremely rare). */
    private const val FALLBACK_SEED_SUFFIX = ":fallback"

    // =========================================================================
    // CHARACTER SETS — order and content must never change
    // =========================================================================

    private const val DIGITS    = "0123456789"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val SYMBOLS   = "!@#\$%^&*()-_=+[]{};:,.<>?"

    private const val LETTERS  = LOWERCASE + UPPERCASE
    private const val ALPHANUM = LETTERS + DIGITS
    private const val ALL      = ALPHANUM + SYMBOLS

    // =========================================================================
    // Public API
    // =========================================================================

    /** Character-set options exposed to the UI. */
    enum class CharsetOption(val label: String, val description: String) {
        ALL("All",     "Letters + digits + symbols"),
        ALPHANUM("A-Z 0-9", "Letters + digits only"),
        LETTERS("A-Z",  "Letters only"),
        DIGITS("0-9",   "Digits only")
    }

    fun charsetFor(option: CharsetOption): String = when (option) {
        CharsetOption.ALL      -> ALL
        CharsetOption.ALPHANUM -> ALPHANUM
        CharsetOption.LETTERS  -> LETTERS
        CharsetOption.DIGITS   -> DIGITS
    }

    /**
     * Generates a deterministic, high-entropy string from [seed].
     *
     * @param seed   The master password / phrase (UTF-8 encoded before hashing).
     * @param length Desired output length (1..MAX_LENGTH).
     * @param option Which character set to use.
     * @return       The generated string — always identical for the same inputs.
     */
    fun generate(seed: String, length: Int, option: CharsetOption): String {
        require(seed.isNotEmpty()) { "Seed must not be empty" }
        require(length in MIN_LENGTH..MAX_LENGTH) {
            "Length must be between $MIN_LENGTH and $MAX_LENGTH"
        }

        val charset    = charsetFor(option)
        val charsetLen = charset.length

        // Rejection-sampling threshold: largest multiple of charsetLen below 256.
        // This guarantees a perfectly uniform distribution (no modulo bias).
        val maxValid    = 256 - (256 % charsetLen)
        val useRejection = maxValid != 0

        val neededBytes = if (useRejection) estimateBytes(length, charsetLen) else length * 4
        val rawBytes    = deriveBytes(seed, neededBytes)

        val result = ArrayList<Char>(length)
        var idx = 0

        while (result.size < length && idx < rawBytes.size) {
            val b = rawBytes[idx].toInt() and 0xFF
            idx++
            if (!useRejection || b < maxValid) {
                result.add(charset[b % charsetLen])
            }
        }

        // Fallback: extremely rare — only fires if rejection discards all initial bytes.
        var fallbackCount = 0
        while (result.size < length) {
            fallbackCount++
            val extraSeed  = "$seed$FALLBACK_SEED_SUFFIX$fallbackCount"
            val extraBytes = deriveBytes(extraSeed, length * 4)
            for (b in extraBytes) {
                val bInt = b.toInt() and 0xFF
                if (!useRejection || bInt < maxValid) {
                    result.add(charset[bInt % charsetLen])
                    if (result.size == length) break
                }
            }
        }

        return result.joinToString("")
    }

    private fun estimateBytes(length: Int, charsetLen: Int): Int {
        val maxValid       = 256 - (256 % charsetLen)
        if (maxValid == 0) return length * 256
        val acceptanceRate = maxValid / 256.0
        val needed         = ceil(length / acceptanceRate).toInt() * 2
        return maxOf(needed, length * 2)
    }

    /**
     * Wraps [pbkdf2HmacSha512] with the app's fixed salt and iteration count.
     */
    private fun deriveBytes(seed: String, dkLen: Int): ByteArray =
        pbkdf2HmacSha512(
            password   = seed.toByteArray(Charsets.UTF_8),
            salt       = SALT,
            iterations = ITERATIONS,
            dkLen      = dkLen
        )

    private fun pbkdf2HmacSha512(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        dkLen: Int
    ): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(password, "HmacSHA512"))

        val hashLen = 64                              // SHA-512 block = 64 bytes
        val blocks  = (dkLen + hashLen - 1) / hashLen
        val result  = ByteArray(dkLen)

        for (block in 1..blocks) {
            // U1 = PRF(Password, Salt || INT(block))
            mac.update(salt)
            mac.update(
                byteArrayOf(
                    (block ushr 24 and 0xFF).toByte(),
                    (block ushr 16 and 0xFF).toByte(),
                    (block ushr 8  and 0xFF).toByte(),
                    (block         and 0xFF).toByte()
                )
            )
            var u = mac.doFinal()   // doFinal resets the Mac automatically
            val t = u.copyOf()      // T_i starts as U1

            // U2..Uc — XOR each successive PRF output into T_i
            repeat(iterations - 1) {
                u = mac.doFinal(u)
                for (j in t.indices) {
                    t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
                }
            }

            // Copy relevant bytes of T_i into result
            val offset = (block - 1) * hashLen
            val len    = minOf(hashLen, dkLen - offset)
            t.copyInto(result, destinationOffset = offset, startIndex = 0, endIndex = len)
        }

        return result
    }
}
