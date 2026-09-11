"use client";

import ButtonWithBorder from "@/components/button";
import { useRef, useState } from "react";
import { useScaleShow } from "@/animations/ScaleShow";
import toast from "react-hot-toast";
import { useTheme } from "next-themes";
import useNavigationContext from "@/hooks/contexts/useNavigationContext";
import useUserContext, {
  useUserDetails,
} from "@/hooks/contexts/useUserContext";
import "./index.css";
import useAvailableCourses from "@/hooks/course/course-choice/useAvailableCourses";
import Loading from "@/components/loading";
import usePreferredCourseUpdate from "@/hooks/course/course-choice/usePreferredCourseUpdate";
import Selector from "@/components/selector";
import useDownloadCourseConfig from "@/hooks/course/config/useDownloadCourseConfig";
import { Roles } from "@/interfaces/api/user";
import {
  CourseFileAction,
  CourseFileActions,
} from "@/components/speed-dial/modals/file-import/import-course/upload/types";
import { LazyChangePasswordModal } from "@/app/(logged-in)/settings/modals/change-password/lazy";
import { LazyUploadCourseModal } from "@/components/speed-dial/modals/lazy";

export default function Settings() {
  const {
    isSidebarLockedOpened,
    setIsSidebarLockedOpened,
    isSidebarLockedClosed,
    setIsSidebarLockedClosed,
  } = useNavigationContext();
  const wrapperRef = useScaleShow();
  const { resolvedTheme, setTheme } = useTheme();
  const { courseId } = useUserDetails();
  const { userRole } = useUserContext();
  const containerRef = useRef<HTMLDivElement | null>(null);
  const { data: courses, isLoading } = useAvailableCourses();
  const currentCourse = courses?.find((course) => course.id === courseId);
  const [changePasswordModalVisible, setChangePasswordModalVisible] =
    useState(false);
  const [importCourseModalVisible, setImportCourseModalVisible] =
    useState(false);
  const [courseFileAction, setCourseFileAction] = useState<CourseFileAction>(
    CourseFileActions.UPDATE
  );
  const setPreferredCourse = usePreferredCourseUpdate({
    shouldRedirectToMainPage: false,
  });
  const downloadCourseMutation = useDownloadCourseConfig();
  const toggleSidebarLockOpened = () => {
    if (isSidebarLockedClosed) {
      toast.error(
        "Nie można włączyć trybu ‘zawsze otwarty’, gdy sidebar jest ustawiony jako ‘zawsze zamknięty’.",
        {
          id: "settings",
        }
      );
      return;
    }
    setIsSidebarLockedOpened(!isSidebarLockedOpened);
  };

  const toggleSidebarLockClosed = () => {
    if (isSidebarLockedOpened) {
      toast.error(
        "Nie można włączyć trybu ‘zawsze zamknięty’, gdy sidebar jest ustawiony jako ‘zawsze otwarty’.",
        {
          id: "settings",
        }
      );
      return;
    }
    setIsSidebarLockedClosed(!isSidebarLockedClosed);
  };

  if (isLoading || !courses || !currentCourse) {
    return <Loading />;
  }

  return (
    <div ref={wrapperRef} className="settings-outer-wrapper">
      <div className="settings-option-wrapper">
        <h3>Sidebar zawsze otwarty</h3>
        <ButtonWithBorder
          text={isSidebarLockedOpened ? "Odblokuj" : "Zablokuj"}
          onClick={toggleSidebarLockOpened}
          size="md"
          className="!mx-0"
        />
      </div>

      <div className="settings-option-wrapper">
        <h3>Sidebar zawsze zamkniety</h3>
        <ButtonWithBorder
          text={isSidebarLockedClosed ? "Odblokuj" : "Zablokuj"}
          onClick={toggleSidebarLockClosed}
          size="md"
          className="!mx-0"
        />
      </div>

      <div className="settings-option-wrapper">
        <h3>Darkmode</h3>
        <ButtonWithBorder
          text={resolvedTheme === "dark" ? "Wyłącz" : "Włącz"}
          onClick={
            resolvedTheme === "dark"
              ? () => setTheme("light")
              : () => setTheme("dark")
          }
          size="md"
          className="!mx-0"
        />
      </div>
      <div className="settings-option-wrapper">
        <h3>Hasło</h3>
        <ButtonWithBorder
          text="Zmień hasło"
          onClick={() => setChangePasswordModalVisible(true)}
          size="md"
          className="!mx-0"
        />
      </div>
      <div ref={containerRef} className="settings-option-wrapper">
        <h3>Aktywny kurs</h3>
        <div className="settings-selector-wrapper">
          <Selector
            options={courses.map((course) => ({
              value: course.id.toString(),
              label: course.name,
            }))}
            value={currentCourse.id.toString() || ""}
            onChange={(value) => setPreferredCourse(Number(value))}
            placeholder={currentCourse.name || "Wybierz kurs"}
            size="3xl"
            padding="md"
          />
        </div>
      </div>
      {userRole === Roles.COORDINATOR && (
        <div className="settings-option-wrapper">
          <h3>Konfiguracja kursu</h3>
          <ButtonWithBorder
            text="Pobierz"
            onClick={() => downloadCourseMutation.mutate()}
            size="md"
            className="!mx-0"
          />
          <ButtonWithBorder
            text="Zmień"
            onClick={() => {
              setCourseFileAction(CourseFileActions.UPDATE);
              setImportCourseModalVisible(true);
            }}
            size="md"
            className="!mx-0"
          />
        </div>
      )}
      {userRole === Roles.INSTRUCTOR ||
        (userRole === Roles.COORDINATOR && (
          <div className="settings-option-wrapper">
            <h3>Nowy kurs</h3>
            <ButtonWithBorder
              text="Utwórz"
              onClick={() => {
                setCourseFileAction(CourseFileActions.CREATE);
                setImportCourseModalVisible(true);
              }}
              size="md"
              className="!mx-0"
            />
          </div>
        ))}
      {changePasswordModalVisible && (
        <LazyChangePasswordModal
          onClosedAction={() => setChangePasswordModalVisible(false)}
        />
      )}
      {importCourseModalVisible && (
        <LazyUploadCourseModal
          onClosedAction={() => setImportCourseModalVisible(false)}
          courseFileAction={courseFileAction}
        />
      )}
    </div>
  );
}
