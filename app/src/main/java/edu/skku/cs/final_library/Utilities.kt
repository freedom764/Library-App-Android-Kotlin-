import android.net.Uri
import android.util.Log
import edu.skku.cs.final_library.Book
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset

class Utilities {
    companion object{
        val LOG_TAG: String = Utilities::class.java.simpleName

        fun fetchBooks(queryUrl: Uri?): ArrayList<Book?>{
            val url: URL? = createUrl(queryUrl.toString())
            var jsonResponse: String? = null
            try {
                jsonResponse = makeHttpConnection(url)
            }
            catch (e: IOException){
                Log.e(LOG_TAG, "error closing input stream", e)
            }

            return extractBooks(jsonResponse)
        }

        private fun extractBooks(jsonResponse: String?): ArrayList<Book?> {
            var books: ArrayList<Book?> = ArrayList()
            var jsonObject = JSONObject(jsonResponse!!)
            if (jsonObject.getString("totalItems") == "0") {
                return books
            }
            if (jsonObject.has("items")) {
                val jsonArray = jsonObject.getJSONArray("items")
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val volume = item.getJSONObject("volumeInfo")
                    val id = item.getString("id")
                    val authors = StringBuilder()
                    val genre = StringBuilder()
                    if (volume.has("authors")) {
                        val authorsArray = volume.getJSONArray("authors")
                        for (i in 0 until authorsArray.length()) {
                            if (i == 0) {
                                authors.append(authorsArray.get(i).toString())
                            } else {
                                authors.append(", " + authorsArray.get(i).toString())
                            }
                        }
                    }
                    if (volume.has("categories")) {
                        val genreArray = volume.getJSONArray("categories")
                        for (i in 0 until genreArray.length()) {
                            if (i == 0) {
                                genre.append(genreArray.get(i).toString())
                            } else {
                                genre.append(", " + genreArray.get(i).toString())
                            }
                        }
                    }
                    val book = Book(authors.toString())
                    book.setGenre(genre.toString())
                    if (volume.has("title")) {
                        book.setTitle(volume.getString("title"))
                    }
                    if (volume.has("subtitle")) {
                        book.setSubtitle(volume.getString("subtitle"))
                    }
                    if (volume.has("pageCount")) {
                        book.setPageCount(volume.getString("pageCount"))
                    }
                    if (volume.has("imageLinks")) {
                        val imageLinks = volume.getJSONObject("imageLinks")
                        book.setThumbnail(imageLinks.getString("smallThumbnail"))
                    }
                    if (volume.has("description")) {
                        book.setDescription(volume.getString("description"))
                    }
                    if (volume.has("publishedDate")) {
                        book.setYear(volume.getString("publishedDate"))
                    }
                    if (volume.has("canonicalVolumeLink")) {
                        book.setLink(volume.getString("canonicalVolumeLink"))
                    }

                        book.setID(id)

                    books.add(book)
                }
            }
            return books
        }



        private fun makeHttpConnection(url: URL?): String? {
            var urlConnection: HttpURLConnection? = null
            var inputStream: InputStream? = null

            var jsonResponse: String? = null

            if (url==null){
                return jsonResponse
            }
            try {
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.readTimeout = 10000
                urlConnection.connectTimeout = 15000
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                if (urlConnection.responseCode == 200){
                    inputStream = urlConnection.inputStream
                    jsonResponse = readInputStream(inputStream)
                }
                else{
                    Log.e(LOG_TAG, "url not found: ${urlConnection.responseCode}")
                }
            }
            catch (e: IOException){
                Log.e(LOG_TAG, "problem retrieving json")
            }
            finally {
                urlConnection?.disconnect()
                inputStream?.close()
            }
            return  jsonResponse
        }

        private fun readInputStream(inputStream: InputStream?): String? {
            var output = StringBuilder()
            val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null){
                output.append(line)
                line = reader.readLine()
            }
            if (output.isEmpty()){
                Log.v(LOG_TAG, "output is empty")
            }
            return output.toString()
        }

        private fun createUrl(queryUrl: String?): URL? {
            var url: URL? = null
            if (queryUrl!!.isEmpty()){
                return url
            }
            try {
                url = URL(queryUrl)
            }
            catch (e: MalformedURLException){
                Log.e(LOG_TAG,"URL empty", e)
            }

            return url
        }
    }
}