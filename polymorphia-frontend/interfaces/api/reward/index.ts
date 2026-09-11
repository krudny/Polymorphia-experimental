export type RewardType = "ITEM" | "CHEST";

export interface BaseReward {
  id: number;
  name: string;
  imageUrl: string;
  orderIndex: number;
}

interface BaseRewardResponseDTOWithType<
  T extends RewardType,
  R extends BaseReward,
> {
  rewardType: T;
  reward: R;
}

export type RewardResponseDTO =
  ItemResponseDTOWithType | ChestResponseDTOWithType;

// Items
export type ItemBonusType = "FLAT_BONUS" | "PERCENTAGE_BONUS";

export interface BaseItem extends BaseReward {
  itemBonusType: ItemBonusType;
  bonusText: string;
  shortBonusText: string;
  limit: number;
  isLimitReached: boolean;
  eventSectionId: number;
}

export type FlatBonusItemBehavior =
  "ONE_EVENT_TRIGGERED" | "MULTIPLE_EVENTS_INSTANT";

export interface BaseFlatBonusItem extends BaseItem {
  itemBonusType: "FLAT_BONUS";
  xp: string;
  behavior: FlatBonusItemBehavior;
}

export interface BasePercentageBonusItem extends BaseItem {
  itemBonusType: "PERCENTAGE_BONUS";
  percentage: number;
}

export type ItemResponseDTO = BaseFlatBonusItem | BasePercentageBonusItem;

type ItemResponseDTOWithType = BaseRewardResponseDTOWithType<
  "ITEM",
  ItemResponseDTO
>;

// Chests
export const ChestBehaviors = {
  ALL: "ALL",
  ONE_OF_MANY: "ONE_OF_MANY",
} as const;

export type ChestBehavior =
  (typeof ChestBehaviors)[keyof typeof ChestBehaviors];

export interface BaseChest extends BaseReward {
  behaviorText: string;
  behavior: ChestBehavior;
  chestItems: ItemResponseDTO[];
}

export type ChestResponseDTO = BaseChest;
type ChestResponseDTOWithType = BaseRewardResponseDTOWithType<
  "CHEST",
  ChestResponseDTO
>;
