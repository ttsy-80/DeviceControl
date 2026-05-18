package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelCardUi
import kotlinx.coroutines.launch

/** 型号管理目录（P13），数据来自 Room */
class V2ModelCatalogViewModel(
    private val repository: EngineRepository,
) : ViewModel() {

    private val _items = MutableLiveData<List<V2ModelCardUi>>(emptyList())
    val items: LiveData<List<V2ModelCardUi>> = _items

    init {
        observeCatalog()
    }

    private fun observeCatalog() {
        viewModelScope.launch {
            repository.getAllModelsWithConfigItems().collect { models ->
                val cards = models.map { V2ModelCardUi(it.model.id, it.model.name) } +
                    V2ModelCardUi(id = ADD_CARD_ID, name = "", isAddCard = true)
                _items.postValue(cards)
                V2Log.d(TAG, "catalog models=${models.size}")
            }
        }
    }

    fun onAddCardClicked() {
        V2Log.i(TAG, "onAddCardClicked")
    }

    companion object {
        private const val TAG = "ModelCatalogVM"
        const val ADD_CARD_ID = -1L
    }
}
