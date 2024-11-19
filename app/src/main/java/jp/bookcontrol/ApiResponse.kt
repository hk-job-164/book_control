package jp.bookcontrol

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "Items")
    val items: List<Items>
)

@JsonClass(generateAdapter = true)
data class Items(
    @Json(name = "Item")
    val book: ResponseBook
)

@JsonClass(generateAdapter = true)
data class ResponseBook(
    @Json(name = "smallImageUrl")
    val imageUrl: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "isbn")
    val isbn: String,
    @Json(name="author")
    val author: String,
    @Json(name="itemPrice")
    val price: String,
    @Json(name="salesDate")
    val releaseDate: String,
    @Json(name="itemUrl")
    val rakutenUrl: String,
)