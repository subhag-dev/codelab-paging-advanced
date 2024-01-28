package com.example.android.codelabs.paging.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.api.IN_QUALIFIER
import com.example.android.codelabs.paging.data.GithubRepository.Companion.NETWORK_PAGE_SIZE
import com.example.android.codelabs.paging.model.Repo
import retrofit2.HttpException
import java.io.IOException

const val GITHUB_STARTING_PAGE_INDEX = 1
class GithubPagingSource(
    private val service: GithubService,
    private val query: String
) : PagingSource<Int, Repo>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        val position = params.key ?: GITHUB_STARTING_PAGE_INDEX
        val apiQuery = query + IN_QUALIFIER
        return try {

            val repos = service.searchRepos(apiQuery, position, params.loadSize).items
            val prevKey = if (position == GITHUB_STARTING_PAGE_INDEX) {
                null
            } else {
                position - 1
            }
            val nextKey = if (repos.isEmpty()) {
                null
            } else {
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page (
                data = repos,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (ioException: IOException) {
            return LoadResult.Error(ioException)
        } catch (httpException: HttpException) {
            return LoadResult.Error(httpException)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

}