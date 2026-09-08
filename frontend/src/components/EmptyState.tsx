import React from "react";
import { View, Text, Pressable, StyleSheet } from "react-native";
import Icon from "@react-native-vector-icons/material-design-icons";
import { fonts, radius, spacing, useTheme } from "@/src/theme";

interface Props {
  icon?: string;
  title: string;
  message?: string;
  actionLabel?: string;
  onAction?: () => void;
  testID?: string;
}

export function EmptyState({
  icon = "newspaper-variant-outline",
  title,
  message,
  actionLabel,
  onAction,
  testID,
}: Props) {
  const { colors } = useTheme();
  return (
    <View style={styles.container} testID={testID}>
      <View
        style={[
          styles.iconWrap,
          { backgroundColor: colors.surfaceTertiary, borderColor: colors.border },
        ]}
      >
        <Icon name={icon as any} size={32} color={colors.muted} />
      </View>
      <Text
        style={[
          styles.title,
          { color: colors.onSurface, fontFamily: fonts.serifBold },
        ]}
      >
        {title}
      </Text>
      {message ? (
        <Text
          style={[
            styles.message,
            { color: colors.muted, fontFamily: fonts.sansRegular },
          ]}
        >
          {message}
        </Text>
      ) : null}
      {onAction && actionLabel ? (
        <Pressable
          testID="empty-retry-button"
          onPress={onAction}
          style={({ pressed }) => [
            styles.button,
            {
              backgroundColor: pressed ? colors.brandSecondary : colors.brandPrimary,
            },
          ]}
        >
          <Text
            style={{ color: colors.onBrandPrimary, fontFamily: fonts.sansSemiBold }}
          >
            {actionLabel}
          </Text>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.xxl,
    gap: spacing.sm,
  },
  iconWrap: {
    width: 72,
    height: 72,
    borderRadius: 36,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: StyleSheet.hairlineWidth,
    marginBottom: spacing.md,
  },
  title: { fontSize: 20, textAlign: "center" },
  message: {
    fontSize: 14,
    lineHeight: 20,
    textAlign: "center",
    marginTop: 4,
  },
  button: {
    marginTop: spacing.lg,
    paddingHorizontal: spacing.xl,
    paddingVertical: 12,
    borderRadius: radius.pill,
  },
});
