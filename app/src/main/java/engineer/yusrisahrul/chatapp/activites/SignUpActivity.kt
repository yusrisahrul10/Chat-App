package engineer.yusrisahrul.chatapp.activites

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import engineer.yusrisahrul.chatapp.R
import engineer.yusrisahrul.chatapp.databinding.ActivitySignInBinding
import engineer.yusrisahrul.chatapp.databinding.ActivitySignUpBinding
import java.io.ByteArrayOutputStream
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.firebase.firestore.FirebaseFirestore
import engineer.yusrisahrul.chatapp.util.Constants
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IMAGE
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_IS_SIGNED_IN
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_NAME
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import engineer.yusrisahrul.chatapp.util.PreferenceManager
import java.io.FileNotFoundException
import java.io.InputStream


class SignUpActivity : AppCompatActivity() {

    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    private lateinit var preferenceManager: PreferenceManager

    private var encodedImage : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            textSignIn.setOnClickListener {
                onBackPressed()
            }
            buttonSignUp.setOnClickListener {
                if (isValidSignUpDetails()) {
                    signUp()
                }
            }

            layoutImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                with(intent) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                pickImage.launch(intent)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        with(binding) {
            loading(true)
            val database = FirebaseFirestore.getInstance()
            val user = HashMap<String, Any>()
            user[Constants.KEY_NAME] = inputName.text.toString()
            user[Constants.KEY_EMAIL] = inputEmail.text.toString()
            user[Constants.KEY_PASSWORD] = inputPassword.text.toString()
            user[Constants.KEY_IMAGE] = encodedImage.toString()
            database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener {
                    loading(false)
                    preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(KEY_USER_ID, it.id)
                    preferenceManager.putString(KEY_NAME, inputName.text.toString())
                    preferenceManager.putString(KEY_IMAGE, encodedImage.toString())
                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    loading(false)
                    showToast(it.message.toString())
                }
        }

    }

    private fun encodedImage(bitmap: Bitmap) : String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        with(binding) {
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    val imageUri: Uri = result.data?.data ?: Uri.parse("null")
                    try {
                        val inputStream: InputStream? =
                            contentResolver.openInputStream(imageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageProfile.setImageBitmap(bitmap)
                        textAddImage.visibility = View.GONE
                        encodedImage = encodedImage(bitmap)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    private fun isValidSignUpDetails() : Boolean {
        with(binding) {
            if (encodedImage == null) {
                showToast("Select profile image")
                return false
            } else if ( inputName.text.toString().trim().isEmpty()) {
                showToast("Enter Name")
                return false
            } else if (inputEmail.text.toString().trim().isEmpty()) {
                showToast("Enter Email")
                return false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()) {
                showToast("Enter Valid Email")
                return false
            } else if (inputPassword.text.toString().trim().isEmpty()) {
                showToast("Enter Password")
                return false
            } else if (inputConfirmPassword.text.toString().trim().isEmpty()) {
                showToast("Confirm Your Password")
                return false
            }
            else if (!inputPassword.text.toString().equals(inputConfirmPassword.text.toString())) {
                showToast("Password & Confirm password must be same")
                return false
            }
            else {
                return true
            }
        }

    }

    private fun loading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                buttonSignUp.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            } else {
                buttonSignUp.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        }
    }
}