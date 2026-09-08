import React, { useCallback, useMemo, useRef, useState } from "react";
import {
  View,
  Text,
  Pressable,
  StyleSheet,
  FlatList,
  RefreshControl,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useFocusEffect, useRouter } from "expo-router";
import Icon from "@react-native-vector-icons/material-design-icons";

import { Logo } from "@/src/components/Logo";
import { CategoryChips, CategoryChip } from "@/src/components/CategoryChips";
import { HeroCard } from "@/src/components/HeroCard";
import { NewsCard } from "@/src/components/NewsCard";
import { HeroSkeleton, CardListSkeleton } from "@/src/components/Skeletons";
import { EmptyState } from "@/src/components/EmptyState";
import { OfflineBanner } from "@/src/components/OfflineBanner";
import { NewStoriesPill } from "@/src/components/NewStoriesPill";
import { fetchPosts, fetchCategories } from "@/src/api/wordpress";
import { fonts, spacing, useTheme } from "@/src/theme";

const LATEST = "latest" as const;

export default function HomeScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { colors } = useTheme();
  const queryClient = useQueryClient();

  const [selectedCat, setSelectedCat] = useState<number | string>(LATEST);
  const [newCount, setNewCount] = useState(0);
  const knownTopIdRef = useRef<number | null>(null);
  const listRef = useRef<FlatList<any>>(null);

  const categoriesQuery = useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
    staleTime: 10 * 60 * 1000,
  });

  const postsQuery = useQuery({
    queryKey: ["posts", selectedCat],
    queryFn: () =>
      fetchPosts(
        selectedCat === LATEST
          ? { perPage: 20 }
          : { categoryId: Number(selectedCat), perPage: 20 },
      ),
    staleTime: 2 * 60 * 1000,
  });

  const chips: CategoryChip[] = useMemo(() => {
    const base: CategoryChip[] = [{ id: LATEST, name: "Latest" }];
    const cats = (categoriesQuery.data ?? [])
      .filter((c) => c.slug !== "uncategorized")
      .slice(0, 14)
      .map((c) => ({ id: c.id, name: c.name }));
    return [...base, ...cats];
  }, [categoriesQuery.data]);

  const posts = postsQuery.data ?? [];
  const hero = posts[0];
  const rest = posts.slice(1);

  // Track new-stories indicator when the app resumes/foreground.
  useFocusEffect(
    useCallback(() => {
      let cancelled = false;
      const check = async () => {
        if (selectedCat !== LATEST) return;
        try {
          const fresh = await fetchPosts({ perPage: 20 });
          if (cancelled) return;
          const topId = fresh[0]?.id;
          const known = knownTopIdRef.current;
          if (known && topId && topId !== known) {
            const idx = fresh.findIndex((p) => p.id === known);
            const count = idx > 0 ? idx : fresh.length;
            setNewCount(count);
          } else if (!known && topId) {
            knownTopIdRef.current = topId;
          }
        } catch {}
      };
      check();
      return () => {
        cancelled = true;
      };
    }, [selectedCat]),
  );

  // When posts arrive, update baseline id.
  React.useEffect(() => {
    if (selectedCat === LATEST && posts[0]?.id && !newCount) {
      knownTopIdRef.current = posts[0].id;
    }
  }, [posts, newCount, selectedCat]);

  const onRefreshPill = useCallback(async () => {
    setNewCount(0);
    await queryClient.invalidateQueries({ queryKey: ["posts", selectedCat] });
    listRef.current?.scrollToOffset({ offset: 0, animated: true });
  }, [queryClient, selectedCat]);

  const renderHeader = () => (
    <View>
      <View style={styles.chipsWrap}>
        <CategoryChips
          chips={chips}
          selectedId={selectedCat}
          onSelect={(id) => setSelectedCat(id)}
          loading={categoriesQuery.isLoading}
        />
      </View>
      {postsQuery.isLoading ? (
        <View style={{ paddingTop: spacing.sm }}>
          <HeroSkeleton />
        </View>
      ) : hero ? (
        <View style={{ paddingTop: spacing.sm }}>
          <HeroCard post={hero} />
        </View>
      ) : null}

      {rest.length > 0 ? (
        <View style={styles.sectionHeader}>
          <Text
            style={[
              styles.sectionTitle,
              { color: colors.onSurface, fontFamily: fonts.serifBold },
            ]}
          >
            Latest News
          </Text>
          <View style={[styles.rule, { backgroundColor: colors.divider }]} />
        </View>
      ) : null}
    </View>
  );

  const renderEmpty = () => {
    if (postsQuery.isLoading) return <CardListSkeleton count={4} />;
    if (postsQuery.isError)
      return (
        <EmptyState
          icon="wifi-off"
          title="Couldn't load stories"
          message="Check your connection and try again."
          actionLabel="Retry"
          onAction={() => postsQuery.refetch()}
        />
      );
    return (
      <EmptyState
        title="No stories found"
        message="Pull down to refresh."
        actionLabel="Refresh"
        onAction={() => postsQuery.refetch()}
      />
    );
  };

  return (
    <View style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={{ paddingTop: insets.top, backgroundColor: colors.surface }}>
        <View style={styles.header}>
          <Logo size={34} />
          <View style={styles.headerRight}>
            <Pressable
              testID="header-search-button"
              hitSlop={12}
              onPress={() => router.push("/search")}
              style={({ pressed }) => [
                styles.iconBtn,
                { backgroundColor: pressed ? colors.surfaceTertiary : "transparent" },
              ]}
            >
              <Icon name="magnify" size={22} color={colors.onSurface} />
            </Pressable>
            <Pressable
              testID="header-bell-button"
              hitSlop={12}
              onPress={() => router.push("/settings")}
              style={({ pressed }) => [
                styles.iconBtn,
                { backgroundColor: pressed ? colors.surfaceTertiary : "transparent" },
              ]}
            >
              <Icon name="bell-outline" size={22} color={colors.onSurface} />
            </Pressable>
          </View>
        </View>
        <View style={[styles.headerRule, { backgroundColor: colors.border }]} />
      </View>

      <OfflineBanner />

      <FlatList
        ref={listRef}
        data={rest}
        keyExtractor={(item) => String(item.id)}
        ListHeaderComponent={renderHeader}
        ListEmptyComponent={
          hero ? null : (
            <View style={{ paddingTop: spacing.lg }}>{renderEmpty()}</View>
          )
        }
        renderItem={({ item, index }) => <NewsCard post={item} index={index} />}
        contentContainerStyle={{ paddingBottom: spacing.xxl }}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={postsQuery.isRefetching && !postsQuery.isLoading}
            onRefresh={() => postsQuery.refetch()}
            tintColor={colors.brandPrimary}
            colors={[colors.brandPrimary]}
          />
        }
      />

      <NewStoriesPill
        count={newCount}
        onPress={onRefreshPill}
        topOffset={insets.top + 72}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: {
    height: 56,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: spacing.lg,
  },
  headerRight: { flexDirection: "row", alignItems: "center", gap: spacing.sm },
  iconBtn: { padding: 8, borderRadius: 999 },
  headerRule: { height: StyleSheet.hairlineWidth },
  chipsWrap: {},
  sectionHeader: {
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
    paddingBottom: spacing.sm,
    gap: spacing.sm,
  },
  sectionTitle: { fontSize: 22, lineHeight: 28 },
  rule: { height: 2, width: 32 },
});
