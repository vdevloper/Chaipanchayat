import React, { useEffect, useState } from "react";
import { LogBox, Platform, View, Text, StyleSheet } from "react-native";
import { QueryClientProvider } from "@tanstack/react-query";
import { Stack, useRouter } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import * as Font from "expo-font";
import * as SplashScreen from "expo-splash-screen";
import * as Notifications from "expo-notifications";
import * as Linking from "expo-linking";

import { ErrorBoundary } from "@/src/components/error-boundary";
import { queryClient } from "@/src/query-client";
import { registerForPush } from "@/src/api/push";
import { fonts, useTheme } from "@/src/theme";

LogBox.ignoreAllLogs(true);
SplashScreen.preventAutoHideAsync().catch(() => {});

// --- Notifications module-scope setup (per Emergent Push playbook) ---
if (Platform.OS !== "web") {
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldPlaySound: true,
      shouldSetBadge: false,
      shouldShowBanner: true,
      shouldShowList: true,
    }),
  });
}

// -------------------------------------------------------------------
export default function RootLayout() {
  const router = useRouter();
  const [fontsReady, setFontsReady] = useState(false);
  const { colors, scheme } = useTheme();

  // Load Chai Panchayat fonts (with Devanagari support).
  useEffect(() => {
    (async () => {
      try {
        await Font.loadAsync({
          "NotoSerifDevanagari-Regular": require("../assets/fonts/NotoSerifDevanagari-Regular.ttf"),
          "NotoSerifDevanagari-Bold": require("../assets/fonts/NotoSerifDevanagari-Bold.ttf"),
          "NotoSerifDevanagari-ExtraBold": require("../assets/fonts/NotoSerifDevanagari-ExtraBold.ttf"),
          "Inter-Regular": require("../assets/fonts/Inter-Regular.ttf"),
          "Inter-Medium": require("../assets/fonts/Inter-Medium.ttf"),
          "Inter-SemiBold": require("../assets/fonts/Inter-SemiBold.ttf"),
          "Inter-Bold": require("../assets/fonts/Inter-Bold.ttf"),
        });
      } catch (e) {
        console.warn("Font load failed:", e);
      } finally {
        setFontsReady(true);
        SplashScreen.hideAsync().catch(() => {});
      }
    })();
  }, []);

  // Register device for push and set up notification handlers.
  useEffect(() => {
    if (Platform.OS === "web") return;

    if (Platform.OS === "android") {
      Notifications.setNotificationChannelAsync("default", {
        name: "Default",
        importance: Notifications.AndroidImportance.MAX,
        sound: "default",
      }).catch(() => {});
    }

    const backendUrl = process.env.EXPO_PUBLIC_BACKEND_URL;
    if (backendUrl) registerForPush(backendUrl);

    const tapSub = Notifications.addNotificationResponseReceivedListener((response) => {
      const data = response.notification.request.content.data ?? {};
      const url = (data as any).deeplink || (data as any).action_url;
      if (!url) return;
      if (typeof url === "string" && url.startsWith("http")) Linking.openURL(url);
      else router.push(url as any);
    });

    Notifications.getLastNotificationResponseAsync().then((response) => {
      if (!response) return;
      const data = response.notification.request.content.data ?? {};
      const url = (data as any).deeplink || (data as any).action_url;
      if (!url) return;
      if (typeof url === "string" && url.startsWith("http")) Linking.openURL(url);
      else router.push(url as any);
    });

    return () => {
      tapSub.remove();
    };
  }, [router]);

  if (!fontsReady) {
    return (
      <View style={[styles.splash, { backgroundColor: colors.surface }]}>
        <Text style={{ color: colors.brandPrimary, fontFamily: fonts.serifExtraBold, fontSize: 24 }}>
          Chai Panchayat
        </Text>
      </View>
    );
  }

  return (
    <GestureHandlerRootView style={{ flex: 1, backgroundColor: colors.surface }}>
      <SafeAreaProvider>
        <ErrorBoundary>
          <QueryClientProvider client={queryClient}>
            <StatusBar style={scheme === "dark" ? "light" : "dark"} />
            <Stack
              screenOptions={{
                headerShown: false,
                contentStyle: { backgroundColor: colors.surface },
                animation: "fade",
              }}
            />
          </QueryClientProvider>
        </ErrorBoundary>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  splash: { flex: 1, alignItems: "center", justifyContent: "center" },
});
