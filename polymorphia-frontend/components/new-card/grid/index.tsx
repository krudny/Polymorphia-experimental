"use client";

import useNewCardGridParams from "@/hooks/new-card/useNewCardGridParams";
import { getCardStepCount } from "@/components/new-card/card/metrics";
import NewCardGridLayout from "@/components/new-card/grid/layout";
import { NewCardGridViewProps } from "@/components/new-card/grid/types";
import "./index.css";

export default function NewCardGridView({
  cardConfigurations,
  usesPointsSummary,
  pointsSummaryConfiguration,
  ref,
  mobileRows,
}: NewCardGridViewProps) {
  const cardStepCount = Math.max(
    ...cardConfigurations.map((cardConfig) => getCardStepCount(cardConfig))
  );

  const gridParams = useNewCardGridParams(
    ref,
    cardStepCount,
    usesPointsSummary,
    mobileRows
  );
  return (
    <div className="new-card-grid-view-wrapper">
      <div ref={ref} className="new-card-grid-view">
        <NewCardGridLayout
          gridParams={gridParams}
          cardConfigurations={cardConfigurations}
          usesPointsSummary={usesPointsSummary}
          pointsSummaryConfiguration={pointsSummaryConfiguration}
        />
      </div>
    </div>
  );
}
