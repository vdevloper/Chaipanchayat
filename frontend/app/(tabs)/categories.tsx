import React, { useMemo, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  Pressable,
  ScrollView,
  TextInput,
  RefreshControl,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useQuery } from "@tanstack/react-query";
import { useRouter } from "expo-router";
import Icon from "@react-native-vector-icons/material-design-icons";

import { fetchCategories } from "@/src/api/wordpress";
import { EmptyState } from "@/src/components/EmptyState";
import { fonts, radius, spacing, useTheme } from "@/src/theme";

export default function CategoriesScreen() {
  const insets = useSafeAreaInsets();
  const { colors } = useTheme();
  const router = useRouter();
  const [query, setQuery] = useState("");

  const q = useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
    staleTime: 10 * 60 * 1000,
  });

  const filtered = useMemo(() => {
    const all = q.data ?? [];
    const clean = all.filter((c) => c.slug !== "uncategorized");
    if (!query.trim()) return clean;
    const s = query.toLowerCase();
    return clean.filter((c) => c.name.toLowerCase().includes(s));
  }, [q.data, query]);

  return (
    <View style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={{ paddingTop: insets.top, backgroundColor: colors.surface }}>
        <View style={styles.header}>
          <Text
            style={[styles.headerTitle, { color: colors.onSurface, fontFamily: fonts.serifExtraBold }]}
          >
            Categories
          </Text>
          <Text style={[styles.headerSubtitle, { color: colors.muted, fontFamily: fonts.sansRegular }]}>
            Browse all sections
          </Text>
        </View>
        <View style={[styles.rule, { backgroundColor: colors.border }]} />
      </View>

      <View style={{ paddingHorizontal: spacing.lg, paddingTop: spacing.md }}>
        <View
          style={[
            styles.search,
            { backgroundColor: colors.surfaceTertiary, borderColor: colors.border },
          ]}
        >
          <Icon name="magnify" size={18} color={colors.muted} />
          <TextInput
            testID="category-search-input"
            value={query}
            onChangeText={setQuery}
            placeholder="Filter categories"
            placeholderTextColor={colors.muted}
            style={{
              flex: 1,
              color: colors.onSurface,
              fontFamily: fonts.sansMedium,
              fontSize: 14,
              paddingVertical: 0,
            }}
          />
        </View>
      </View>

      <ScrollView
        contentContainerStyle={{ padding: spacing.lg, paddingBottom: spacing.xxl }}
        refreshControl={
          <RefreshControl
            refreshing={q.isFetching}
            onRefresh={() => q.refetch()}
            tintColor={colors.brandPrimary}
          />
        }
      >
        {q.isError ? (
          <EmptyState
            icon="wifi-off"
            title="Couldn't load categories"
            actionLabel="Retry"
            onAction={() => q.refetch()}
          />
        ) : filtered.length === 0 && !q.isLoading ? (
          <EmptyState title="No categories" message="Nothing matches your search." />
        ) : (
          <View style={styles.grid}>
            {filtered.map((c) => (
              <Pressable
                key={c.id}
                testID={`category-tile-${c.id}`}
                onPress={() => router.push(`/category/${c.id}?name=${encodeURIComponent(c.name)}`)}
                style={({ pressed }) => [
                  styles.tile,
                  {
                    backgroundColor: colors.surfaceSecondary,
                    borderColor: colors.border,
                    opacity: pressed ? 0.85 : 1,
                  },
                ]}
              >
                <View style={[styles.badge, { backgroundColor: colors.brandTertiary }]}>
                  <Text
                    style={{
                      color: colors.brandPrimary,
                      fontFamily: fonts.sansBold,
                      fontSize: 11,
                      letterSpacing: 0.6,
                    }}
                  >
                    {c.count}
                  </Text>
                </View>
                <Text
                  numberOfLines={2}
                  style={{
                    color: colors.onSurfaceSecondary,
                    fontFamily: fonts.serifBold,
                    fontSize: 16,
                    lineHeight: 21,
                    marginTop: spacing.md,
                  }}
                >
                  {c.name}
                </Text>
                <Text
                  style={{
                    color: colors.muted,
                    fontFamily: fonts.sansMedium,
                    fontSize: 11,
                    marginTop: 4,
                    letterSpacing: 0.3,
                  }}
                >
                  {c.count === 1 ? "1 story" : `${c.count} stories`}
                </Text>
              </Pressable>
            ))}
          </View>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: { paddingHorizontal: spacing.lg, paddingVertical: spacing.md },
  headerTitle: { fontSize: 26, lineHeight: 32 },
  headerSubtitle: { fontSize: 13, marginTop: 2 },
  rule: { height: StyleSheet.hairlineWidth },
  search: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    paddingHorizontal: spacing.md,
    paddingVertical: 12,
    borderRadius: radius.md,
    borderWidth: StyleSheet.hairlineWidth,
  },
  grid: { flexDirection: "row", flexWrap: "wrap", gap: spacing.md },
  tile: {
    width: "48%",
    minHeight: 110,
    borderRadius: radius.md,
    borderWidth: StyleSheet.hairlineWidth,
    padding: spacing.md,
  },
  badge: {
    alignSelf: "flex-start",
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 999,
  },
});
