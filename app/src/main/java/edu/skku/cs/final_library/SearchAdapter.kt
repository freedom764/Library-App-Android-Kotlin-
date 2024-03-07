package edu.skku.cs.final_library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class SearchAdapter(private val listener: onClick, var  books: ArrayList<Book?>): RecyclerView.Adapter<ImageHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val targetView = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        var viewHolder =  ImageHolder(targetView)
        targetView.setOnClickListener(){
            listener.onItemClick(books[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val currentItem = books[position]
        holder.titleView.text = currentItem!!.getTitle()
        holder.authors.text = currentItem.getAuthors()
        holder.year.text = currentItem.getYear()
        if (currentItem.getThumbnail().isNotEmpty()){
            Glide.with(holder.itemView.context).load(currentItem.getThumbnail()).dontAnimate().into(holder.imageView)
        }
        else {
            holder.imageView.setImageResource(R.drawable.book_icon)
        }

    }

    override fun getItemCount(): Int {
        return books.size
    }
    fun update(updatedArray: ArrayList<Book?>) {
        books.clear()
        books.addAll(updatedArray)
        notifyDataSetChanged()
    }
    fun append(newData: ArrayList<Book?>) {
        books.addAll(newData)
        notifyDataSetChanged()
    }
    fun clear() {
        books.clear()
        notifyDataSetChanged()

    }
}
class ImageHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val titleView: TextView = itemView.findViewById(R.id.title)
    val authors = itemView.findViewById<TextView>(R.id.authors)
    val year = itemView.findViewById<TextView>(R.id.year)
    val imageView = itemView.findViewById<ImageView>(R.id.thumbnail)
}
