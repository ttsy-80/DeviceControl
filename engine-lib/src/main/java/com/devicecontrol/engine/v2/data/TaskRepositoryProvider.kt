package com.devicecontrol.engine.v2.data

import android.content.Context
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.repository.TaskRepository

object TaskRepositoryProvider {

    @Volatile
    private var repository: TaskRepository? = null

    fun get(context: Context): TaskRepository {
        return repository ?: synchronized(this) {
            repository ?: TaskRepository(
                AppDatabase.getDatabase(context.applicationContext).taskDao(),
            ).also { repository = it }
        }
    }
}
