package com.rinat.livetypingapp.network.common

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rinat.livetypingapp.data.BookPreview
import com.rinat.livetypingapp.extensions.getError
import com.rinat.livetypingapp.extensions.mapToBookPreview
import com.rinat.livetypingapp.network.api.BookApi
import com.rinat.livetypingapp.utils.Constants.HTTP_STATUS_CODE_SUCCES
import com.rinat.livetypingapp.utils.Constants.HTTP_STATUS_NOT_FOUND
import retrofit2.HttpException
import java.io.IOException

private const val UNSPLASH_STARTING_PAGE_INDEX = 0

class UnsplashPagingSource(
    private val unsplashApi: BookApi,
    private val query: String
) : PagingSource<Int, BookPreview>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BookPreview> {
        val position = params.key ?: UNSPLASH_STARTING_PAGE_INDEX

        return try {

            val response = unsplashApi.getBooks(query, position * params.loadSize, query)
            when (response.code()) {
                HTTP_STATUS_CODE_SUCCES -> {
                    val books =
                        response.body()?.items?.map { it.mapToBookPreview() } ?: emptyList()
                    LoadResult.Page(
                        data = books,
                        prevKey = if (position == UNSPLASH_STARTING_PAGE_INDEX) null else position - 1,
                        nextKey = if (books.isEmpty()) null else position + 1
                    )
                }
                HTTP_STATUS_NOT_FOUND -> {
                    LoadResult.Error(Exception("${response.code()}"))
                }
                else -> {
                    LoadResult.Error(
                        Exception(
                            response.getError()?.message
                        )
                    )
                }
            }

        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, BookPreview>): Int? {
        TODO("Not yet implemented")
    }
}


