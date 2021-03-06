package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {

        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_REQUEST_CODE = 1001
    }

    private lateinit var binding: ActivityAuthenticationBinding

    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_authentication)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_authentication
        )


        binding.loginButton.setOnClickListener { launchSignInFlow() }

    }

    /**
     * Check if user as sign in successfully
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // check if the user as sign in in successfully
        if (requestCode == SIGN_IN_REQUEST_CODE) {

            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // sigin in successfully
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)

            } else {
                // response.getError().getErrorCode() and handle the error.
                Log.i("FAILUREEEE:", "UNSUCCESSFUL ${response?.error?.errorCode}")
                Toast.makeText(this, "${response?.error?.errorCode}", Toast.LENGTH_LONG).show()
                finish()

            }
        }

    }

    /**
     * Firebase auth ui
     * */
    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // SIGN_IN_REQUEST_CODE
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.map)
                .setIsSmartLockEnabled(false)
                .build(),
            SIGN_IN_REQUEST_CODE
        )


    }
}
