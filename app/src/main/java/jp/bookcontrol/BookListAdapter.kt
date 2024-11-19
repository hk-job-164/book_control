package jp.bookcontrol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import jp.bookcontrol.databinding.FragmentBookBinding
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class BookListAdapter :
    ListAdapter<Book, BookListAdapter.BookViewHolder>(BookDiffCallback()) {

    var onItemClick: ((Book) -> Unit)? = null
    var onItemClickWebViewCallback : ((Book) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(
            FragmentBookBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position), this)
    }

    inner class BookViewHolder(private val binding: FragmentBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book, adapter: BookListAdapter) {
            binding.title.text = book.title
            if (book.imageUrl != "") {
                Picasso.get().load(book.imageUrl).into(binding.bookImageView)
            }

            binding.title.setOnClickListener{
                adapter.onItemClick?.invoke(book)
            }

            binding.bookImageView.setOnClickListener {
                adapter.onItemClickWebViewCallback?.invoke(book)
            }
        }
    }

    // DiffUtilのコールバッククラス
    internal class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }

}
