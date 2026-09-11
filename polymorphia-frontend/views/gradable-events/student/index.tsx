"use client";

import { useScaleShow } from "@/animations/ScaleShow";
import { useEffect, useState } from "react";
import "./index.css";
import Loading from "@/components/loading";
import { useRouter } from "next/navigation";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import useStudentsGradableEvents from "@/hooks/course/gradable-event/useStudentsGradableEvents";
import { EventTypes } from "@/interfaces/general";
import usePointsSummary from "@/hooks/course/gradable-event/usePointsSummary";
import ErrorComponent from "@/components/error";
import { GradableEventDTO } from "@/interfaces/api/gradable_event/types";
import NewCardGridView from "@/components/new-card/grid";
import NewCardPointsAccessory from "@/components/new-card/card/accessory/points";
import { LazyGradeModal } from "@/components/speed-dial/modals/lazy";

export default function StudentView() {
  const { eventType, eventSectionId } = useEventParams();
  const {
    data: gradableEvents,
    isLoading: areGradableEventsLoading,
    isError: areGradableEventsError,
  } = useStudentsGradableEvents();
  const {
    data: pointsSummary,
    isLoading: isPointsSummaryLoading,
    isError: isPointsSummaryError,
  } = usePointsSummary();
  const isLoading = areGradableEventsLoading || isPointsSummaryLoading;
  const isError = areGradableEventsError || isPointsSummaryError;
  const router = useRouter();
  const containerRef = useScaleShow(!isLoading);
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);

  useEffect(() => {
    if (
      !areGradableEventsLoading &&
      gradableEvents &&
      gradableEvents.length === 1 &&
      eventType !== EventTypes.TEST
    ) {
      router.push(
        `/course/${eventType.toLowerCase()}/${eventSectionId}/${gradableEvents[0].id}`
      );
    }
  }, [
    isLoading,
    gradableEvents,
    eventType,
    eventSectionId,
    router,
    areGradableEventsLoading,
  ]);

  if (isLoading || (gradableEvents && gradableEvents.length < 2)) {
    return <Loading />;
  }

  if (isError || !gradableEvents || !pointsSummary) {
    return <ErrorComponent message="Nie udało się załadować wydarzeń." />;
  }

  if (gradableEvents.length === 0) {
    return (
      <ErrorComponent
        title="Brak wydarzeń"
        message="W tej sekcji nie ma żadnych wydarzeń."
      />
    );
  }

  const handleClick = (gradableEvent: GradableEventDTO) => {
    if (gradableEvent.isLocked) {
      return;
    }

    if (eventType === EventTypes.TEST) {
      setSelectedEventId(gradableEvent.id);
    } else {
      router.push(
        `/course/${eventType.toLowerCase()}/${eventSectionId}/${gradableEvent.id}`
      );
    }
  };

  return (
    <>
      <NewCardGridView
        ref={containerRef}
        cardConfigurations={gradableEvents.map((gradableEvent) => ({
          title: gradableEvent.name,
          subtitle: gradableEvent.topic,
          rightComponent: ({ mode }) => (
            <NewCardPointsAccessory
              mode={mode}
              points={gradableEvent.gainedXp}
              isSumLabelVisible={true}
              backgroundColor="gray"
              hasChest={gradableEvent.hasPossibleReward}
              shouldGrayOutReward={
                gradableEvent.isGraded && !gradableEvent.isRewardAssigned
              }
            />
          ),
          color: gradableEvent.gainedXp != undefined ? "green" : "sky",
          onClick: () => handleClick(gradableEvent),
          isLocked: gradableEvent.isLocked,
        }))}
        usesPointsSummary={true}
        pointsSummaryConfiguration={pointsSummary}
      />
      {eventType === EventTypes.TEST && selectedEventId && (
        <LazyGradeModal
          onClosedAction={() => setSelectedEventId(null)}
          gradableEventIdProp={selectedEventId}
        />
      )}
    </>
  );
}
