import {
  EquipmentSectionProps,
  EquipmentSectionTypes,
} from "@/views/equipment/section/types";
import {
  EQUIPMENT_COLS,
  EQUIPMENT_MAX_WIDTH,
} from "@/views/equipment/section/util";
import {
  EquipmentChestResponseDTO,
  EquipmentItemResponseDTO,
} from "@/interfaces/api/equipment";
import EquipmentItem from "@/components/equipment/components/item";
import EquipmentChest from "@/components/equipment/components/chest";
import { ChestButtons } from "@/components/equipment/components/chest-buttons";

export function EquipmentSection({
  type,
  data,
  columns,
}: EquipmentSectionProps) {
  return (
    <section className={`${EQUIPMENT_MAX_WIDTH[columns]} mb-5 mx-auto`}>
      <h1 className="equipment-header">
        {type === EquipmentSectionTypes.ITEM ? "Przedmioty" : "Skrzynki"}
      </h1>
      <div className={`equipment-grid ${EQUIPMENT_COLS[columns]}`}>
        {data.map((reward) => {
          if (type === EquipmentSectionTypes.ITEM) {
            const itemData = reward as EquipmentItemResponseDTO;

            return (
              <div key={itemData.base.id}>
                <EquipmentItem itemData={itemData} />
              </div>
            );
          } else {
            const chestData = reward as EquipmentChestResponseDTO;

            return (
              <div key={chestData.base.id}>
                <EquipmentChest chestData={chestData} />
                {chestData.details.id && <ChestButtons chestData={chestData} />}
              </div>
            );
          }
        })}
      </div>
    </section>
  );
}
