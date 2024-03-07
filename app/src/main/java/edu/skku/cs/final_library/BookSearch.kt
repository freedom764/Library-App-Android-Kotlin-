package edu.skku.cs.final_library

import Utilities
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookSearch : AppCompatActivity(), LoaderManager.LoaderCallbacks<ArrayList<Book?>>, onClick {
    private lateinit var mQuery: String
    private lateinit var mAdapter: SearchAdapter
    private lateinit var mEmptyTextView: TextView
    private lateinit var listView : RecyclerView
    var maxResults = 20
    var startIndex = 0
    var hasMoreResults = true
    var count = 0
    var isLoading = false
    var isNewSearch = false
    private lateinit var books: ArrayList<BookFirebase>

    companion object{
        val BOOK_API = "https://www.googleapis.com/books/v1/volumes?"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)
        SaveSharedPreference.setLast(this,null)
        listView = findViewById<RecyclerView>(R.id.recyclerView)
        listView.layoutManager = LinearLayoutManager(this)
        val bundle = intent.extras
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mQuery = bundle!!.get("query").toString()
        Log.d("query", mQuery)
        mEmptyTextView = findViewById(R.id.empty_textView)
        mAdapter = SearchAdapter(this, ArrayList())
        val searchEditText = findViewById<EditText>(R.id.editSearch)
        searchEditText.setText(mQuery)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true
        if (isConnected){
            LoaderManager.getInstance(this).initLoader(1, null, this)
        }
        else{

            progressBar.visibility = View.GONE
            mEmptyTextView.text = "No Internet!"
        }
        listView.adapter = mAdapter
        val progressBar2 = findViewById<ProgressBar>(R.id.progress_bar2)
        progressBar2.visibility = View.GONE
        books = arrayListOf<BookFirebase>()
        val database = FirebaseDatabase.getInstance().getReference("books")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                books.clear()
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val empData = empSnap.getValue(BookFirebase::class.java)
                        books.add(empData!!)
                    }


                }



            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!isLoading && !isNewSearch && hasMoreResults && lastVisibleItemPosition == totalItemCount - 1) { // Check the isLoading and isNewSearch flags before loading new data

                        progressBar2.visibility = View.VISIBLE


                    count++
                    startIndex += maxResults
                    val url = Uri.parse(BOOK_API).buildUpon()
                        .appendQueryParameter("q", mQuery)
                        .appendQueryParameter("printType", "books")
                        .appendQueryParameter("maxResults", maxResults.toString())
                        .appendQueryParameter("startIndex", startIndex.toString())
                        .build()
                    isLoading = true
                    LoaderManager.getInstance(this@BookSearch).restartLoader(1, null, this@BookSearch)
                }
            }
        })

        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                mQuery = v.text.toString()
                if (mQuery.isNotEmpty()){
                    startIndex = 0
                    hasMoreResults = true
                    progressBar.visibility = View.VISIBLE
                    Log.d("search", mQuery)

                    mAdapter.clear()
                    isNewSearch = true
                    LoaderManager.getInstance(this).restartLoader(1, null, this)
                }
                else {
                    Toast.makeText(this, "Enter something", Toast.LENGTH_SHORT).show()
                }

                true
            } else {
                false
            }
        }
        val showAllBooksCheckBox = findViewById<CheckBox>(R.id.checkBox)
        showAllBooksCheckBox.setOnCheckedChangeListener { _, _ ->
            progressBar.visibility = View.VISIBLE
            mAdapter.clear()
            startIndex = 0
            isNewSearch = true
            LoaderManager.getInstance(this).restartLoader(1, null, this)
        }

    }

    class SearchBook(context: Context, url: String, query: String, maxResults: Int, startIndex: Int): AsyncTaskLoader<ArrayList<Book?>>(context){
        private var mUrl = url
        private var mQuery = query
        private var mMaxResults = maxResults
        private var mStartIndex = startIndex

        override fun onStartLoading() {
            forceLoad()
        }

        override fun loadInBackground(): ArrayList<Book?> {

            val QUERY_PARA: String = "q"
            val TYPE: String = "printType"
            val MAX_RESULTS: String = "maxResults"
            val START_INDEX: String = "startIndex"
            val url = Uri.parse(mUrl).buildUpon()
                .appendQueryParameter(QUERY_PARA, mQuery)
                .appendQueryParameter(TYPE, "books")
                .appendQueryParameter(MAX_RESULTS, mMaxResults.toString())
                .appendQueryParameter(START_INDEX, mStartIndex.toString())
                .build()
            Log.d("url", url.toString())
            return Utilities.fetchBooks(url)
        }
    }



    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ArrayList<Book?>> {
        return SearchBook(this, BOOK_API, mQuery, maxResults, startIndex)
    }

    override fun onLoadFinished(loader: Loader<ArrayList<Book?>>, data: ArrayList<Book?>){
        isLoading = false
        isNewSearch = false

        var progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressBar2 = findViewById<ProgressBar>(R.id.progress_bar2)
        progressBar.visibility = View.GONE
        progressBar2.visibility = View.GONE

        val showAllBooksCheckBox = findViewById<CheckBox>(R.id.checkBox)
        val filteredData = if (showAllBooksCheckBox.isChecked) {
            data
        } else {
            val email = SaveSharedPreference.getUserName(this).toString()
            val excludedBooks = books.filter { it.who_borrowed != "noone" }.map { it.id }
            data.filter { book -> !excludedBooks.contains(book?.getID()) }
        }

        if (filteredData.isNotEmpty()){
            mAdapter.append(filteredData as ArrayList<Book?>)
        } else {
            hasMoreResults = false
            if (count==0){
                mEmptyTextView.text = "No Books"
            }
            else {
                Toast.makeText(this, "No more books to show", Toast.LENGTH_SHORT).show()
            }

        }
    }




    override fun onLoaderReset(loader: Loader<ArrayList<Book?>>) {

    }

    override fun onItemClick(item: Book?) {
        val intent = Intent(this, BookPage::class.java)
        intent.putExtra("clicked", item)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRestart() {
        super.onRestart()
        if (SaveSharedPreference.getLast(this)!!.isNotEmpty()){
            finish()
            val intent = getIntent()
            intent.putExtra("query", mQuery)
            intent.action = Intent.ACTION_SEND
            startActivity(getIntent())
            SaveSharedPreference.setLast(this,null)
        }

    }

}


interface onClick{
    fun onItemClick(item: Book?)
}

