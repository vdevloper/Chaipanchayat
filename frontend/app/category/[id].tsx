import React from "react";
import { View, Text, StyleSheet, FlatList, RefreshControl } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useQuery } from "@tanstack/react-query";

import { TopBar } from "@/src/components/TopBar";
import { NewsCard } from "@/src/components/NewsCard";
import { CardListSkeleton } from "@/src/components/Skeletons";
import { EmptyState } from "@/src/components/EmptyState";
import { fetchPosts } from "@/src/api/wordpress";
import { fonts, spacing, useTheme } from "@/src/theme";

export default function CategoryScreen() {
  const { id, name } = useLocalSearchParams<{ id: string; name?: string }>();
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { colors } = useTheme();

  const q = useQuery({
    queryKey: ["category-posts", id],
    queryFn: () => fetchPosts({ categoryId: Number(id), perPage: 20 }),
    enabled: !!id,
  });

  const posts = q.data ?? [];

  return (
    <View style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={{ paddingTop: insets.top, backgroundColor: colors.surface }}>
        <TopBar onBack={() => router.back()} />
        <View style={styles.header}>
          <Text style={{ color: colors.brandPrimary, fontFamily: fonts.sansBold, fontSize: 12, letterSpacing: 1.2 }}>
            CATEGORY
          </Text>
          <Text
            style={{
              color: colors.onSurface,
              fontFamily: fonts.serifExtraBold,
              fontSize: 28,
              lineHeight: 34,
              marginTop: 4,
            }}
          >
            {decodeURIComponent(name || "")}
          </Text>
        </View>
        <View style={[styles.rule, { backgroundColor: colors.border }]} />
      </View>

      <FlatList
        data={posts}
        keyExtractor={(p) => String(p.id)}
        contentContainerStyle={{ paddingVertical: spacing.md, paddingBottom: spacing.xxl }}
        renderItem={({ item, index }) => <NewsCard post={item} index={index} />}
        ListEmptyComponent={
          q.isLoading ? (
            <View style={{ paddingTop: spacing.md }}>
              <CardListSkeleton count={5} />
            </View>
          ) : q.isError ? (
            <EmptyState
              icon="wifi-off"
              title="Couldn't load stories"
              actionLabel="Retry"
              onAction={() => q.refetch()}
            />
          ) : (
            <EmptyState title="No stories in this category" />
          )
        }
        refreshControl={
          <RefreshControl
            refreshing={q.isFetching && !q.isLoading}
            onRefresh={() => q.refetch()}
            tintColor={colors.brandPrimary}
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: { paddingHorizontal: spacing.lg, paddingBottom: spacing.md },
  rule: { height: StyleSheet.hairlineWidth },
});
