// Chai Panchayat design tokens. Light + Dark themes drawn from
// /app/design_guidelines.json and the user's exact brand hexes.

import { useMemo } from "react";
import { StyleSheet, useColorScheme } from "react-native";

export type ColorScheme = "light" | "dark";

const light = {
  // Surfaces
  surface: "#FFFDF8", // warm editorial background
  onSurface: "#171717",
  surfaceSecondary: "#FFFFFF", // cards
  onSurfaceSecondary: "#171717",
  surfaceTertiary: "#F5F2EC", // inputs, chips
  onSurfaceTertiary: "#171717",
  surfaceInverse: "#101010",
  onSurfaceInverse: "#F5F5F5",
  muted: "#666666",

  // Brand — Chai Saffron
  brand: "#E85D04",
  onBrand: "#FFFFFF",
  brandPrimary: "#E85D04",
  onBrandPrimary: "#FFFFFF",
  brandSecondary: "#C94B00",
  onBrandSecondary: "#FFFFFF",
  brandTertiary: "#FCECE3", // subtle saffron tint
  onBrandTertiary: "#C94B00",

  // Status
  success: "#2A9D5B",
  onSuccess: "#FFFFFF",
  warning: "#D97706",
  onWarning: "#FFFFFF",
  error: "#C1121F", // breaking news
  onError: "#FFFFFF",
  info: "#666666",
  onInfo: "#FFFFFF",

  // Lines
  border: "#E8E4DE",
  borderStrong: "#D3CEC6",
  divider: "#E8E4DE",

  // Extras
  skeleton: "#EDE7DE",
  overlay: "rgba(16,16,16,0.55)",
  tabInactive: "#777777",
};

const dark: typeof light = {
  surface: "#101010",
  onSurface: "#F5F5F5",
  surfaceSecondary: "#1A1A1A",
  onSurfaceSecondary: "#F5F5F5",
  surfaceTertiary: "#222222",
  onSurfaceTertiary: "#F5F5F5",
  surfaceInverse: "#FFFDF8",
  onSurfaceInverse: "#171717",
  muted: "#A7A7A7",

  brand: "#E85D04",
  onBrand: "#FFFFFF",
  brandPrimary: "#E85D04",
  onBrandPrimary: "#FFFFFF",
  brandSecondary: "#C94B00",
  onBrandSecondary: "#FFFFFF",
  brandTertiary: "#3A1F10",
  onBrandTertiary: "#E85D04",

  success: "#2A9D5B",
  onSuccess: "#FFFFFF",
  warning: "#D97706",
  onWarning: "#FFFFFF",
  error: "#C1121F",
  onError: "#FFFFFF",
  info: "#A7A7A7",
  onInfo: "#101010",

  border: "#2A2A2A",
  borderStrong: "#444444",
  divider: "#2A2A2A",

  skeleton: "#242424",
  overlay: "rgba(0,0,0,0.65)",
  tabInactive: "#888888",
};

export type ThemeColors = typeof light;

export const defaultScheme = "light" satisfies ColorScheme;
export const themes: { light: ThemeColors; dark: ThemeColors } = { light, dark };

export function useTheme(): { scheme: ColorScheme; colors: ThemeColors } {
  const system = useColorScheme();
  const scheme: ColorScheme = system && themes[system] ? system : defaultScheme;
  return { scheme, colors: themes[scheme] ?? themes.light };
}

export function makeStyles<
  T extends StyleSheet.NamedStyles<T> | StyleSheet.NamedStyles<any>,
>(factory: (colors: ThemeColors) => T & StyleSheet.NamedStyles<any>): () => T {
  return function useStyles(): T {
    const { colors } = useTheme();
    return useMemo(() => StyleSheet.create(factory(colors)), [colors]);
  };
}

// Typography tokens
export const fonts = {
  serifRegular: "NotoSerifDevanagari-Regular",
  serifBold: "NotoSerifDevanagari-Bold",
  serifExtraBold: "NotoSerifDevanagari-ExtraBold",
  sansRegular: "Inter-Regular",
  sansMedium: "Inter-Medium",
  sansSemiBold: "Inter-SemiBold",
  sansBold: "Inter-Bold",
} as const;

export const typeScale = {
  small: { fontSize: 11, lineHeight: 14 },
  metadata: { fontSize: 13, lineHeight: 18 },
  body: { fontSize: 17, lineHeight: 27 },
  cardHeadline: { fontSize: 18, lineHeight: 23 },
  section: { fontSize: 22, lineHeight: 28 },
  articleHeadline: { fontSize: 28, lineHeight: 34 },
  heroHeadline: { fontSize: 32, lineHeight: 38 },
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
  xxxl: 48,
} as const;

export const radius = {
  sm: 4,
  md: 10,
  lg: 16,
  pill: 999,
} as const;
