package com.yorklunchclub.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object BiometricHelper {

    private const val PREFS_NAME = "ylc_secure_creds"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    data class StoredCredentials(val email: String, val password: String)

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKey,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveCredentials(context: Context, email: String, password: String) {
        prefs(context).edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun loadCredentials(context: Context): StoredCredentials? {
        val p = prefs(context)
        val email = p.getString(KEY_EMAIL, null) ?: return null
        val password = p.getString(KEY_PASSWORD, null) ?: return null
        return StoredCredentials(email, password)
    }

    fun clearCredentials(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val mgr = BiometricManager.from(context)
        return mgr.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun hasStoredCredentials(context: Context): Boolean = loadCredentials(context) != null

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: (StoredCredentials) -> Unit,
        onError: (String) -> Unit
    ) {
        val creds = loadCredentials(activity)
        if (creds == null) {
            onError("No stored credentials. Sign in with email/password first.")
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(creds)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onError("Biometric authentication failed")
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("York Lunch Club")
            .setSubtitle("Sign in with biometrics")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(info)
    }
}
