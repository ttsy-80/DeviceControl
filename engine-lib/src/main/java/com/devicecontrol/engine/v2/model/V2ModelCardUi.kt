package com.devicecontrol.engine.v2.model

/** 型号宫格卡片展示模型（检测入口 / 型号目录共用） */
data class V2ModelCardUi(
    val id: Long,
    val name: String,
    val isAddCard: Boolean = false,
)
