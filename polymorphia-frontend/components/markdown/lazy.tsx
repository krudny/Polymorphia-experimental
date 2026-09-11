"use client";

import dynamic from "next/dynamic";
import Loading from "@/components/loading";

export const LazyMarkdownViewer = dynamic(
  () => import("@/components/markdown/markdown-viewer"),
  { loading: () => <Loading />, ssr: false }
);

export const LazyMarkdownEditor = dynamic(
  () => import("@/components/markdown/markdown-editor"),
  { loading: () => <Loading />, ssr: false }
);
