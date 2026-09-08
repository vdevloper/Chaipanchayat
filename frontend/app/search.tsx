import React, { useEffect, useRef, useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  FlatList,
  Pressable,
  Keyboard,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useRouter } from "expo-router";
import Icon from "@react-native-vector-icons/material-design-icons";

import { TopBar } from "@/src/components/TopBar";
import { NewsCard } from "@/src/components/NewsCard";
import { CardListSkeleton } from "@/src/components/Skeletons";
import { EmptyState } from "@/src/components/EmptyState";
import { fetchPosts, WPPost } from "@/src/api/wordpress";
import { fonts, radius, spacing, useTheme } from "@/src/theme";

export default function SearchScreen() {
  const insets = useSafeAreaInsets();
  const { colors } = useTheme();
  const router = useRouter();
  const inputRef = useRef<TextInput>(null);
  const [query, setQuery] = useState("");
  const [debounced, setDebounced] = useState("");
  const [results, setResults] = useState<WPPost[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setDebounced(query.trim()), 350);
    return () => clearTimeout(t);
  }, [query]);

  useEffect(() => {
    let cancelled = false;
    if (!debounced) {
      setResults(null);
      setLoading(false);
      setError(false);
      return;
    }
    setLoading(true);
    setError(false);
    fetchPosts({ search: debounced, perPage: 20 })
      .then((r) => {
        if (!cancelled) setResults(r);
      })
      .catch(() => {
        if (!cancelled) setError(true);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [debounced]);

  useEffect(() => {
    const t = setTimeout(() => inputRef.current?.focus(), 100);
    return () => clearTimeout(t);
  }, []);

  return (
    <View style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={{ paddingTop: insets.top, backgroundColor: colors.surface }}>
        <TopBar onBack={() => router.back()} />
        <View style={styles.searchWrap}>
          <View
            style={[
              styles.search,
              { backgroundColor: colors.surfaceTertiary, borderColor: colors.border },
            ]}
          >
            <Icon name="magnify" size={20} color={colors.muted} />
            <TextInput
              ref={inputRef}
              testID="search-input"
              value={query}
              onChangeText={setQuery}
              placeholder="Search Chai Panchayat"
              placeholderTextColor={colors.muted}
              returnKeyType="search"
              onSubmitEditing={Keyboard.dismiss}
              style={{
                flex: 1,
                color: colors.onSurface,
                fontFamily: fonts.sansMedium,
                fontSize: 15,
                paddingVertical: 0,
              }}
            />
            {query.length > 0 ? (
              <Pressable
                testID="search-clear-button"
                hitSlop={10}
                onPress={() => setQuery("")}
              >
                <Icon name="close-circle" size={18} color={colors.muted} />
              </Pressable>
            ) : null}
          </View>
        </View>
      </View>

      {!debounced ? (
        <View style={{ paddingHorizontal: spacing.lg, paddingTop: spacing.lg }}>
          <Text style={{ color: colors.muted, fontFamily: fonts.sansRegular, fontSize: 13 }}>
            Search headlines, categories, and reporters from chaipanchayat.com.
          </Text>
        </View>
      ) : loading ? (
        <View style={{ paddingTop: spacing.md }}>
          <CardListSkeleton count={4} />
        </View>
      ) : error ? (
        <EmptyState
          icon="wifi-off"
          title="Search failed"
          message="Check your connection and try again."
        />
      ) : (results?.length ?? 0) === 0 ? (
        <EmptyState
          icon="text-search"
          title="No results"
          message={`No stories match "${debounced}".`}
        />
      ) : (
        <>
          <Text
            style={{
              color: colors.muted,
              fontFamily: fonts.sansMedium,
              fontSize: 12,
              paddingHorizontal: spacing.lg,
              paddingTop: spacing.md,
              letterSpacing: 0.5,
            }}
          >
            {results!.length} {results!.length === 1 ? "RESULT" : "RESULTS"}
          </Text>
          <FlatList
            data={results!}
            keyExtractor={(p) => String(p.id)}
            renderItem={({ item, index }) => <NewsCard post={item} index={index} />}
            contentContainerStyle={{ paddingVertical: spacing.md, paddingBottom: spacing.xxl }}
            keyboardShouldPersistTaps="handled"
          />
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  searchWrap: { paddingHorizontal: spacing.lg, paddingBottom: spacing.md },
  search: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    borderRadius: radius.md,
    borderWidth: StyleSheet.hairlineWidth,
    paddingHorizontal: spacing.md,
    paddingVertical: 12,
  },
});
