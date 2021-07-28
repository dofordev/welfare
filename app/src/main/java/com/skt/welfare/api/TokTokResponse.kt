package com.skt.welfare.api

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name="skmo")
data class TokTokResponse(

    @PropertyElement
    var result: String? = "",
    @PropertyElement
    var resultMessage: String? = "",
    @PropertyElement
    var email: String? = "",
    @PropertyElement
    var empId : String? = "",
    @PropertyElement
    var loginId : String? = "",
    @PropertyElement
    var primitive : String? = "",
    @PropertyElement
    var deviceToken : String? = "",
    @PropertyElement
    var mblTypCd : String? = "A"
)


