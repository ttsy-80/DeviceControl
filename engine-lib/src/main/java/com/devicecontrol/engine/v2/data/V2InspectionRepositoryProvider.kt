package com.devicecontrol.engine.v2.data

import android.content.Context
import com.devicecontrol.engine.data.database.AppDatabase

object V2InspectionRepositoryProvider {

    @Volatile
    private var repository: V2InspectionRepository? = null

    fun get(context: Context): V2InspectionRepository {
        return repository ?: synchronized(this) {
            repository ?: V2InspectionRepository(
                TaskRepositoryProvider.get(context.applicationContext),
                AppDatabase.getDatabase(context.applicationContext).v2InspectionConfigDao(),
            ).also { repository = it }
        }
    }
}
