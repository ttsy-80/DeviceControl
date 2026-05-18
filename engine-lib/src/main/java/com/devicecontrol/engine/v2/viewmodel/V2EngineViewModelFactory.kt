package com.devicecontrol.engine.v2.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devicecontrol.engine.v2.data.EngineRepositoryProvider

class V2EngineViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {

  private val repository by lazy { EngineRepositoryProvider.get(application) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(V2ModelAddViewModel::class.java) ->
                V2ModelAddViewModel(repository) as T
            modelClass.isAssignableFrom(V2ModelCatalogViewModel::class.java) ->
                V2ModelCatalogViewModel(repository) as T
            modelClass.isAssignableFrom(V2InspectionModelViewModel::class.java) ->
                V2InspectionModelViewModel(repository) as T
            modelClass.isAssignableFrom(V2ModelDetailViewModel::class.java) ->
                V2ModelDetailViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
