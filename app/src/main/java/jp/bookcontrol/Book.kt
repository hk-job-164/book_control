package jp.bookcontrol

import io.realm.kotlin.Realm
import io.realm.kotlin.TypedRealm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class Book(
    id: Int,
    imageUrl: String,
    isbn: String,
    title: String,
    author: String,
    price: String,
    releaseDate: String,
    rakutenUrl: String
) : RealmObject {
    @PrimaryKey
    var id = -1
    var imageUrl = ""
    var isbn = ""
    var title = ""
    var author = ""
    var price = ""
    var releaseDate = ""
    var rakutenUrl = ""

    init {
        this.id = id
        this.imageUrl = imageUrl
        this.isbn = isbn
        this.title = title
        this.author = author
        this.price = price
        this.releaseDate = releaseDate
        this.rakutenUrl = rakutenUrl
    }

    constructor() : this(-1, "", "", "", "", "", "", "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false
        return id == other.id && title == other.title
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        return result
    }

    companion object {
        /**
         * お気に入りのShopを全件取得
         */
        fun findAll(realm: Realm): List<Book> {
            // Realmデータベースから書籍情報を取得
            // mapでディープコピーしてresultに代入する
            val result = realm.query<Book>().find()
                .map {
                    Book(
                        it.id,
                        it.imageUrl,
                        it.isbn,
                        it.title,
                        it.author,
                        it.price,
                        it.releaseDate,
                        it.rakutenUrl
                    )
                }

            return result
        }

        /**
         * 登録されているBookをidで検索して返す
         * 登録されていなければnullで返す
         */
        fun findBy(id: Int, realm: TypedRealm): Book? {
            return realm.query<Book>("id=='$id'").first().find()
        }

        /**
         * 書籍追加
         */
        fun insert(book: Book, realm: Realm): Boolean {
            // 最大のid+1をセット
            book.id = (realm.query<Book>().max("id", Int::class).find() ?: -1) + 1

            if (book.title == "") {
                return false
            }


            // 登録処理
            realm.writeBlocking {
                copyToRealm(book)
            }

            return true
        }

        suspend fun update(book: Book, realm: Realm): Boolean {
            realm.write {
                val targetBook = findBy(book.id, this)
                if (targetBook?.title == "") {
                    return@write false
                }

                targetBook?.let {
                    it.imageUrl = book.imageUrl
                    it.isbn = book.isbn
                    it.title = book.title
                    it.author = book.author
                    it.price = book.price
                    it.releaseDate = book.releaseDate
                    it.rakutenUrl = book.rakutenUrl
                }
            }

            return true
        }

        /**
         * idでお気に入りから削除する
         */
        fun delete(id: Int, realm: Realm) {
            // 削除処理
            realm.writeBlocking {
                val favoriteShops = query<Book>("id=='$id'").find()
                favoriteShops.forEach {
                    delete(it)
                }
            }
        }
    }
}