package nl.newlin.abnamro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GitReposDao {
    @Query("SELECT * FROM GitRepoEntity")
    fun getAll(): List<GitRepoEntity>

    @Insert
    fun insertAll(vararg repos: GitRepoEntity)

    @Delete
    fun delete(repo: GitRepoEntity)

    @Query("DELETE FROM GitRepoEntity")
    fun deleteAll()
}