package edu.skku.cs.final_library

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*


class BorrowedFragment : Fragment() {

    private lateinit var databaseUser : DatabaseReference
    private lateinit var booksBor: ArrayList<BookBorrowed>
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_borrowed, container, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarr)
        val listView = view.findViewById<RecyclerView>(R.id.listView)
        val empText = view.findViewById<TextView>(R.id.emptyText)
        val searchEdit = view.findViewById<EditText>(R.id.searchEdit)
        booksBor = arrayListOf<BookBorrowed>()
        listView.visibility = View.GONE

            activity?.let {
                listView.layoutManager = LinearLayoutManager(it)
                val email = SaveSharedPreference.getUserName(it).toString()
                val newEmail = email.replace(".", "")
                databaseUser = FirebaseDatabase.getInstance().getReference("users/$newEmail/books")

                databaseUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        booksBor.clear()
                        if (snapshot.exists()) {
                            for (empSnap in snapshot.children) {
                                val empData = empSnap.getValue(BookBorrowed::class.java)
                                if (empData!!.history == 0) {
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

                                val intent = Intent(it, BorrowedBookPage::class.java)

                                val book = booksBor[position]
                                intent.putExtra("clicked", book)
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

                                val intent = Intent(it, BorrowedBookPage::class.java)

                                val book = filteredList[position]
                                intent.putExtra("clicked", book)
                                startActivity(intent)
                            }

                        })
                        true
                    } else {
                        false
                    }
                }




            }



        return view


    }


}







