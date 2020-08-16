package com.example.messengerapp.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.messengerapp.ModelUsers.Users
import com.example.messengerapp.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.view.*
import kotlin.coroutines.Continuation


class SettingFragment : Fragment() {

    var userReference:DatabaseReference? = null
    var firebaseUser:FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri : Uri? = null
    private var storageRef: StorageReference? =null
    private var  coverChecker:String? = ""
    private var  socialChecker:String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_setting, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference!!.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user: Users? = p0.getValue(Users::class.java)

                    if(context != null){
                        view.username_settings.text = user!!.getUsername()
                        Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                        Picasso.get().load(user.getCover()).into(view.cover_image)
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        view.profile_image_settings.setOnClickListener{
            pickImage()
        }

        view.cover_image.setOnClickListener{
            coverChecker = "cover"
            pickImage()
        }

        view.set_facebook.setOnClickListener{
            socialChecker = "facebook"
            setSocialLink()
        }
        view.set_instagram.setOnClickListener{
            socialChecker = "instagram"
            setSocialLink()
        }
        view.set_website.setOnClickListener{
            socialChecker = "website"
            setSocialLink()
        }
        return view
    }

    private fun setSocialLink() {
        val builder : AlertDialog.Builder =
            AlertDialog.Builder(context!!,R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if(socialChecker == "website"){
            builder.setTitle("Write URL:")
        }
        else{
            builder.setTitle("Write username:")
        }
        val editText = EditText(context)
        if(socialChecker == "website"){
            editText.hint = "www.google.com"
        }
        else{
            editText.hint = "e.g zzz111"
        }

        builder.setView(editText)
        builder.setPositiveButton("Create",DialogInterface.OnClickListener {
                dialog, which ->
            val str = editText.text.toString()

            if(str == null){
                Toast.makeText(context,"Please write something",Toast.LENGTH_LONG).show()
            }else{
                saveSocialLink(str)
            }

        })

        builder.setNegativeButton("Create",DialogInterface.OnClickListener {
                dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(str : String) {
        val mapSocial = HashMap<String,Any>()


        when(socialChecker ){
            "facebook" ->
            {
                mapSocial["facebook"] = "https://m.facebool.com/$str"
            }
            "instagram" ->
            {
                mapSocial["instagram"] = "https://m.facebook.com/$str"
            }
            "website" ->
            {
                mapSocial["website"] = "https://$str"
            }
        }

        userReference!!.updateChildren(mapSocial).addOnCompleteListener {
            task ->
            if (task.isSuccessful)
            {
                Toast.makeText(context,"Updated successfully",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null){
            imageUri = data.data
            Toast.makeText(context,"Uploading....",Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Uploading image,please wait")
        progressBar.show()

        if(imageUri != null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString()+".jpg")

            var uploadTask : StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot,Task<Uri>> {task ->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if(coverChecker == "cover"){
                        val mapCoverImg = HashMap<String,Any>()
                        mapCoverImg["cover"] = url
                        userReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                        progressBar.dismiss()

                    }else{
                        var mapProfileImg = HashMap<String,Any>()
                        mapProfileImg["profile"] = url
                        userReference!!.updateChildren(mapProfileImg)
                        progressBar.dismiss()

                    }
                }

            }
        }

    }

}