package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelCardUi

/** 型号管理目录（P13），含蓝色「新增」卡 */
class V2ModelCatalogViewModel : ViewModel() {

    private val _items = MutableLiveData(catalogItems())
    val items: LiveData<List<V2ModelCardUi>> = _items

    fun onAddCardClicked() {
        V2Log.i(TAG, "onAddCardClicked")
    }

    companion object {
        private const val TAG = "ModelCatalogVM"

        private fun catalogItems() = listOf(
            V2ModelCardUi(1, "CFM56-5B"),
            V2ModelCardUi(2, "CFM56-3B"),
            V2ModelCardUi(3, "PW4000"),
            V2ModelCardUi(4, "LEAP-1C"),
            V2ModelCardUi(5, "F119-PW-100"),
            V2ModelCardUi(-1, "", isAddCard = true),
        )
    }
}
