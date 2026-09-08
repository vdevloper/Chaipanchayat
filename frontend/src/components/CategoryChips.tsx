import React from "react";
import { ScrollView, Pressable, Text, StyleSheet, View } from "react-native";
import * as Haptics from "expo-haptics";
import { fonts, radius, spacing, useTheme } from "@/src/theme";

export interface CategoryChip {
  id: number | string;
  name: string;
}

interface Props {
  chips: CategoryChip[];
  selectedId: number | string;
  onSelect: (id: number | string) => void;
  loading?: boolean;
}

export function CategoryChips({ chips, selectedId, onSelect, loading }: Props) {
  const { colors } = useTheme();

  if (loading) {
    return (
      <View style={styles.row}>
        <View style={styles.inner}>
          {[0, 1, 2, 3, 4].map((i) => (
            <View
              key={i}
              style={[
                styles.chip,
                styles.skeletonChip,
                { backgroundColor: colors.skeleton },
              ]}
            />
          ))}
        </View>
      </View>
    );
  }

  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      style={styles.row}
      contentContainerStyle={styles.inner}
      testID="category-chip-row"
    >
      {chips.map((c) => {
        const active = String(c.id) === String(selectedId);
        return (
          <Pressable
            key={String(c.id)}
            testID={`chip-${c.id}`}
            onPress={() => {
              Haptics.selectionAsync().catch(() => {});
              onSelect(c.id);
            }}
            style={({ pressed }) => [
              styles.chip,
              {
                backgroundColor: active
                  ? colors.brandPrimary
                  : colors.surfaceSecondary,
                borderColor: active ? colors.brandPrimary : colors.border,
                opacity: pressed ? 0.85 : 1,
              },
            ]}
          >
            <Text
              style={{
                color: active ? colors.onBrandPrimary : colors.onSurfaceSecondary,
                fontFamily: active ? fonts.sansSemiBold : fonts.sansMedium,
                fontSize: 13,
                letterSpacing: 0.2,
              }}
              numberOfLines={1}
            >
              {c.name}
            </Text>
          </Pressable>
        );
      })}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  row: { height: 56 },
  inner: {
    alignItems: "center",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  chip: {
    height: 36,
    minWidth: 60,
    borderRadius: radius.pill,
    borderWidth: StyleSheet.hairlineWidth,
    paddingHorizontal: spacing.md,
    alignItems: "center",
    justifyContent: "center",
    flexShrink: 0,
  },
  skeletonChip: { width: 80, borderColor: "transparent" },
});
