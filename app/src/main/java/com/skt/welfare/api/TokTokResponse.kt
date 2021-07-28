package com.skt.welfare.api

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name="skmo")
data class TokTokResponse(

    @PropertyElement
    var result: String? = "1000",
    @PropertyElement
    var resultMessage: String? = "서비스 요청 성공",
    @PropertyElement
    var email: String? = "lilykang@sk.com",
    @PropertyElement
    var empId : String? = "01103901",
    @PropertyElement
    var loginId : String? = "SKT.01103901",
    @PropertyElement
    var primitive : String? = "COMMON_COMMON_EMPINFO",
    @PropertyElement
    var deviceToken : String? = "",
    @PropertyElement
    var mblTypCd : String? = "A"
)


