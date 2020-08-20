package com.challenge.passwordmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialise()
    }

    // Initialize Firebase database and authorization references
    private var fbDatabaseReference: DatabaseReference? = null
    private var fbDatabase: FirebaseDatabase? = null
    private var fbAuthInstance: FirebaseAuth? = null

    // Initialize UI placeholders
    private var btnLogout: Button? = null
    private var tvFirstName: TextView? = null
    private var tvLastName: TextView? = null
    private var tvEmail: TextView? = null
    private var tvEmailVerified: TextView? = null

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

    private fun initialise() {
        btnLogout = findViewById<View>(R.id.btn_logout) as Button

        fbDatabase = FirebaseDatabase.getInstance()
        fbDatabaseReference = fbDatabase!!.reference.child("Users")
        fbAuthInstance = FirebaseAuth.getInstance()

        tvFirstName = findViewById<View>(R.id.tv_first_name) as TextView
        tvLastName = findViewById<View>(R.id.tv_last_name) as TextView
        tvEmail = findViewById<View>(R.id.tv_email) as TextView
        tvEmailVerified = findViewById<View>(R.id.tv_email_verified) as TextView

        btnLogout!!.setOnClickListener { logoutUser() }
    }

    private fun logoutUser() {
        fbAuthInstance?.signOut()
        updateUI()
    }

    private fun updateUI() {
        val intent = Intent(this@MainActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
