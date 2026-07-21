package com.ucall.widget.lite

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.CallLog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri

class PermissionGateActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TARGET = "target"
        const val EXTRA_NUMBER = "number"
        private const val REQ_RUNTIME = 2001
        private const val REQ_ONBOARDING = 9001
    }

    private var onboardingPending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        proceed()
    }

    private fun proceed() {
        if (!PermissionUtils.hasRuntimePermissions(this)) {
            ActivityCompat.requestPermissions(this, PermissionUtils.missingRuntimePermissions(this), REQ_RUNTIME)
            return
        }

        maybeShowOnboardingOrProceed()
    }

    private fun maybeShowOnboardingOrProceed() {
        if (!OnboardingPrefs.isShown(this)) {
            onboardingPending = true
            startActivityForResult(Intent(this, OnboardingActivity::class.java), REQ_ONBOARDING)
            OnboardingPrefs.markShown(this)
        } else {
            doTargetAndFinish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONBOARDING && onboardingPending) {
            onboardingPending = false
            doTargetAndFinish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proceed()
    }

    private fun doTargetAndFinish() {
        val target = intent.getStringExtra(EXTRA_TARGET)
        val number = intent.getStringExtra(EXTRA_NUMBER)

        try {
            when (target) {
                "DIAL" -> startActivity(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                "CALL_LOG" -> {
                    val viewLog = Intent(Intent.ACTION_VIEW).apply {
                        type = CallLog.Calls.CONTENT_TYPE
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(
                        if (viewLog.resolveActivity(packageManager) != null) viewLog
                        else Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                "CALL" -> if (!number.isNullOrBlank()) {
                    startActivity(Intent(Intent.ACTION_CALL).apply {
                        data = "tel:$number".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
        } catch (_: Exception) {
        }

        setResult(Activity.RESULT_OK)
        finish()
    }
}
