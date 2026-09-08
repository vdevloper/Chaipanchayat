import React from "react";
import { Pressable, Text, View, StyleSheet } from "react-native";
import { Image } from "expo-image";
import { useRouter } from "expo-router";
import * as Haptics from "expo-haptics";
import Animated, { FadeInDown } from "react-native-reanimated";
import type { WPPost } from "@/src/api/wordpress";
import { featuredImage, primaryCategoryName, stripHtml } from "@/src/api/wordpress";
import { fonts, radius, spacing, useTheme } from "@/src/theme";
import { timeAgo } from "@/src/utils/date";

interface Props {
  post: WPPost;
  index?: number;
}

export function NewsCard({ post, index = 0 }: Props) {
  const { colors } = useTheme();
  const router = useRouter();
  const title = stripHtml(post.title.rendered);
  const category = primaryCategoryName(post);
  const img = featuredImage(post);

  return (
    <Animated.View
      entering={FadeInDown.delay(Math.min(index, 8) * 30).duration(220)}
    >
      <Pressable
        testID={`news-card-${post.id}`}
        onPress={() => {
          Haptics.selectionAsync().catch(() => {});
          router.push(`/article/${post.id}`);
        }}
        style={({ pressed }) => [
          styles.card,
          {
            backgroundColor: colors.surfaceSecondary,
            borderColor: colors.border,
            opacity: pressed ? 0.85 : 1,
          },
        ]}
      >
        <View style={[styles.thumb, { backgroundColor: colors.skeleton }]}>
          {img ? (
            <Image
              source={{ uri: img }}
              style={styles.thumbImage}
              contentFit="cover"
              transition={200}
            />
          ) : null}
        </View>
        <View style={styles.body}>
          {category ? (
            <Text
              style={[
                styles.category,
                { color: colors.brandPrimary, fontFamily: fonts.sansBold },
              ]}
              numberOfLines={1}
            >
              {category.toUpperCase()}
            </Text>
          ) : null}
          <Text
            style={[
              styles.headline,
              { color: colors.onSurfaceSecondary, fontFamily: fonts.serifBold },
            ]}
            numberOfLines={3}
          >
            {title}
          </Text>
          <Text
            style={[
              styles.meta,
              { color: colors.muted, fontFamily: fonts.sansRegular },
            ]}
          >
            {timeAgo(post.date)}
          </Text>
        </View>
      </Pressable>
    </Animated.View>
  );
}

const IMAGE_W = 118;

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
  thumb: {
    width: IMAGE_W,
    height: IMAGE_W * 0.75,
    borderRadius: radius.sm + 2,
    overflow: "hidden",
  },
  thumbImage: { width: "100%", height: "100%" },
  body: { flex: 1, justifyContent: "space-between", minHeight: IMAGE_W * 0.75 },
  category: { fontSize: 11, letterSpacing: 0.8, marginBottom: 4 },
  headline: { fontSize: 17, lineHeight: 22 },
  meta: { fontSize: 12, marginTop: 6 },
});
