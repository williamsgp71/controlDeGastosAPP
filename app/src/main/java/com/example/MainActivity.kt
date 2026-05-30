package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Category
import com.example.data.model.ExpenseTransaction
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Global representation of Screens
enum class Screen {
    LOGIN,
    REGISTER,
    CHANGE_PASSWORD,
    DASHBOARD
}

@Composable
fun AppNavigation() {
    val viewModel: ExpenseViewModel = viewModel()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

    // Synchronize logout/login states
    LaunchedEffect(loggedInUser) {
        if (loggedInUser != null) {
            currentScreen = Screen.DASHBOARD
        } else if (currentScreen == Screen.DASHBOARD) {
            currentScreen = Screen.LOGIN
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CosmicDarkBg)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransitions"
            ) { screen ->
                when (screen) {
                    Screen.LOGIN -> LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = { currentScreen = Screen.REGISTER },
                        onNavigateToChangePassword = { currentScreen = Screen.CHANGE_PASSWORD }
                    )
                    Screen.REGISTER -> RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = { currentScreen = Screen.LOGIN }
                    )
                    Screen.CHANGE_PASSWORD -> ChangePasswordScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.LOGIN }
                    )
                    Screen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ---------------- LOGIN SCREEN ----------------
@Composable
fun LoginScreen(
    viewModel: ExpenseViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToChangePassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showFingerprintMockDialog by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Auto-authenticate via fingerprint if biometrics enabled for latest user
    LaunchedEffect(Unit) {
        if (viewModel.loginSuccess.value) {
            // Checked via synchronization in navigation
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Large icon container
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(CosmicCardBg, RoundedCornerShape(24.dp))
                .border(2.dp, CosmicBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Wallet,
                contentDescription = "Wallet Icon",
                tint = CosmicAccentPurple,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Control de Gastos",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = CosmicTextLight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Administra tus finanzas personales con arquitectura desacoplada para app móvil",
            fontSize = 14.sp,
            color = CosmicTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Main Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // CORREO
                Text(
                    text = "CORREO ELECTRÓNICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("ejemplo@correo.com", color = CosmicTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedContainerColor = CosmicDarkBg,
                        unfocusedContainerColor = CosmicDarkBg,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = CosmicTextMuted)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CONTRASEÑA
                Text(
                    text = "CONTRASEÑA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = CosmicTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedContainerColor = CosmicDarkBg,
                        unfocusedContainerColor = CosmicDarkBg,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CosmicTextMuted)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = CosmicTextMuted
                            )
                        }
                    }
                )

                // Error login representation
                loginError?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = CosmicAccentRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Login Button & Fingerprint side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentPurple),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Iniciar Sesión", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Fingerprint Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CosmicDarkBg)
                            .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                if (email.isBlank()) {
                                    Toast.makeText(context, "Por favor, introduce tu correo electrónico primero", Toast.LENGTH_SHORT).show()
                                } else {
                                    triggerBiometricAuthentication(
                                        activity = context as FragmentActivity,
                                        viewModel = viewModel,
                                        email = email,
                                        onBiometricUnavailable = {
                                            showFingerprintMockDialog = true
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Login Fingerprint",
                            tint = CosmicAccentGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation helpers
                Text(
                    text = "¿No tienes cuenta? Registrate gratis ahora",
                    fontSize = 13.sp,
                    color = CosmicAccentPurple,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRegister() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cambiar Contraseña",
                    fontSize = 12.sp,
                    color = CosmicTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToChangePassword() }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Shield Decoupled API footer decoration
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CosmicAccentGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "API Desacoplada con Auth JWT (Segura para Apps Móviles)",
                color = CosmicTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Fingerprint Mock Simulation dialog
    if (showFingerprintMockDialog) {
        Dialog(onDismissRequest = { showFingerprintMockDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint scanning simulation",
                        tint = CosmicAccentGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Simulación de Sensor de Huella",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "El dispositivo no cuenta con sensor biométrico enrolado. Puede simular un inicio de sesión biométrico rápido completándolo con la cuenta de correo escrita arriba.",
                        fontSize = 13.sp,
                        color = CosmicTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Tu email para login biométrico:",
                        fontSize = 11.sp,
                        color = CosmicTextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = CosmicTextLight, unfocusedTextColor = CosmicTextLight)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showFingerprintMockDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicTextMuted),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (email.isBlank()) {
                                    Toast.makeText(context, "Por favor introduce un correo", Toast.LENGTH_SHORT).show()
                                } else {
                                    showFingerprintMockDialog = false
                                    // Try biometric login
                                    viewModel.loginBiometricDirect(email)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentGreen),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Simular Éxito")
                        }
                    }
                }
            }
        }
    }
}

// ---------------- REGISTER SCREEN ----------------
@Composable
fun RegisterScreen(
    viewModel: ExpenseViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerError by viewModel.registerError.collectAsStateWithLifecycle()
    val registerSuccess by viewModel.registerSuccess.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(registerSuccess) {
        registerSuccess?.let { successMsg ->
            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
            viewModel.logout() // clear registration/success states
            onNavigateToLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(CosmicCardBg, RoundedCornerShape(16.dp))
                .border(2.dp, CosmicBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Register icon",
                tint = CosmicAccentPurple,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Crear Cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CosmicTextLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registra un nuevo usuario para controlar tus gastos",
            fontSize = 14.sp,
            color = CosmicTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // CORREO
                Text(
                    text = "CORREO ELECTRÓNICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("ejemplo@correo.com", color = CosmicTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CONTRASEÑA
                Text(
                    text = "CONTRASEÑA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = CosmicTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CONFIRMAR CONTRASEÑA
                Text(
                    text = "CONFIRMAR CONTRASEÑA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("••••••••", color = CosmicTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                // Feedback
                registerError?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = it, color = CosmicAccentRed, fontSize = 13.sp)
                }
                registerSuccess?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = it, color = CosmicAccentGreen, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.register(email, password, confirmPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registrarse gratis", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "¿Ya tienes una cuenta? Inicia sesión aquí",
                    fontSize = 13.sp,
                    color = CosmicAccentPurple,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

// ---------------- CHANGE PASSWORD SCREEN ----------------
@Composable
fun ChangePasswordScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val changePasswordStatus by viewModel.changePasswordStatus.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(CosmicCardBg, RoundedCornerShape(16.dp))
                .border(2.dp, CosmicBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = "Password reset icon",
                tint = CosmicAccentPurple,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cambiar Contraseña",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = CosmicTextLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Modifica la contraseña actual para un acceso seguro",
            fontSize = 13.sp,
            color = CosmicTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // CORREO
                Text(
                    text = "CORREO DEL USUARIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // CONTRASEÑA ACTUAL
                Text(
                    text = "CONTRASEÑA ACTUAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // NUEVA CONTRASEÑA
                Text(
                    text = "NUEVA CONTRASEÑA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // CONFIRMAR NUEVA CONTRASEÑA
                Text(
                    text = "CONFIRMAR NUEVA CONTRASEÑA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextLight,
                        unfocusedTextColor = CosmicTextLight,
                        focusedBorderColor = CosmicAccentPurple,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                changePasswordStatus?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = if (it.contains("exitosa", ignoreCase = true)) CosmicAccentGreen else CosmicAccentRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.changePassword(email, oldPassword, newPassword, confirmNewPassword)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Actualizar Contraseña", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.forceResetPassword(email, newPassword)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, CosmicBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Forzar Restablecer (Demo)", color = CosmicAccentPurple, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Volver al inicio de sesión",
                    fontSize = 13.sp,
                    color = CosmicAccentPurple,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateBack() }
                )
            }
        }
    }
}

// ---------------- DASHBOARD SCREEN ----------------
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel
) {
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categoriesList by viewModel.categories.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()

    val analysisCurrency by viewModel.selectedAnalysisCurrency.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle() // Triple(balance, income, expense)

    var showBudgetDialog by remember { mutableStateOf(false) }
    var dashboardSection by remember { mutableStateOf("resumen") } // "resumen", "operaciones", "historial"

    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsStateWithLifecycle()
    val latestVersionName by viewModel.latestVersionName.collectAsStateWithLifecycle()
    val updateNotes by viewModel.updateNotes.collectAsStateWithLifecycle()

    if (isUpdateAvailable) {
        Dialog(onDismissRequest = { viewModel.dismissUpdateDialog() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Actualización Disponible",
                        tint = CosmicAccentPurple,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Nueva Actualización!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTextLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Versión disponible: v$latestVersionName",
                        fontSize = 13.sp,
                        color = CosmicAccentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Release notes card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicDarkBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Novedades:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = updateNotes,
                                fontSize = 11.sp,
                                color = CosmicTextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.dismissUpdateDialog() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicTextMuted),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Más tarde")
                        }
                        Button(
                            onClick = {
                                viewModel.startUpdateDownload()
                                viewModel.dismissUpdateDialog()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentPurple),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Actualizar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // TOP CUSTOM HEADER (Title, API REST CENTRALIZADA Badge, user profile williams, Cerrar Sesión)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = CosmicAccentPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Control de Gastos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTextLight
                    )
                }
            }

            // User chip & Log out
            Row(verticalAlignment = Alignment.CenterVertically) {
                // williams style chip
                val nameDisplay = loggedInUser?.email?.substringBefore("@") ?: "invitado"
                Row(
                    modifier = Modifier
                        .background(CosmicCardBg, RoundedCornerShape(12.dp))
                        .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = CosmicTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = nameDisplay,
                        color = CosmicTextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Logout button
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .background(CosmicAccentRed.copy(alpha = 0.1f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar Sesión",
                        tint = CosmicAccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        val isBiometricEnabled = loggedInUser?.isBiometricEnabled ?: false
        BiometricPromptCard(
            isBiometricEnabled = isBiometricEnabled,
            onEnable = { enabled ->
                loggedInUser?.let { user ->
                    viewModel.setBiometricEnabled(user.id, enabled)
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(CosmicCardBg, RoundedCornerShape(12.dp))
                .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val sections = listOf(
                Triple("resumen", "Análisis & Gráficos", Icons.Default.Analytics),
                Triple("operaciones", "Agregar Movimiento", Icons.Default.AddCircle),
                Triple("historial", "Historial & Listas", Icons.Default.List)
            )
            sections.forEach { (route, label, icon) ->
                val isSel = dashboardSection == route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSel) CosmicAccentPurple else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { dashboardSection = route }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSel) Color(0xFF381E72) else CosmicAccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color(0xFF381E72) else CosmicTextLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (dashboardSection == "resumen") {
            // 1. BANNER: MONEDA DE ANÁLISIS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Paid, contentDescription = null, tint = CosmicAccentPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MONEDA DE ANÁLISIS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextLight
                            )
                        }
                        Text(
                            text = "Presiona un botón para seleccionar la moneda principal de tus métricas",
                            fontSize = 10.sp,
                            color = CosmicTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeColor = CosmicAccentPurple
                        val inactiveColor = CosmicDarkBg

                        Button(
                            onClick = { viewModel.selectedAnalysisCurrency.value = "S/" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (analysisCurrency == "S/") activeColor else inactiveColor,
                                contentColor = if (analysisCurrency == "S/") Color(0xFF381E72) else CosmicTextLight
                            ),
                            border = BorderStroke(1.dp, if (analysisCurrency == "S/") CosmicAccentPurple else CosmicBorder),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (analysisCurrency == "S/") {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF381E72))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("Soles (S/)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.selectedAnalysisCurrency.value = "$" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (analysisCurrency == "$") activeColor else inactiveColor,
                                contentColor = if (analysisCurrency == "$") Color(0xFF381E72) else CosmicTextLight
                            ),
                            border = BorderStroke(1.dp, if (analysisCurrency == "$") CosmicAccentPurple else CosmicBorder),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (analysisCurrency == "$") {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF381E72))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("Dólares ($)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. BANNER: FILTRAR POR PERIODO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CosmicAccentPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FILTRAR POR PERIODO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextLight
                            )
                        }
                        // Quick Set Budget Button
                        Button(
                            onClick = { showBudgetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Modificar Presupuesto", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Filtra y analiza tus finanzas por mes y año seleccionado",
                        fontSize = 10.sp,
                        color = CosmicTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // AÑO dropdown / select
                        Box(modifier = Modifier.weight(1f)) {
                            PeriodSelector(
                                label = "AÑO",
                                selectedValue = if (selectedYear == 0) "Todos los años" else selectedYear.toString(),
                                options = listOf("Todos los años", "2210", "2026", "2025", "2024", "2023"),
                                onSelected = {
                                    if (it == "Todos los años") viewModel.selectedYear.value = 0
                                    else viewModel.selectedYear.value = it.toInt()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // MES dropdown / select
                        Box(modifier = Modifier.weight(1f)) {
                            val spanishMonths = listOf(
                                "Todos los meses", "Enero", "Febrero", "Marzo", "Abril", "Mayo",
                                "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
                            )
                            PeriodSelector(
                                label = "MES",
                                selectedValue = spanishMonths[selectedMonth],
                                options = spanishMonths,
                                onSelected = {
                                    val idx = spanishMonths.indexOf(it)
                                    viewModel.selectedMonth.value = idx
                                }
                            )
                        }
                    }
                }
            }

            // 3. STATS BALANCES CARDS Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val balance = stats.first
                val income = stats.second
                val expense = stats.third

                // Balance card
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        title = "BALANCE GENERAL",
                        value = formatCurrency(balance, analysisCurrency),
                        description = "Flujo neto disponible actualmente.",
                        icon = Icons.Default.MonetizationOn,
                        iconColor = CosmicAccentGreen
                    )
                }

                // Income Card
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        title = "INGRESOS TOTALES",
                        value = "+ " + formatCurrency(income, analysisCurrency),
                        description = "Sumatoria de todos los ingresos activos.",
                        icon = Icons.Default.TrendingUp,
                        iconColor = CosmicAccentGreen
                    )
                }

                // Expense Card
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(
                        title = "EGRESOS TOTALES",
                        value = "- " + formatCurrency(expense, analysisCurrency),
                        description = "Sumatoria de egresos acumulados.",
                        icon = Icons.Default.TrendingDown,
                        iconColor = CosmicAccentRed
                    )
                }
            }

            // 4. CHARTS VISUAL PANEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Historial de Flujo de Caja Card
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(262.dp)
                        .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Historial de Flujo de Caja", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
                                Text("Últimos meses registrados", fontSize = 9.sp, color = CosmicTextMuted)
                            }
                            // Small indicators label
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(CosmicAccentGreen, CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Ingresos", fontSize = 8.sp, color = CosmicTextMuted)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(6.dp).background(CosmicAccentRed, CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Gastos", fontSize = 8.sp, color = CosmicTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val monthlyData by viewModel.monthlyFlowData.collectAsStateWithLifecycle()

                        if (monthlyData.isEmpty()) {
                            // Empty states representation with Custom Micro Sine Waves
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Canvas(modifier = Modifier.size(80.dp, 30.dp)) {
                                        val width = size.width
                                        val height = size.height
                                        val path = androidx.compose.ui.graphics.Path()
                                        path.moveTo(0f, height / 2)
                                        for (i in 0..100) {
                                            val x = (i.toFloat() / 100f) * width
                                            val y = (height / 2) + Math.sin(i.toDouble() * 0.15).toFloat() * (height / 3)
                                            path.lineTo(x, y)
                                        }
                                        drawPath(
                                            path = path,
                                            color = CosmicAccentPurple,
                                            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        "No hay suficientes datos históricos aún",
                                        color = CosmicTextMuted,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Draw custom bar graphics on Canvas
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val monthLabels = listOf("", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

                                    for ((month, pair) in monthlyData) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Find max to scale
                                            val maxVal = monthlyData.values.flatMap { listOf(it.first, it.second) }.maxOrNull() ?: 1.0
                                            val scaleFactor = (if (maxVal == 0.0) 1.0 else maxVal)

                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.height(120.dp)
                                            ) {
                                                // Income bar (green)
                                                val incomeHeightPercent = (pair.first / scaleFactor).toFloat().coerceIn(0.01f, 1.0f)
                                                Box(
                                                    modifier = Modifier
                                                        .width(6.dp)
                                                        .fillMaxHeight(incomeHeightPercent)
                                                        .background(CosmicAccentGreen, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                                )
                                                // Expense bar (red)
                                                val expenseHeightPercent = (pair.second / scaleFactor).toFloat().coerceIn(0.01f, 1.0f)
                                                Box(
                                                    modifier = Modifier
                                                        .width(6.dp)
                                                        .fillMaxHeight(expenseHeightPercent)
                                                        .background(CosmicAccentRed, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = monthLabels.getOrElse(month) { month.toString() },
                                                fontSize = 8.sp,
                                                color = CosmicTextMuted,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Distribución de Gastos (Donut/Remaining Target comparison)
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(262.dp)
                        .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Distribución de Gastos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
                                Text("Categorías de egresos con sus montos", fontSize = 8.sp, color = CosmicTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compute Percentage of Remaining budget vs savings/expenses
                        val limitAmount = selectedBudget?.amount ?: 0.0
                        val currentExpensesSum = stats.third

                        val percentDisplay = if (limitAmount > 0) {
                            val pct = ((limitAmount - currentExpensesSum) / limitAmount) * 100
                            pct.coerceIn(0.0, 100.0)
                        } else 0.0

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicAccentRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Ahorrado / Restante: ${percentDisplay.toInt()}%",
                                color = CosmicAccentRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val donutData by viewModel.categoryDistributionData.collectAsStateWithLifecycle()

                        if (donutData.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Outlined.Analytics, contentDescription = null, tint = CosmicTextMuted, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Registra gastos para ver la distribución aquí",
                                        color = CosmicTextMuted,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Drawing a customized visual horizontal percentages bar list to show category consumption clearly
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                donutData.take(5).forEach { (catName, percent) ->
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(catName, fontSize = 9.sp, color = CosmicTextLight, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            Text("${percent.toInt()}%", fontSize = 9.sp, color = CosmicTextMuted, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        // Simulated bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .background(CosmicDarkBg, RoundedCornerShape(2.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth((percent / 100f).toFloat().coerceIn(0f, 1f))
                                                    .height(5.dp)
                                                    .background(CosmicAccentPurple, RoundedCornerShape(2.dp))
                                            )
                                        }
                                    }
                                }
                                if (donutData.size > 5) {
                                    Text("+ otros gastos...", fontSize = 8.sp, color = CosmicTextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
        } else if (dashboardSection == "operaciones") {
            // Secciones de operaciones: Agregar Movimiento & Crear Categoría
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TransactionCreator(viewModel = viewModel, categoriesBytes = categoriesList)
                CategoryCreator(viewModel = viewModel)
            }
        } else {
            // Secciones de historial / listas
            RightSideTabbedController(viewModel = viewModel, transactionsBytes = transactions, categoriesBytes = categoriesList)
        }

        Spacer(modifier = Modifier.height(45.dp))
    }

    // Modal budget editor
    if (showBudgetDialog) {
        var budgetInput by remember { mutableStateOf(if (selectedBudget?.amount != null) selectedBudget!!.amount.toInt().toString() else "1500") }
        Dialog(onDismissRequest = { showBudgetDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Adjust, contentDescription = null, tint = CosmicAccentGreen, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Presupuesto Mensual",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTextLight
                    )
                    Text(
                        text = "Establece tu meta límite de gastos para este mes y año seleccionados para visualizar tus ahorros con precisión.",
                        fontSize = 12.sp,
                        color = CosmicTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicTextLight,
                            unfocusedTextColor = CosmicTextLight,
                            focusedBorderColor = CosmicAccentPurple,
                            unfocusedBorderColor = CosmicBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showBudgetDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicTextMuted),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val amt = budgetInput.toDoubleOrNull() ?: 1500.0
                                viewModel.setBudget(amt)
                                showBudgetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentGreen),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Drops down Selector Component
@Composable
fun PeriodSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = CosmicTextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CosmicDarkBg, RoundedCornerShape(10.dp))
                .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue,
                    color = CosmicTextLight,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = CosmicTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(CosmicCardBg)
                    .border(1.dp, CosmicBorder)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, color = CosmicTextLight, fontSize = 13.sp) },
                        onClick = {
                            onSelected(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// STAT CARD ELEMENT
@Composable
fun StatCard(
    title: String,
    value: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextMuted,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .background(iconColor.copy(alpha = 0.1f), CircleShape)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when (iconColor) {
                    CosmicAccentGreen -> CosmicAccentGreen
                    CosmicAccentRed -> CosmicAccentRed
                    else -> CosmicTextLight
                }
            )

            Text(
                text = description,
                fontSize = 8.sp,
                color = CosmicTextMuted,
                lineHeight = 11.sp
            )
        }
    }
}

// ---------------- LEFT COMPONENT: REGISTER EXPENDITURE/INCOME ----------------
@Composable
fun TransactionCreator(
    viewModel: ExpenseViewModel,
    categoriesBytes: List<Category>
) {
    var isExpenseSelected by remember { mutableStateOf(true) } // true=Nuevo Gasto, false=Nuevo Ingreso
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("S/ (PEN)") }
    var description by remember { mutableStateOf("") }
    var expandedCat by remember { mutableStateOf(false) }
    
    // Sort and filter active lists
    val filteredCategories = categoriesBytes.filter { it.isExpense == isExpenseSelected }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // Reset categorization state if lists change
    LaunchedEffect(isExpenseSelected, categoriesBytes) {
        selectedCategory = filteredCategories.firstOrNull()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = CosmicAccentPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar Movimiento", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Selector Tabs: Gasto vs Ingreso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDarkBg, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isExpenseSelected) CosmicAccentRed.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { isExpenseSelected = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nuevo Gasto",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpenseSelected) CosmicAccentRed else CosmicTextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isExpenseSelected) CosmicAccentGreen.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { isExpenseSelected = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nuevo Ingreso",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isExpenseSelected) CosmicAccentGreen else CosmicTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row: MONTO and MONEDA
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Text("MONTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("25.50", color = CosmicTextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicTextLight,
                            unfocusedTextColor = CosmicTextLight,
                            focusedBorderColor = CosmicAccentPurple,
                            unfocusedBorderColor = CosmicBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    var expandedCur by remember { mutableStateOf(false) }
                    Text("MONEDA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicDarkBg, RoundedCornerShape(10.dp))
                            .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                            .clickable { expandedCur = true }
                            .padding(horizontal = 8.dp, vertical = 14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedCurrency, color = CosmicTextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CosmicTextMuted, modifier = Modifier.size(14.dp))
                        }
                        DropdownMenu(
                            expanded = expandedCur,
                            onDismissRequest = { expandedCur = false },
                            modifier = Modifier.background(CosmicCardBg)
                        ) {
                            DropdownMenuItem(text = { Text("S/ (PEN)", color = CosmicTextLight) }, onClick = { selectedCurrency = "S/ (PEN)"; expandedCur = false })
                            DropdownMenuItem(text = { Text("$ (USD)", color = CosmicTextLight) }, onClick = { selectedCurrency = "$ (USD)"; expandedCur = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DESCRIPCIÓN
            Text("DESCRIPCIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Ej. Supermercado, Alquiler", color = CosmicTextMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().testTag("transaction_description_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CosmicTextLight,
                    unfocusedTextColor = CosmicTextLight,
                    focusedBorderColor = CosmicAccentPurple,
                    unfocusedBorderColor = CosmicBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CATEGORÍA Selector
            Text("CATEGORÍA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDarkBg, RoundedCornerShape(10.dp))
                    .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                    .clickable { expandedCat = true }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedCategory?.name ?: "Selecciona una categoría",
                        color = if (selectedCategory == null) CosmicTextMuted else CosmicTextLight,
                        fontSize = 13.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CosmicTextMuted, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(
                    expanded = expandedCat,
                    onDismissRequest = { expandedCat = false },
                    modifier = Modifier.background(CosmicCardBg).border(1.dp, CosmicBorder).fillMaxWidth(0.9f)
                ) {
                    if (filteredCategories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay categorías configuradas", color = CosmicTextMuted) },
                            onClick = { expandedCat = false }
                        )
                    } else {
                        filteredCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name, color = CosmicTextLight) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FECHA (Initialized to current date as standard static string, fully editable)
            var dateInput by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
            Text("FECHA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = dateInput,
                onValueChange = { dateInput = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CosmicTextLight,
                    unfocusedTextColor = CosmicTextLight,
                    focusedBorderColor = CosmicAccentPurple,
                    unfocusedBorderColor = CosmicBorder
                ),
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CosmicTextMuted, modifier = Modifier.size(16.dp))
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Submission Button based on tabs selection
            val context = LocalContext.current
            val focusManager = LocalFocusManager.current
            Button(
                onClick = {
                    val amtFloat = amount.toDoubleOrNull()
                    val catId = selectedCategory?.id

                    if (amtFloat == null || amtFloat <= 0) {
                        Toast.makeText(context, "Por favor inserta un monto válido", Toast.LENGTH_SHORT).show()
                    } else if (catId == null) {
                        Toast.makeText(context, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show()
                    } else if (description.isBlank()) {
                        Toast.makeText(context, "Por favor añade una descripción", Toast.LENGTH_SHORT).show()
                    } else {
                        // Submit expense
                        viewModel.addTransaction(
                            amount = amtFloat,
                            currency = if (selectedCurrency.contains("S/")) "S/" else "$",
                            description = description,
                            categoryId = catId,
                            dateStr = dateInput,
                            isExpense = isExpenseSelected
                        )
                        amount = ""
                        description = ""
                        focusManager.clearFocus()
                        Toast.makeText(context, "Transacción guardada", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("submit_transaction_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpenseSelected) CosmicAccentRed else CosmicAccentGreen
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isExpenseSelected) "Registrar Gasto" else "Registrar Ingreso",
                    color = if (isExpenseSelected) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ---------------- LEFT COMPONENT: ADD CATEGORY FORM ----------------
@Composable
fun CategoryCreator(
    viewModel: ExpenseViewModel
) {
    var name by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) } // true=Es gasto, false=Es ingreso

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = CosmicAccentPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nueva Categoría", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Category inputs name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Ej. Gimnasio, Freelance", color = CosmicTextMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().testTag("category_name_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CosmicTextLight,
                    unfocusedTextColor = CosmicTextLight,
                    focusedBorderColor = CosmicAccentPurple,
                    unfocusedBorderColor = CosmicBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mode selectors
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isExpense = true }
                ) {
                    RadioButton(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        colors = RadioButtonDefaults.colors(selectedColor = CosmicAccentPurple)
                    )
                    Text("Es Gasto", color = CosmicTextLight, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isExpense = false }
                ) {
                    RadioButton(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        colors = RadioButtonDefaults.colors(selectedColor = CosmicAccentPurple)
                    )
                    Text("Es Ingreso", color = CosmicTextLight, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val context = LocalContext.current
            val focusManager = LocalFocusManager.current
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Añade un nombre para la categoría", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addCategory(name, isExpense)
                        name = ""
                        focusManager.clearFocus()
                        Toast.makeText(context, "Categoría creada con éxito", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("submit_category_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Crear Categoría", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ---------------- RIGHT COMPONENT: TABBED HISTORIAL/CATALOG/MICROSYNC ----------------
@Composable
fun RightSideTabbedController(
    viewModel: ExpenseViewModel,
    transactionsBytes: List<ExpenseTransaction>,
    categoriesBytes: List<Category>
) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val analysisCurrency by viewModel.selectedAnalysisCurrency.collectAsStateWithLifecycle()

    val tabs = listOf(
        "Historial de Transacciones",
        "Categorías (${categoriesBytes.size})",
        "Seguridad Biométrica"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Horizontally Scrollable tabs row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(CosmicDarkBg, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEach { tabName ->
                    val isSelected = activeTab == tabName
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) CosmicAccentPurple else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { viewModel.activeTab.value = tabName }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else CosmicTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs Content Router
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentTransition"
            ) { active ->
                when {
                    active == "Historial de Transacciones" -> {
                        HistorialTransactionsSub(
                            transactions = transactionsBytes,
                            categories = categoriesBytes,
                            currencySymbol = analysisCurrency,
                            onDelete = { viewModel.deleteTransaction(it) }
                        )
                    }
                    active.startsWith("Categorías") -> {
                        CategoriesListSub(
                            categories = categoriesBytes,
                            onDelete = { viewModel.deleteCategory(it) }
                        )
                    }
                    active == "Seguridad Biométrica" -> {
                        ConectividadStatusSub(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// 1. Tab content: TRANSACTIONS LIST SUB
@Composable
fun HistorialTransactionsSub(
    transactions: List<ExpenseTransaction>,
    categories: List<Category>,
    currencySymbol: String,
    onDelete: (ExpenseTransaction) -> Unit
) {
    var historyFilter by remember { mutableStateOf("Todos") } // Todos, Ingresos, Gastos
    var currencyFilter by remember(currencySymbol) { mutableStateOf(currencySymbol) } // Todos, S/, $
    var transactionToDelete by remember { mutableStateOf<ExpenseTransaction?>(null) }
    
    val filteredHistory = transactions.filter { tx ->
        val matchesType = when (historyFilter) {
            "Ingresos" -> !tx.isExpense
            "Gastos" -> tx.isExpense
            else -> true
        }
        val matchesCurrency = when (currencyFilter) {
            "Todos" -> true
            else -> tx.currency == currencyFilter
        }
        matchesType && matchesCurrency
    }

    transactionToDelete?.let { tx ->
        Dialog(onDismissRequest = { transactionToDelete = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Confirmación de eliminación",
                        tint = CosmicAccentRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿Eliminar Transacción?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicTextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¿Estás seguro de que quieres eliminar la transacción \"" + tx.description + "\"? Esta acción no se puede deshacer.",
                        fontSize = 13.sp,
                        color = CosmicTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { transactionToDelete = null },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicTextMuted),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                onDelete(tx)
                                transactionToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentRed),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Eliminar", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Egresos / Ingresos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
                Text("Últimos movimientos registrados", fontSize = 10.sp, color = CosmicTextMuted)
            }

            Column(horizontalAlignment = Alignment.End) {
                // Type Filter: Todos, Ingresos, Gastos
                Row(
                    modifier = Modifier
                        .background(CosmicDarkBg, RoundedCornerShape(6.dp))
                        .padding(2.dp)
                ) {
                    listOf("Todos", "Ingresos", "Gastos").forEach { f ->
                        val isSelected = historyFilter == f
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) CosmicAccentPurple.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable { historyFilter = f }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(f, fontSize = 9.sp, color = if (isSelected) CosmicTextLight else CosmicTextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Currency Filter: Todos, S/, $
                Row(
                    modifier = Modifier
                        .background(CosmicDarkBg, RoundedCornerShape(6.dp))
                        .padding(2.dp)
                ) {
                    listOf("Todos", "S/", "$").forEach { c ->
                        val isSelected = currencyFilter == c
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) CosmicAccentPurple.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable { currencyFilter = c }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(c, fontSize = 9.sp, color = if (isSelected) CosmicTextLight else CosmicTextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (filteredHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LayersClear, contentDescription = null, tint = CosmicTextMuted, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No se encontraron movimientos registrados.",
                        color = CosmicTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredHistory.forEach { tx ->
                    val catName = categories.find { it.id == tx.categoryId }?.name ?: "Otros"
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicDarkBg, RoundedCornerShape(12.dp))
                            .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (tx.isExpense) CosmicAccentRed.copy(alpha = 0.15f) else CosmicAccentGreen.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (tx.isExpense) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = if (tx.isExpense) CosmicAccentRed else CosmicAccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = tx.description,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicTextLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(CosmicAccentPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(catName, color = CosmicAccentPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tx.date, fontSize = 9.sp, color = CosmicTextMuted)
                                }
                            }
                        }

                        // Rightside Amount & quick delete action
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = (if (tx.isExpense) "-" else "+") + " " + formatCurrency(tx.amount, tx.currency),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.isExpense) CosmicAccentRed else CosmicAccentGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { transactionToDelete = tx },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar transaccion",
                                    tint = CosmicAccentRed.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Tab content: CATEGORIES LIST SUB
@Composable
fun CategoriesListSub(
    categories: List<Category>,
    onDelete: (Category) -> Unit
) {
    Column {
        Text("Catálogo de Categorías", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
        Text("Distingue presupuestos marcando egresos e ingresos por separado", fontSize = 10.sp, color = CosmicTextMuted)
        
        Spacer(modifier = Modifier.height(12.dp))

        if (categories.isEmpty()) {
            Text("No hay categorías creadas", color = CosmicTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicDarkBg, RoundedCornerShape(10.dp))
                            .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (cat.isExpense) CosmicAccentRed.copy(alpha = 0.1f) else CosmicAccentGreen.copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (cat.isExpense) "Gasto" else "Ingreso",
                                    color = if (cat.isExpense) CosmicAccentRed else CosmicAccentGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cat.name, fontSize = 13.sp, color = CosmicTextLight, fontWeight = FontWeight.Medium)
                        }

                        // Prevent deleting defaults (-1 user)
                        if (cat.userId != -1) {
                            IconButton(onClick = { onDelete(cat) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = CosmicAccentRed, modifier = Modifier.size(14.dp))
                            }
                        } else {
                            Text("Por defecto", color = CosmicTextMuted, fontSize = 9.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                        }
                    }
                }
            }
        }
    }
}

// 3. Tab content: COGNITIVE MOBILE DECOUPLED SYNC DETAILS SUB
@Composable
fun ConectividadStatusSub(viewModel: ExpenseViewModel) {
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val isBiometricEnabled = loggedInUser?.isBiometricEnabled ?: false

    Column {
        Text("Seguridad Biométrica", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
        Text("Configura el acceso rápido a tu aplicación con huella digital", fontSize = 10.sp, color = CosmicTextMuted)

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Inicio de sesión con huella digital", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
                    Text("Usa tu huella digital para acceder de forma rápida y segura.", fontSize = 10.sp, color = CosmicTextMuted)
                }

                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { enabled ->
                        loggedInUser?.let { user ->
                            viewModel.setBiometricEnabled(user.id, enabled)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CosmicAccentPurple,
                        uncheckedThumbColor = CosmicTextMuted,
                        uncheckedTrackColor = CosmicCardBg
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Actualizaciones de la Aplicación", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
        Text("Busca nuevas versiones de la aplicación", fontSize = 10.sp, color = CosmicTextMuted)

        Spacer(modifier = Modifier.height(12.dp))

        val isChecking by viewModel.isCheckingForUpdates.collectAsStateWithLifecycle()
        val updateError by viewModel.updateError.collectAsStateWithLifecycle()
        val appVersion = viewModel.getAppVersionName()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Versión Instalada: v$appVersion", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicTextLight)
                    Text(
                        text = if (isChecking) "Buscando actualizaciones..." else if (updateError != null) updateError!! else "Tu aplicación se encuentra al día.",
                        fontSize = 10.sp,
                        color = CosmicTextMuted
                    )
                }

                Button(
                    onClick = { viewModel.checkForUpdates() },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentPurple),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isChecking,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 1.5.dp)
                    } else {
                        Text("Buscar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- HELPER WRAPPER TRIGGER STANDARD OR SIMULATED FINGERPRINT ---
private fun triggerBiometricAuthentication(
    activity: FragmentActivity,
    viewModel: ExpenseViewModel,
    email: String,
    onBiometricUnavailable: () -> Unit
) {
    val biometricManager = BiometricManager.from(activity)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    
    when (biometricManager.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            // Real physical biometrics exist and can be triggered!
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Auto authenticate standard user if already has biometric enabled
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Autenticación Biométrica Exitosa", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.loginBiometricDirect(email)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Huella no reconocida: $errString", Toast.LENGTH_SHORT).show()
                        }
                        onBiometricUnavailable()
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Control de Gastos")
                .setSubtitle("Inicia sesión por huella digital")
                .setAllowedAuthenticators(authenticators)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
        else -> {
            // Unsupported (streaming emulators, etc.) -> Launch simulation overlay
            activity.runOnUiThread {
                Toast.makeText(activity, "El dispositivo no cuenta con sensor biométrico enrolado.", Toast.LENGTH_LONG).show()
            }
            onBiometricUnavailable()
        }
    }
}

// Formatter helper
private fun formatCurrency(valData: Double, currency: String): String {
    val df = DecimalFormat("#,##0.00")
    val symb = if (currency.startsWith("S/")) "S/" else "$"
    return "$symb ${df.format(valData)}"
}

@Composable
fun BiometricPromptCard(
    isBiometricEnabled: Boolean,
    onEnable: (Boolean) -> Unit
) {
    if (!isBiometricEnabled) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, CosmicAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CosmicAccentPurple.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = CosmicAccentPurple,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¿Activar acceso con Huella?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicTextLight
                        )
                        Text(
                            text = "Evita escribir tu contraseña en tu próximo ingreso.",
                            fontSize = 11.sp,
                            color = CosmicTextMuted
                        )
                    }
                }

                Button(
                    onClick = { onEnable(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentPurple),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Activar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
