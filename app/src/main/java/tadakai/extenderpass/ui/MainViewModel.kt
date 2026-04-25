package tadakai.extenderpass.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tadakai.extenderpass.core.PasswordGenerator

class MainViewModel : ViewModel() {

    // ── Inputs ────────────────────────────────────────────────────────────────

    var seed by mutableStateOf("")
        private set

    var length by mutableIntStateOf(PasswordGenerator.DEFAULT_LENGTH)
        private set

    var charsetOption by mutableStateOf(PasswordGenerator.CharsetOption.ALL)
        private set

    // ── UI state ──────────────────────────────────────────────────────────────

    var generatedPassword by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var seedVisible by mutableStateOf(false)
        private set

    var resultVisible by mutableStateOf(false)
        private set

    // ── Input handlers ────────────────────────────────────────────────────────

    fun onSeedChange(value: String) {
        seed = value
        clearResult()
    }

    fun onLengthChange(value: Int) {
        if (value in PasswordGenerator.MIN_LENGTH..PasswordGenerator.MAX_LENGTH) {
            length = value
            clearResult()
        }
    }

    fun onLengthTextChange(text: String) {
        text.toIntOrNull()?.let { onLengthChange(it) }
    }

    fun onCharsetChange(option: PasswordGenerator.CharsetOption) {
        charsetOption = option
        clearResult()
    }

    fun toggleSeedVisibility() {
        seedVisible = !seedVisible
    }

    // ── Generation ────────────────────────────────────────────────────────────

    fun generate() {
        if (seed.isBlank()) {
            errorMessage = "Please enter a password or phrase."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // PBKDF2 is CPU-intensive — run off the main thread
                val result = withContext(Dispatchers.Default) {
                    PasswordGenerator.generate(seed.trim(), length, charsetOption)
                }
                generatedPassword = result
                resultVisible     = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun clearResult() {
        generatedPassword = ""
        resultVisible     = false
        errorMessage      = null
    }

    fun clearError() {
        errorMessage = null
    }
}
