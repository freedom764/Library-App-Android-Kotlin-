package edu.skku.cs.final_library

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SignUpActivity : AppCompatActivity() {
    private lateinit var database : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        var firebaseAuth: FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        val signUp = findViewById<Button>(R.id.buttonSIgnUp)
        val emailEdit = findViewById<EditText>(R.id.emailEdit)
        val passwordEdit = findViewById<EditText>(R.id.passwordEdit)
        val passConfEdit = findViewById<EditText>(R.id.repasswordEdit)


        val loadingDialog = LoadingDialog(this@SignUpActivity)

        signUp.setOnClickListener {

            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val passConf = passConfEdit.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty() && passConf.isNotEmpty()) {

                if (password == passConf) {
                    loadingDialog.startLoadingDialog()

                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                        if (it.isSuccessful) {
                            val newEmail = email.replace(".", "")
                            val User = User(email, "")
                            database.child(newEmail).setValue(User).addOnSuccessListener {

                                loadingDialog.dismissDialog()


                            }.addOnFailureListener{
                                loadingDialog.dismissDialog()



                            }
                            sendVerificationEmail()
                        } else {
                            val error = it.exception.toString().split(": ").takeLast(1).joinToString(": ")
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()

                        }
                        loadingDialog.dismissDialog()
                    }


                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()

            }

        }







    }

    private fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        val loadingDialog = LoadingDialog(this@SignUpActivity)
        loadingDialog.startLoadingDialog()
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadingDialog.dismissDialog()

                    FirebaseAuth.getInstance().signOut()

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Success")
                    builder.setMessage("Your account was successfully created! Verification link was sent to your email.")
                    builder.setCancelable(false)

                    builder.setPositiveButton("Ok") { dialog, which ->

                        onBackPressed()

                    }


                    builder.show()

                } else {
                    loadingDialog.dismissDialog()
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Success")
                    builder.setMessage("Your account was successfully created! But verification link was not sent to your email...")
                    builder.setCancelable(false)

                    builder.setPositiveButton("Ok") { dialog, which ->
                        onBackPressed()

                    }


                    builder.show()
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()

        }
        return super.onOptionsItemSelected(item)
    }


}