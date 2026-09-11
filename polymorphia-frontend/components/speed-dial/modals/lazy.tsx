"use client";

import dynamic from "next/dynamic";

export const LazyGradeModal = dynamic(
  () => import("@/components/speed-dial/modals/grade"),
  { ssr: false }
);

export const LazyUploadCourseModal = dynamic(
  () =>
    import("@/components/speed-dial/modals/file-import/import-course").then(
      (m) => m.UploadCourseModal
    ),
  { ssr: false }
);
