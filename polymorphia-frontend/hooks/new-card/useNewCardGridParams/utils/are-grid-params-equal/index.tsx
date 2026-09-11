import { GridParams } from "@/hooks/new-card/useNewCardGridParams/types";

export function areGridParamsEqual(
  prev: GridParams,
  next: GridParams
): boolean {
  return (
    prev.cols === next.cols &&
    prev.rows === next.rows &&
    prev.mode === next.mode &&
    prev.isDesktop === next.isDesktop &&
    prev.isReady === next.isReady &&
    prev.cardMaxWidth === prev.cardMaxWidth
  );
}
