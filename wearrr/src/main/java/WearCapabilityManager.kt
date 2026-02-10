import android.content.Context
import android.net.Uri
import com.example.wearrr.DummyMessage
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log


class WearCapabilityManager(
    context: Context
) : CapabilityClient.OnCapabilityChangedListener,
    MessageClient.OnMessageReceivedListener {

    companion object {
        private const val TAG = "WearCapabilityMgr"
        private const val CAPABILITY_NAME = "capability_mobile"
        private const val MESSAGE_PATH = "/dummy_text"
    }

    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val messageClient = Wearable.getMessageClient(context)

    private val _mobileInstalled = MutableStateFlow<Boolean?>(null)
    val mobileInstalled: StateFlow<Boolean?> = _mobileInstalled

    private val _receivedMessage = MutableStateFlow<DummyMessage?>(null)
    val receivedMessage: StateFlow<DummyMessage?> = _receivedMessage

    fun start() {
        Log.d(TAG, "start() called – registering listeners")

        capabilityClient.addListener(
            this,
            Uri.parse("wear://*/$CAPABILITY_NAME"),
            CapabilityClient.FILTER_REACHABLE
        )

        messageClient.addListener(this)

        Log.d(TAG, "Listeners registered")

        Log.d(TAG, "Checking initial capability: $CAPABILITY_NAME")

        capabilityClient
            .getCapability(
                CAPABILITY_NAME,
                CapabilityClient.FILTER_REACHABLE
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
        Log.d(TAG, "stop() called – removing listeners")
        capabilityClient.removeListener(this)
        messageClient.removeListener(this)
        Log.d(TAG, "Listeners removed")
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

    override fun onMessageReceived(event: MessageEvent) {
        Log.d(TAG, "onMessageReceived() -> path=${event.path}")

        if (event.path == MESSAGE_PATH) {
            try {
                val message = DummyMessage.fromBytes(event.data)
                _receivedMessage.value = message
                Log.i(TAG, "Message received: $message")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse message", e)
            }
        }
    }
}
