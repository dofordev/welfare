package com.skt.welfare

import com.skt.welfare.api.TokTokResponse

object Constants {
//    val baseUrl = "https://appsvcmobilefront.z32.web.core.windows.net/"
    val baseUrl = "https://appsvc-ehr-mobile-front.azurewebsites.net/"
    val backendUrl = "https://appsvc-ehr-was.azurewebsites.net/"
    val appCloseWaitText = "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다."
    val appCloseText = "앱이 종료됩니다."
    val javascriptBridgeName = "welfare"
    val clipToastText = "클립보드에 복사했습니다"
    val cameraPermissionText = "카메라 권한을 승인해야지만 앱을 사용할 수 있습니다."
    val storagePermissionText = "저장소 권한을 승인해야지만 앱을 사용할 수 있습니다."
    val phonePermissionText = "전화 권한을 승인해야지만 앱을 사용할 수 있습니다."

    val cameraCompanyCode: Int = 1001
    val cameraJobNo: Int = 0

    val ocrImgFolder = "welfare"

    val toktokAppId = "Z000ST0057"
    val toktokDevUrl = "https://devgmp.sktelecom.com:9443"
    val toktokPrdUrl = "https://m.toktok.sk.com:9443"

    val toktokStorePhonePackageName = "com.skt.pe.activity.mobileclient"
    val toktokPhonePackageName = "com.skt.pe.provider"
    val toktokStoreTabletPackageName = "com.sk.tablet.group.store"
    val toktokTabletPackageName = "com.skt.tablet.group.login"

    val toktokPhoneActionName = "com.sk.pe.group.auth.GMP_LOGIN"
    val toktokTabletActionName = "com.sk.pe.auth.GMP_LOGIN"

    val storeUrl = "https://m.toktok.sk.com/fordev.jsp"

    var phoneNumber = ""

    var loginInfo: TokTokResponse? = null
}