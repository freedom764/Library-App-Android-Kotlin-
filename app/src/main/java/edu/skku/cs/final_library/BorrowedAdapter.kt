package edu.skku.cs.final_library

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class BorrowedAdapter(private val books: ArrayList<BookBorrowed>) :
    RecyclerView.Adapter<BorrowedAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(edu.skku.cs.final_library.R.layout.book_item, parent, false)
        return ViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = books[position]
        holder.title.text = currentItem.title
        holder.authors.text = currentItem.authors
        val borY = currentItem.brY
        val borM = currentItem.brM
        val borD = currentItem.brD
        val retY = currentItem.retY
        val retM = currentItem.retM
        val retD = currentItem.retD
        holder.retDate.text = "$borY.$borM.$borD\n$retY.$retM.$retD"
        if (currentItem.thumbnail?.isNotEmpty() == true){
            Glide.with(holder.itemView.context).load(currentItem.thumbnail).dontAnimate().into(holder.imageView)
        }
        else {
            holder.imageView.setImageResource(edu.skku.cs.final_library.R.drawable.book_icon)
        }

    }

    override fun getItemCount(): Int {
        return books.size
    }

    class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val title : TextView = itemView.findViewById(R.id.title)
        val authors : TextView = itemView.findViewById(R.id.authors)
        val imageView : ImageView = itemView.findViewById(R.id.thumbnail)
        val retDate : TextView = itemView.findViewById(R.id.year)


        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }

    }

}