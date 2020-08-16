package com.example.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.MessageChatActivity
import com.example.messengerapp.ModelUsers.Users
import com.example.messengerapp.R
import com.example.messengerapp.WelcomeActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.uesr_search_item_layout.view.*

class UserAdadpter(
    mContext : Context,
    mUsers : List<Users>,
    isChatCheck: Boolean
) : RecyclerView.Adapter<UserAdadpter.ViewHolder?>()

{
    private  val mContext : Context
    private val mUsers : List<Users>
    private  var isChatCheck : Boolean

    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.uesr_search_item_layout,viewGroup,false)
        return UserAdadpter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val user:Users = mUsers[i]
        holder.usernameTxt.text = user!!.getUsername()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profileImageView)

        ///day 9

        holder.itemView.setOnClickListener{
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder:AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want")
            builder.setItems(options,DialogInterface.OnClickListener { dialog, position ->
                if(position == 0){
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id",user.getUID())
                    mContext.startActivity(intent)

                }
                if(position == 1){

                }
            })
            builder.show()
        }

        ///////////
    }
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var usernameTxt : TextView
        var profileImageView : CircleImageView
        var onlineImageView : CircleImageView
        var offlineImageView : CircleImageView
        var lastMessageTxt : TextView

        init {
            usernameTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineImageView = itemView.findViewById(R.id.image_online)
            offlineImageView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)
        }
    }


}