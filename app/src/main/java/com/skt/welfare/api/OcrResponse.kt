package com.skt.welfare.api

data class OcrResponse(
    val data: Any,
    val resultCd: Int,
    val resultMsg: String,
    val resultMsgTyp: String
)

