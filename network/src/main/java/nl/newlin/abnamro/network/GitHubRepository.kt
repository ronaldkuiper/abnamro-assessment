package nl.newlin.abnamro.network

data class GitHubRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val private: Boolean,
    val visibility: RepoVisibility,
    val description: String?,
    val avatarImageUrl: String?,
    val htmlUrl: String,

    )

enum class RepoVisibility{
    Public,
    Private
}