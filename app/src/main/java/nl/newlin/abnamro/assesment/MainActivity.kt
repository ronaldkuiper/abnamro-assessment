package nl.newlin.abnamro.assesment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.newlin.abnamro.assesment.ui.theme.AbnAmroAssesmentTheme
import nl.newlin.abnamro.network.DataResource
import nl.newlin.abnamro.network.GitHubRepository
import nl.newlin.abnamro.network.GithubApi
import nl.newlin.abnamro.network.GithubApiImpl
import nl.newlin.abnamro.network.RepoVisibility
import org.koin.android.annotation.KoinViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication
import org.koin.ksp.generated.defaultModule

@KoinViewModel
class MainViewModel: ViewModel() {
    val repositories = MutableStateFlow<DataResource<List<GitHubRepository>>>(DataResource.Loading)

    val network: GithubApi = GithubApiImpl()
    private var currentPage = 1
    private var canLoadMore = true

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            network.fetchRepositories(currentPage, 10).collect { result ->
                repositories.update { result }
            }
        }
    }

    fun loadMore() {
        if (!canLoadMore) return

        currentPage++
        println("Should load $currentPage")
        viewModelScope.launch(Dispatchers.IO) {
            network.fetchRepositories(currentPage, 10).collect { result ->
                if (result is DataResource.Success) {
                    if (result.data.isEmpty()) {
                        println("Page $currentPage is empty, stop")
                        canLoadMore = false
                    }

                    repositories.update { current ->
                        val currentList =
                            if (current is DataResource.Success) current.data else emptyList()
                        DataResource.Success(currentList + result.data)
                    }

                }
            }
        }
    }

}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
        val context = LocalContext.current
        KoinApplication(application = {

            defaultModule()
            androidContext(context)
        }) {
            val navController = rememberNavController()
            MainNavigation(navController)
        }
}

@Preview(showBackground = true)
@Composable
fun RepoCardPreview() {
    RepoCard(name = "Dummy",
        avatarImageUrl = "https://avatars.githubusercontent.com/u/15876397?v=4",
        visibility = RepoVisibility.Public,
        private = false)
}

@Composable
fun RepoCard(name: String, avatarImageUrl: String?, visibility: RepoVisibility, private: Boolean, modifier: Modifier = Modifier) {
    Row(Modifier.height(IntrinsicSize.Min).padding(horizontal = 16.dp).fillMaxWidth().then(modifier)) {
        Spacer(Modifier.width(10.dp))
        Box(Modifier.width(40.dp).fillMaxHeight()) {
            if (avatarImageUrl != null) {
                AsyncImage(
                    model = avatarImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.height(80.dp), verticalArrangement = Arrangement.SpaceEvenly) {
            Text(name, fontWeight = FontWeight.Bold)
            Text(when(visibility){
                RepoVisibility.Public -> "Public"
                RepoVisibility.Private -> "Private"
            })
            Text("Is private: " + if (private) "Yes" else "No")

        }
    }
}

@Composable
fun MainNavigation(navController: NavHostController) {
    AbnAmroAssesmentTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(Modifier.padding(innerPadding)) {
                Greeting()
            }
        }
    }
}

@Composable
fun Greeting(vm: MainViewModel = koinViewModel(), modifier: Modifier = Modifier) {
    LaunchedEffect(vm) {
        vm.fetch()
    }
    val repositories by vm.repositories.collectAsState()
    val currentState = repositories

   when (currentState) {
       is DataResource.Loading -> CircularProgressIndicator()
       is DataResource.Success -> {
           LazyColumn {
               itemsIndexed(currentState.data) { index, item ->
                   RepoCard(
                       name = item.name,
                       avatarImageUrl = item.avatarImageUrl,
                       visibility = item.visibility,
                       private = item.private,
                       modifier = if (index%2==0) Modifier.background(colorResource(R.color.accent)) else Modifier
                   )

                   if (index == currentState.data.lastIndex) {
                       vm.loadMore()
                   }
               }
           }
       }
       is DataResource.Error -> {
           Text("There was an error loading")
       }
   }
}