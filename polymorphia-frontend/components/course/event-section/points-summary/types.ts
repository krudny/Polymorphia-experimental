import {
  PointsSummaryDetailsResponseDTO,
  PointsSummaryResponseDTO,
} from "@/interfaces/api/points-summary";
import { AssignedItemResponseDTO } from "@/interfaces/api/reward/assigned";
import { ModalProps } from "@/components/modal/types";
import { RefObject } from "react";

export interface PointsSummaryProps {
  pointsSummary: PointsSummaryResponseDTO;
  ref: RefObject<HTMLDivElement | null>;
}

export interface PointsSummaryElementProps {
  bonus: PointsSummaryDetailsResponseDTO;
  onClick?: () => void;
  horizontal?: boolean;
}

export interface BonusInfoModalProps extends Omit<
  ModalProps,
  "title" | "isDataPresented" | "children" | "onRequestClose"
> {
  bonusInfo: PointsSummaryDetailsResponseDTO | null;
}

export interface BonusItemCardProps {
  assignedItem: AssignedItemResponseDTO;
}
