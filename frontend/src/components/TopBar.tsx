import React from "react";
import { View, Pressable, StyleSheet } from "react-native";
import Icon from "@react-native-vector-icons/material-design-icons";
import { spacing, useTheme } from "@/src/theme";

interface Props {
  onBack?: () => void;
  right?: React.ReactNode;
  left?: React.ReactNode;
  title?: React.ReactNode;
  backgroundColor?: string;
  showBorder?: boolean;
}

export function TopBar({ onBack, right, left, title, backgroundColor, showBorder = true }: Props) {
  const { colors } = useTheme();
  return (
    <View
      style={[
        styles.container,
        {
          backgroundColor: backgroundColor ?? colors.surface,
          borderBottomColor: showBorder ? colors.border : "transparent",
        },
      ]}
    >
      <View style={styles.side}>
        {onBack ? (
          <Pressable
            testID="topbar-back"
            hitSlop={12}
            onPress={onBack}
            style={({ pressed }) => [
              styles.iconBtn,
              { backgroundColor: pressed ? colors.surfaceTertiary : "transparent" },
            ]}
          >
            <Icon name="arrow-left" size={22} color={colors.onSurface} />
          </Pressable>
        ) : (
          left
        )}
      </View>
      <View style={styles.center}>{title}</View>
      <View style={[styles.side, { justifyContent: "flex-end" }]}>{right}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: spacing.lg,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  side: { flex: 1, flexDirection: "row", alignItems: "center", gap: spacing.sm },
  center: { flex: 2, alignItems: "center", justifyContent: "center" },
  iconBtn: { padding: 6, borderRadius: 999 },
});
