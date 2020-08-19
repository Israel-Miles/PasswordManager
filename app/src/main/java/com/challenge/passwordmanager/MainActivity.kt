package com.challenge.passwordmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialise()
    }

    // Initialize Firebase database references
    private var fbDatabaseReference: DatabaseReference? = null
    private var fbDatabase: FirebaseDatabase? = null
    private var fbAuthInstance: FirebaseAuth? = null

    // Initialize UI placeholders
    private var tvFirstName: TextView? = null
    private var tvLastName: TextView? = null
    private var tvEmail: TextView? = null
    private var tvEmailVerified: TextView? = null

    private fun initialise() {
        fbDatabase = FirebaseDatabase.getInstance()
        fbDatabaseReference = fbDatabase!!.reference.child("Users")
        fbAuthInstance = FirebaseAuth.getInstance()

        tvFirstName = findViewById<View>(R.id.tv_first_name) as TextView
        tvLastName = findViewById<View>(R.id.tv_last_name) as TextView
        tvEmail = findViewById<View>(R.id.tv_email) as TextView
        tvEmailVerified = findViewById<View>(R.id.tv_email_verified) as TextView
    }

    override fun onStart() {
        super.onStart()

        val fbUser = fbAuthInstance!!.currentUser
        val fbUserReference = fbDatabaseReference!!.child(fbUser!!.uid)

        tvEmail!!.text = fbUser.email
        tvEmailVerified!!.text = fbUser.isEmailVerified.toString()

        fbUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvFirstName!!.text = snapshot.child("firstName").value as String
                tvLastName!!.text = snapshot.child("lastName").value as String
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
