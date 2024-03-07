package edu.skku.cs.final_library

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class PasswordChange : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_change)
        val passwordEdit = findViewById<EditText>(R.id.passwordEdit)
        val passConfEdit = findViewById<EditText>(R.id.repasswordEdit)
        val changeButton = findViewById<Button>(R.id.buttonChange)
        val user = FirebaseAuth.getInstance().currentUser
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        val loadingDialog = LoadingDialog(this)

        changeButton.setOnClickListener {


            val password = passwordEdit.text.toString()
            val passConf = passConfEdit.text.toString()
            if ( password.isNotEmpty() && passConf.isNotEmpty()) {

                if (password == passConf) {
                    loadingDialog.startLoadingDialog()

                    user!!.updatePassword(password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loadingDialog.dismissDialog()
                            Toast.makeText(this, "Password was changed successfully", Toast.LENGTH_SHORT).show()
                            onBackPressed()

                        } else {
                            loadingDialog.dismissDialog()
                            val error = task.exception.toString().split(": ").takeLast(1).joinToString(": ")
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()

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