"use client";

import "./index.css";
import { useMediaQuery } from "react-responsive";
import TaskDesktop from "@/components/task/desktop";
import TaskMobile from "@/components/task/mobile";

export default function TasksView() {
  const isDesktop = useMediaQuery({ minWidth: "768px" });

  return (
    <div className="h-full w-full overflow-hidden p-6">
      {isDesktop ? <TaskDesktop /> : <TaskMobile />}
    </div>
  );
}
