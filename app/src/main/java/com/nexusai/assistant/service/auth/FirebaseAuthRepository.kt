package com.nexusai.assistant.service.auth

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.nexusai.assistant.domain.repository.AuthRepository
import com.nexusai.assistant.domain.repository.OtpRequestResult
import com.nexusai.assistant.domain.repository.OtpVerifyResult
import com.nexusai.assistant.domain.repository.Session
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Real SMS login with Firebase Phone Authentication.
 * The one-time code is checked by Firebase. This app never sees it or stores it.
 */
class FirebaseAuthRepository(private val activityHolder: ActivityHolder) : AuthRepository {

    override val isDemo: Boolean = false

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var pendingPhone: String? = null

    override val session: Flow<Session?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { a ->
            val phone = a.currentUser?.phoneNumber?.takeIf { it.isNotBlank() }
            trySend(phone?.let { Session(it, isDemo = false) })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun requestOtp(countryCode: String, number: String): OtpRequestResult {
        val digits = number.filter { it.isDigit() }
        if (!countryCode.startsWith("+")) return OtpRequestResult.Failed("Select a country code.")
        if (countryCode == "+91" && (digits.length != 10 || digits.first() !in '6'..'9')) {
            return OtpRequestResult.Failed("Enter a valid 10-digit Indian mobile number.")
        }
        if (digits.length !in 6..14) return OtpRequestResult.Failed("Enter a valid mobile number.")

        val activity = activityHolder.current
            ?: return OtpRequestResult.Failed("App is not ready. Please try again.")
        val phone = countryCode + digits
        if (phone != pendingPhone) resendToken = null
        pendingPhone = phone

        return suspendCancellableCoroutine { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Android read the SMS automatically. Sign in right away.
                    auth.signInWithCredential(credential)
                    if (cont.isActive) cont.resume(OtpRequestResult.Sent(null))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    if (cont.isActive) cont.resume(OtpRequestResult.Failed(friendlyError(e)))
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = id
                    resendToken = token
                    if (cont.isActive) cont.resume(OtpRequestResult.Sent(null))
                }
            }

            val builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
            resendToken?.let { builder.setForceResendingToken(it) }
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    override suspend fun verifyOtp(code: String): OtpVerifyResult {
        val id = verificationId ?: return OtpVerifyResult.Expired
        return try {
            val credential = PhoneAuthProvider.getCredential(id, code)
            auth.signInWithCredential(credential).await()
            verificationId = null
            OtpVerifyResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == "ERROR_SESSION_EXPIRED") OtpVerifyResult.Expired
            else OtpVerifyResult.Invalid(null)
        } catch (e: FirebaseTooManyRequestsException) {
            OtpVerifyResult.LockedOut
        } catch (e: Exception) {
            OtpVerifyResult.Invalid(null)
        }
    }

    override suspend fun idToken(): String? =
        try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    override suspend fun logout() {
        verificationId = null
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        verificationId = null
        try {
            auth.currentUser?.delete()?.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Firebase refuses deletion if the login is old. Sign out so the device is cleared.
        } finally {
            auth.signOut()
        }
    }

    private fun friendlyError(e: FirebaseException): String = when (e) {
        is FirebaseAuthInvalidCredentialsException -> "That phone number is not valid."
        is FirebaseTooManyRequestsException -> "Too many attempts. Please try again later."
        else -> "Could not send the code. Check your internet and try again."
    }
}
