package nl.newlin.abnamro.data
import androidx.room.ColumnInfo
import androidx.room.Entity;
import androidx.room.PrimaryKey

@Entity
data class GitRepo(
    @PrimaryKey val uid: Long,
    val name: String,
    val fullName: String,
    val private: Boolean,
    val visibility: String,
    val description: String?,
    val avatarImageUrl: String?,
    val htmlUrl: String,
)