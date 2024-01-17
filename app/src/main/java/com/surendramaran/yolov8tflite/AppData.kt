package com.surendramaran.yolov8tflite

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object AppData {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private const val RC_SIGN_IN = 123
    private var isLoggedIn: Boolean = false
        get() = FirebaseAuth.getInstance().currentUser != null
        private set

    fun setLoginStatus(isLoggedIn: Boolean) {
        // Update the login status and handle Firebase authentication accordingly
        this.isLoggedIn = isLoggedIn

        if (isLoggedIn) {
            FirebaseAuth.getInstance().signInAnonymously()
        } else {
            FirebaseAuth.getInstance().signOut()
        }
    }

    fun signInWithGoogle(activity: Activity, onComplete: (Boolean) -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("369320849354-6age63inu8gqtteqv0ih98drg1melvt6.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent

        // Start the activity for result
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun firebaseAuthWithGoogle(idToken: String, onComplete: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)

                // Update the login status after Google authentication
                setLoginStatus(task.isSuccessful)
            }
    }
}