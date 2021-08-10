package com.skt.welfare

import com.skt.Tmap.TMapTapi
import com.skt.welfare.api.TokTokResponse

object Constants {
//    val baseUrl = "https://appsvcmobilefront.z32.web.core.windows.net/"
    val baseUrl = "http://welfaredevm.sktelecom.com:50000"

//    val backendUrl = "https://appsvc-ehr-was.azurewebsites.net"
    val backendUrl = "http://welfaredevm.sktelecom.com:50001"

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


    val benepiaAppId = "com.sk.benepia"
    val tmapApiKey = "l7xx6b03efc9ecf74b5488741abe9ffc7392"

    val toktokAppId = "Z000ST0057"
    val toktokDevUrl = "https://devgmp.sktelecom.com:9443"
//    val toktokDevUrl = "https://m.toktok.sk.com:9443"
//    val toktokPrdUrl = "https://devgmp.sktelecom.com:9443"
    val toktokPrdUrl = "https://m.toktok.sk.com:9443"

    val toktokStorePhonePackageName = "com.skt.pe.activity.mobileclient"
    val toktokPhonePackageName = "com.skt.pe.provider"
    val toktokStoreTabletPackageName = "com.sk.tablet.group.store"
    val toktokTabletPackageName = "com.skt.tablet.group.login"

    val toktokPhoneActionName = "com.sk.pe.group.auth.GMP_LOGIN"
    val toktokTabletActionName = "com.sk.pe.auth.GMP_LOGIN"

    val vacationPackageName = "com.gmp.skt.dywt.mobile"
    val vacationAppId = "Z000ST0049";

    val storeUrl = "https://m.toktok.sk.com/fordev.jsp"

    var phoneNumber = ""

    var token = ""

    var loginInfo: TokTokResponse? = null

    var tmaptapi: TMapTapi? = null

}