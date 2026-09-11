import {
  ChestBehavior,
  ChestResponseDTO,
  ItemResponseDTO,
} from "@/interfaces/api/reward";
import {
  ChestAssignmentDetailsResponseDTO,
  ItemAssignmentDetailsResponseDTO,
} from "@/interfaces/api/reward/assigned";

export interface EquipmentItemResponseDTO {
  base: ItemResponseDTO;
  details: ItemAssignmentDetailsResponseDTO[];
}

export interface EquipmentChestResponseDTO {
  base: ChestResponseDTO;
  details: ChestAssignmentDetailsResponseDTO;
}

export interface EquipmentChestOpenRequestDTO {
  assignedChestId: number;
  itemId: number | null;
}

export interface BasePotentialXpResponseDTO {
  bonusXp: string;
}

export interface PotentialXpWithLossResponseDTO extends BasePotentialXpResponseDTO {
  lossXp: string;
  totalBonusXp: string;
}

export interface BaseChestPotentialXpResponseDTO<
  P extends BasePotentialXpResponseDTO,
> {
  itemDetails: Record<string, P>;
}

export interface AllChestPotentialXpResponseDTO extends BaseChestPotentialXpResponseDTO<BasePotentialXpResponseDTO> {
  summary: PotentialXpWithLossResponseDTO;
}

export type OneChestPotentialXpResponseDTO =
  BaseChestPotentialXpResponseDTO<PotentialXpWithLossResponseDTO>;

interface BaseChestPotentialXpResponseDTOWithType<
  T extends ChestBehavior,
  R extends OneChestPotentialXpResponseDTO | AllChestPotentialXpResponseDTO,
> {
  behavior: T;
  potentialXp: R;
}

type AllPotentialXpResponseDTOWithType =
  BaseChestPotentialXpResponseDTOWithType<
    "ALL",
    AllChestPotentialXpResponseDTO
  >;

type OnePotentialXpResponseDTOWithType =
  BaseChestPotentialXpResponseDTOWithType<
    "ONE_OF_MANY",
    OneChestPotentialXpResponseDTO
  >;

export type ChestPotentialXpResponseDTOWithType =
  AllPotentialXpResponseDTOWithType | OnePotentialXpResponseDTOWithType;
