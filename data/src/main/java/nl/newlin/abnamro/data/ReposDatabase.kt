package nl.newlin.abnamro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GitRepoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gitReposDao(): GitReposDao
}

interface ReposDatabase {
    fun saveAll(repos: List<GitRepoEntity>)
    fun getAll(): List<GitRepoEntity>
    fun deleteAll()
}

class ReposDatabaseImpl(context: Context): ReposDatabase {

    private val db: AppDatabase

    init {
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "githubDbRepo"
        ).build()
    }

    override fun saveAll(repos: List<GitRepoEntity>) {
        db.gitReposDao().insertAll(*repos.toTypedArray())
    }

    override fun getAll(): List<GitRepoEntity>{
        return db.gitReposDao().getAll()
    }

    override fun deleteAll() {
        db.gitReposDao().deleteAll()
    }
}