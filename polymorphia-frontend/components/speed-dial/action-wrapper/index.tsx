import { SpeedDialAction } from "@mui/material";
import { SpeedDialActionWrapperProps } from "@/components/speed-dial/action-wrapper/types";

export default function SpeedDialActionWrapper({
  item,
  setActiveModal,
  slotProps: injectedSlotProps,
  ...props
}: SpeedDialActionWrapperProps) {
  const { onClick, modal, shouldBeRendered = true } = item.useDynamicBehavior();

  if (!shouldBeRendered) {
    return null;
  }

  // SpeedDial injects critical slotProps (`fab.ref`, `tooltip.placement`) via cloneElement.
  // A shallow merge on `slotProps` would overwrite MUI's internal props, so we extract and merge them per slot.
  const injected =
    typeof injectedSlotProps === "function" ? undefined : injectedSlotProps;

  // Type guard: extract object-based slotProps only (functions are not injected by SpeedDial).
  const injectedFab =
    typeof injected?.fab === "function" ? undefined : injected?.fab;
  const injectedTooltip =
    typeof injected?.tooltip === "function" ? undefined : injected?.tooltip;

  return (
    <SpeedDialAction
      {...props}
      icon={<span className="material-symbols">{item.icon}</span>}
      slotProps={{
        ...injected,
        tooltip: {
          ...injectedTooltip,
          title: item.label,
          placement: "left",
          slotProps: {
            transition: { timeout: 150 },
            popper: {
              modifiers: [
                {
                  name: "computeStyles",
                  options: { roundOffsets: false, gpuAcceleration: false },
                },
                { name: "offset", options: { offset: [0, 3] } },
              ],
            },
          },
        },
        fab: {
          ...injectedFab,
          style: {
            ...injectedFab?.style,
            backgroundColor: item.color ?? "#262626",
            color: "#d4d4d4",
            borderRadius: 8,
            width: 56,
            height: 56,
            fontSize: 28,
          },
        },
      }}
      onClick={() => {
        if (modal) {
          setActiveModal(modal(() => setActiveModal(null)));
        } else if (onClick) {
          onClick();
        }
      }}
    />
  );
}
