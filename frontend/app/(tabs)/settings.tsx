import React from "react";
import { View, Text, StyleSheet, Pressable, ScrollView, Linking } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import Icon from "@react-native-vector-icons/material-design-icons";

import { Logo } from "@/src/components/Logo";
import { fonts, radius, spacing, useTheme } from "@/src/theme";
import { useSettings, TextSize } from "@/src/api/settings";

export default function SettingsScreen() {
  const insets = useSafeAreaInsets();
  const { colors, scheme } = useTheme();
  const { settings, update } = useSettings();

  return (
    <View style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={{ paddingTop: insets.top, backgroundColor: colors.surface }}>
        <View style={styles.header}>
          <Text
            style={[styles.headerTitle, { color: colors.onSurface, fontFamily: fonts.serifExtraBold }]}
          >
            Settings
          </Text>
        </View>
        <View style={[styles.rule, { backgroundColor: colors.border }]} />
      </View>

      <ScrollView contentContainerStyle={{ padding: spacing.lg, paddingBottom: spacing.xxl }}>
        {/* Brand banner */}
        <View
          style={[
            styles.banner,
            { backgroundColor: colors.surfaceSecondary, borderColor: colors.border },
          ]}
        >
          <Logo size={44} />
          <Text style={{ color: colors.muted, fontFamily: fonts.sansRegular, fontSize: 13, marginTop: spacing.md }}>
            Premium Indian journalism. Delivered fast, read easy.
          </Text>
        </View>

        <SectionTitle>Reading</SectionTitle>
        <View style={[styles.card, { backgroundColor: colors.surfaceSecondary, borderColor: colors.border }]}>
          <Text style={[styles.rowLabel, { color: colors.onSurfaceSecondary, fontFamily: fonts.sansSemiBold }]}>
            Text size
          </Text>
          <View style={styles.segment}>
            {(["small", "medium", "large"] as TextSize[]).map((size) => {
              const active = settings.textSize === size;
              return (
                <Pressable
                  key={size}
                  testID={`textsize-${size}`}
                  onPress={() => update({ textSize: size })}
                  style={({ pressed }) => [
                    styles.segmentItem,
                    {
                      backgroundColor: active ? colors.brandPrimary : "transparent",
                      opacity: pressed ? 0.85 : 1,
                    },
                  ]}
                >
                  <Text
                    style={{
                      color: active ? colors.onBrandPrimary : colors.onSurface,
                      fontFamily: active ? fonts.sansSemiBold : fonts.sansMedium,
                      fontSize: size === "small" ? 12 : size === "medium" ? 14 : 16,
                    }}
                  >
                    {size === "small" ? "Aa" : size === "medium" ? "Aa" : "Aa"}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </View>

        <SectionTitle>Appearance</SectionTitle>
        <View style={[styles.card, { backgroundColor: colors.surfaceSecondary, borderColor: colors.border }]}>
          <View style={{ flexDirection: "row", alignItems: "center", gap: spacing.md }}>
            <Icon
              name={scheme === "dark" ? "weather-night" : "white-balance-sunny"}
              size={20}
              color={colors.brandPrimary}
            />
            <View style={{ flex: 1 }}>
              <Text style={{ color: colors.onSurface, fontFamily: fonts.sansSemiBold, fontSize: 15 }}>
                {scheme === "dark" ? "Dark mode" : "Light mode"}
              </Text>
              <Text
                style={{
                  color: colors.muted,
                  fontFamily: fonts.sansRegular,
                  fontSize: 12,
                  marginTop: 2,
                }}
              >
                Follows your system setting.
              </Text>
            </View>
          </View>
        </View>

        <SectionTitle>About</SectionTitle>
        <View style={[styles.card, { backgroundColor: colors.surfaceSecondary, borderColor: colors.border, padding: 0 }]}>
          <LinkRow
            icon="web"
            label="Visit chaipanchayat.com"
            onPress={() => Linking.openURL("https://chaipanchayat.com")}
            testID="link-website"
          />
          <Divider />
          <LinkRow
            icon="shield-check-outline"
            label="Privacy policy"
            onPress={() => Linking.openURL("https://chaipanchayat.com/privacy-policy")}
            testID="link-privacy"
          />
          <Divider />
          <LinkRow icon="information-outline" label="Version 1.0.0" testID="link-version" />
        </View>

        <Text
          style={{
            textAlign: "center",
            color: colors.muted,
            fontFamily: fonts.sansMedium,
            fontSize: 12,
            marginTop: spacing.xl,
          }}
        >
          Made with चाय · Chai Panchayat
        </Text>
      </ScrollView>
    </View>
  );
}

function SectionTitle({ children }: { children: React.ReactNode }) {
  const { colors } = useTheme();
  return (
    <Text
      style={{
        color: colors.muted,
        fontFamily: fonts.sansBold,
        fontSize: 11,
        letterSpacing: 1.2,
        marginTop: spacing.xl,
        marginBottom: spacing.sm,
      }}
    >
      {String(children).toUpperCase()}
    </Text>
  );
}

function LinkRow({
  icon,
  label,
  onPress,
  testID,
}: {
  icon: string;
  label: string;
  onPress?: () => void;
  testID?: string;
}) {
  const { colors } = useTheme();
  return (
    <Pressable
      testID={testID}
      onPress={onPress}
      disabled={!onPress}
      style={({ pressed }) => [
        styles.linkRow,
        { backgroundColor: pressed ? colors.surfaceTertiary : "transparent" },
      ]}
    >
      <Icon name={icon as any} size={18} color={colors.muted} />
      <Text style={{ flex: 1, color: colors.onSurface, fontFamily: fonts.sansMedium, fontSize: 14 }}>
        {label}
      </Text>
      {onPress ? <Icon name="chevron-right" size={18} color={colors.muted} /> : null}
    </Pressable>
  );
}

function Divider() {
  const { colors } = useTheme();
  return <View style={{ height: StyleSheet.hairlineWidth, backgroundColor: colors.border, marginLeft: 44 }} />;
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: { paddingHorizontal: spacing.lg, paddingVertical: spacing.md },
  headerTitle: { fontSize: 26, lineHeight: 32 },
  rule: { height: StyleSheet.hairlineWidth },
  banner: {
    borderRadius: radius.md,
    borderWidth: StyleSheet.hairlineWidth,
    padding: spacing.lg,
  },
  card: {
    borderRadius: radius.md,
    borderWidth: StyleSheet.hairlineWidth,
    padding: spacing.md,
  },
  rowLabel: { fontSize: 14, marginBottom: spacing.sm },
  segment: {
    flexDirection: "row",
    borderRadius: radius.pill,
    padding: 4,
    gap: 4,
    backgroundColor: "rgba(0,0,0,0.04)",
  },
  segmentItem: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    height: 34,
    borderRadius: radius.pill,
  },
  linkRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
    paddingHorizontal: spacing.md,
    paddingVertical: 14,
  },
});
