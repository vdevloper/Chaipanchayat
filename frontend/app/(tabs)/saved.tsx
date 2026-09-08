import React from "react";
import { View, Text, StyleSheet, Pressable, FlatList } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import { Image } from "expo-image";
import Icon from "@react-native-vector-icons/material-design-icons";
import Animated, { FadeInDown } from "react-native-reanimated";

import { EmptyState } from "@/src/components/EmptyState";
import { useBookmarks, removeBookmark, Bookmark } from "@/src/api/bookmarks";
import { fonts, radius, spacing, useTheme } from "@/src/theme";
import { timeAgo } from "@/src/utils/date";

export default function SavedScreen() {
  const insets = useSafeAreaInsets();
  const { colors } = useTheme();
  const router = useRouter();
  const { bookmarks, ready } = useBookmarks();

  return (
    <View style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={{ paddingTop: insets.top, backgroundColor: colors.surface }}>
        <View style={styles.header}>
          <Text
            style={[styles.headerTitle, { color: colors.onSurface, fontFamily: fonts.serifExtraBold }]}
          >
            Saved
          </Text>
          <Text style={[styles.headerSubtitle, { color: colors.muted, fontFamily: fonts.sansRegular }]}>
            Your bookmarked stories
          </Text>
        </View>
        <View style={[styles.rule, { backgroundColor: colors.border }]} />
      </View>

      {ready && bookmarks.length === 0 ? (
        <EmptyState
          icon="bookmark-outline"
          title="You haven't saved any stories"
          message="Tap the bookmark icon on any article to save it here for later."
        />
      ) : (
        <FlatList
          data={bookmarks}
          keyExtractor={(b) => String(b.id)}
          contentContainerStyle={{ paddingVertical: spacing.md, paddingBottom: spacing.xxl }}
          renderItem={({ item, index }) => (
            <BookmarkRow
              b={item}
              index={index}
              onPress={() => router.push(`/article/${item.id}`)}
              onRemove={() => removeBookmark(item.id)}
            />
          )}
        />
      )}
    </View>
  );
}

function BookmarkRow({
  b,
  index,
  onPress,
  onRemove,
}: {
  b: Bookmark;
  index: number;
  onPress: () => void;
  onRemove: () => void;
}) {
  const { colors } = useTheme();
  return (
    <Animated.View entering={FadeInDown.delay(Math.min(index, 6) * 25).duration(200)}>
      <Pressable
        testID={`saved-row-${b.id}`}
        onPress={onPress}
        style={({ pressed }) => [
          styles.row,
          {
            backgroundColor: colors.surfaceSecondary,
            borderColor: colors.border,
            opacity: pressed ? 0.9 : 1,
          },
        ]}
      >
        <View style={[styles.thumb, { backgroundColor: colors.skeleton }]}>
          {b.image ? (
            <Image source={{ uri: b.image }} style={styles.thumbImg} contentFit="cover" transition={180} />
          ) : null}
        </View>
        <View style={{ flex: 1, justifyContent: "space-between" }}>
          {b.category ? (
            <Text style={{ color: colors.brandPrimary, fontFamily: fonts.sansBold, fontSize: 11, letterSpacing: 0.8 }}>
              {b.category.toUpperCase()}
            </Text>
          ) : null}
          <Text
            numberOfLines={3}
            style={{
              color: colors.onSurfaceSecondary,
              fontFamily: fonts.serifBold,
              fontSize: 16,
              lineHeight: 21,
              marginTop: 4,
            }}
          >
            {b.title}
          </Text>
          <Text style={{ color: colors.muted, fontFamily: fonts.sansRegular, fontSize: 12, marginTop: 6 }}>
            Saved · {timeAgo(new Date(b.savedAt).toISOString())}
          </Text>
        </View>
        <Pressable
          testID={`saved-remove-${b.id}`}
          hitSlop={12}
          onPress={onRemove}
          style={({ pressed }) => [
            styles.removeBtn,
            { backgroundColor: pressed ? colors.surfaceTertiary : "transparent" },
          ]}
        >
          <Icon name="close" size={18} color={colors.muted} />
        </Pressable>
      </Pressable>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: { paddingHorizontal: spacing.lg, paddingVertical: spacing.md },
  headerTitle: { fontSize: 26, lineHeight: 32 },
  headerSubtitle: { fontSize: 13, marginTop: 2 },
  rule: { height: StyleSheet.hairlineWidth },
  row: {
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
    marginHorizontal: spacing.lg,
    marginBottom: spacing.md,
    borderRadius: radius.md,
    borderWidth: StyleSheet.hairlineWidth,
  },
  thumb: { width: 96, height: 72, borderRadius: 6, overflow: "hidden" },
  thumbImg: { width: "100%", height: "100%" },
  removeBtn: { padding: 4, borderRadius: 999, alignSelf: "flex-start" },
});
