import { NewPointsSummaryElementProps } from "@/components/new-card/grid/points-summary/element/types";
import { getPointsSummaryElementStyles } from "@/components/new-card/grid/points-summary/element/metrics";
import clsx from "clsx";
import { SquareMousePointer } from "lucide-react";
import { NewCardModes } from "@/components/new-card/types";
import "./index.css";

export default function NewPointsSummaryElement({
  bonus,
  mode,
  onClick,
  inline,
  isDesktop,
}: NewPointsSummaryElementProps) {
  return (
    <div
      className={clsx(
        "new-card-points-summary-element",
        inline && "new-card-points-summary-element-inline",
        !inline && "new-card-points-summary-element-not-inline",
        isDesktop ? "items-end" : "items-center",
        inline && isDesktop ? "justify-end" : "justify-center",
        onClick && "new-card-points-summary-element-hover"
      )}
      style={getPointsSummaryElementStyles({ mode })}
    >
      <div
        className={clsx(
          "new-card-points-summary-element-content",
          onClick && "-ml-[40px]"
        )}
      >
        {onClick && <SquareMousePointer className="text-neutral-500" />}
        <h1
          className={clsx(
            "text-nowrap",
            !inline && (mode === NewCardModes.NORMAL ? "text-5xl" : "text-3xl"),
            inline && (mode === NewCardModes.NORMAL ? "text-7xl" : "text-5xl")
          )}
        >
          {bonus.title}
        </h1>
      </div>
      <h2
        className={clsx(
          "text-nowrap",
          mode === NewCardModes.NORMAL ? "text-7xl" : "text-5xl"
        )}
      >
        {bonus.gainedXp} xp
      </h2>
    </div>
  );
}
