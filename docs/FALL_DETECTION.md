# Fall Detection on Wear OS

## Overview

The watch continuously monitors the accelerometer for falls. When a fall is detected, the user is prompted on-watch. If they don't respond within 8 seconds or tap "Help", an alert API is called from the watch. If the watch API call fails, the alert is forwarded to the phone as a fallback.

## Architecture

```
[Accelerometer] → [FallDetector] → [FallDetectionService (foreground)]
                                           |
                                    Fall detected
                                           |
                                    [FallResponseActivity] ← "Are you OK?"
                                           |
                         ┌─────────────────┼─────────────────┐
                     "I'm OK"          "Help"          8s timeout
                     (dismiss)             |                  |
                                    [API call from watch]     |
                                           |                  |
                                    Success? → done    Same API call
                                    Failure? ──────┐          |
                                                   v          v
                                    [MessageClient "/fall_alert" → Phone]
                                                   |
                                    [FallAlertListenerService on phone]
                                           |
                                    [API call from phone]
```

## Detection Algorithm

**Sensor:** `TYPE_ACCELEROMETER` at `SENSOR_DELAY_GAME` (~50 Hz)

The algorithm uses two phases:

| Phase | Condition | Duration |
|-------|-----------|----------|
| Free-fall | Acceleration magnitude < 3.0 m/s² | At least 80 ms |
| Impact | Acceleration magnitude > 25.0 m/s² | Within 500 ms after free-fall ends |

A 30-second cooldown prevents duplicate detections.

**Why accelerometer only?** Gyroscope is not available on all Wear OS devices. The accelerometer is universally supported.

## Files

### Watch (`wearrr/.../fall/`)

| File | Purpose |
|------|---------|
| `FallDetector.kt` | `SensorEventListener` — pure detection logic, takes `SensorManager` and a callback |
| `FallDetectionService.kt` | Foreground service — owns the detector, manages timeout, triggers alert |
| `FallResponseActivity.kt` | Compose UI — shows countdown, "OK" and "Help" buttons |
| `FallAlertApiCaller.kt` | HTTP POST to alert API using `HttpURLConnection` |
| `FallMessageSender.kt` | Sends `/fall_alert` to phone via `MessageClient` |

### Phone (`app/.../`)

| File | Purpose |
|------|---------|
| `FallAlertListenerService.kt` | `WearableListenerService` — receives `/fall_alert`, calls API as fallback |
| `FallAlertApiCaller.kt` | Same HTTP POST logic with `source = "phone"` |

## Key Design Decisions

### Communication: Static StateFlow
`FallDetectionService` exposes a `FallState` flow on its companion object. The activity collects this flow to know when to dismiss. No service binding needed.

```
FallState: IDLE → PROMPTING → ALERTING or DISMISSED → IDLE
```

### Authoritative Timeout
The 8-second timer lives in the **service**, not the activity. The activity shows a visual countdown, but the service's `delay(8_000)` coroutine is the source of truth. This ensures the timeout triggers even if the activity is killed by the system.

### No HTTP Library
Uses `java.net.HttpURLConnection` for the single POST call. This avoids adding OkHttp/Retrofit to the watch APK, keeping it lightweight.

### Service Configuration
- `foregroundServiceType="health"` — required for body sensor access from a foreground service
- `START_STICKY` — system will restart the service if it gets killed

## Permissions

### Watch (`wearrr/AndroidManifest.xml`)
- `FOREGROUND_SERVICE` — run a foreground service
- `FOREGROUND_SERVICE_HEALTH` — foreground service type health
- `BODY_SENSORS` — access accelerometer (runtime permission)
- `HIGH_SAMPLING_RATE_SENSORS` — use `SENSOR_DELAY_GAME` rate

### Phone (`app/AndroidManifest.xml`)
- `INTERNET` — already present, used for API call

## API Endpoint

Both callers POST to a placeholder URL:

```
POST https://example.com/api/fall-alert
Content-Type: application/json

{"source": "watch|phone", "timestamp": 1234567890}
```

Replace `ALERT_URL` in both `FallAlertApiCaller.kt` files (watch and phone) with the real endpoint.

## Testing

### Build
```bash
./gradlew :app:assembleDebug :wearrr:assembleDebug
```

### Manual Testing
1. Install both APKs on watch and phone
2. Watch shows "Fall Detection Active" notification
3. To test without a real fall, temporarily lower thresholds in `FallDetector.kt`:
   - `FREE_FALL_THRESHOLD` → `6.0f`
   - `IMPACT_THRESHOLD` → `15.0f`
4. Shake the watch sharply → `FallResponseActivity` should appear
5. Tap "OK" → dismisses
6. Trigger again, tap "Help" or wait 8s → logcat shows API call attempt and phone fallback

### Logcat Tags
| Tag | Source |
|-----|--------|
| `FallDetectionService` | Service lifecycle, fall events, alert flow |
| `FallMessageSender` | Watch-to-phone messaging |
| `FallAlertListener` | Phone-side alert handling |
