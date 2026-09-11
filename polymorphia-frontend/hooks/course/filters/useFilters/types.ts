// can be extended in the future with different options
export const SpecialBehaviors = {
  EXCLUSIVE: "EXCLUSIVE",
} as const;

export type SpecialBehavior =
  (typeof SpecialBehaviors)[keyof typeof SpecialBehaviors];

export interface FilterOption {
  value: string;
  label?: string;
  specialBehavior?: SpecialBehavior;
}

export interface FilterConfig<FilterIdType extends string> {
  id: FilterIdType;
  title: string;
  options: FilterOption[];
  min?: number; // defaults to 1
  max?: number; // defaults to 1
  defaultValues?: string[];
  additionalInfo?: string;
}

export type FilterState<FilterIdType extends string> = Record<
  FilterIdType,
  string[]
>;

export const FilterActions = {
  TOGGLE: "TOGGLE",
  SET: "SET",
  RESET: "RESET",
} as const;

export type FilterActionType =
  (typeof FilterActions)[keyof typeof FilterActions];

export type FilterAction<FilterIdType extends string> =
  | { type: typeof FilterActions.TOGGLE; id: FilterIdType; value: string }
  | { type: typeof FilterActions.SET; state: FilterState<FilterIdType> }
  | { type: typeof FilterActions.RESET };

export interface ValidateFiltersProps<FilterIdType extends string> {
  valid: boolean;
  errors: Record<FilterIdType, string>;
}
