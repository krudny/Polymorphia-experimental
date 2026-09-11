"use client";

import { useState } from "react";
import ProgressBar from "@/components/progressbar/ProgressBar";
import { useFadeInAnimate } from "@/animations/FadeIn";
import ProgressBarElement from "@/components/progressbar/ProgressBarElement";
import Loading from "@/components/loading";
import { useMediaQuery } from "react-responsive";
import "./styles.css";
import { useRoadmap } from "@/hooks/course/roadmap/useRoadmap";
import { LazyGradeModal } from "@/components/speed-dial/modals/lazy";
import ErrorComponent from "@/components/error";
import useUserContext from "@/hooks/contexts/useUserContext";
import StudentGradableEventCard from "@/views/gradable-events/student/StudentGradableEventCard";
import { Roles } from "@/interfaces/api/user";
import InstructorGradableEventCard from "@/views/gradable-events/instructor/InstructorGradableEventCard";
import {
  GradableEventDTO,
  StudentGradableEventResponseDTO,
  TeachingRoleGradableEventResponseDTO,
} from "@/interfaces/api/gradable_event/types";
import { Sizes } from "@/interfaces/general";

export default function Roadmap() {
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const wrapperRef = useFadeInAnimate();
  const { data: roadmap, isLoading, isError } = useRoadmap();
  const { userRole } = useUserContext();
  const isXL = useMediaQuery({ minWidth: 1280 });
  const isMd = useMediaQuery({ minWidth: 768 });
  const isSm = useMediaQuery({ minWidth: 400 });

  if (isLoading) {
    return <Loading />;
  }

  if (isError || !roadmap) {
    return <ErrorComponent message="Nie udało się załadować danych." />;
  }

  const handleClick = (gradableEvent: GradableEventDTO) => {
    if (isStudent) {
      setSelectedEventId(gradableEvent.id);
    }
  };

  const isStudent = userRole === Roles.STUDENT;
  const cardSize = isXL ? Sizes.MD : Sizes.SM;

  const cards = roadmap.map((gradableEvent) =>
    isStudent ? (
      <StudentGradableEventCard
        key={gradableEvent.id}
        size={cardSize}
        gradableEvent={gradableEvent as StudentGradableEventResponseDTO}
        handleClick={handleClick}
      />
    ) : (
      <InstructorGradableEventCard
        key={gradableEvent.id}
        size={cardSize}
        gradableEvent={gradableEvent as TeachingRoleGradableEventResponseDTO}
        handleClick={handleClick}
      />
    )
  );

  const cardHeightWithGap = isXL ? 8 : isMd ? 6.5 : isSm ? 9 : 7;
  const totalHeight = `${roadmap.length * cardHeightWithGap}rem`;

  return (
    <>
      <div
        ref={wrapperRef}
        style={{ minHeight: totalHeight }}
        className="roadmap"
      >
        <ProgressBar
          minXP={0}
          currentXP={27}
          maxXP={100}
          numSquares={cards?.length ?? 0}
          segmentSizes={Array.from({ length: cards.length * 2 - 1 }, (_, i) =>
            i % 2 === 0 ? 0 : 100 / (cards.length - 1)
          )}
          isHorizontal={false}
          upperElement={
            <ProgressBarElement
              elements={cards ?? []}
              alternate={isMd}
              isUpper={true}
              isHorizontal={false}
            />
          }
          lowerElement={
            isMd && (
              <ProgressBarElement
                elements={cards ?? []}
                alternate={isMd}
                isUpper={false}
                isHorizontal={false}
              />
            )
          }
        />
      </div>
      {selectedEventId && (
        <LazyGradeModal
          gradableEventIdProp={selectedEventId}
          onClosedAction={() => setSelectedEventId(null)}
        />
      )}
    </>
  );
}
