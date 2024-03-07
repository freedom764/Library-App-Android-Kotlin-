package edu.skku.cs.final_library

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val searchButton = view.findViewById<Button>(R.id.buttonSearch)
        val searchEdit = view.findViewById<EditText>(R.id.searchEdit)
        searchButton.setOnClickListener {

            activity?.let {
                val intent = Intent(it, BookSearch::class.java)
                val query = searchEdit.text.toString()
                if (query == ""){
                    Toast.makeText(it, "Enter something", Toast.LENGTH_SHORT).show()
                }
                else {
                    intent.putExtra("query", query)
                    intent.action = Intent.ACTION_SEND
                    it.startActivity(intent)
                }

            }
        }
        return view


    }


}