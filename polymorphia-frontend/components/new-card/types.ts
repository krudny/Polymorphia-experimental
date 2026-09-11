// Important: from largest to smallest!
export const NewCardModes = {
  NORMAL: "NORMAL",
  COMPACT: "COMPACT",
} as const;

export type NewCardMode = (typeof NewCardModes)[keyof typeof NewCardModes];
