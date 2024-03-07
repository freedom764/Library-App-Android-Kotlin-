package edu.skku.cs.final_library

import java.io.Serializable

data class BookBorrowed(val id:String? = null, val title: String? = null, val authors: String? = null,val thumbnail: String? = null,
                        val description: String? = null, val pgcount: String? = null, val subtitle: String? = null,
                        val genre: String? = null, val year: String? = null, val link: String? = null, val score: Double? = null, val count: Double? = null,
                        val brY: Int? = null, val brM: Int? = null, val brD: Int? = null, val retY: Int? = null, val retM: Int? = null, val retD: Int? = null,
                        val history: Int? = null, val rated: Int? = null
                        ): Serializable {






}