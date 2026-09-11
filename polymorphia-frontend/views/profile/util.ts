import { Sizes } from "@/interfaces/general";
import { UserPointsProps } from "@/views/profile/types";

export function getUserPointsProps(
  isMd: boolean,
  isXl: boolean
): UserPointsProps {
  if (isXl) {
    return {
      className: "profile-user-points-xl",
      titleSize: Sizes.MD,
      xpSize: Sizes.LG,
    };
  }

  if (isMd) {
    return {
      className: "profile-user-points-md",
      titleSize: Sizes.SM,
      xpSize: Sizes.MD,
    };
  }

  return {
    className: "profile-user-points-xs",
    titleSize: Sizes.SM,
    xpSize: Sizes.MD,
  };
}
