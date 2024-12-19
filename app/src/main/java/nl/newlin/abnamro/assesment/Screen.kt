package nl.newlin.abnamro.assesment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable
import nl.newlin.abnamro.assesment.ui.theme.AbnAmroAssesmentTheme
import nl.newlin.abnamro.data.ReposDatabase
import nl.newlin.abnamro.data.ReposDatabaseImpl
import nl.newlin.abnamro.network.DataResource
import nl.newlin.abnamro.network.GithubApi
import nl.newlin.abnamro.network.GithubApiImpl
import nl.newlin.abnamro.network.RepoVisibility
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

@Composable
fun App() {
        val context = LocalContext.current
        KoinApplication(application = {

            modules(appGlueModule, defaultModule)
            androidContext(context)

        }) {
            val navController = rememberNavController()
            MainNavigation(navController)
        }
}

val appGlueModule = module {
    single<ReposDatabase> { ReposDatabaseImpl(get())}
    single<GithubApi> { GithubApiImpl() }
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
    Row(
        Modifier
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .then(modifier)) {
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .width(40.dp)
                .fillMaxHeight()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(navController: NavHostController) {

    var currentScreen: Screen by remember { mutableStateOf(Screen.Home) }
    var pageTitle by remember { mutableStateOf("Repos") }
    LaunchedEffect(currentScreen) {

        navController.navigate(currentScreen, navOptions {
                if (currentScreen is Screen.Home) popUpTo<Screen.Home>()
            })

        pageTitle = when(currentScreen) {
            is Screen.Home -> "Repos"
            is Screen.Details -> (currentScreen as Screen.Details).name
        }
    }

    AbnAmroAssesmentTheme {
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = {
                    Text(pageTitle)
                },
                    navigationIcon = {
                        if (currentScreen is Screen.Details) {
                            IconButton(onClick = {
                               currentScreen = Screen.Home
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Localized description"
                                )
                            }
                        }
                    })
            }) { innerPadding ->
                NavHost(navController, startDestination = Screen.Home, Modifier.padding(innerPadding)) {
                    composable<Screen.Home> { RepositoriesList(onNavigation = { screen ->
                        currentScreen = screen
                    }) }
                    composable<Screen.Details> { route ->
                        val details = route.toRoute<Screen.Details>()
                        RepositoryDetails(details) }
                }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun RepositoryDetailsPreview() {
    RepositoryDetails(Screen.Details(
        name = "Example repo",
        fullName = "Example/example_repro",
        ownerAvatarImageUrl = null,
        visibility = RepoVisibility.Public,
        private = false,
        description = "Just a exmaple repo",
        htmlUrl = "https://something"
    ))
}

@Composable
fun RepositoryDetails(details: Screen.Details) {
    Column(Modifier.padding(16.dp).fillMaxWidth()) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(100.dp)
            ) {
            if (details.ownerAvatarImageUrl != null) {
                AsyncImage(
                    model = details.ownerAvatarImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
        Text(details.fullName)
        details.description?.let { description ->
            Text(description, Modifier.padding(top = 16.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text(when(details.visibility){
            RepoVisibility.Public -> "Public"
            RepoVisibility.Private -> "Private"
        })
        Text("Is private: " + if (details.private) "Yes" else "No")

        val uriHandler = LocalUriHandler.current

        Button(modifier = Modifier.padding(top = 16.dp),
            shape = RectangleShape,
            onClick = {
                uriHandler.openUri(details.htmlUrl)
        }) {
            Text("Open details")
        }
    }

}

sealed class Screen {
    @Serializable
    data object Home: Screen()

    @Serializable
    data class Details(
        val name: String,
        val fullName: String,
        val description: String?,
        val ownerAvatarImageUrl: String?,
        val visibility: RepoVisibility,
        val private: Boolean,
        val htmlUrl: String
    ): Screen()
}

@Composable
fun RepositoriesList(onNavigation: (Screen) -> Unit, vm: MainViewModel = koinViewModel(), modifier: Modifier = Modifier) {
    LaunchedEffect(vm) {
        vm.fetch()
    }
    val repositories by vm.datasource.repos.collectAsState(emptyList())
    val syncing by vm.datasource.isSyncing.collectAsState(false)

   when (syncing) {
       true -> Box(Modifier.fillMaxSize()) {
           CircularProgressIndicator(Modifier.align(Alignment.Center))
       }
       false -> {
           LazyColumn {
               itemsIndexed(repositories) { index, item ->
                   RepoCard(
                       name = item.name,
                       avatarImageUrl = item.avatarImageUrl,
                       visibility = item.visibility,
                       private = item.private,
                       modifier = Modifier
                           .clickable {
                               onNavigation(
                                   Screen.Details(
                                       name = item.name,
                                       fullName = item.fullName,
                                       ownerAvatarImageUrl = item.avatarImageUrl,
                                       visibility = item.visibility,
                                       private = item.private,
                                       description = item.description,
                                       htmlUrl = item.htmlUrl
                                   )
                               )
                           }
                           .then(
                               if (index % 2 == 0) Modifier
                                   .background(colorResource(R.color.accent)) else Modifier
                           )
                   )

                   /*if (index == repositories.lastIndex) {
                       vm.loadMore()
                   }*/
               }
           }
       }
   }
}