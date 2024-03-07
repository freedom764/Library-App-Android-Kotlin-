package edu.skku.cs.final_library

import Utilities
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class UserFragment : Fragment(), LoaderManager.LoaderCallbacks<ArrayList<Book?>>, onClick {
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var mQuery: String
    private lateinit var mAdapter: SearchAdapter
    //private lateinit var mEmptyTextView: TextView
    private lateinit var listView : RecyclerView
    private lateinit var context: Context
    private lateinit var dataUser : DatabaseReference

    companion object{
        val BOOK_API = "https://www.googleapis.com/books/v1/volumes?"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {






        val view = inflater.inflate(R.layout.fragment_book_search, container, false)
        activity?.let {
            var last: String? = null
            context = it
            val email = SaveSharedPreference.getUserName(it).toString()
            val textEmail = view.findViewById<TextView>(R.id.textView4)
            val empText = view.findViewById<TextView>(R.id.textView6)
            empText.visibility = View.GONE
            textEmail.text = email
            val newEmail = email.replace(".", "")
            dataUser = FirebaseDatabase.getInstance().getReference("users/$newEmail")
            dataUser.child("last").get().addOnSuccessListener {
               last = it.value as String?
                listView = view.findViewById<RecyclerView>(R.id.recyclerView2)
                listView.layoutManager = LinearLayoutManager(context)
                mQuery = last.toString()

                // mEmptyTextView = view.findViewById(R.id.empty_textView)
                mAdapter = SearchAdapter(this, ArrayList())

                val cm =
                   context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                val isConnected = activeNetwork?.isConnectedOrConnecting == true
                if (isConnected) {
                    LoaderManager.getInstance(this).initLoader(1, null, this)
                } else {
                    val progressBar = view.findViewById<ProgressBar>(R.id.progressBar3)
                    progressBar.visibility = View.GONE
                    //mEmptyTextView.text = "No Internet!"
                }
                if (mQuery.isNullOrEmpty()) {
                    val progressBar = view.findViewById<ProgressBar>(R.id.progressBar3)
                    progressBar.visibility = View.GONE
                    empText.visibility = View.VISIBLE
                    empText.text = "No Recommendations"
                }
                else {
                    listView.adapter = mAdapter
                }
            }.addOnFailureListener{

            }
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(it , gso)
            firebaseAuth = FirebaseAuth.getInstance()







        }
        return view
    }

    class SearchBook(context: Context, url: String, query: String): AsyncTaskLoader<ArrayList<Book?>>(context){
        private var mUrl = url
        private var mQuery = query

        override fun onStartLoading() {
            forceLoad()
        }

        override fun loadInBackground(): ArrayList<Book?> {
            val QUERY_PARA: String = "q"
            val TYPE: String = "printType"
            val MAX_RESULTS: String = "maxResults"
            val url = Uri.parse(mUrl).buildUpon().appendQueryParameter(QUERY_PARA, "subject:$mQuery").appendQueryParameter(TYPE, "books").appendQueryParameter(MAX_RESULTS, "30").build()
            return Utilities.fetchBooks(url)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ArrayList<Book?>> {
        return SearchBook(requireContext(), BOOK_API, mQuery)
    }

    override fun onLoadFinished(loader: Loader<ArrayList<Book?>>, data: ArrayList<Book?>){
        var progressBar = view?.findViewById<ProgressBar>(R.id.progressBar3)
        progressBar?.visibility = View.GONE
        if (data.isNotEmpty()){
            mAdapter.update(data)
        }
        else{
            //mEmptyTextView.text = "No Books"
        }
    }

    override fun onLoaderReset(loader: Loader<ArrayList<Book?>>) {

    }

    override fun onItemClick(item: Book?) {
        val intent = Intent(requireContext(), BookPage::class.java)
        intent.putExtra("clicked", item)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val inflater: MenuInflater = inflater
        inflater.inflate(R.menu.top_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {



            R.id.seeBor -> {

                val intent = Intent(context, HistoryActivity::class.java)

                startActivity(intent)
                true
            }
            R.id.changePas -> {
                val intent = Intent(context, PasswordChange::class.java)

                startActivity(intent)
                true
            }
            R.id.signOut -> {
                SaveSharedPreference.setUserName(context, null)
                firebaseAuth.signOut()
                googleSignInClient.signOut()
                val intent = Intent(context, MainActivity::class.java)

                startActivity(intent)
                activity?.finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}


