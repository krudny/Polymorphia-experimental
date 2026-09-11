import Image from "next/image";
import { API_STATIC_URL } from "@/services/api";
import { ReactNode } from "react";
import "../../index.css";
import { EquipmentChestProps } from "@/components/equipment/components/chest/types";
import ImageBadge from "@/components/image-badge/ImageBadge";
import useEquipmentContext from "@/hooks/contexts/useEquipmentContext";
import { EquipmentActions } from "@/providers/equipment/reducer/types";
import { equipmentVariants } from "@/components/equipment/variants";

export default function EquipmentChest({
  chestData,
  showBadge = false,
  size = "lg",
}: EquipmentChestProps): ReactNode {
  const { dispatch } = useEquipmentContext();
  const { lockedText, badgeContainer } = equipmentVariants({ size });

  const handleClick = () => {
    if (chestData.details.id) {
      if (chestData.details.isUsed) {
        dispatch({
          type: EquipmentActions.SHOW_CHEST_MODAL,
          payload: chestData,
        });
      } else {
        dispatch({
          type: EquipmentActions.SHOW_OPENING_CHEST_MODAL,
          payload: chestData,
        });
      }
    }
  };

  return (
    <div
      className={`equipment-grid-item ${chestData.details.id ? "hover:cursor-pointer hover:shadow-lg" : ""}`}
      onClick={handleClick}
    >
      <Image
        src={`${API_STATIC_URL}/${chestData.base.imageUrl}`}
        alt={chestData.base.name}
        fill
        className="equipment-image"
        sizes="(max-width: 600px) 50vw, (max-width: 1024px) 25vw, 228px"
      />
      {showBadge && chestData.details.id && (
        <ImageBadge
          icon={chestData.details.isUsed ? "trophy" : "key"}
          className={`equipment-image-badge ${badgeContainer()}`}
        />
      )}
      {!chestData.details.id && (
        <div className="equipment-locked-item">
          <span>
            <p className={lockedText()}>lock</p>
          </span>
        </div>
      )}
    </div>
  );
}
