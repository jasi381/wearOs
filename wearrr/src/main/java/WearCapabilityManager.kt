import android.content.Context
import android.net.Uri
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log


class WearCapabilityManager(
    context: Context
) : CapabilityClient.OnCapabilityChangedListener {

    companion object {
        private const val TAG = "WearCapabilityMgr"
        private const val CAPABILITY_NAME = "capability_mobile"
    }

    private val capabilityClient = Wearable.getCapabilityClient(context)

    private val _mobileInstalled = MutableStateFlow<Boolean?>(null)
    val mobileInstalled: StateFlow<Boolean?> = _mobileInstalled

    fun start() {
        Log.d(TAG, "start() called – registering capability listener")

        capabilityClient.addListener(
            this,
            Uri.parse("wear://*/$CAPABILITY_NAME"),
            CapabilityClient.FILTER_ALL
        )

        Log.d(TAG, "Capability listener registered")

        // Initial capability check
        Log.d(TAG, "Checking initial capability: $CAPABILITY_NAME")

        capabilityClient
            .getCapability(
                CAPABILITY_NAME,
                CapabilityClient.FILTER_ALL
            )
            .addOnSuccessListener { info ->
                val isInstalled = info.nodes.isNotEmpty()
                _mobileInstalled.value = isInstalled

                Log.i(
                    TAG,
                    "Initial capability result -> mobileInstalled=$isInstalled, nodes=${info.nodes}"
                )
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Failed to fetch capability: $CAPABILITY_NAME",
                    exception
                )
            }
    }

    fun stop() {
        Log.d(TAG, "stop() called – removing capability listener")
        capabilityClient.removeListener(this)
        Log.d(TAG, "Capability listener removed")
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        Log.d(
            TAG,
            "onCapabilityChanged() -> name=${info.name}, nodes=${info.nodes}"
        )

        if (info.name == CAPABILITY_NAME) {
            val isInstalled = info.nodes.isNotEmpty()
            _mobileInstalled.value = isInstalled

            Log.i(
                TAG,
                "Capability updated -> mobileInstalled=$isInstalled"
            )
        }
    }
}

