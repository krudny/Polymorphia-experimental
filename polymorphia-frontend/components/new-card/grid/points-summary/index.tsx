import { PointsSummaryDetailsResponseDTO } from "@/interfaces/api/points-summary";
import { useState } from "react";
import { NewPointsSummaryProps } from "@/components/new-card/grid/points-summary/types";
import { getPointsSummaryStyles } from "@/components/new-card/grid/points-summary/metrics";
import ErrorComponent from "@/components/error";
import NewPointsSummaryElement from "@/components/new-card/grid/points-summary/element";
import { NewCardModes } from "@/components/new-card/types";
import BonusInfoModal from "@/components/course/event-section/points-summary/BonusInfoModal";
import "./index.css";

export default function NewPointsSummary({
  mode,
  pointsSummary,
  isDesktop,
}: NewPointsSummaryProps) {
  const [currentBonusInfoModal, setCurrentBonusInfoModal] =
    useState<PointsSummaryDetailsResponseDTO | null>(null);

  const effectiveMode = isDesktop ? mode : NewCardModes.NORMAL;

  const divider = <div className="new-card-points-summary-divider" />;
  const invisibleDivider = (
    <div className="new-card-points-summary-divider opacity-0" />
  );

  return (
    <>
      <div
        className="new-card-points-summary"
        style={getPointsSummaryStyles({
          mode: effectiveMode,
        })}
      >
        {pointsSummary !== undefined ? (
          <>
            <NewPointsSummaryElement
              mode={effectiveMode}
              isDesktop={isDesktop}
              bonus={pointsSummary.gained}
            />
            {invisibleDivider}
            <NewPointsSummaryElement
              mode={effectiveMode}
              isDesktop={isDesktop}
              bonus={pointsSummary.flatBonus}
              onClick={
                pointsSummary.flatBonus.assignedItems?.length
                  ? () => setCurrentBonusInfoModal(pointsSummary.flatBonus)
                  : undefined
              }
            />
            {invisibleDivider}
            <NewPointsSummaryElement
              mode={effectiveMode}
              isDesktop={isDesktop}
              bonus={pointsSummary.percentageBonus}
              onClick={
                pointsSummary.percentageBonus.assignedItems?.length
                  ? () =>
                      setCurrentBonusInfoModal(pointsSummary.percentageBonus)
                  : undefined
              }
            />
            {divider}
            <NewPointsSummaryElement
              mode={effectiveMode}
              isDesktop={isDesktop}
              bonus={pointsSummary.total}
              inline={true}
            />
          </>
        ) : (
          <ErrorComponent
            title="Brak danych"
            message="Nie znaleziono punktów podsumowania."
          />
        )}
      </div>
      <BonusInfoModal
        bonusInfo={currentBonusInfoModal}
        onClosed={() => setCurrentBonusInfoModal(null)}
      />
    </>
  );
}
