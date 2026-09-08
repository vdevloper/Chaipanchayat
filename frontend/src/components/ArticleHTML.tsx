// Lightweight WordPress HTML renderer using native primitives.
// Handles paragraphs, headings, strong/em, links, images, blockquotes, lists.
import React from "react";
import { Linking, StyleSheet, Text, View, useWindowDimensions } from "react-native";
import { Image } from "expo-image";
import { decodeEntities } from "@/src/api/wordpress";
import { fonts, radius, spacing, useTheme } from "@/src/theme";
import { textSizeMultiplier, TextSize } from "@/src/api/settings";

type Inline =
  | { type: "text"; text: string }
  | { type: "strong"; children: Inline[] }
  | { type: "em"; children: Inline[] }
  | { type: "a"; href: string; children: Inline[] };

type Block =
  | { type: "p"; children: Inline[] }
  | { type: "h"; level: number; children: Inline[] }
  | { type: "blockquote"; children: Inline[] }
  | { type: "ul"; items: Inline[][] }
  | { type: "ol"; items: Inline[][] }
  | { type: "img"; src: string; alt?: string }
  | { type: "hr" };

/* ------------------------------ Parser ------------------------------ */

function tokenize(html: string): Block[] {
  // Normalize
  let src = html
    .replace(/\r\n?/g, "\n")
    .replace(/<script[\s\S]*?<\/script>/gi, "")
    .replace(/<style[\s\S]*?<\/style>/gi, "")
    .replace(/<figure[^>]*>/gi, "")
    .replace(/<\/figure>/gi, "")
    .replace(/<figcaption[\s\S]*?<\/figcaption>/gi, "")
    .replace(/<div[^>]*>/gi, "")
    .replace(/<\/div>/gi, "")
    .replace(/<span[^>]*>/gi, "")
    .replace(/<\/span>/gi, "")
    .replace(/<br\s*\/?>(\s*)/gi, "\n");

  const blocks: Block[] = [];
  const blockRegex =
    /<(p|h1|h2|h3|h4|h5|h6|blockquote|ul|ol|img|hr)([^>]*)>([\s\S]*?)<\/\1>|<img([^>]*)\/?>|<hr\s*\/?>(?!\S)/gi;
  let m: RegExpExecArray | null;
  const seenRanges: [number, number][] = [];

  while ((m = blockRegex.exec(src)) !== null) {
    seenRanges.push([m.index, m.index + m[0].length]);
    const tag = (m[1] || (m[4] !== undefined ? "img" : "hr")).toLowerCase();
    const attrs = m[2] || m[4] || "";
    const inner = m[3] || "";
    if (tag === "hr") {
      blocks.push({ type: "hr" });
    } else if (tag === "img") {
      const srcMatch = attrs.match(/src=["']([^"']+)["']/i);
      const altMatch = attrs.match(/alt=["']([^"']*)["']/i);
      if (srcMatch) blocks.push({ type: "img", src: srcMatch[1], alt: altMatch?.[1] });
    } else if (tag === "p") {
      const children = parseInline(inner);
      if (children.length) blocks.push({ type: "p", children });
    } else if (/^h[1-6]$/.test(tag)) {
      blocks.push({ type: "h", level: Number(tag[1]), children: parseInline(inner) });
    } else if (tag === "blockquote") {
      blocks.push({ type: "blockquote", children: parseInline(inner) });
    } else if (tag === "ul" || tag === "ol") {
      const items: Inline[][] = [];
      const liRe = /<li[^>]*>([\s\S]*?)<\/li>/gi;
      let lm: RegExpExecArray | null;
      while ((lm = liRe.exec(inner)) !== null) {
        items.push(parseInline(lm[1]));
      }
      if (items.length) blocks.push({ type: tag as "ul" | "ol", items });
    }
  }

  // If we found no block structure at all, treat whole thing as one paragraph
  if (blocks.length === 0) {
    const stripped = decodeEntities(src.replace(/<[^>]+>/g, "")).trim();
    if (stripped) blocks.push({ type: "p", children: [{ type: "text", text: stripped }] });
  }
  return blocks;
}

function parseInline(html: string): Inline[] {
  const out: Inline[] = [];
  const re = /<(strong|b|em|i|a)([^>]*)>([\s\S]*?)<\/\1>/gi;
  let last = 0;
  let m: RegExpExecArray | null;
  while ((m = re.exec(html)) !== null) {
    if (m.index > last) {
      const text = stripTagsToText(html.slice(last, m.index));
      if (text) out.push({ type: "text", text });
    }
    const tag = m[1].toLowerCase();
    const attrs = m[2];
    const inner = m[3];
    const children = parseInline(inner);
    if (tag === "strong" || tag === "b") out.push({ type: "strong", children });
    else if (tag === "em" || tag === "i") out.push({ type: "em", children });
    else if (tag === "a") {
      const hrefMatch = attrs.match(/href=["']([^"']+)["']/i);
      out.push({ type: "a", href: hrefMatch?.[1] ?? "", children });
    }
    last = m.index + m[0].length;
  }
  if (last < html.length) {
    const text = stripTagsToText(html.slice(last));
    if (text) out.push({ type: "text", text });
  }
  return out;
}

function stripTagsToText(s: string): string {
  return decodeEntities(s.replace(/<[^>]+>/g, ""));
}

/* ------------------------------ Renderer ------------------------------ */

interface Props {
  html: string;
  textSize?: TextSize;
}

export function ArticleHTML({ html, textSize = "medium" }: Props) {
  const blocks = React.useMemo(() => tokenize(html), [html]);
  return (
    <View style={{ paddingHorizontal: spacing.lg, gap: spacing.md }}>
      {blocks.map((b, i) => (
        <BlockView key={i} block={b} textSize={textSize} />
      ))}
    </View>
  );
}

function BlockView({ block, textSize }: { block: Block; textSize: TextSize }) {
  const { colors } = useTheme();
  const { width } = useWindowDimensions();
  const mult = textSizeMultiplier(textSize);

  const bodyStyle = {
    color: colors.onSurface,
    fontFamily: fonts.sansRegular,
    fontSize: 17 * mult,
    lineHeight: 27 * mult,
  } as const;

  if (block.type === "p") {
    return (
      <Text style={bodyStyle} selectable>
        <InlineRun nodes={block.children} baseStyle={bodyStyle} />
      </Text>
    );
  }
  if (block.type === "h") {
    const map: Record<number, { size: number; line: number }> = {
      1: { size: 26, line: 32 },
      2: { size: 24, line: 30 },
      3: { size: 20, line: 26 },
      4: { size: 18, line: 24 },
      5: { size: 16, line: 22 },
      6: { size: 15, line: 20 },
    };
    const s = map[block.level] ?? map[3];
    const style = {
      color: colors.onSurface,
      fontFamily: fonts.serifBold,
      fontSize: s.size * mult,
      lineHeight: s.line * mult,
      marginTop: spacing.sm,
    } as const;
    return (
      <Text style={style} selectable>
        <InlineRun nodes={block.children} baseStyle={style} />
      </Text>
    );
  }
  if (block.type === "blockquote") {
    return (
      <View
        style={{
          borderLeftWidth: 3,
          borderLeftColor: colors.brandPrimary,
          paddingLeft: spacing.md,
          paddingVertical: spacing.xs,
        }}
      >
        <Text
          style={{
            color: colors.muted,
            fontFamily: fonts.serifBold,
            fontStyle: "italic",
            fontSize: 18 * mult,
            lineHeight: 28 * mult,
          }}
          selectable
        >
          <InlineRun
            nodes={block.children}
            baseStyle={{ color: colors.muted, fontFamily: fonts.serifBold }}
          />
        </Text>
      </View>
    );
  }
  if (block.type === "ul" || block.type === "ol") {
    return (
      <View style={{ gap: spacing.xs }}>
        {block.items.map((item, i) => (
          <View key={i} style={{ flexDirection: "row", gap: 8 }}>
            <Text style={[bodyStyle, { minWidth: 20 }]}>
              {block.type === "ol" ? `${i + 1}.` : "•"}
            </Text>
            <Text style={[bodyStyle, { flex: 1 }]} selectable>
              <InlineRun nodes={item} baseStyle={bodyStyle} />
            </Text>
          </View>
        ))}
      </View>
    );
  }
  if (block.type === "img") {
    const w = width - spacing.lg * 2;
    return (
      <View
        style={{
          width: w,
          aspectRatio: 16 / 9,
          borderRadius: radius.md,
          overflow: "hidden",
          backgroundColor: colors.skeleton,
          marginVertical: spacing.xs,
        }}
      >
        <Image
          source={{ uri: block.src }}
          style={{ width: "100%", height: "100%" }}
          contentFit="cover"
          transition={220}
          accessibilityLabel={block.alt}
        />
      </View>
    );
  }
  if (block.type === "hr") {
    return <View style={{ height: StyleSheet.hairlineWidth, backgroundColor: colors.border }} />;
  }
  return null;
}

function InlineRun({
  nodes,
  baseStyle,
}: {
  nodes: Inline[];
  baseStyle: any;
}) {
  const { colors } = useTheme();
  return (
    <>
      {nodes.map((n, i) => {
        if (n.type === "text") return <Text key={i} style={baseStyle}>{n.text}</Text>;
        if (n.type === "strong")
          return (
            <Text key={i} style={[baseStyle, { fontFamily: fonts.sansBold }]}>
              <InlineRun nodes={n.children} baseStyle={{ ...baseStyle, fontFamily: fonts.sansBold }} />
            </Text>
          );
        if (n.type === "em")
          return (
            <Text key={i} style={[baseStyle, { fontStyle: "italic" }]}>
              <InlineRun nodes={n.children} baseStyle={{ ...baseStyle, fontStyle: "italic" }} />
            </Text>
          );
        if (n.type === "a")
          return (
            <Text
              key={i}
              onPress={() => n.href && Linking.openURL(n.href).catch(() => {})}
              style={[baseStyle, { color: colors.brandPrimary, textDecorationLine: "underline" }]}
            >
              <InlineRun
                nodes={n.children}
                baseStyle={{ ...baseStyle, color: colors.brandPrimary }}
              />
            </Text>
          );
        return null;
      })}
    </>
  );
}
