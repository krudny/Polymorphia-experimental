import Image from "next/image";
import { API_STATIC_URL } from "@/services/api";
import ImageBadge from "@/components/image-badge/ImageBadge";
import "../../index.css";
import { EquipmentItemProps } from "@/components/equipment/components/item/types";
import useEquipmentContext from "@/hooks/contexts/useEquipmentContext";
import { EquipmentActions } from "@/providers/equipment/reducer/types";
import { equipmentVariants } from "@/components/equipment/variants";

export default function EquipmentItem({ itemData, size }: EquipmentItemProps) {
  const { dispatch } = useEquipmentContext();
  const { imageUrl, name } = itemData.base;
  const quantity = itemData.details.length;

  const { lockedText, badgeContainer } = equipmentVariants({ size });

  const handleClick = () => {
    if (quantity > 0) {
      dispatch({
        type: EquipmentActions.SHOW_ITEM_MODAL,
        payload: itemData,
      });
    }
  };

  return (
    <div
      className={`equipment-grid-item ${quantity > 0 ? "hover:cursor-pointer hover:shadow-lg" : ""}`}
      onClick={handleClick}
    >
      <Image
        src={`${API_STATIC_URL}/${imageUrl}`}
        alt={name}
        fill
        className="equipment-image"
        sizes="(max-width: 600px) 50vw, (max-width: 1024px) 25vw, 228px"
      />
      {quantity > 0 ? (
        <ImageBadge
          text={quantity.toString()}
          className={`equipment-image-badge ${badgeContainer()}`}
        />
      ) : (
        <div className="equipment-locked-item">
          <span>
            <p className={lockedText()}>lock</p>
          </span>
        </div>
      )}
    </div>
  );
}
