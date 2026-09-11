"use client";

import dynamic from "next/dynamic";

export const LazyChangePasswordModal = dynamic(
  () => import("@/app/(logged-in)/settings/modals/change-password/index"),
  { ssr: false }
);
