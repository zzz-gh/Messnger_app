package com.example.messengerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.AdapterClasses.ChatsAdapter
import com.example.messengerapp.ModelUsers.Chats
import com.example.messengerapp.ModelUsers.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*

class MessageChatActivity : AppCompatActivity() {
    var userIDvisit: String? = ""
    var firebaseUser: FirebaseUser? = null
    var chatAdapter: ChatsAdapter? = null
    var mchatlist : List<Chats>? = null
    lateinit var recycler_view_chats : RecyclerView
    var reference:DatabaseReference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)
        ///day 12
        val toolbar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            val intent = Intent(this@MessageChatActivity, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

//////////////


        intent = intent
        userIDvisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager

        reference = FirebaseDatabase.getInstance().reference.child("Users").child(userIDvisit!!)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)
                username_mchat.text = user!!.getUsername()

                Picasso.get().load(user.getProfile()).into(profile_image_mchat)

                retrieveMessage(firebaseUser!!.uid, userIDvisit!!,user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        send_message_btn.setOnClickListener {
            val message = text_message.text.toString()
            if (message == "") {
                Toast.makeText(
                    this@MessageChatActivity,
                    "Please write something first....",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIDvisit, message)
            }
            text_message.setText("")
        }
        attach_image_file_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "pick image"), 438)
        }
        seenMessage(userIDvisit!!)
    }



    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val messageHasMap = HashMap<String, Any?>()
        messageHasMap["sender"] = senderId
        messageHasMap["message"] = message
        messageHasMap["receiver"] = receiverId
        messageHasMap["isseen"] = false
        messageHasMap["url"] = ""
        messageHasMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHasMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatsListReference =
                        FirebaseDatabase.getInstance().reference.child("ChatList")
                            .child(firebaseUser!!.uid).child(userIDvisit!!)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if(!p0.exists()){
                                chatsListReference.child("id").setValue(userIDvisit)
                            }
                            val chatsListReceiverReference =
                                FirebaseDatabase.getInstance().reference
                                    .child("ChatList")
                                    .child(userIDvisit!!)
                                    .child(firebaseUser!!.uid)
                            chatsListReference.child("id").setValue(userIDvisit)
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })



                    val reference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data!!.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Uploading image,please wait")
            progressBar.show()


            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filepath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filepath.putFile(fileUri!!)
            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filepath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHasMap = HashMap<String, Any?>()
                    messageHasMap["sender"] = firebaseUser!!.uid
                    messageHasMap["message"] = "sent you a message"
                    messageHasMap["receiver"] = userIDvisit
                    messageHasMap["isseen"] = false
                    messageHasMap["url"] = url
                    messageHasMap["messageId"] = messageId
                    ref.child("Chats").child(messageId!!).setValue(messageHasMap)

                    progressBar.dismiss()
                }

            }
        }
    }

    private fun retrieveMessage(senderId: String, receiverId: String, receriverImageUrl: String?) {
        mchatlist =  ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                (mchatlist as ArrayList<Chats>).clear()
                for (snapshot in p0.children){
                    val chat = snapshot.getValue(Chats::class.java)
                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)){
                        (mchatlist as ArrayList<Chats>).add(chat)
                    }

                    chatAdapter = ChatsAdapter(this@MessageChatActivity, (mchatlist as ArrayList<Chats>),receriverImageUrl!!)
                    recycler_view_chats.adapter = chatAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

///day 12
    val seenListener:ValueEventListener? = null
    private fun seenMessage(userId : String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapShot in p0.children){
                    val chat = dataSnapShot.getValue(Chats::class.java)
                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)){
                        val hasMap = HashMap<String,Any>()
                        hasMap["isseen"] = true
                        dataSnapShot.ref.updateChildren(hasMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }

    //////////////////////
}