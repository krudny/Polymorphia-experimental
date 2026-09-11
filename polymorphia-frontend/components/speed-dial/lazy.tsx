"use client";

import dynamic from "next/dynamic";

export const LazySpeedDial = dynamic(
  () => import("@/components/speed-dial").then((m) => m.SpeedDial),
  { ssr: false }
);
