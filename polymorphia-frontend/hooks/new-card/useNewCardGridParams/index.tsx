import { RefObject, useContext, useLayoutEffect, useState } from "react";
import { useMediaQuery } from "react-responsive";
import { GridParams } from "@/hooks/new-card/useNewCardGridParams/types";
import { NewCardModes } from "@/components/new-card/types";
import { useDebouncedCallback } from "use-debounce";
import { getCardMetrics } from "@/components/new-card/card/metrics";
import { POINTS_SUMMARY_METRICS } from "@/components/new-card/grid/points-summary/metrics";
import { areGridParamsEqual } from "@/hooks/new-card/useNewCardGridParams/utils/are-grid-params-equal";
import { NavigationContext } from "@/providers/navigation";

export default function useNewCardGridParams(
  containerRef: RefObject<HTMLDivElement | null>,
  cardStepCount: number,
  usesPointsSummary: boolean,
  mobileRows: number = 5
): GridParams {
  const { isSidebarLockedOpened, isSidebarExpanded } = useContext(
    NavigationContext
  ) ?? { isSidebarLockedOpened: false, isSidebarExpanded: false };

  const isDesktop = useMediaQuery({ minWidth: 1024 });

  const [gridParams, setGridParams] = useState<GridParams>({
    cols: 2,
    rows: 3,
    mode: NewCardModes.NORMAL,
    isDesktop: true,
    cardMaxWidth: 400,
    isReady: false,
  });

  const sidebarCorrection =
    isDesktop && !isSidebarLockedOpened && isSidebarExpanded ? 288 - 96 : 0;
  const paginationHeight = 48;
  const gap = 20;
  const cardsHorizontalPadding = 12;

  const calculate = useDebouncedCallback(() => {
    if (!containerRef.current) {
      return;
    }

    const rawWidth = containerRef.current.offsetWidth;
    const rawHeight = containerRef.current.offsetHeight;
    const width = rawWidth + sidebarCorrection - 2 * cardsHorizontalPadding;
    const height = rawHeight - paginationHeight - gap;

    const stats = Object.values(NewCardModes).map((cardMode) => {
      const cardMetrics = getCardMetrics({
        mode: cardMode,
        stepCount: cardStepCount,
      });
      const cardWidth = cardMetrics.minWidth;
      const cardHeight = cardMetrics.height;

      const pointsSummaryImpactsDimensions = usesPointsSummary && isDesktop;

      const cardsAvailableWidth =
        width -
        (pointsSummaryImpactsDimensions
          ? POINTS_SUMMARY_METRICS[cardMode].width + gap
          : 0);
      const cardsAvailableHeight = Math.min(
        height,
        POINTS_SUMMARY_METRICS[cardMode].maxHeight
      );

      const rows = isDesktop
        ? Math.max(
            Math.floor((cardsAvailableHeight + gap) / (cardHeight + gap)),
            1
          )
        : mobileRows;
      const cols = Math.max(
        Math.floor((cardsAvailableWidth + gap) / (cardWidth + gap)),
        1
      );

      const cardsActualWidth = Math.min(
        cols * cardWidth + (cols - 1) * gap,
        cardsAvailableWidth
      );
      const cardsActualHeight = Math.min(
        rows * cardHeight + (rows - 1) * gap,
        cardsAvailableHeight
      );

      return {
        mode: cardMode,
        rows: rows,
        cols: cols,
        cardMaxWidth: cardMetrics.maxWidth,
        breaksConstraints:
          (pointsSummaryImpactsDimensions &&
            cardsActualHeight < POINTS_SUMMARY_METRICS[cardMode].minHeight) ||
          cardsActualWidth < cardMetrics.minWidth,
      };
    });

    let bestLayout = stats.find(
      (layout) =>
        layout.rows >= 2 && layout.cols >= 2 && !layout.breaksConstraints
    );

    if (!bestLayout) {
      bestLayout =
        stats.find((layout) => !layout.breaksConstraints) ?? stats.at(-1)!;
    }

    const nextParams: GridParams = {
      ...bestLayout,
      isDesktop,
      isReady: true,
    };

    setGridParams((prevParams) => {
      // Avoid re-renders (by using same reference) if evaluated params are the same
      if (areGridParamsEqual(prevParams, nextParams)) {
        return prevParams;
      }
      return nextParams;
    });
  }, 100);

  useLayoutEffect(() => {
    const element = containerRef.current;
    if (!element) {
      return;
    }

    const resizeObserver = new ResizeObserver(() => {
      calculate();
    });

    resizeObserver.observe(element);

    // Initial calc
    calculate();

    return () => {
      resizeObserver.disconnect();
    };
  }, [containerRef, calculate]);

  return gridParams;
}
