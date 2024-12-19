package nl.newlin.abnamro.assesment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.newlin.abnamro.assesment.data.GithubRepoDatasource
import nl.newlin.abnamro.data.GitRepo
import nl.newlin.abnamro.data.ReposDatabase
import nl.newlin.abnamro.network.DataResource
import nl.newlin.abnamro.network.GitHubRepository
import nl.newlin.abnamro.network.GithubApi
import nl.newlin.abnamro.network.GithubApiImpl
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(val datasource: GithubRepoDatasource): ViewModel() {

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            datasource.loadCachedData()
            //datasource.syncRepositories()
        }
    }

}