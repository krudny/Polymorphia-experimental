import { clsx } from "clsx";
import { NewCardPointsAccessoryProps } from "@/components/new-card/card/accessory/points/types";
import "./index.css";
import { NewCardModes } from "@/components/new-card/types";
import NewCardTextAccessory from "@/components/new-card/card/accessory/text";

export default function NewCardPointsAccessory({
  mode,
  points,
  isSumLabelVisible = false,
  isXPLabelVisible = true,
  hasChest = false,
  shouldGrayOutReward = false,
  backgroundColor,
}: NewCardPointsAccessoryProps) {
  const topText = (points ?? "-") + (isXPLabelVisible ? " xp" : "");
  const bottomText = isSumLabelVisible ? "Suma" : undefined;
  const additionalView = hasChest ? (
    <div
      className={clsx(
        "new-card-points-accessory-view",
        mode === NewCardModes.NORMAL
          ? "new-card-points-accessory-view-normal"
          : "new-card-points-accessory-view-compact",
        shouldGrayOutReward && "text-neutral-400"
      )}
    >
      <span
        className={clsx(
          "material-symbols leading-none",
          mode === NewCardModes.NORMAL ? "text-2xl" : "text-lg"
        )}
      >
        trophy
      </span>
    </div>
  ) : undefined;

  return (
    <NewCardTextAccessory
      topText={topText}
      bottomText={bottomText}
      additionalView={additionalView}
      backgroundColor={backgroundColor}
    />
  );
}
