package com.challenge.passwordmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateAccountActivity : AppCompatActivity() {

    private val LOGGING_TAG = "CreateAccountActivity"

    private val encryptAES = AESEncryption()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        initialise()
    }

    // Initialise global variables
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null

    // Initialise UI placeholders
    private var etFirstName: EditText? = null
    private var etLastName: EditText? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnCreateAccount: Button? = null
    private var progressBar: ProgressBar? = null

    // Initialise Firebase database references
    private var fbDatabaseReference: DatabaseReference? = null
    private var fbDatabase: FirebaseDatabase? = null
    private var fbAuthInstance: FirebaseAuth? = null

    private fun initialise() {
        etFirstName = findViewById<View>(R.id.et_first_name) as EditText
        etLastName = findViewById<View>(R.id.et_last_name) as EditText
        etEmail = findViewById<View>(R.id.et_email) as EditText
        etPassword = findViewById<View>(R.id.et_password) as EditText
        btnCreateAccount = findViewById<View>(R.id.btn_register) as Button
        progressBar = ProgressBar(this)

        fbDatabase = FirebaseDatabase.getInstance()
        fbDatabaseReference = fbDatabase!!.reference.child("Users")
        fbAuthInstance = FirebaseAuth.getInstance()

        btnCreateAccount!!.setOnClickListener { createNewAccount() }
    }

    private fun createNewAccount() {
        firstName = etFirstName?.text.toString()
        lastName = etLastName?.text.toString()
        email = etEmail?.text.toString()
        val masterPassword = etPassword?.text.toString()

        // if user has entered input for every field
        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
            && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(masterPassword)) {

            Toast.makeText(this, "Registering user...", Toast.LENGTH_SHORT).show()

            fbAuthInstance!!
                .createUserWithEmailAndPassword(email!!, masterPassword)
                .addOnCompleteListener(this) { task ->
                    progressBar!!.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(LOGGING_TAG, "createUserWithEmail:success")

                        val userId = fbAuthInstance!!.currentUser!!.uid

                        verifyEmail()

                        // Encrypt master password using AES, key and iv need to be saved here for decryption
                        val key = encryptAES.generateEncryptedKey(masterPassword)
                        val iv = encryptAES.generateIV()
                        val cipher = encryptAES.encryptPassword(masterPassword, key, iv)

                        //update user profile information
                        val currentUserDb = fbDatabaseReference!!.child(userId)
                        currentUserDb.child("firstName").setValue(firstName)
                        currentUserDb.child("lastName").setValue(lastName)
                        currentUserDb.child("email").setValue(email)
                        currentUserDb.child("encrypted_master_password").setValue(cipher)

                        updateUserInfoAndUI()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(LOGGING_TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@CreateAccountActivity, "Email already registered!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserInfoAndUI() {
        //start next activity
        val intent = Intent(this@CreateAccountActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun verifyEmail() {
        val mUser = fbAuthInstance!!.currentUser
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@CreateAccountActivity,
                        "Verification email sent to " + mUser.email,
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(LOGGING_TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(this@CreateAccountActivity,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
