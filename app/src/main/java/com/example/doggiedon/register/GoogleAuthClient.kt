package com.example.doggiedon.register

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

private  const val TAG = "GoogleAuth"
class GoogleAuthClient(private val context : Context) {
    private val tag = "GoogleSignInClient: "

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private fun isSignedIn(): Boolean{
        if (firebaseAuth.currentUser != null){
            print(tag + "already signed in")
            return true
        }
        return false
    }
    suspend fun signIn(): Boolean{
        Log.d("Welcome", "Button clicked and redirected")
        if (isSignedIn()){
            return true
        }
        try {
            val result = buildCredentialRequest()
            return handleSignIn(result)
        }
        catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e

            println(tag + "signIn error: ${e.message}")
            return false
        }
    }
    private suspend fun handleSignIn(result: GetCredentialResponse):Boolean{
        Log.d(TAG, "Button clicked and redirected to handle")
        val credential=result.credential
        if(
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ){
            try {
                val tokenCredential= GoogleIdTokenCredential.createFrom(credential.data)
                println(tag + "name: ${tokenCredential.displayName}")
                println(tag + "email: ${tokenCredential.id}")
                println(tag + "Photo: ${tokenCredential.profilePictureUri}")

                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken,null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                return authResult.user != null
            }
            catch (e: GoogleIdTokenParsingException){
                println(tag + "GoogleIdTokenParsingException: ${e.message}")
                return false
            }
        }
        else{
            println(tag + "credential is not GoogleIdTokenCredential")
            return false
        }
    }
    private suspend fun buildCredentialRequest(): GetCredentialResponse{
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        "374772074763-l3lmmij3i4844u8ep5p1skg9pktua182.apps.googleusercontent.com"
                    )
                    .setAutoSelectEnabled(false)
                    .build()
                )
            .build()

        return credentialManager.getCredential(
            request = request, context= context
            )
    }
    suspend fun signOut(){
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
    }
}