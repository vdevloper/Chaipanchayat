import React, { useMemo } from "react";
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Pressable,
  Share,
  useWindowDimensions,
  Linking,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Image } from "expo-image";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useQuery } from "@tanstack/react-query";
import Icon from "@react-native-vector-icons/material-design-icons";
import * as Haptics from "expo-haptics";

import { TopBar } from "@/src/components/TopBar";
import { ArticleSkeleton } from "@/src/components/Skeletons";
import { EmptyState } from "@/src/components/EmptyState";
import { ArticleHTML } from "@/src/components/ArticleHTML";
import { fetchPost, featuredImage, primaryCategoryName, stripHtml } from "@/src/api/wordpress";
import { fonts, radius, spacing, useTheme } from "@/src/theme";
import { formatDateTime } from "@/src/utils/date";
import { useIsBookmarked, toggleBookmark } from "@/src/api/bookmarks";
import { useSettings, TextSize } from "@/src/api/settings";

export default function ArticleScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const insets = useSafeAreaInsets();
  const { colors } = useTheme();
  const router = useRouter();
  const { width } = useWindowDimensions();
  const { settings, update } = useSettings();

  const q = useQuery({
    queryKey: ["post", id],
    queryFn: () => fetchPost(id!),
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
  });

  const post = q.data;
  const title = useMemo(() => (post ? stripHtml(post.title.rendered) : ""), [post]);
  const category = post ? primaryCategoryName(post) : undefined;
  const img = post ? featuredImage(post) : undefined;
  const bookmarked = useIsBookmarked(post?.id);

  const onShare = async () => {
    if (!post) return;
    try {
      Haptics.selectionAsync().catch(() => {});
      await Share.share({
        title,
        message: `${title}\n\n${post.link}`,
        url: post.link,
      });
    } catch {}
  };

  const onBookmark = async () => {
    if (!post) return;
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium).catch(() => {});
    await toggleBookmark(post);
  };

  const cycleTextSize = () => {
    const order: TextSize[] = ["small", "medium", "large"];
    const idx = order.indexOf(settings.textSize);
    update({ textSize: order[(idx + 1) % order.length] });
    Haptics.selectionAsync().catch(() => {});
  };

  return (
    <View style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={{ paddingTop: insets.top, backgroundColor: colors.surface }}>
        <TopBar
          onBack={() => router.back()}
          right={
            <Pressable
              testID="article-open-web"
              hitSlop={10}
              onPress={() => post && Linking.openURL(post.link)}
              style={({ pressed }) => [
                { padding: 6, borderRadius: 999 },
                { backgroundColor: pressed ? colors.surfaceTertiary : "transparent" },
              ]}
            >
              <Icon name="open-in-new" size={20} color={colors.onSurface} />
            </Pressable>
          }
        />
      </View>

      {q.isLoading ? (
        <ArticleSkeleton />
      ) : q.isError || !post ? (
        <EmptyState
          icon="alert-circle-outline"
          title="Couldn't open article"
          message="This story couldn't be loaded. Please try again."
          actionLabel="Retry"
          onAction={() => q.refetch()}
        />
      ) : (
        <>
          <ScrollView
            contentContainerStyle={{ paddingBottom: 120 }}
            showsVerticalScrollIndicator={false}
          >
            <View style={{ paddingHorizontal: spacing.lg, paddingTop: spacing.md, gap: spacing.sm }}>
              {category ? (
                <Text
                  style={{
                    color: colors.brandPrimary,
                    fontFamily: fonts.sansBold,
                    fontSize: 12,
                    letterSpacing: 1.2,
                  }}
                >
                  {category.toUpperCase()}
                </Text>
              ) : null}
              <Text
                style={{
                  color: colors.onSurface,
                  fontFamily: fonts.serifExtraBold,
                  fontSize: 28,
                  lineHeight: 34,
                }}
                selectable
              >
                {title}
              </Text>
              <Text
                style={{
                  color: colors.muted,
                  fontFamily: fonts.sansMedium,
                  fontSize: 13,
                  marginTop: 4,
                }}
              >
                {formatDateTime(post.date)}
              </Text>
            </View>

            {img ? (
              <View
                style={{
                  marginHorizontal: spacing.lg,
                  marginTop: spacing.lg,
                  borderRadius: radius.md,
                  overflow: "hidden",
                  backgroundColor: colors.skeleton,
                  width: width - spacing.lg * 2,
                  aspectRatio: 16 / 9,
                }}
              >
                <Image
                  source={{ uri: img }}
                  style={{ width: "100%", height: "100%" }}
                  contentFit="cover"
                  transition={260}
                />
              </View>
            ) : null}

            <View style={{ height: spacing.lg }} />
            <ArticleHTML html={post.content.rendered} textSize={settings.textSize} />
          </ScrollView>

          {/* Sticky action bar */}
          <View
            style={[
              styles.actionBar,
              {
                paddingBottom: Math.max(insets.bottom, spacing.md),
                backgroundColor: colors.surfaceSecondary,
                borderTopColor: colors.border,
              },
            ]}
          >
            <ActionButton
              icon={bookmarked ? "bookmark" : "bookmark-outline"}
              label={bookmarked ? "Saved" : "Save"}
              onPress={onBookmark}
              testID="article-bookmark-button"
              active={bookmarked}
            />
            <ActionButton
              icon="format-size"
              label={
                settings.textSize === "small" ? "Small" : settings.textSize === "large" ? "Large" : "Medium"
              }
              onPress={cycleTextSize}
              testID="article-textsize-button"
            />
            <ActionButton
              icon="share-variant-outline"
              label="Share"
              onPress={onShare}
              testID="article-share-button"
              primary
            />
          </View>
        </>
      )}
    </View>
  );
}

function ActionButton({
  icon,
  label,
  onPress,
  testID,
  primary,
  active,
}: {
  icon: string;
  label: string;
  onPress: () => void;
  testID?: string;
  primary?: boolean;
  active?: boolean;
}) {
  const { colors } = useTheme();
  const fg = primary ? colors.onBrandPrimary : active ? colors.brandPrimary : colors.onSurface;
  const bg = primary ? colors.brandPrimary : "transparent";
  return (
    <Pressable
      testID={testID}
      onPress={onPress}
      style={({ pressed }) => [
        styles.actionBtn,
        { backgroundColor: bg, opacity: pressed ? 0.85 : 1 },
      ]}
    >
      <Icon name={icon as any} size={20} color={fg} />
      <Text style={{ color: fg, fontFamily: fonts.sansSemiBold, fontSize: 12, marginTop: 4 }}>
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  actionBar: {
    position: "absolute",
    left: 0,
    right: 0,
    bottom: 0,
    flexDirection: "row",
    borderTopWidth: StyleSheet.hairlineWidth,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
    gap: spacing.sm,
  },
  actionBtn: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 10,
    borderRadius: radius.md,
  },
});
