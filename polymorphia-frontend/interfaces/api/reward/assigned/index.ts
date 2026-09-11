import {
  RewardType,
  BaseReward,
  ItemResponseDTO,
  ChestResponseDTO,
} from "@/interfaces/api/reward";

// Assignment Details
export interface BaseRewardAssignmentDetails {
  id: number;
  receivedDate: string;
  usedDate?: string;
  isUsed: boolean;
}

export interface ItemAssignmentDetailsResponseDTO extends BaseRewardAssignmentDetails {
  gainedXp?: string; // undefined if item hasn't been used
}

export interface ChestAssignmentDetailsResponseDTO extends BaseRewardAssignmentDetails {
  receivedItems?: AssignedItemResponseDTO[]; // undefined if chest hasn't been opened (used)
}

// Assigned Rewards
interface BaseAssignedReward<B, D> {
  base: B;
  details: D;
}
interface BaseAssignedRewardResponseDTOWithType<
  T extends RewardType,
  B extends BaseReward,
  D extends BaseRewardAssignmentDetails,
> {
  rewardType: T;
  assignedReward: BaseAssignedReward<B, D>;
}

export type AssignedRewardResponseDTO =
  AssignedItemResponseDTOWithType | AssignedChestResponseDTOWithType;

// Items
export type AssignedItemResponseDTO = BaseAssignedReward<
  ItemResponseDTO,
  ItemAssignmentDetailsResponseDTO
>;
type AssignedItemResponseDTOWithType = BaseAssignedRewardResponseDTOWithType<
  "ITEM",
  ItemResponseDTO,
  ItemAssignmentDetailsResponseDTO
>;

// Chests
export interface AssignedChestResponseDTO {
  base: ChestResponseDTO;
  details: ChestAssignmentDetailsResponseDTO;
}
type AssignedChestResponseDTOWithType = BaseAssignedRewardResponseDTOWithType<
  "CHEST",
  ChestResponseDTO,
  ChestAssignmentDetailsResponseDTO
>;
