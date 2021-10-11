package engineer.yusrisahrul.chatapp.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import engineer.yusrisahrul.chatapp.databinding.ItemContainerUserBinding
import engineer.yusrisahrul.chatapp.models.User

class UsersAdapter(
    private val context: Context
) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    private var users = ArrayList<User>()

    fun setData(listUser: List<User>?) {
        if (listUser.isNullOrEmpty()) return
        users.clear()
        users.addAll(listUser)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.view) {
            textName.text = users[position].name
            textEmail.text = users[position].email
            imageProfile.setImageBitmap(getUserImage(users[position].image!!))
//            notifyItemChanged(users[position])
        }
    }
    override fun getItemCount(): Int = users.size

    class ViewHolder(val view: ItemContainerUserBinding) : RecyclerView.ViewHolder(view.root)

    private fun getUserImage(encodedImage: String) : Bitmap =
        BitmapFactory.decodeByteArray(Base64.decode(encodedImage, Base64.DEFAULT), 0,
                Base64.decode(encodedImage, Base64.DEFAULT).size)
}