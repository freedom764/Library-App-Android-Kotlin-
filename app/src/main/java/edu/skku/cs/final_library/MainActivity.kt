package edu.skku.cs.final_library

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    private var exit : Int = 0

    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)
        firebaseAuth = FirebaseAuth.getInstance()




        val email = SaveSharedPreference.getUserName(this).toString()
        if (email.isNotEmpty()){

            val intent = Intent(this, UserMain::class.java)

            Log.d("weee", email)
            startActivity(intent)
            finish()
        }
        val loadingDialog = LoadingDialog(this@MainActivity)
        val signIn = findViewById<Button>(R.id.buttonSignIn)
        val emailEdit = findViewById<EditText>(R.id.emailEdit)
        val passwordEdit = findViewById<EditText>(R.id.passwordEdit)
        val signUpView = findViewById<TextView>(R.id.signUpView)
        val buttonG = findViewById<Button>(R.id.buttonG)
        val forgotView = findViewById<TextView>(R.id.textView)


        signIn.setOnClickListener {
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loadingDialog.startLoadingDialog()
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        checkIfEmailVerified()

                    } else {
                        val error = it.exception.toString().split(": ").takeLast(1).joinToString(": ")
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()

                    }
                    loadingDialog.dismissDialog()
                }
            } else {
                Toast.makeText(this, "Please fill all the fields ", Toast.LENGTH_SHORT).show()

            }

        }

        buttonG.setOnClickListener {
            googleSignInClient.signOut()
            signInGoogle()
        }





        signUpView.setOnClickListener {

            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
            emailEdit.text.clear()
            passwordEdit.text.clear()

        }

        forgotView.setOnClickListener() {
            showAlertWithTextInputLayout(this)
        }

    }

    private fun checkIfEmailVerified() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user!!.isEmailVerified) {
            val intent = Intent(this, UserMain::class.java)
            SaveSharedPreference.setUserName(this@MainActivity, user.email.toString())
            startActivity(intent)
            finish()
            Toast.makeText(this, "Successfully logged in", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Email is not verified!", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()

            //restart this activity
        }
    }
    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                val email = account.email
                val auth = FirebaseAuth.getInstance()
                auth.fetchSignInMethodsForEmail(email!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (signInMethods != null && signInMethods.isNotEmpty()) {
                                // Email exists in Firebase
                                updateUI(account)
                            } else {
                                // Email does not exist in Firebase
                                Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateUI(account: GoogleSignInAccount) {
        val loadingDialog = LoadingDialog(this@MainActivity)
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        loadingDialog.startLoadingDialog()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                val user = FirebaseAuth.getInstance().currentUser
                if (user!!.isEmailVerified) {
                    val intent : Intent = Intent(this , UserMain::class.java)
                    SaveSharedPreference.setUserName(this@MainActivity, account.email.toString())

                    startActivity(intent)
                    finish()

                }
                else {
                    googleSignInClient.signOut()
                    firebaseAuth.signOut()
                    Toast.makeText(this, "Email is not verified!", Toast.LENGTH_SHORT).show()

                }

            }else{
                val error = it.exception.toString().split(": ").takeLast(1).joinToString(": ")
                Toast.makeText(this, error , Toast.LENGTH_SHORT).show()

            }
            loadingDialog.dismissDialog()
        }
    }

    private fun showAlertWithTextInputLayout(context: Context) {
        val taskEditText = EditText(context)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Forgot Password")
            .setMessage("Enter Your Email")
            .setView(taskEditText)
            .setPositiveButton(
                "Reset"
            ) { dialog, which -> val task = taskEditText.text.toString()
                FirebaseAuth.getInstance().sendPasswordResetEmail(task)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset link was sent!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }


}