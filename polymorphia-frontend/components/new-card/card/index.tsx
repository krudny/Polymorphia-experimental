import { tv } from "tailwind-variants";
import clsx from "clsx";
import { NewCardProps } from "@/components/new-card/card/types";
import {
  getCardMetrics,
  getCardStepCount,
  getCardStyles,
} from "@/components/new-card/card/metrics";
import { NewCardModes } from "@/components/new-card/types";
import "./index.css";

export const colorVariants = tv({
  slots: {
    borderPrimary: "",
    borderSecondary: "",
    backgroundPrimary: "",
    backgroundSecondary: "",
  },
  variants: {
    color: {
      gold: {
        borderPrimary: "border-b-8 border-amber-400",
        borderSecondary: "border-b-4 border-amber-300",
        backgroundPrimary: "bg-amber-400",
        backgroundSecondary: "bg-amber-300",
      },
      silver: {
        borderPrimary: "border-b-8 border-slate-400",
        borderSecondary: "border-b-4 border-slate-300",
        backgroundPrimary: "bg-slate-400",
        backgroundSecondary: "bg-slate-300",
      },
      bronze: {
        borderPrimary: "border-b-8 border-amber-800",
        borderSecondary: "border-b-4 border-amber-700",
        backgroundPrimary: "bg-amber-800",
        backgroundSecondary: "bg-amber-600",
      },
      green: {
        borderPrimary: "border-b-8 border-primary-success",
        borderSecondary: "border-b-4 border-secondary-success",
        backgroundPrimary: "bg-primary-success",
        backgroundSecondary: "bg-secondary-success",
      },
      sky: {
        borderPrimary: "border-b-8 border-primary-sky",
        borderSecondary: "border-b-4 border-secondary-sky",
        backgroundPrimary: "bg-primary-sky",
        backgroundSecondary: "bg-secondary-sky",
      },
      gray: {
        borderPrimary: "border-b-8 border-primary-gray",
        borderSecondary:
          "border-b-4 border-secondary-gray dark:border-primary-dark",
        backgroundPrimary: "bg-primary-gray",
        backgroundSecondary: "bg-secondary-gray dark:bg-secondary-dark",
      },
    },
  },
});

export default function NewCard({
  mode,
  title,
  subtitle,
  details,
  leftComponent,
  rightComponent,
  color,
  sizeBonus,
  onClick: propOnClick,
  useDynamicBehavior,
  isLocked,
}: NewCardProps) {
  const dynamicBehavior = useDynamicBehavior?.();
  const onClick = dynamicBehavior?.onClick ?? propOnClick;

  return (
    <div
      onClick={!isLocked ? onClick : undefined}
      className={clsx(
        "new-card",
        !isLocked
          ? colorVariants({ color }).borderPrimary()
          : colorVariants({ color: "gray" }).borderPrimary(),
        onClick && !isLocked ? "new-card-hover" : "new-card-regular",
        mode === NewCardModes.NORMAL
          ? "new-card-normal-mode"
          : "new-card-compact-mode"
      )}
      style={getCardStyles({
        cardMetrics: getCardMetrics({
          mode,
          stepCount: getCardStepCount({
            leftComponent,
            rightComponent,
            sizeBonus,
          }),
        }),
      })}
    >
      <div
        className={clsx(
          "new-card-wrapper",
          isLocked && "new-card-wrapper-locked"
        )}
      >
        {leftComponent && (
          <div className="new-card-accessory">
            {leftComponent({ mode, color })}
          </div>
        )}
        <div className={clsx("new-card-middle")}>
          <h1 className="truncate">{title}</h1>
          {subtitle && <h2 className="truncate">{subtitle}</h2>}
          {details && <h2 className="truncate">{details}</h2>}
        </div>
        {rightComponent && (
          <div className="new-card-accessory">
            {rightComponent({ mode, color })}
          </div>
        )}
      </div>
      {isLocked && (
        <div className="new-card-locked-overlay">
          <span
            className={clsx(
              "material-symbols text-primary-dark",
              mode === NewCardModes.NORMAL ? "text-6xl" : "text-5xl"
            )}
          >
            lock
          </span>
        </div>
      )}
    </div>
  );
}
