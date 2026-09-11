"use client";

import clsx from "clsx";
import "./index.css";
import type { BackgroundWrapperProps } from "@/components/background-wrapper/types";

export default function BackgroundWrapper({
  children,
  className,
}: BackgroundWrapperProps) {
  return (
    <div className={clsx("background-wrapper", className)}>{children}</div>
  );
}
