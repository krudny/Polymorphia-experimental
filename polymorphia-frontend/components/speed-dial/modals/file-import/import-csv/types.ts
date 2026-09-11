import { ImportCSVType } from "@/interfaces/general";
import { ReactNode } from "react";

export interface ImportCSVModalProps {
  onClosedAction: () => void;
  importType: ImportCSVType;
}

export interface ImportCSVModalResult {
  content: ReactNode;
  subtitle: string;
}
