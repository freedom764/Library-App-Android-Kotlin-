package edu.skku.cs.final_library

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var databaseUser : DatabaseReference
    private lateinit var booksBor: ArrayList<BookBorrowed>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarr1)
        val listView = findViewById<RecyclerView>(R.id.listView1)
        val empText = findViewById<TextView>(R.id.emptyText1)
        val searchEdit = findViewById<EditText>(R.id.searchEdit1)
        booksBor = arrayListOf<BookBorrowed>()
        listView.visibility = View.GONE


            listView.layoutManager = LinearLayoutManager(this)
            val email = SaveSharedPreference.getUserName(this).toString()
            val newEmail = email.replace(".", "")
            databaseUser = FirebaseDatabase.getInstance().getReference("users/$newEmail/books")

            databaseUser.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksBor.clear()
                    if (snapshot.exists()) {
                        for (empSnap in snapshot.children) {
                            val empData = empSnap.getValue(BookBorrowed::class.java)
                            if (empData!!.history == 1) {
                                booksBor.add(empData)
                            }
                        }


                    }
                    if (booksBor.isEmpty()) {
                        empText.text = "No Books"
                        empText.visibility = View.VISIBLE
                    } else {
                        empText.visibility = View.GONE
                    }

                    progressBar.visibility= View.GONE
                    Log.d("fewfwe", booksBor.toString())
                    val mAdapter = BorrowedAdapter(booksBor)
                    listView.adapter = mAdapter
                    listView.visibility = View.VISIBLE
                    mAdapter.setOnItemClickListener(object : BorrowedAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {

                            val intent = Intent(this@HistoryActivity, BookPage::class.java)

                            val book = booksBor[position]
                            val bookNew = Book(book.authors!!)
                            bookNew.setID(book.id!!)
                            bookNew.setTitle(book.title!!)
                            bookNew.setSubtitle(book.subtitle!!)
                            bookNew.setLink(book.link!!)
                            bookNew.setDescription(book.description!!)
                            bookNew.setGenre(book.genre!!)
                            bookNew.setPageCount(book.pgcount!!)
                            bookNew.setYear(book.year!!)
                            bookNew.setThumbnail(book.thumbnail!!)

                            intent.putExtra("clicked", bookNew)
                            startActivity(intent)


                        }

                    })



                }


                override fun onCancelled(error: DatabaseError) {

                }

            })

            searchEdit.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val filteredList = booksBor.filter { book ->
                        book.title!!.contains(searchEdit.text.toString(), ignoreCase = true)
                    }
                    val mAdapter = BorrowedAdapter(filteredList as ArrayList<BookBorrowed>)
                    listView.adapter = mAdapter
                    mAdapter.setOnItemClickListener(object : BorrowedAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {

                            val intent = Intent(this@HistoryActivity, BookPage::class.java)

                            val book = filteredList[position]
                            val bookNew = Book(book.authors!!)
                            bookNew.setID(book.id!!)
                            bookNew.setTitle(book.title!!)
                            bookNew.setSubtitle(book.subtitle!!)
                            bookNew.setLink(book.link!!)
                            bookNew.setDescription(book.description!!)
                            bookNew.setGenre(book.genre!!)
                            bookNew.setPageCount(book.pgcount!!)
                            bookNew.setYear(book.year!!)
                            bookNew.setThumbnail(book.thumbnail!!)

                            intent.putExtra("clicked", bookNew)
                            startActivity(intent)
                        }

                    })
                    true
                } else {
                    false
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