"use client";

import {
  CourseChoiceClickedDetails,
  CourseChoiceProps,
} from "@/components/course-choice/types";
import usePreferredCourseUpdate from "@/hooks/course/course-choice/usePreferredCourseUpdate";
import CreateAnimalModal from "@/components/course-choice/modal/createAnimal";
import React, { useState } from "react";
import NewCardGridView from "@/components/new-card/grid";
import getCourseChoiceCardConfiguration from "@/components/course-choice/get-course-choice-card-configuration";
import ErrorComponent from "@/components/error";

export default function CourseChoiceGrid({
  courses,
  currentCourseId,
  containerRef,
  fastForward,
}: CourseChoiceProps) {
  const [clickedDetails, setClickedDetails] =
    useState<CourseChoiceClickedDetails | null>(null);

  const handleCourseSelection = usePreferredCourseUpdate({
    shouldRedirectToMainPage: fastForward,
  });

  return (
    <>
      {courses.length > 0 ? (
        <NewCardGridView
          ref={containerRef}
          cardConfigurations={courses.map((availableCourse) =>
            getCourseChoiceCardConfiguration({
              availableCourse,
              currentCourseId,
              handleCourseSelection,
              setClickedDetails,
            })
          )}
          usesPointsSummary={false}
          mobileRows={2}
        />
      ) : (
        <ErrorComponent
          title="Brak kursów"
          message="Nie jesteś przypisany do żadnego kursu."
        />
      )}
      {clickedDetails && (
        <CreateAnimalModal
          clickedDetails={clickedDetails}
          onClosedAction={() => setClickedDetails(null)}
        />
      )}
    </>
  );
}
