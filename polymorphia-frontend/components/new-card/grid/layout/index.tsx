import Pagination from "@/components/pagination/Pagination";
import NewPointsSummary from "@/components/new-card/grid/points-summary";
import clsx from "clsx";
import SlideAnimationWrapper from "@/animations/SlideAnimationWrapper";
import { usePageable } from "@/hooks/general/usePageable";
import { useMemo } from "react";
import NewCard from "@/components/new-card/card";
import { NewCardGridLayoutProps } from "@/components/new-card/grid/layout/types";
import "./index.css";

export default function NewCardGridLayout({
  gridParams,
  cardConfigurations,
  pointsSummaryConfiguration,
  usesPointsSummary,
}: NewCardGridLayoutProps) {
  const { currentPage, direction, handlePageChange } = usePageable({
    resetDependency: gridParams, // reset page when gridParams change
  });

  const pageSize = gridParams.rows * gridParams.cols;
  const pageCount = Math.ceil(cardConfigurations.length / pageSize);

  const cardConfigurationsForPage = useMemo(() => {
    return cardConfigurations.slice(
      currentPage * pageSize,
      currentPage * pageSize + pageSize
    );
  }, [cardConfigurations, currentPage, pageSize]);

  const pagination = useMemo(
    () => (
      <Pagination
        pageCount={pageCount}
        onPageChange={handlePageChange}
        forcePage={pageCount > 0 ? currentPage : undefined}
        pageRangeDisplayed={2}
        marginPagesDisplayed={1}
        containerClassName={gridParams.isDesktop ? "ml-3" : ""}
      />
    ),
    [pageCount, handlePageChange, currentPage, gridParams.isDesktop]
  );

  const cardsView = (
    <SlideAnimationWrapper
      triggerKey={currentPage}
      direction={direction}
      className={clsx(
        gridParams.isDesktop
          ? "new-card-grid-layout-animation-desktop"
          : "new-card-grid-layout-animation-mobile"
      )}
    >
      <div
        className={clsx(
          "new-card-grid-layout-cards",
          gridParams.isReady ? "opacity-100" : "opacity-0",
          gridParams.isDesktop
            ? "new-card-grid-layout-cards-desktop"
            : "new-card-grid-layout-cards-mobile"
        )}
        style={{
          gridTemplateColumns: `repeat(${gridParams.cols}, minmax(0, ${gridParams.cardMaxWidth}px))`,
          gridTemplateRows: `repeat(${gridParams.rows}, 1fr)`,
        }}
      >
        {cardConfigurationsForPage.map((cardConfig, index) => (
          <NewCard
            key={`${currentPage}-${index}`}
            {...cardConfig}
            mode={gridParams.mode}
          />
        ))}
      </div>
    </SlideAnimationWrapper>
  );

  const pointsSummaryView = (
    <NewPointsSummary
      pointsSummary={pointsSummaryConfiguration}
      mode={gridParams.mode}
      isDesktop={gridParams.isDesktop}
    />
  );

  if (gridParams.isDesktop) {
    return (
      <div
        className={clsx(
          "new-card-grid-layout-desktop-wrapper",
          usesPointsSummary && "w-full"
        )}
      >
        <div className="new-card-grid-layout-desktop-top">
          <div className="new-card-grid-layout-desktop-cards-wrapper">
            {cardsView}
          </div>
          {usesPointsSummary && (
            <div className="new-card-grid-layout-desktop-points-summary-wrapper">
              {pointsSummaryView}
            </div>
          )}
        </div>
        <div>{pagination}</div>
      </div>
    );
  } else {
    return (
      <div className="new-card-grid-layout-mobile-wrapper">
        <div className="new-card-grid-layout-mobile-cards-wrapper">
          {cardsView}
        </div>
        <div className="new-card-grid-layout-mobile-pagination-wrapper">
          {pagination}
        </div>
        {usesPointsSummary && (
          <div className="new-card-grid-layout-mobile-points-summary-wrapper">
            {pointsSummaryView}
          </div>
        )}
      </div>
    );
  }
}
