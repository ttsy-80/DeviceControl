package com.devicecontrol.engine.v2.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devicecontrol.engine.v2.data.EngineRepositoryProvider
import com.devicecontrol.engine.v2.data.TaskRepositoryProvider
import com.devicecontrol.engine.v2.data.V2InspectionRepositoryProvider

class V2EngineViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {

    private val engineRepository by lazy { EngineRepositoryProvider.get(application) }
    private val taskRepository by lazy { TaskRepositoryProvider.get(application) }
    private val inspectionRepository by lazy { V2InspectionRepositoryProvider.get(application) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(V2ModelAddViewModel::class.java) ->
                V2ModelAddViewModel(engineRepository) as T
            modelClass.isAssignableFrom(V2ModelCatalogViewModel::class.java) ->
                V2ModelCatalogViewModel(engineRepository) as T
            modelClass.isAssignableFrom(V2InspectionModelViewModel::class.java) ->
                V2InspectionModelViewModel(engineRepository) as T
            modelClass.isAssignableFrom(V2InspectionControlViewModel::class.java) ->
                V2InspectionControlViewModel(
                    application,
                    engineRepository,
                    taskRepository,
                    inspectionRepository,
                ) as T
            modelClass.isAssignableFrom(V2ModelDetailViewModel::class.java) ->
                V2ModelDetailViewModel(engineRepository) as T
            modelClass.isAssignableFrom(V2ModeSettingsViewModel::class.java) ->
                V2ModeSettingsViewModel(inspectionRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
