import { TargetRequestDTO } from "@/interfaces/api/target";
import { ReactNode } from "react";

export interface ProjectGroupConfigurationModalProps {
  onClosedAction: () => void;
  initialTarget: TargetRequestDTO | null;
}

export interface ProjectGroupConfigurationModalResult {
  content: ReactNode;
  subtitle: string;
}
