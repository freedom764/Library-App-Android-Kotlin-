package edu.skku.cs.final_library

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class BorrowedBookPage : AppCompatActivity() {
    private lateinit var database : DatabaseReference
    private lateinit var databaseUser : DatabaseReference
    private lateinit var books: ArrayList<BookFirebase>
    private lateinit var booksBor: ArrayList<BookBorrowed>
    private var isActivityVisible = false

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borrowed_book_page)

        val book = intent.getSerializableExtra("clicked") as BookBorrowed?
        Log.d("ewwefwe", book?.title!!)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val loadingDialog = LoadingDialog(this)
        val returnBtn = findViewById<Button>(R.id.buttonReturn)
        val previewBtn = findViewById<Button>(R.id.buttonPreview1)
        val ratingBar1 = findViewById<RatingBar>(R.id.ratingBar1)

        val imageView = findViewById<ImageView>(R.id.thumbnail1)
        val email = SaveSharedPreference.getUserName(this).toString()
        var justOpen = 0
        if (book.thumbnail!!.isNotEmpty()){
            Glide.with(this).load(book.thumbnail!!).into(imageView)
        }
        else {
            imageView.setImageResource(R.drawable.book_icon)

        }

        val title = findViewById<TextView>(R.id.title1)
        title.text = book.title
        val subtitle = findViewById<TextView>(R.id.subtitle1)
        subtitle.text = book.subtitle
        val author = findViewById<TextView>(R.id.authors1)
        author.text = "by: "+book.authors
        val publishD = findViewById<TextView>(R.id.yearValue1)
        publishD.text = " "+book.year
        val description = findViewById<TextView>(R.id.description1)
        description.text = book.description
        val pgCount = findViewById<TextView>(R.id.pgValue1)
        pgCount.text = book.pgcount
        val genre = findViewById<TextView>(R.id.genValue1)
        genre.text = book.genre
        val url = book.link
        val borDate = findViewById<TextView>(R.id.borVal)
        val retDate = findViewById<TextView>(R.id.borVal2)
        val borY = book.brY
        val borM = book.brM
        val borD = book.brD
        val retY = book.retY
        val retM = book.retM
        val retD = book.retD
        borDate.text ="$borY.$borM.$borD"
        retDate.text ="$retY.$retM.$retD"
        database = FirebaseDatabase.getInstance().getReference("books")
        val newEmail = email.replace(".", "")
        databaseUser = FirebaseDatabase.getInstance().getReference("users/$newEmail/books")
        var score = 0.0
        var count = 0.0
        var is_borrowed = 0
        var is_rated = 0
        var is_history = -1
        var whoBorrow: String = ""



        books = arrayListOf<BookFirebase>()
        booksBor = arrayListOf<BookBorrowed>()


        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                books.clear()
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val empData = empSnap.getValue(BookFirebase::class.java)
                        books.add(empData!!)
                    }
                    for (bookFirebase in books) {
                        if (bookFirebase.id == book.id) {
                            score = bookFirebase.score!!
                            count = bookFirebase.count!!
                            if (bookFirebase.who_borrowed=="noone")
                                is_borrowed = 0
                            else {
                                is_borrowed = 1
                                whoBorrow = bookFirebase.who_borrowed.toString()

                            }

                            break
                        }
                    }
                    ratingBar1.rating = (score/count).toFloat()
                    val ratingText = findViewById<TextView>(R.id.ratValue1)
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.CEILING
                    ratingText.text = df.format(score/count)

                    if(is_borrowed==0){
                        returnBtn.isEnabled = false

                    }




                }







            }


            override fun onCancelled(error: DatabaseError) {

            }

        })



        var messageShown = false
        var messageShown2 = false
        var messageShown3 = false


        databaseUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                booksBor.clear()
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val empData = empSnap.getValue(BookBorrowed::class.java)
                        booksBor.add(empData!!)
                    }
                }

                for (bookFirebase in booksBor) {
                    if (bookFirebase.id == book.id!!) {
                        is_rated = bookFirebase.rated!!
                        is_history = bookFirebase.history!!
                        break
                    }
                }

                if (ratingBar1.isEnabled) {
                    ratingBar1.onRatingBarChangeListener =
                        RatingBar.OnRatingBarChangeListener { ratingBar1, rating, fromUser ->
                            if (justOpen != 0) {
                                Log.d("wrewrwer1", is_history.toString())
                                if (is_history != -1) {
                                    if (is_rated == 0) {
                                        val bookFir = mapOf<String, Double>(
                                            "score" to (score + rating),
                                            "count" to (count + 1)
                                        )

                                        database.child(book.id!!).updateChildren(bookFir)
                                            .addOnSuccessListener {
                                                is_rated = 1
                                                val bookFi = mapOf<String, Int>(
                                                    "rated" to is_rated
                                                )

                                                databaseUser.child(book.id!!)
                                                    .updateChildren(bookFi)
                                                    .addOnSuccessListener {
                                                        if (!messageShown2) {
                                                            Toast.makeText(
                                                                this@BorrowedBookPage,
                                                                "Rated successfully",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            messageShown2 = true
                                                            messageShown = true
                                                            messageShown3 = true
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        // Handle failure
                                                    }
                                            }
                                            .addOnFailureListener {
                                                if (!messageShown2) {
                                                    Toast.makeText(
                                                        this@BorrowedBookPage,
                                                        "Failed to rate",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    messageShown2 = true
                                                }
                                            }
                                    } else {
                                        ratingBar1.isClickable = false
                                        ratingBar1.isEnabled = false
                                        ratingBar1.rating = (score / count).toFloat()
                                        if (isActivityVisible) {
                                            if (!messageShown3) {
                                                Toast.makeText(
                                                    this@BorrowedBookPage,
                                                    "You already rated this book",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                messageShown3 = true
                                            }
                                        }

                                    }
                                } else {
                                    ratingBar1.isClickable = false
                                    ratingBar1.isEnabled = false
                                    ratingBar1.rating = (score / count).toFloat()

                                }
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation
            }
        })
        Handler(Looper.getMainLooper()).postDelayed({
            justOpen++
        }, 500)



        returnBtn.setOnClickListener {
            loadingDialog.startLoadingDialog()

            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            val day = c.get(Calendar.DAY_OF_MONTH)

            val returnFir = mapOf<String, String>(
                "who_borrowed" to "noone"


            )


            database.child(book.id!!).updateChildren(returnFir)
                .addOnSuccessListener {

                    val changeUserFir = mapOf<String, Int>(
                        "history" to 1,
                        "retY" to year,
                        "retM" to month,
                        "retD" to day



                    )


                    databaseUser.child(book.id!!)
                        .updateChildren(changeUserFir).addOnSuccessListener {
                            Toast.makeText(
                                this@BorrowedBookPage,
                                "Returned successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingDialog.dismissDialog()
                            onBackPressed()

                        }.addOnFailureListener {
                            loadingDialog.dismissDialog()


                        }




                }.addOnFailureListener {
                    loadingDialog.dismissDialog()


                    Toast.makeText(
                        this@BorrowedBookPage,
                        "Failed to return",
                        Toast.LENGTH_SHORT
                    ).show()


                }



        }
        previewBtn.setOnClickListener {


            if (url!!.isNotEmpty()) {
                loadingDialog.startLoadingDialog()
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "http://$url"
                }


                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                loadingDialog.dismissDialog()
                startActivity(browserIntent)
            }
            else {
                Toast.makeText(this,"Preview is not available",Toast.LENGTH_SHORT).show()
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