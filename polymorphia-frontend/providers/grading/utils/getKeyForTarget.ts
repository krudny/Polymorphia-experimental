import {
  TargetRequestDTO,
  TargetResponseDTO,
  TargetTypes,
} from "@/interfaces/api/target";

export function getKeyForTarget(
  target: TargetResponseDTO | TargetRequestDTO | null
): string | undefined {
  if (target === null) {
    return undefined;
  } else if (target.type === TargetTypes.STUDENT) {
    return `${target.type}_${target.id}`;
  } else {
    return `${target.type}_${target.groupId}`;
  }
}
