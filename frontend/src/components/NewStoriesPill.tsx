import React from "react";
import { Pressable, Text, StyleSheet } from "react-native";
import Animated, { FadeInUp, FadeOutUp } from "react-native-reanimated";
import Icon from "@react-native-vector-icons/material-design-icons";
import { fonts, radius, useTheme } from "@/src/theme";

interface Props {
  count: number;
  onPress: () => void;
  topOffset?: number;
}

export function NewStoriesPill({ count, onPress, topOffset = 0 }: Props) {
  const { colors } = useTheme();
  if (count <= 0) return null;
  return (
    <Animated.View
      entering={FadeInUp.duration(220)}
      exiting={FadeOutUp.duration(180)}
      style={[styles.wrap, { top: topOffset }]}
      pointerEvents="box-none"
    >
      <Pressable
        testID="new-stories-pill"
        onPress={onPress}
        style={({ pressed }) => [
          styles.pill,
          {
            backgroundColor: colors.brandPrimary,
            opacity: pressed ? 0.9 : 1,
            shadowColor: "#000",
          },
        ]}
      >
        <Icon name="arrow-up" size={14} color={colors.onBrandPrimary} />
        <Text
          style={{
            color: colors.onBrandPrimary,
            fontFamily: fonts.sansSemiBold,
            fontSize: 13,
          }}
        >
          {count} new {count === 1 ? "story" : "stories"}
        </Text>
      </Pressable>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    position: "absolute",
    left: 0,
    right: 0,
    alignItems: "center",
  },
  pill: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: radius.pill,
    shadowOpacity: 0.18,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 4 },
    elevation: 4,
  },
});
