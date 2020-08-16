package com.example.messengerapp.AdapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.ModelUsers.ChatList
import com.example.messengerapp.ModelUsers.Chats
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.message_item_left.view.*

class ChatsAdapter (
    mContext : Context,
    mChatList: List<Chats>,
    imageUrl : String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?>(){

    private val mContext:Context
    private val mChatList:List<Chats>
    private val imageUrl:String
    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mContext = mContext
        this.mChatList = mChatList
        this.imageUrl = imageUrl
    }
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1){
            val view:View = LayoutInflater.from(mContext).inflate(com.example.messengerapp.R.layout.message_item_right,parent,false)
            ViewHolder(view)
        }else{
            val view:View = LayoutInflater.from(mContext).inflate(com.example.messengerapp.R.layout.message_item_left,parent,false)
            ViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return  mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chats : Chats = mChatList[position]
        Picasso.get().load(imageUrl).into(holder.profile_image)

        if(chats.getMessage().equals("sent you a message") && !chats.getUrl().equals("")){
            //image message right side
            if(chats.getSender().equals(firebaseUser!!.uid)){
                holder.show_text_msg!!.visibility = View.GONE
                holder.right_image!!.visibility = View.VISIBLE
                Picasso.get().load(chats.getUrl()).into(holder.right_image)
            }
            //image message left side
            else if(!(chats.getSender()).equals(firebaseUser!!.uid)){
                holder.show_text_msg!!.visibility = View.GONE
                holder.left_image!!.visibility = View.VISIBLE
                Picasso.get().load(chats.getUrl()).into(holder.left_image)
            }
        }
        //text message
        else{
            holder.show_text_msg!!.text = chats.getMessage()
        }

        //send and seen message
        if(position == mChatList.size-1)
        {

           if(chats.getisSeen()!!)
           {
               holder.text_seen!!.text = "Seen"
               if(chats.getMessage().equals("sent you a message") && !chats.getUrl().equals("")){
                   val lp:RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                   lp!!.setMargins(0,245,10,0)
                   holder.text_seen!!.layoutParams = lp
               }
           }
           else
           {
               holder.text_seen!!.text = "Sent"
               if(chats.getMessage().equals("sent you a message") && !chats.getUrl().equals("")){
                   val lp:RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                   lp!!.setMargins(0,245,10,0)
                   holder.text_seen!!.layoutParams = lp
               }
           }

        }
        else{
            holder.text_seen!!.visibility = View.GONE
        }


    }
    inner  class  ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var profile_image: CircleImageView? = null
        var show_text_msg: TextView? = null
        var left_image: ImageView? = null
        var  text_seen: TextView? = null
        var right_image: ImageView? = null

        init {
            profile_image = itemView.findViewById(R.id.profile_image)
            show_text_msg = itemView.findViewById(R.id.show_text_msg)
            left_image = itemView.findViewById(R.id.left_image)
            text_seen = itemView.findViewById(R.id.text_seen)
            right_image = itemView.findViewById(R.id.right_image)
        }
    }

    override fun getItemViewType(position: Int): Int {
        
        return if(mChatList[position].getSender().equals(firebaseUser!!.uid)){
            1
        }else{
            0
        }
    }
}