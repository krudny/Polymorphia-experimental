import { TargetRequestDTO } from "@/interfaces/api/target";
import { StudentDetailsDTOWithName } from "@/interfaces/api/user";

export interface UseProjectGroupConfigurationGroupPickStudentsProps {
  target: TargetRequestDTO | null;
  groups: string[];
}

export interface UseProjectGroupConfigurationGroupPickStudents {
  data: StudentDetailsDTOWithName[] | undefined;
  isLoading: boolean;
  isError: boolean;
}
