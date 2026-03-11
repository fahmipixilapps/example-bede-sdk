package com.pixilapps.bookey.presentation.checkout

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fahmi.testbede.ui.theme.TestBedeTheme
import kw.bede.android.pay.BedeAPI
import kw.bede.android.pay.PaymentOptionCallback
import kw.bede.android.pay.component.PaymentOptionsView
import kw.bede.android.pay.extension.registerPaymentActivity
import kw.bede.android.pay.pojo.PayOption
import kw.bede.android.pay.pojo.PaymentDetail
import kw.bede.android.pay.pojo.PaymentOptionResponse
import kw.bede.android.pay.pojo.PaymentResult
import kw.bede.android.pay.pojo.payment.PaymentAPIResponse
import kw.bede.android.pay.pojo.payment.Paymentstatus
import kw.bede.android.pay.util.LogUtil
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onPaymentClick: () -> Unit = {}
) {

    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+987") }
    var amount by remember { mutableStateOf("") }
    var selectedPaymentOption by remember { mutableStateOf<PayOption?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf<PaymentAPIResponse?>(null)}
    var refreshTrigger by remember { mutableStateOf(0) }

    val paymentHandler = registerPaymentActivity { result ->
        isLoading = false
        isProcessingPayment = false
        if(result.error) {
            Toast.makeText(context, "Payment Failed: ${result.message ?: "An error occurred during payment processing"}", Toast.LENGTH_LONG).show()
            dialogMessage = "Payment Failed: ${result.message ?: "An error occurred during payment processing"}"
            showDialog = true
        }
        else {
            dialogMessage = "Payment Successful! Transaction completed successfully."
            showDialog = true
        }
    }

    val paymentId = remember { "PAY-${UUID.randomUUID().toString().take(8).uppercase()}" }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),

                title = {
                    Text(
                        text = "Payment Methods",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) {paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .clickable(
                    onClick = { focusManager.clearFocus() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
//                Text(
//                    text = paymentId,
//                    style = MaterialTheme.typography.headlineSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
//                )

                Text(
                    text = "Amount (KWD)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                PaymentOptionsView(
                    onPaymentOptionSelected = { payOption ->
                        selectedPaymentOption = payOption
                    },
                )

                ElevatedButton(
                    onClick = {
                        if (isProcessingPayment) return@ElevatedButton

                        isProcessingPayment = true
                        isLoading = true
                        val detail = PaymentDetail(
                            phone = "98981122",
                            countryCode = "+986",
                            name = "Test User Name",
                            paymentId = paymentId,
                            amount = amount,
                            transactionHDR = "TXN-${UUID.randomUUID().toString().take(6).uppercase()}",
                            paymentMethod = selectedPaymentOption?.pmCd ?: "KNET",
                            uniqueId = null
                        )

                        paymentHandler.requestPaymentLink(paymentDetail = detail) { result ->
                            paymentResult = result.response
                            isLoading = false
                            isProcessingPayment = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = 10.dp),
                    enabled = !isProcessingPayment && !isLoading && amount.isNotBlank() && selectedPaymentOption != null,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.Black, // Warna Ungu khas Dark Mode
                        contentColor = Color.Black,         // Teks hitam agar kontras di atas ungu terang
                        disabledContainerColor = Color.DarkGray,
                        disabledContentColor = Color.LightGray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Pay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }

                ElevatedButton(onClick = {
                    if(paymentResult == null) {
                        Toast.makeText(context, "No payment initiated yet", Toast.LENGTH_LONG).show()
                    }
                    else {
                        try {
                            BedeAPI.checkPaymentStatus(paymentResult = paymentResult, callback = { results: List<Paymentstatus?>? ->
                                try {

                                    // show alert dialog with the payment status
                                    if (results.isNullOrEmpty()) {
                                        dialogMessage = "No payment status information available"
                                    } else {
                                        dialogMessage = results.joinToString(separator = "\n\n") { status ->
                                            buildString {
                                                append("Payment Type: ${status?.paymentType ?: "N/A"}")
                                                append("\nStatus Description: ${status?.statusDescription ?: "N/A"}")
                                                append("\nFinal Status: ${status?.finalStatus ?: "N/A"}")
                                            }
                                        }
                                    }
                                    showDialog = true
                                } catch (e: Exception) {
                                    dialogMessage = "Error processing payment status: ${e.message}"
                                    showDialog = true
                                }
                            })
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error checking payment status: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }

                },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = 10.dp),
                    enabled = paymentResult != null && amount.isNotEmpty() && !amount.equals("0"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.Black, // Warna Ungu khas Dark Mode
                        contentColor = Color.Black,         // Teks hitam agar kontras di atas ungu terang
                        disabledContainerColor = Color.DarkGray,
                        disabledContentColor = Color.LightGray
                    )) {
                    if(isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Payment Status",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }

                ElevatedButton(onClick = {
                    refreshTrigger++
                },  modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = 10.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.Black, // Warna Ungu khas Dark Mode
                        contentColor = Color.Black,         // Teks hitam agar kontras di atas ungu terang
                        disabledContainerColor = Color.DarkGray,
                        disabledContentColor = Color.LightGray
                    )) {
                    if(isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Get Payment Methods",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color.White

                        )
                    }
                }


            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(text = if (dialogMessage.contains("Failed")) "Payment Failed" else "Payment Success")
                },
                text = {
                    Text(text = dialogMessage)
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun OrderItem(
    name: String,
    price: String,
    quantity: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Qty: $quantity",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Text(
            text = price,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    TestBedeTheme {
        CheckoutScreen(onPaymentClick = {})
    }
}