"use client";

import CourseChoiceGrid from "@/components/course-choice";
import "./index.css";
import useAvailableCourses from "@/hooks/course/course-choice/useAvailableCourses";
import Loading from "@/components/loading";
import { useFadeInAnimate } from "@/animations/FadeIn";
import ButtonWithBorder from "@/components/button";
import useLogout from "@/hooks/course/auth/useLogout";
import { useRef } from "react";

export default function CourseChoice() {
  const { data: courses, isLoading } = useAvailableCourses();
  const { mutate: logout } = useLogout();

  const wrapperRef = useFadeInAnimate(!isLoading);
  const gridContainerRef = useRef<HTMLDivElement>(null);

  if (isLoading || !courses) {
    return <Loading />;
  }

  return (
    <div ref={wrapperRef} className="course-choice-wrapper">
      <div className="course-choice-text">
        <h1>Wybierz kurs</h1>
      </div>
      <div ref={gridContainerRef} className="course-choice-grid">
        <CourseChoiceGrid
          courses={courses}
          containerRef={gridContainerRef}
          fastForward={true}
        />
      </div>
      <div className="course-choice-logout">
        <ButtonWithBorder text="Wyloguj" onClick={logout} />
      </div>
    </div>
  );
}
