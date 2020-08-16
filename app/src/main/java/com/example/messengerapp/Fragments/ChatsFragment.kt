package com.example.messengerapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.AdapterClasses.UserAdadpter
import com.example.messengerapp.ModelUsers.ChatList
import com.example.messengerapp.ModelUsers.Users
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ChatsFragment : Fragment() {

    private var userAdapter: UserAdadpter? = null
    private var mUsers: List<Users>? = null
    private var userChatLists: List<ChatList>? = null
    lateinit var recycler_view_chatlists:RecyclerView
    private  var firebaseUser:FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        recycler_view_chatlists = view.findViewById(R.id.recycler_view_chatList)
        recycler_view_chatlists.setHasFixedSize(true)
        recycler_view_chatlists.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userChatLists = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                (userChatLists as ArrayList).clear()

                for(dataSnapshot in p0.children){
                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    (userChatLists as ArrayList).add(chatList!!)

                }
                retrievesAllChatLists()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        return  view
    }

    private fun retrievesAllChatLists(){
        mUsers = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for(dataSnapshot in p0.children){
                    val user = dataSnapshot.getValue(Users::class.java)

                    for(eachChatlist in userChatLists!!){
                        if(user!!.getUID() == eachChatlist.getId()){
                            (mUsers as ArrayList).add(user)
                        }
                    }
                }
                userAdapter = UserAdadpter(context!!,(mUsers as ArrayList<Users>),true)
                recycler_view_chatlists.adapter =userAdapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

}