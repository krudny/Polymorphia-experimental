import { TargetRequestDTO } from "@/interfaces/api/target";

export interface ProjectGroupDeletionModalContentProps {
  initialTarget: TargetRequestDTO | null;
}

export interface ProjectGroupDeletionModalProps {
  onClosedAction: () => void;
  initialTarget: TargetRequestDTO | null;
}
