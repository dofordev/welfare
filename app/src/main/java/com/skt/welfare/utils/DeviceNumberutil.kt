import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import kotlin.collections.ArrayList

class DeviceNumberUtil(private val context: Context) {
    fun enabledUSIM(): Boolean {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return when (telephonyManager.simState) {
            TelephonyManager.SIM_STATE_READY -> true
            else -> false
        }
    }

    fun getSubInfoList(): List<SubscriptionInfo>? = if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) (context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager).activeSubscriptionInfoList else null

    fun getPhoneNumberList(subInfoList: List<SubscriptionInfo>?): ArrayList<String> {
        val phoneNumbers = ArrayList<String>()
        if (subInfoList != null) for (subInfo in subInfoList) {
            phoneNumbers.add(subInfo.number)
        }
        return phoneNumbers
    }
}

