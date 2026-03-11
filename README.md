# Bede Payment SDK - Android Integration Guide

A sample Android project demonstrating integration of the **Bede Kuwait Payment SDK**.

## Requirements

- Android Studio Ladybug or later
- Min SDK 24 (Android 7.0)
- Kotlin 2.0+
- Jetpack Compose enabled

## Installation

### Step 1: Add Repositories

Ensure `mavenCentral()` and `google()` are in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

### Step 2: Add Dependencies

In your app-level `build.gradle.kts`, add:

```kotlin
dependencies {
    implementation("io.github.bede-kuwait:pay:0.1.6")
    implementation("com.google.android.material:material:1.12.0")
}
```

### Step 3: Sync Project

Click **Sync Now** in Android Studio or run:

```bash
./gradlew build
```

## Implementation

### 1. Initialize the SDK

Create a custom `Application` class to initialize the SDK with your merchant credentials:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        BedeSDK.getInstance().initialize(
            merchantDetails = MerchantDetails(
                merchantId = "your_merchant_id",
                successUrl = "https://your-domain.com/paymentSuccess",
                failureUrl = "https://your-domain.com/paymentFailure"
            ),
            environment = BedeSDK.Environment.TEST, // Use .PRODUCTION for live
            secretKey = "your_secret_key"
        )
    }
}
```

Register it in `AndroidManifest.xml`:

```xml
<application
    android:name=".App"
    ... >
</application>
```

### 2. Payment UI (Jetpack Compose)

Use `PaymentOptionsView` to display payment methods and `registerPaymentActivity` to handle the payment flow:

```kotlin
@Composable
fun CheckoutScreen() {
    var amount by remember { mutableStateOf("") }
    var selectedPaymentOption by remember { mutableStateOf<PayOption?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf<PaymentAPIResponse?>(null) }

    val paymentId = remember { "PAY-${UUID.randomUUID().toString().take(8).uppercase()}" }

    val paymentHandler = registerPaymentActivity { result ->
        isLoading = false
        if (result.error) {
            // Handle payment failure
        } else {
            // Handle payment success
        }
    }

    Column {
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (KWD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        PaymentOptionsView(
            onPaymentOptionSelected = { payOption ->
                selectedPaymentOption = payOption
            }
        )

        Button(
            onClick = {
                isLoading = true
                val detail = PaymentDetail(
                    phone = "98765432",
                    countryCode = "+965",
                    name = "Customer Name",
                    paymentId = paymentId,
                    amount = amount,
                    transactionHDR = "TXN-${UUID.randomUUID().toString().take(6).uppercase()}",
                    paymentMethod = selectedPaymentOption?.pmCd ?: "KNET",
                    uniqueId = null
                )
                paymentHandler.requestPaymentLink(paymentDetail = detail) { result ->
                    paymentResult = result.response
                    isLoading = false
                }
            },
            enabled = amount.isNotBlank() && selectedPaymentOption != null
        ) {
            Text("Pay")
        }
    }
}
```

### 3. Payment UI (XML Layout)

You can also use `PaymentOptionsComposeView` directly in XML:

```xml
<kw.bede.android.pay.component.PaymentOptionsComposeView
    android:id="@+id/paymentOptions"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

### 4. Check Payment Status

```kotlin
BedeAPI.checkPaymentStatus(
    paymentResult = paymentResult,
    callback = { results: List<Paymentstatus?>? ->
        results?.forEach { status ->
            Log.d("Status", "Type: ${status?.paymentType}")
            Log.d("Status", "Description: ${status?.statusDescription}")
            Log.d("Status", "Final: ${status?.finalStatus}")
        }
    }
)
```