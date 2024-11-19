package jp.bookcontrol

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import jp.bookcontrol.databinding.FragmentBookListBinding

/**
 * A fragment representing a list of Items.
 */
class BookFragment : Fragment() {
    private lateinit var realm: Realm
    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private val bookListAdapter by lazy { BookListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Realmデータベースとの接続を開く
        val config = RealmConfiguration.create(schema = setOf(Book::class))
        realm = Realm.open(config)


        bookListAdapter.onItemClick = {
            val intent = Intent(activity, BookAddActivity::class.java)
            intent.putExtra("id", it.id)
            activity?.startActivity(intent)
        }

        bookListAdapter.onItemClickWebViewCallback = {
            val intent = Intent(activity, WebViewActivity::class.java)
            intent.putExtra("url", it.rakutenUrl)
            activity?.startActivity(intent)
        }

        val recyclerView = binding.list

        // RecyclerViewのレイアウトマネージャーを設定
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // RecyclerViewにアダプターを設定
        recyclerView.adapter = bookListAdapter

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(activity, BookAddActivity::class.java)
            activity?.startActivity(intent)
        }

        updateData()

        // 区切り線追加
        val itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(itemDecoration)

        // スワイプで削除
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val book = bookListAdapter.currentList[position]
                Book.delete(book.id, realm)
                updateData()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.list)
        updateData()
    }

    private fun updateData() {
        bookListAdapter.submitList(Book.findAll(realm))
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onResume() {
        super.onResume()
        updateData()
    }
}