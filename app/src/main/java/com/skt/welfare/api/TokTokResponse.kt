package com.skt.welfare.api

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name="skmo")
data class TokTokResponse(

    @PropertyElement
    val result: String?,
    @PropertyElement
    val resultMessage: String?,
    @PropertyElement
    val email: String?,
    @PropertyElement
    val empId : String?,
    @PropertyElement
    val loginId : String?,
    @PropertyElement
    val primitive : String?

)


