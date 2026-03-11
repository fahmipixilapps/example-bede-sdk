package com.fahmi.testbede

import android.app.Application
import kw.bede.android.pay.BedeSDK
import kw.bede.android.pay.pojo.MerchantDetails

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        BedeSDK.getInstance().initialize(
            merchantDetails = MerchantDetails(
                merchantId = "mer2500011",
                successUrl = "https://demo.bookeey.com/portal/paymentSuccess",
                failureUrl = "https://demo.bookeey.com/portal/paymentfailure"
            ),
            environment = BedeSDK.Environment.TEST,
            secretKey = "7483493"
        )
    }
}
