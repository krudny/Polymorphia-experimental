"use client";

import "./index.css";
import ButtonWithBorder from "@/components/button";
import { useTaskContext } from "@/hooks/contexts/useTaskContext";
import useUserContext from "@/hooks/contexts/useUserContext";
import { Roles } from "@/interfaces/api/user";

export default function TaskActionsBar() {
  const {
    isRunTaskPending,
    isSubmitting,
    isSubmitTaskError,
    handleRunTask,
    handleSubmitTask,
  } = useTaskContext();

  const { userRole } = useUserContext();
  const isStudentRole = userRole === Roles.STUDENT;

  return (
    <div className="task-result-actions">
      {isSubmitTaskError && (
        <p className="task-result-status-rejected">
          Nie udało się wysłać zgłoszenia. Spróbuj ponownie.
        </p>
      )}
      <ButtonWithBorder
        text={isRunTaskPending ? "Uruchamianie..." : "Uruchom"}
        size="sm"
        className="mx-0! rounded-lg!"
        forceLight={true}
        onClick={() => handleRunTask()}
        isActive={!isRunTaskPending && !isSubmitting}
      />
      {isStudentRole && (
        <ButtonWithBorder
          text={isSubmitting ? "Wysyłanie..." : "Prześlij"}
          size="sm"
          className="mx-0! rounded-lg!"
          forceLight={true}
          onClick={() => handleSubmitTask()}
          isActive={!isRunTaskPending && !isSubmitting}
        />
      )}
    </div>
  );
}
