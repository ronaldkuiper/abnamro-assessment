package nl.newlin.abnamro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GitReposDao {
    @Query("SELECT * FROM GitRepo")
    fun getAll(): List<GitRepo>

    @Insert
    fun insertAll(vararg repos: GitRepo)

    @Delete
    fun delete(repo: GitRepo)

    @Query("DELETE FROM Gitrepo")
    fun deleteAll()
}