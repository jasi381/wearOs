# Fall Detection — How It Works

## What It Does

The smartwatch continuously monitors the wearer's movements using built-in motion sensors. If a fall is detected, the system automatically alerts a caregiver or emergency contact — even if the wearer is unable to respond.

This feature is designed specifically for **senior citizens**, where falls are a leading cause of serious injury and delayed help can be life-threatening.

## How a Fall Is Detected

The watch uses its motion sensor to look for the signature pattern of a real fall. Every fall has three phases:

1. **Free-fall** — A brief moment of weightlessness as the person loses balance and starts falling. The watch detects this as a sudden, sharp drop in motion force (well below the normal pull of gravity).

2. **Impact** — The sudden jolt when the person hits the ground. The watch detects this as a spike in force significantly higher than any normal movement like walking, arm swings, or sitting down.

3. **Stillness after impact** — After a real fall, especially for seniors, the person typically remains still on the ground for several seconds. The watch monitors for this post-fall stillness over a 4-second window to confirm it was a genuine fall and not just a bump or knock.

All three phases must occur in sequence for a fall to be confirmed. This dramatically reduces false alarms.

### Additional Detection Methods

Beyond the classic free-fall-then-impact pattern, the watch also detects:

- **Collapse with orientation change** — If the watch detects the wrist going from an upright position to a horizontal (lying down) position along with a moderate impact, it flags a potential fall. This catches slow collapses where the free-fall phase is minimal.

- **Extreme impact** — A very high force spike (roughly double the normal fall threshold) triggers a check even without a preceding free-fall. This catches sudden, violent falls.

In all cases, the 4-second stillness check must pass before an alert is sent.

### Filtering Out False Alarms

Several layers work together to prevent false alerts:

- **Noise smoothing** — Sensor readings are continuously smoothed to filter out random vibrations, jolts from transportation, or sensor noise.
- **Threshold tuning** — The force thresholds are set above what normal daily activities produce (arm swings, clapping, bumping into furniture) but low enough to catch real falls.
- **Stillness verification** — The strongest filter. Normal activities (sitting down hard, stumbling but catching yourself, dropping the hand) are always followed by continued movement. Only a real fall results in several seconds of near-total stillness.
- **30-second cooldown** — After a fall is detected, the system waits 30 seconds before it can detect another one, preventing repeated alerts from the same incident.

## What Happens When a Fall Is Detected

```
Watch detects fall
       |
Watch vibrates urgently (SOS pattern) to alert the wearer
       |
Watch sends alert to the paired phone
       |
Phone shows a notification:
"Fall Detected on Watch!"
"Sending help in 5 seconds unless you respond"
       |
  ┌────┴─────────────────────┐
  |                          |
User taps              No response
"I'm OK"              within 5 seconds
  |                          |
Alert dismissed        Emergency API
                       is called automatically
  |
User taps
"Send Help"
  |
Emergency API
is called immediately
```

### Step by Step

1. **Watch vibrates** — When a fall is confirmed, the watch vibrates with a strong, repeating SOS pattern for about 4 seconds so the wearer knows the system has been triggered.

2. **Phone gets notified** — The watch immediately sends a message to the paired phone.

3. **Phone shows an alert** — The phone displays an urgent notification with two buttons:
   - **"I'm OK"** — Tap this to dismiss the alert. Nothing further happens.
   - **"Send Help"** — Tap this to immediately call the emergency API.

4. **5-second auto-escalation** — If nobody taps either button within 5 seconds, the system assumes the person needs help and automatically calls the emergency API. This is critical for situations where the person is unconscious or the phone is out of reach.

## Why the Phone Handles the Alert (Not the Watch)

- **Better notifications** — Phone notifications are louder, more visible, and easier to interact with than tiny watch screens.
- **More reliable networking** — Phones have stronger Wi-Fi and cellular connections for making the emergency API call.
- **Battery friendly** — Keeping network calls off the watch preserves its battery for continuous fall monitoring.
- **Caregiver access** — A caregiver or family member nearby is more likely to see and respond to the phone notification.

## Sensors Used

- **Accelerometer** — Measures motion force in all directions. Available on every smartwatch. This is the primary sensor for fall detection.
- **Gravity sensor** — Detects the orientation of the wrist (upright vs. lying flat). Used to catch slow collapses. If a watch doesn't have this sensor, the system still works using the accelerometer alone.

## Limitations

- **Not 100% accurate** — No fall detection system is perfect. Some unusual falls may not match the expected pattern, and some vigorous activities could occasionally trigger a false alert.
- **Watch must be worn** — The watch needs to be on the wrist for detection to work. Leaving it on a table or in a bag will not produce meaningful results.
- **Phone must be paired** — The alert is sent to the paired phone. If the phone is off, out of Bluetooth range, or disconnected, the alert cannot be delivered.
- **Requires the app to be running** — The fall detection service runs in the background, but if the app is force-closed or the watch is restarted, the service needs to be started again by opening the app.
