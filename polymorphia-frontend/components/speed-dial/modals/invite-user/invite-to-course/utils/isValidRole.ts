import { Role, Roles } from "@/interfaces/api/user";

export function isValidRole(value: string): value is Role {
  return Object.values(Roles).includes(value as Role);
}
