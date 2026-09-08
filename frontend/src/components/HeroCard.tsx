import React from "react";
import { Pressable, Text, View, StyleSheet, useWindowDimensions } from "react-native";
import { Image } from "expo-image";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import * as Haptics from "expo-haptics";
import Animated, { FadeIn } from "react-native-reanimated";
import type { WPPost } from "@/src/api/wordpress";
import { featuredImage, primaryCategoryName, stripHtml } from "@/src/api/wordpress";
import { fonts, radius, spacing, useTheme } from "@/src/theme";
import { timeAgo } from "@/src/utils/date";

interface Props {
  post: WPPost;
}

export function HeroCard({ post }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const { width } = useWindowDimensions();
  const w = width - spacing.lg * 2;
  const h = Math.round((w * 9) / 16) + 60; // 16:9 + text room
  const title = stripHtml(post.title.rendered);
  const category = primaryCategoryName(post);
  const img = featuredImage(post);

  return (
    <Animated.View entering={FadeIn.duration(320)} style={{ paddingHorizontal: spacing.lg }}>
      <Pressable
        testID={`hero-card-${post.id}`}
        onPress={() => {
          Haptics.selectionAsync().catch(() => {});
          router.push(`/article/${post.id}`);
        }}
        style={({ pressed }) => [
          styles.card,
          {
            height: h,
            backgroundColor: colors.surfaceSecondary,
            borderColor: colors.border,
            opacity: pressed ? 0.9 : 1,
          },
        ]}
      >
        <View style={[styles.imageWrap, { backgroundColor: colors.skeleton }]}>
          {img ? (
            <Image
              source={{ uri: img }}
              style={StyleSheet.absoluteFill}
              contentFit="cover"
              transition={220}
            />
          ) : null}
          <LinearGradient
            colors={["rgba(0,0,0,0)", "rgba(0,0,0,0.35)", "rgba(0,0,0,0.85)"]}
            locations={[0, 0.55, 1]}
            style={StyleSheet.absoluteFill}
          />
          <View style={styles.overlay}>
            {category ? (
              <View style={[styles.pill, { backgroundColor: colors.brandPrimary }]}>
                <Text
                  style={{
                    color: colors.onBrandPrimary,
                    fontFamily: fonts.sansBold,
                    fontSize: 11,
                    letterSpacing: 0.8,
                  }}
                >
                  {category.toUpperCase()}
                </Text>
              </View>
            ) : null}
            <Text
              style={[
                styles.headline,
                { fontFamily: fonts.serifExtraBold },
              ]}
              numberOfLines={3}
            >
              {title}
            </Text>
            <Text style={[styles.meta, { fontFamily: fonts.sansMedium }]}>
              {timeAgo(post.date)}
            </Text>
          </View>
        </View>
      </Pressable>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: radius.md,
    overflow: "hidden",
    borderWidth: StyleSheet.hairlineWidth,
    marginBottom: spacing.lg,
  },
  imageWrap: { flex: 1, justifyContent: "flex-end" },
  overlay: { padding: spacing.lg, gap: spacing.sm },
  pill: {
    alignSelf: "flex-start",
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: radius.sm,
  },
  headline: { color: "#FFFFFF", fontSize: 24, lineHeight: 30 },
  meta: { color: "rgba(255,255,255,0.85)", fontSize: 12 },
});
