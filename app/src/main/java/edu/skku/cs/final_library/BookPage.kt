package edu.skku.cs.final_library

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.autofill.SavedDatasetsInfo
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


class BookPage : AppCompatActivity() {
    private lateinit var database : DatabaseReference
    private lateinit var databaseUser : DatabaseReference
    private lateinit var dataUser : DatabaseReference
    private lateinit var books: ArrayList<BookFirebase>
    private lateinit var booksBor: ArrayList<BookBorrowed>
    private var isActivityVisible = false
    var only_once = 0

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_page)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val loadingDialog = LoadingDialog(this)
        val borrowBtn = findViewById<Button>(R.id.buttonBorrow)
        val previewBtn = findViewById<Button>(R.id.buttonPreview)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val book = intent.getSerializableExtra("clicked") as Book?
        val imageView = findViewById<ImageView>(R.id.thumbnail)
        val email = SaveSharedPreference.getUserName(this).toString()
        var justOpen = 0
        if (book!!.getThumbnail().isNotEmpty()){
            Glide.with(this).load(book!!.getThumbnail()).into(imageView)
        }
        else {
                imageView.setImageResource(R.drawable.book_icon)

        }

        val title = findViewById<TextView>(R.id.title)
        title.text = book.getTitle()
        val subtitle = findViewById<TextView>(R.id.subtitle)
        subtitle.text = book.getSubtitle()
        val author = findViewById<TextView>(R.id.authors)
        author.text = "by: "+book.getAuthors()
        val publishD = findViewById<TextView>(R.id.yearValue)
        publishD.text = " "+book.getYear()
        val description = findViewById<TextView>(R.id.description)
        description.text = book.getDescription()
        val pgCount = findViewById<TextView>(R.id.pgValue)
        pgCount.text = book.getPgCount()
        val genre = findViewById<TextView>(R.id.genValue)
        genre.text = book.getGenre()
        val url = book.getLink()
        database = FirebaseDatabase.getInstance().getReference("books")
        val newEmail = email.replace(".", "")
        databaseUser = FirebaseDatabase.getInstance().getReference("users/$newEmail/books")
        dataUser = FirebaseDatabase.getInstance().getReference("users/$newEmail")
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
                            if (bookFirebase.id == book.getID()) {
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
                        ratingBar.rating = (score/count).toFloat()
                        val ratingText = findViewById<TextView>(R.id.ratValue)
                        val df = DecimalFormat("#.##")
                        df.roundingMode = RoundingMode.CEILING
                        ratingText.text = df.format(score/count)

                        if(is_borrowed==1){
                            borrowBtn.isEnabled = false

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
                    if (bookFirebase.id == book.getID()) {
                        is_rated = bookFirebase.rated!!
                        is_history = bookFirebase.history!!
                        break
                    }
                }

                if (ratingBar.isEnabled) {
                    ratingBar.onRatingBarChangeListener =
                        OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                            if (justOpen != 0) {
                                Log.d("wrewrwer", is_history.toString())
                                if (is_history != -1) {
                                    if (is_rated == 0) {
                                        val bookFir = mapOf<String, Double>(
                                            "score" to (score + rating),
                                            "count" to (count + 1)
                                        )

                                        database.child(book.getID()).updateChildren(bookFir)
                                            .addOnSuccessListener {
                                                is_rated = 1
                                                val bookFi = mapOf<String, Int>(
                                                    "rated" to is_rated
                                                )

                                                databaseUser.child(book.getID())
                                                    .updateChildren(bookFi)
                                                    .addOnSuccessListener {
                                                        if (!messageShown2) {
                                                            Toast.makeText(
                                                                this@BookPage,
                                                                "Rated successfully",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            messageShown2 = true
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        // Handle failure
                                                    }
                                            }
                                            .addOnFailureListener {
                                                if (!messageShown2) {
                                                    Toast.makeText(
                                                        this@BookPage,
                                                        "Failed to rate",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    messageShown2 = true
                                                    messageShown = true
                                                    messageShown3 = true
                                                }
                                            }
                                    } else {
                                        ratingBar.isClickable = false
                                        ratingBar.isEnabled = false
                                        ratingBar.rating = (score / count).toFloat()
                                        if (isActivityVisible) {
                                            if (!messageShown) {
                                                Toast.makeText(
                                                    this@BookPage,
                                                    "You already rated this book",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                messageShown = true
                                            }
                                        }

                                    }
                                } else {
                                    ratingBar.isClickable = false
                                    ratingBar.isEnabled = false
                                    ratingBar.rating = (score / count).toFloat()
                                    if (isActivityVisible) {
                                        if (!messageShown3) {
                                            Toast.makeText(
                                                this@BookPage,
                                                "You cannot rate books you never borrowed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            messageShown3 = true
                                        }
                                    }

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



        borrowBtn.setOnClickListener {
            loadingDialog.startLoadingDialog()

            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            val day = c.get(Calendar.DAY_OF_MONTH)

            c.add(Calendar.DAY_OF_YEAR, 14)
            val yearR = c.get(Calendar.YEAR)
            val monthR = c.get(Calendar.MONTH) + 1
            val dayR = c.get(Calendar.DAY_OF_MONTH)

            val BookFirebase = BookFirebase(book.getID(),book.getTitle(),book.getAuthors(),book.getThumbnail(), book.getDescription(), book.getPgCount(), book.getSubtitle(), book.getGenre(), book.getYear(), book.getLink(), email, score , count)
            database.child(book.getID()).setValue(BookFirebase).addOnSuccessListener {
                Toast.makeText(this@BookPage,"Successfully Borrowed!",Toast.LENGTH_SHORT).show()

                val lastFir = mapOf<String,String>(
                    "last" to book.getGenre()


                )



                dataUser.updateChildren(lastFir).addOnSuccessListener {





                    }.addOnFailureListener {


                    }


            }.addOnFailureListener{



            }

            val BookBorrowed = BookBorrowed(book.getID(),book.getTitle(),book.getAuthors(),book.getThumbnail(), book.getDescription(), book.getPgCount(), book.getSubtitle(), book.getGenre(), book.getYear(), book.getLink(), score , count, year, month, day,yearR, monthR, dayR, 0, is_rated )
            databaseUser.child(book.getID()).setValue(BookBorrowed).addOnSuccessListener {

                loadingDialog.dismissDialog()
                SaveSharedPreference.setLast(this,"1")
                onBackPressed()

            }.addOnFailureListener{
                loadingDialog.dismissDialog()
                Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()


            }
        }
        previewBtn.setOnClickListener {


            if (url.isNotEmpty()) {
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