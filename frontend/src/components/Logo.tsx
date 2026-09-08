import React from "react";
import { View, Text, StyleSheet } from "react-native";
import { Image } from "expo-image";
import { fonts, useTheme } from "@/src/theme";

interface Props {
  size?: number;
  showWordmark?: boolean;
}

export function Logo({ size = 32, showWordmark = true }: Props) {
  const { colors } = useTheme();
  return (
    <View style={styles.row}>
      <Image
        source={require("../../assets/images/logo.png")}
        style={{ width: size, height: size, borderRadius: size / 2 }}
        contentFit="cover"
        testID="app-logo"
      />
      {showWordmark ? (
        <View style={styles.wordmark}>
          <Text
            style={[
              styles.title,
              { color: colors.onSurface, fontFamily: fonts.serifExtraBold },
            ]}
          >
            Chai Panchayat
          </Text>
          <Text
            style={[
              styles.subtitle,
              { color: colors.brandPrimary, fontFamily: fonts.sansSemiBold },
            ]}
          >
            चाय पंचायत
          </Text>
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: "row", alignItems: "center", gap: 10 },
  wordmark: { justifyContent: "center" },
  title: { fontSize: 18, lineHeight: 22, letterSpacing: 0.1 },
  subtitle: { fontSize: 10, lineHeight: 12, marginTop: 1, letterSpacing: 0.4 },
});
