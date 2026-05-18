package com.devicecontrol.engine.v2.data

import android.content.Context
import com.devicecontrol.engine.data.database.AppDatabase
import com.devicecontrol.engine.data.repository.EngineRepository

/** 2.0 与 1.0 共用 Room [EngineRepository] 单例入口 */
object EngineRepositoryProvider {

    @Volatile
    private var repository: EngineRepository? = null

    fun get(context: Context): EngineRepository {
        return repository ?: synchronized(this) {
            repository ?: EngineRepository(
                AppDatabase.getDatabase(context.applicationContext).engineDao(),
            ).also { repository = it }
        }
    }
}
