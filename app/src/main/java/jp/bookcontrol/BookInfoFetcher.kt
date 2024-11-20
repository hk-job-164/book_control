package jp.bookcontrol

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class BookInfoFetcher(
    private val context: Context,
    private val binding: ViewBinding,  // または FragmentBinding
    private val onSuccess: (Book) -> Unit,
    private val onFailure: (String) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun fetchBookInfo(isbn: String) {
        // キーボードを閉じる
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(binding.root.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        if (isbn.isEmpty()) {
            Snackbar.make(
                binding.root,
                context.getString(R.string.input_isbn),
                Snackbar.LENGTH_LONG
            )
                .show()
            return
        }

        val url = StringBuilder()
            .append(context.getString(R.string.base_url))
            .append("?applicationId=").append(context.getString(R.string.app_id))
            .append("&format=").append(context.getString(R.string.json))
            .append("&isbnjan=").append(isbn)
            .toString()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onFailure(context.getString(R.string.failure))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.body?.string()?.let {
                        try {

                            val moshi = Moshi.Builder().build()
                            val jsonAdapter = moshi.adapter(ApiResponse::class.java)
                            val results = jsonAdapter.fromJson(it)
                            if (results!!.items.isEmpty()) {
                                onFailure(context.getString(R.string.not_found))
                                return
                            }

                            val responseBook = results.items.first().book

                            val book = Book(
                                -1,
                                responseBook.imageUrl,
                                responseBook.isbn,
                                responseBook.title,
                                responseBook.author,
                                responseBook.price,
                                responseBook.releaseDate,
                                responseBook.rakutenUrl
                            )

                            onSuccess(book)
                        } catch (e: JsonDataException) {
                            onFailure(context.getString(R.string.failure))
                        }
                    }
                } catch (e: IOException) {
                    onFailure(context.getString(R.string.failure))
                }
            }
        })
    }
}

