export interface ButtonWithBorderProps {
  text?: string;
  className?: string;
  onClick?: () => void;
  icon?: string;
  type?: "button" | "submit" | "reset";
}

export type VariantProps = {
  size?: "sm" | "base" | "md" | "lg";
  isActive?: boolean;
  forceDark?: boolean;
  forceLight?: boolean;
};
