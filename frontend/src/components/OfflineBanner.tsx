import React, { useEffect, useState } from "react";
import { View, Text, StyleSheet, Platform } from "react-native";
import Icon from "@react-native-vector-icons/material-design-icons";
import { fonts, spacing, useTheme } from "@/src/theme";

// Minimal offline banner using fetch heartbeat (avoids adding NetInfo).
export function OfflineBanner() {
  const { colors } = useTheme();
  const [offline, setOffline] = useState(false);

  useEffect(() => {
    if (Platform.OS === "web") return;
    let alive = true;
    const check = async () => {
      try {
        const c = new AbortController();
        const t = setTimeout(() => c.abort(), 4000);
        await fetch("https://chaipanchayat.com/wp-json/", {
          method: "HEAD",
          signal: c.signal,
        });
        clearTimeout(t);
        if (alive) setOffline(false);
      } catch {
        if (alive) setOffline(true);
      }
    };
    check();
    const iv = setInterval(check, 20000);
    return () => {
      alive = false;
      clearInterval(iv);
    };
  }, []);

  if (!offline) return null;

  return (
    <View
      testID="offline-banner"
      style={[
        styles.banner,
        { backgroundColor: colors.surfaceInverse, borderColor: colors.border },
      ]}
    >
      <Icon name="wifi-off" size={16} color={colors.onSurfaceInverse} />
      <Text
        style={{
          color: colors.onSurfaceInverse,
          fontFamily: fonts.sansMedium,
          fontSize: 13,
        }}
      >
        You&apos;re offline. Showing cached stories.
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
});
