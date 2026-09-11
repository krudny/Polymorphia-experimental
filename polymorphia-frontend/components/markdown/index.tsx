"use client";

import useMarkdownContext from "@/hooks/contexts/useMarkdownContext";
import { useFadeInAnimate } from "@/animations/FadeIn";
import "./index.css";
import { MarkdownWrapperProps } from "@/components/markdown/types";
import { LazySpeedDial } from "@/components/speed-dial/lazy";
import {
  LazyMarkdownEditor,
  LazyMarkdownViewer,
} from "@/components/markdown/lazy";

export default function MarkdownWrapper({
  speedDialKey,
}: MarkdownWrapperProps) {
  const { isEditing } = useMarkdownContext();
  const wrapperRef = useFadeInAnimate();

  return (
    <div className="markdown" ref={wrapperRef}>
      <LazySpeedDial speedDialKey={speedDialKey} />
      <div className="markdown-wrapper">
        {isEditing ? <LazyMarkdownEditor /> : <LazyMarkdownViewer />}
      </div>
    </div>
  );
}
