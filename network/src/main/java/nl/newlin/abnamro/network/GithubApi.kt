package nl.newlin.abnamro.network

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptySerializersModule
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {
    fun fetchRepositories(page: Int, perPage: Int): Flow<DataResource<List<GitHubRepository>>>
}

class GithubApiImpl: GithubApi {

    private val api: GithubRetrofitApi
    private val abnAmroUser = "abnamrocoesd"

    init {
        val json = Json {
            ignoreUnknownKeys = true // This will ignore any unknown fields
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()))
            .build()

        api = retrofit.create(GithubRetrofitApi::class.java)
    }

    override fun fetchRepositories(page: Int, perPage: Int) = fetchResource {
        val result = api.listRepos(abnAmroUser, page, perPage)

        result.map { 
            GitHubRepository(
                name = it.name,
                fullName = it.fullName,
                private = it.private,
                visibility = if (it.visibility == "public") RepoVisibility.Public else RepoVisibility.Private,
                description = it.description,
                avatarImageUrl = it.owner?.avatarUrl,
                htmlUrl = it.htmlUrl
            )
        }
    }
}

internal interface GithubRetrofitApi {
    @Headers("Authorization: token: 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")
    @GET("users/{user}/repos")
    suspend fun listRepos(@Path("user") user: String?, @Query("page") page: Int, @Query("per_page") perPage: Int): List<Repository>
}

@Serializable
internal data class Repository(
    val id: Long,
    @SerialName("node_id") val nodeId: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val private: Boolean,
    val owner: Owner? = null,
    @SerialName("html_url") val htmlUrl: String,
    val description: String? = null,
    val fork: Boolean,
    val url: String,
    @SerialName("forks_url") val forksUrl: String,
    @SerialName("keys_url") val keysUrl: String,
    @SerialName("collaborators_url") val collaboratorsUrl: String,
    @SerialName("teams_url") val teamsUrl: String,
    @SerialName("hooks_url") val hooksUrl: String,
    @SerialName("issue_events_url") val issueEventsUrl: String,
    @SerialName("events_url") val eventsUrl: String,
    @SerialName("assignees_url") val assigneesUrl: String,
    @SerialName("branches_url") val branchesUrl: String,
    @SerialName("tags_url") val tagsUrl: String,
    @SerialName("blobs_url") val blobsUrl: String,
    @SerialName("git_tags_url") val gitTagsUrl: String,
    @SerialName("git_refs_url") val gitRefsUrl: String,
    @SerialName("trees_url") val treesUrl: String,
    @SerialName("statuses_url") val statusesUrl: String,
    @SerialName("languages_url") val languagesUrl: String,
    @SerialName("stargazers_url") val stargazersUrl: String,
    @SerialName("contributors_url") val contributorsUrl: String,
    @SerialName("subscribers_url") val subscribersUrl: String,
    @SerialName("subscription_url") val subscriptionUrl: String,
    @SerialName("commits_url") val commitsUrl: String,
    @SerialName("git_commits_url") val gitCommitsUrl: String,
    @SerialName("comments_url") val commentsUrl: String,
    @SerialName("issue_comment_url") val issueCommentUrl: String,
    @SerialName("contents_url") val contentsUrl: String,
    @SerialName("compare_url") val compareUrl: String,
    @SerialName("merges_url") val mergesUrl: String,
    @SerialName("archive_url") val archiveUrl: String,
    @SerialName("downloads_url") val downloadsUrl: String,
    @SerialName("issues_url") val issuesUrl: String,
    @SerialName("pulls_url") val pullsUrl: String,
    @SerialName("milestones_url") val milestonesUrl: String,
    @SerialName("notifications_url") val notificationsUrl: String,
    @SerialName("labels_url") val labelsUrl: String,
    @SerialName("releases_url") val releasesUrl: String,
    @SerialName("deployments_url") val deploymentsUrl: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("pushed_at") val pushedAt: String,
    @SerialName("git_url") val gitUrl: String,
    @SerialName("ssh_url") val sshUrl: String,
    @SerialName("clone_url") val cloneUrl: String,
    @SerialName("svn_url") val svnUrl: String,
    val homepage: String? = null,
    val size: Int,
    @SerialName("stargazers_count") val stargazersCount: Int,
    @SerialName("watchers_count") val watchersCount: Int,
    val language: String? = null,
    @SerialName("has_issues") val hasIssues: Boolean,
    @SerialName("has_projects") val hasProjects: Boolean,
    @SerialName("has_downloads") val hasDownloads: Boolean,
    @SerialName("has_wiki") val hasWiki: Boolean,
    @SerialName("has_pages") val hasPages: Boolean,
    @SerialName("has_discussions") val hasDiscussions: Boolean,
    @SerialName("forks_count") val forksCount: Int,
    @SerialName("mirror_url") val mirrorUrl: String? = null,
    val archived: Boolean,
    val disabled: Boolean,
    @SerialName("open_issues_count") val openIssuesCount: Int,
    val license: License? = null,
    @SerialName("allow_forking") val allowForking: Boolean,
    @SerialName("is_template") val isTemplate: Boolean,
    @SerialName("web_commit_signoff_required") val webCommitSignoffRequired: Boolean,
    val topics: List<String>,
    val visibility: String,
    val forks: Int,
    @SerialName("open_issues") val openIssues: Int,
    val watchers: Int,
    @SerialName("default_branch") val defaultBranch: String
)

@Serializable
data class Owner(
    val login: String? = null,
    val id: Long? = null,
    @SerialName("node_id") val nodeId: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("gravatar_id") val gravatarId: String? = null,
    val url: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("followers_url") val followersUrl: String? = null,
    @SerialName("following_url") val followingUrl: String? = null,
    @SerialName("gists_url") val gistsUrl: String? = null,
    @SerialName("starred_url") val starredUrl: String? = null,
    @SerialName("subscriptions_url") val subscriptionsUrl: String? = null,
    @SerialName("organizations_url") val organizationsUrl: String? = null,
    @SerialName("repos_url") val reposUrl: String? = null,
    @SerialName("events_url") val eventsUrl: String? = null,
    @SerialName("received_events_url") val receivedEventsUrl: String? = null,
    val type: String? = null,
    @SerialName("site_admin") val siteAdmin: Boolean? = null
)

@Serializable
data class License(
    val key: String,
    val name: String,
    @SerialName("spdx_id") val spdxId: String,
    val url: String? = null,
    @SerialName("node_id") val nodeId: String
)

