package engineer.yusrisahrul.chatapp.activites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_AVAILABILITY
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_COLLECTION_USERS
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_USER_ID
import engineer.yusrisahrul.chatapp.util.PreferenceManager

open class BaseActivity : AppCompatActivity() {

    private lateinit var documentReference: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(this)
        val database = FirebaseFirestore.getInstance()
        documentReference = database.collection(
            KEY_COLLECTION_USERS
        ).document(preferenceManager.getString(KEY_USER_ID))
    }

    override fun onPause() {
        super.onPause()
        documentReference.update(KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference.update(KEY_AVAILABILITY, 1)
    }
}