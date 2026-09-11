import { useContext } from "react";
import { TaskContext } from "@/providers/task";
import { TaskContextInterface } from "@/providers/task/types";

export function useTaskContext(): TaskContextInterface {
  const context = useContext(TaskContext);

  if (!context) {
    throw new Error("useTaskContext must be used within a TaskProvider");
  }

  return context;
}
