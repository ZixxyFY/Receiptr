package com.receiptr.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.domain.model.AuthResult
import com.receiptr.presentation.viewmodel.AuthViewModel
import com.receiptr.ui.theme.ReceiptrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val activity = context as Activity
    val authResult by viewModel.authResult.collectAsState()
    val verificationId by viewModel.verificationId.collectAsState()
    
    // Handle authentication result
    LaunchedEffect(authResult) {
        when (val result = authResult) {
            is AuthResult.Success -> {
                when (result.user?.id) {
                    "otp_sent" -> {
                        // OTP was sent successfully
                        if (!isOtpSent) {
                            isOtpSent = true
                        }
                    }
                    "otp_resent" -> {
                        // OTP was resent successfully
                        if (!isOtpSent) {
                            isOtpSent = true
                        }
                        // Show success message
                        // Could add a toast here if needed
                    }
                    "auto_verified", null -> {
                        // Authentication successful (auto-verified or manual verification)
                        if (isOtpSent || result.user?.id == "auto_verified") {
                            navController.navigate("home") {
                                popUpTo("phone_auth") { inclusive = true }
                            }
                            viewModel.clearAuthResult()
                        }
                    }
                    else -> {
                        // Normal successful authentication
                        if (isOtpSent) {
                            navController.navigate("home") {
                                popUpTo("phone_auth") { inclusive = true }
                            }
                            viewModel.clearAuthResult()
                        }
                    }
                }
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Phone Authentication",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!isOtpSent) {
            // Phone Number Input Phase
            Text(
                text = "Enter Your Phone Number",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "We'll send you a verification code",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { newValue ->
                    // Allow only digits, plus sign, and spaces
                    val filtered = newValue.filter { it.isDigit() || it == '+' || it == ' ' || it == '(' || it == ')' || it == '-' }
                    phoneNumber = filtered
                },
                label = { Text("Phone Number") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = { Text("+1234567890") },
                supportingText = {
                    Text(
                        text = "Include country code (e.g., +1 for US)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                isError = phoneNumber.isNotEmpty() && !isValidPhoneNumberFormat(phoneNumber),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Send OTP Button
            Button(
                onClick = {
                    viewModel.signInWithPhone(phoneNumber, activity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = phoneNumber.isNotEmpty() && isValidPhoneNumberFormat(phoneNumber) && authResult !is AuthResult.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (authResult is AuthResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Send Verification Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // OTP Verification Phase
            Text(
                text = "Enter Verification Code",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "We sent a code to $phoneNumber",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // OTP Input
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Verification Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = { Text("123456") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Verify Button
            Button(
                onClick = {
                    viewModel.verifyOtp(otpCode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = otpCode.isNotEmpty() && authResult !is AuthResult.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (authResult is AuthResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Verify Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Resend Code Button
            TextButton(
                onClick = {
                    viewModel.resendOtp(phoneNumber, activity)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authResult !is AuthResult.Loading
            ) {
                Text(
                    text = if (authResult is AuthResult.Loading) "Sending..." else "Didn't receive the code? Resend",
                    color = if (authResult is AuthResult.Loading) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Change Phone Number
            TextButton(
                onClick = {
                    isOtpSent = false
                    otpCode = ""
                    viewModel.clearAuthResult()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Change phone number",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Error message
        (authResult as? AuthResult.Error)?.let { errorResult ->
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorResult.message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info text
        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy. Standard message and data rates may apply.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

// Helper function to validate phone number format
fun isValidPhoneNumberFormat(phoneNumber: String): Boolean {
    // Remove all non-digit characters except +
    val cleaned = phoneNumber.replace(Regex("[^+\\d]"), "")
    
    // Basic validation - should start with + and have at least 10 digits
    return cleaned.matches(Regex("^\\+[1-9]\\d{9,14}$"))
}

@Preview(showBackground = true)
@Composable
fun PhoneAuthScreenPreview() {
    ReceiptrTheme {
        PhoneAuthScreen(navController = rememberNavController())
    }
}
