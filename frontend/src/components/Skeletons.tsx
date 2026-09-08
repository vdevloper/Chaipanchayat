import React, { useEffect } from "react";
import { View, StyleSheet, useWindowDimensions } from "react-native";
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withRepeat,
  withTiming,
  Easing,
} from "react-native-reanimated";
import { radius, spacing, useTheme } from "@/src/theme";

function useShimmer() {
  const opacity = useSharedValue(0.5);
  useEffect(() => {
    opacity.value = withRepeat(
      withTiming(1, { duration: 900, easing: Easing.inOut(Easing.ease) }),
      -1,
      true,
    );
  }, [opacity]);
  return useAnimatedStyle(() => ({ opacity: opacity.value }));
}

function Line({ width = "100%", height = 12, mt = 8 }: { width?: any; height?: number; mt?: number }) {
  const { colors } = useTheme();
  const anim = useShimmer();
  return (
    <Animated.View
      style={[
        { backgroundColor: colors.skeleton, width, height, borderRadius: 4, marginTop: mt },
        anim,
      ]}
    />
  );
}

export function HeroSkeleton() {
  const { colors } = useTheme();
  const { width } = useWindowDimensions();
  const w = width - spacing.lg * 2;
  const h = Math.round((w * 9) / 16);
  const anim = useShimmer();
  return (
    <View style={{ paddingHorizontal: spacing.lg }}>
      <Animated.View
        style={[
          { width: w, height: h, borderRadius: radius.md, backgroundColor: colors.skeleton },
          anim,
        ]}
      />
      <Line width="70%" height={20} mt={12} />
      <Line width="40%" height={12} mt={8} />
    </View>
  );
}

export function CardSkeleton() {
  const { colors } = useTheme();
  const anim = useShimmer();
  return (
    <View
      style={[
        styles.card,
        { backgroundColor: colors.surfaceSecondary, borderColor: colors.border },
      ]}
    >
      <Animated.View
        style={[
          { width: 118, height: 88, borderRadius: 6, backgroundColor: colors.skeleton },
          anim,
        ]}
      />
      <View style={{ flex: 1, gap: 6 }}>
        <Line width="30%" height={10} mt={0} />
        <Line width="95%" height={14} mt={6} />
        <Line width="80%" height={14} mt={6} />
        <Line width="40%" height={10} mt={8} />
      </View>
    </View>
  );
}

export function CardListSkeleton({ count = 5 }: { count?: number }) {
  return (
    <View>
      {Array.from({ length: count }).map((_, i) => (
        <CardSkeleton key={i} />
      ))}
    </View>
  );
}

export function ArticleSkeleton() {
  return (
    <View style={{ paddingHorizontal: spacing.lg, paddingVertical: spacing.lg }}>
      <Line width="25%" height={12} mt={0} />
      <Line width="95%" height={28} mt={12} />
      <Line width="80%" height={28} mt={8} />
      <Line width="50%" height={12} mt={12} />
      <View style={{ marginTop: spacing.lg }}>
        <HeroSkeleton />
      </View>
      <Line width="100%" height={14} mt={20} />
      <Line width="98%" height={14} mt={8} />
      <Line width="96%" height={14} mt={8} />
      <Line width="60%" height={14} mt={8} />
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    borderRadius: radius.md,
    borderWidth: StyleSheet.hairlineWidth,
    padding: spacing.md,
    gap: spacing.md,
    marginHorizontal: spacing.lg,
    marginBottom: spacing.md,
  },
});
