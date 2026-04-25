package tadakai.extenderpass.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tadakai.extenderpass.core.PasswordGenerator
import tadakai.extenderpass.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel) {
    val context       = LocalContext.current
    val focusManager  = LocalFocusManager.current
    val snackbarState = remember { SnackbarHostState() }

    // Show errors in Snackbar
    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let {
            snackbarState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("ExtenderPass", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Subtitle ───────────────────────────────────────────────────
            Text(
                text  = "Turn any short password into a strong, unique one.\n" +
                        "Same input always produces the same output.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Seed input ────────────────────────────────────────────────
            SectionLabel("Password / Phrase")
            OutlinedTextField(
                value         = vm.seed,
                onValueChange = vm::onSeedChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("Password") },
                singleLine    = true,
                visualTransformation = if (vm.seedVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon  = {
                    IconButton(onClick = vm::toggleSeedVisibility) {
                        Icon(
                            imageVector = if (vm.seedVisible) Icons.Default.VisibilityOff
                                          else Icons.Default.Visibility,
                            contentDescription = if (vm.seedVisible) "Hide" else "Show"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp)
            )

            // ── Length slider ─────────────────────────────────────────────
            SectionLabel("Output Length: ${vm.length} characters")
            Slider(
                value         = vm.length.toFloat(),
                onValueChange = { vm.onLengthChange(it.toInt()) },
                valueRange    = PasswordGenerator.MIN_LENGTH.toFloat()..128f,
                steps         = 0,
                modifier      = Modifier.fillMaxWidth()
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "1",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "128",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Charset chips ─────────────────────────────────────────────
            SectionLabel("Character Set")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                PasswordGenerator.CharsetOption.values().forEach { option ->
                    FilterChip(
                        selected = vm.charsetOption == option,
                        onClick  = { vm.onCharsetChange(option) },
                        label    = { Text(option.label, fontSize = 12.sp) }
                    )
                }
            }

            // ── Generate button ───────────────────────────────────────────
            Button(
                onClick  = {
                    focusManager.clearFocus()
                    vm.generate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !vm.isLoading,
                shape   = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                if (vm.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Computing…")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generate", fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Result card ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = vm.resultVisible && vm.generatedPassword.isNotEmpty(),
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                ResultCard(
                    password = vm.generatedPassword,
                    onCopy   = { copyToClipboard(context, vm.generatedPassword) }
                )
            }

            // ── Info card ─────────────────────────────────────────────────
            InfoCard()

            Spacer(Modifier.height(16.dp))

            // ── Footer ────────────────────────────────────────────────────
            Text(
                text      = "Created by Tadakai · github.com/tadakai/extenderpass-app",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Result card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResultCard(password: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Generated Password",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector   = Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        tint          = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(14.dp)
            ) {
                Text(
                    text       = password,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 15.sp,
                    color      = MaterialTheme.colorScheme.onSurface,
                    overflow   = TextOverflow.Visible
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text  = "${password.length} characters · PBKDF2-HMAC-SHA512 · 300 000 iterations",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Info card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector        = Icons.Default.Info,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier           = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text  = "ExtenderPass is 100% deterministic and works offline. " +
                        "Your password is never stored or transmitted. " +
                        "The same input always produces the exact same output on " +
                        "any device or future version of this app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val cm   = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("ExtenderPass", text)
    cm.setPrimaryClip(clip)

    // On Android 13+ the OS shows a visual confirmation automatically
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
