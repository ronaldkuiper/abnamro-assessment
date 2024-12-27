package nl.newlin.abnamro.assesment.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import nl.newlin.abnamro.data.GitRepoEntity
import nl.newlin.abnamro.data.ReposDatabase
import nl.newlin.abnamro.network.DataResource
import nl.newlin.abnamro.network.GitHubRepository
import nl.newlin.abnamro.network.GithubApi
import nl.newlin.abnamro.network.RepoVisibility
import org.koin.core.annotation.Single

@Single
class GithubRepoDatasource(val api: GithubApi, val database: ReposDatabase) {

    private val _repos = MutableStateFlow<List<GitHubRepository>>(emptyList())
    private val _isSyncing = MutableStateFlow(false)

    val repos = _repos.asSharedFlow()
    val isSyncing = _isSyncing.asSharedFlow()

    suspend fun syncRepositories() {
        if (_isSyncing.value) return

        _isSyncing.update { true }
        fetchData()
    }

    suspend fun loadCachedData() {
        val repos = database.getAll()
        _repos.update {
            repos.map {
                GitHubRepository(
                    id = it.uid,
                    name = it.name,
                    fullName = it.fullName,
                    private = it.private,
                    visibility = RepoVisibility.valueOf(it.visibility),
                    description = it.description,
                    avatarImageUrl = it.avatarImageUrl,
                    htmlUrl = it.htmlUrl
                )
            }
        }
    }

    private suspend fun storeResult(allRepos: List<GitHubRepository>) {
        database.deleteAll()
        database.saveAll(allRepos.map {
            GitRepoEntity(
                uid = it.id,
                name = it.name,
                fullName = it.fullName,
                private = it.private,
                visibility = it.visibility.toString(),
                description = it.description,
                avatarImageUrl = it.avatarImageUrl,
                htmlUrl = it.htmlUrl,
            )
        })
    }

    private suspend fun fetchData(currentPage: Int = 1, currentResults: List<GitHubRepository> = emptyList()) {
        api.fetchRepositories(currentPage, 10).collect { result ->
            if (result is DataResource.Success) {
                _repos.update { currentResults + result.data }
                if (result.data.isEmpty()) {
                    println("No more data found - stopping")
                    storeResult(currentResults)
                    _isSyncing.update { false }
                }
                else {
                    fetchData(currentPage + 1, currentResults + result.data)
                }
            }
            else if (result is DataResource.Error) {
                println("Unexpected error - stopping")
                _isSyncing.update { false }
            }
        }
    }
}