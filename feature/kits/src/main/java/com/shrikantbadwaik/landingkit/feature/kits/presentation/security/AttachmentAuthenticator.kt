package com.shrikantbadwaik.landingkit.feature.kits.presentation.security

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

internal class AttachmentAuthenticator(
    activity: FragmentActivity,
) {
    private var onSuccess: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null
    private var onError: (() -> Unit)? = null

    private val biometricManager = BiometricManager.from(activity)
    private val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val callback = onSuccess
                clearCallbacks()
                callback?.invoke()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                val callback = if (errorCode in CANCELLATION_ERRORS) onCancelled else onError
                clearCallbacks()
                callback?.invoke()
            }
        },
    )

    fun availability(): AttachmentAuthenticationAvailability = when (
        biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS)
    ) {
        BiometricManager.BIOMETRIC_SUCCESS -> AttachmentAuthenticationAvailability.Available
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            AttachmentAuthenticationAvailability.NotEnrolled
        else -> AttachmentAuthenticationAvailability.Unavailable
    }

    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onCancelled: () -> Unit,
        onError: () -> Unit,
    ) {
        this.onSuccess = onSuccess
        this.onCancelled = onCancelled
        this.onError = onError
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
                .setConfirmationRequired(false)
                .build(),
        )
    }

    private fun clearCallbacks() {
        onSuccess = null
        onCancelled = null
        onError = null
    }

    private companion object {
        const val ALLOWED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val CANCELLATION_ERRORS = setOf(
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_USER_CANCELED,
        )
    }
}

internal sealed interface AttachmentAuthenticationAvailability {
    data object Available : AttachmentAuthenticationAvailability
    data object NotEnrolled : AttachmentAuthenticationAvailability
    data object Unavailable : AttachmentAuthenticationAvailability
}

internal fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return current as? FragmentActivity
}
