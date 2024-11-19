package jp.bookcontrol

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import jp.bookcontrol.databinding.ActivityBookAddBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookAddActivity : AppCompatActivity() {
    private var id: Int = -1
    private var book: Book? = null
    private lateinit var binding: ActivityBookAddBinding
    private var calendar = Calendar.getInstance()
    private lateinit var realm: Realm

    private var launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            getBookInfoFromIsbn(result.data!!.getStringExtra("isbn").toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        book = Book()
        val config = RealmConfiguration.create(schema = setOf(Book::class))
        realm = Realm.open(config)


        id = intent.getIntExtra("id", -1)
        if (id != -1) {
            book = Book.findBy(id, realm)
            if (book != null) {
                setEditText(book!!)
            }
        }

        binding.calendarButton.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    setDateButtonText()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.addButton.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                addBook()
            }
        }

        binding.fromIsbnButton.setOnClickListener {
            getBookInfoFromIsbn(binding.isbnText.text.toString())
        }

        binding.fromCameraButton.setOnClickListener {
            val intent = Intent(this, CameraViewActivity::class.java)
            launcher.launch(intent)
        }
    }

    private fun getBookInfoFromIsbn(isbn: String) {
        val bookFetcher = BookInfoFetcher(
            this,
            binding,
            onFailure = {
                Snackbar.make(binding.root, getString(R.string.failure), Snackbar.LENGTH_LONG)
                    .show()
            },
            onSuccess = {
                runOnUiThread {
                    setEditText(it)
                }
            }
        )
        bookFetcher.fetchBookInfo(isbn)
    }

    private fun setEditText(book: Book) {
        book.let {
            binding.imageUrlText.setText(it.imageUrl)
            binding.isbnText.setText(it.isbn)
            binding.titleText.setText(it.title)
            binding.authorText.setText(it.author)
            binding.releaseDateText.setText(it.releaseDate)
            binding.priceText.setText(it.price)
            binding.rakutenLinkText.setText(it.rakutenUrl)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private suspend fun addBook() {
        val upsertBook = Book(
            id = id.takeIf { it != -1 } ?: -1,
            binding.imageUrlText.text.toString(),
            binding.isbnText.text.toString(),
            binding.titleText.text.toString(),
            binding.authorText.text.toString(),
            binding.priceText.text.toString(),
            binding.releaseDateText.text.toString(),
            binding.rakutenLinkText.text.toString()
        )

        val isSuccess = if (id == -1) {
            Book.insert(upsertBook, realm)
        } else {
            Book.update(upsertBook, realm)
        }

        if (isSuccess) {
            finish()
        } else {
            // タイトルが入力されていない時はエラーを表示するだけ
            Snackbar
                .make(binding.root, this.getString(R.string.input_title), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun setDateButtonText() {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.JAPANESE)
        binding.releaseDateText.setText(dateFormat.format(calendar.time))
    }
}