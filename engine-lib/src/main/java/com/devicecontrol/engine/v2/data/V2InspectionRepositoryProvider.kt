package com.devicecontrol.engine.v2.data

import android.content.Context
import com.devicecontrol.engine.data.database.AppDatabase

object V2InspectionRepositoryProvider {

    @Volatile
    private var repository: V2InspectionRepository? = null

    fun get(context: Context): V2InspectionRepository {
        return repository ?: synchronized(this) {
            repository ?: run {
                val app = context.applicationContext
                val db = AppDatabase.getDatabase(app)
                V2InspectionRepository(
                    TaskRepositoryProvider.get(app),
                    db.v2ModeSettingDao(),
                ).also { repository = it }
            }
        }
    }
}
