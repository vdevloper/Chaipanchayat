// Register this device for push under a shared broadcast id.
import { Platform } from "react-native";
import * as Notifications from "expo-notifications";

const BROADCAST_USER_ID = "chai_all_subscribers";

export async function registerForPush(backendUrl: string): Promise<void> {
  if (Platform.OS === "web") return;
  try {
    const { status } = await Notifications.requestPermissionsAsync();
    if (status !== "granted") return;

    const tokenResp = await Notifications.getDevicePushTokenAsync();
    await fetch(`${backendUrl}/api/register-push`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        user_id: BROADCAST_USER_ID,
        platform: Platform.OS,
        device_token: tokenResp.data,
      }),
    });
  } catch (e) {
    // Non-blocking — push registration failure must not stop the app.
    console.warn("Push registration failed:", e);
  }
}
