import android.content.Context
import android.net.Uri
import com.example.wearrr.DummyMessage
import com.example.wearrr.LoginMessage
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
        private const val LOGIN_PATH = "/login_status"
    }

    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val messageClient = Wearable.getMessageClient(context)

    private val _peerInstalled = MutableStateFlow<Boolean?>(null)
    val peerInstalled: StateFlow<Boolean?> = _peerInstalled

    private val _peerReachable = MutableStateFlow<Boolean?>(null)
    val peerReachable: StateFlow<Boolean?> = _peerReachable

    private val _receivedMessage = MutableStateFlow<DummyMessage?>(null)
    val receivedMessage: StateFlow<DummyMessage?> = _receivedMessage

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun start() {
        Log.d(TAG, "start() called – registering listeners")

        capabilityClient.addListener(
            this,
            Uri.parse("wear://*/$CAPABILITY_NAME"),
            CapabilityClient.FILTER_REACHABLE
        )

        messageClient.addListener(this)

        Log.d(TAG, "Listeners registered")

        // Check if app is installed anywhere (even if not reachable)
        capabilityClient
            .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_ALL)
            .addOnSuccessListener { info ->
                _peerInstalled.value = info.nodes.isNotEmpty()
                Log.d(TAG, "FILTER_ALL -> installed=${info.nodes.isNotEmpty()}, nodes=${info.nodes}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch FILTER_ALL capability", e)
            }

        // Check if app is currently reachable (installed + connected)
        capabilityClient
            .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener { info ->
                val isReachable = info.nodes.isNotEmpty()
                _peerReachable.value = isReachable
                if (isReachable) _peerInstalled.value = true
                Log.i(TAG, "FILTER_REACHABLE -> reachable=$isReachable, nodes=${info.nodes}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch FILTER_REACHABLE capability", e)
            }
    }

    fun stop() {
        Log.d(TAG, "stop() called – removing listeners")
        capabilityClient.removeListener(this)
        messageClient.removeListener(this)
        Log.d(TAG, "Listeners removed")
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        Log.d(TAG, "onCapabilityChanged() -> name=${info.name}, nodes=${info.nodes}")

        if (info.name == CAPABILITY_NAME) {
            val isReachable = info.nodes.isNotEmpty()
            _peerReachable.value = isReachable

            if (isReachable) {
                _peerInstalled.value = true
            } else {
                // Re-check FILTER_ALL to distinguish uninstall vs disconnect
                capabilityClient
                    .getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_ALL)
                    .addOnSuccessListener { allInfo ->
                        _peerInstalled.value = allInfo.nodes.isNotEmpty()
                        Log.d(TAG, "Re-check FILTER_ALL -> installed=${allInfo.nodes.isNotEmpty()}")
                    }
            }

            Log.i(TAG, "Capability updated -> reachable=$isReachable")
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        Log.d(TAG, "onMessageReceived() -> path=${event.path}")

        when (event.path) {
            MESSAGE_PATH -> {
                try {
                    val message = DummyMessage.fromBytes(event.data)
                    _receivedMessage.value = message
                    Log.i(TAG, "Message received: $message")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse message", e)
                }
            }
            LOGIN_PATH -> {
                try {
                    val loginMessage = LoginMessage.fromBytes(event.data)
                    _isLoggedIn.value = loginMessage.isLoggedIn
                    Log.i(TAG, "Login status received: ${loginMessage.isLoggedIn}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse login message", e)
                }
            }
        }
    }
}
