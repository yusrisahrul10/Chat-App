package engineer.yusrisahrul.chatapp.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.databinding.ActivitySignInBinding
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_USERS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_EMAIL
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IS_SIGNED_IN
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_PASSWORD
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import engineer.yusrisahrul.chatapp.util.PreferenceManager
import java.util.*
import kotlin.collections.HashMap

class SignInActivity : AppCompatActivity() {

    private val binding: ActivitySignInBinding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    private lateinit var preferenceManager: PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        if (preferenceManager.getBoolean(KEY_IS_SIGNED_IN)) {
             startActivity(Intent(this, MainActivity::class.java))
             finish()
        }
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            textCreateNewAccount.setOnClickListener {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }
            buttonSignIn.setOnClickListener {
                if (isValidSignUpDetails()) {
                    signIn()
                }
            }
        }
    }

    private fun signIn() {
        with(binding) {
            loading(true)
            val database = FirebaseFirestore.getInstance()
            database.collection(KEY_COLLECTION_USERS)
                .whereEqualTo(KEY_EMAIL, inputEmail.text.toString())
                .whereEqualTo(KEY_PASSWORD, inputPassword.text.toString())
                .get()
                .addOnCompleteListener {
                    val result = it.result?.documents?.size ?: 0
                    if (it.isSuccessful && it.result != null && result > 0) {
                        val documentSnapshot = it.result!!.documents[0]
                        preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(KEY_USER_ID, documentSnapshot.id)
                        preferenceManager.putString(KEY_NAME, documentSnapshot.getString(KEY_NAME)!!)
                        preferenceManager.putString(KEY_IMAGE, documentSnapshot.getString(KEY_IMAGE)!!)
                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        loading(false)
                        showToast("Unable to Sign In")
                    }
                }
        }

    }

    private fun loading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                buttonSignIn.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            } else {
                buttonSignIn.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignUpDetails() : Boolean {
        with(binding) {
            return if (inputEmail.text.toString().trim().isEmpty()) {
                showToast("Enter email")
                false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()) {
                showToast("Enter Valid Email")
                false
            } else if (inputPassword.text.toString().trim().isEmpty()) {
                showToast("Enter Password")
                false
            } else {
                true
            }
        }

    }
}