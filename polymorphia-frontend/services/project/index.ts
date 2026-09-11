import {
  ProjectCategoryWithVariantsResponseDTO,
  ProjectGroupConfigurationResponseDTO,
  ProjectVariantWithCategoryNameResponseDTO,
} from "@/interfaces/api/project";
import { ApiClient } from "@/services/api/client";
import {
  StudentDetailsDTOWithName,
  StudentDetailsDTOWithType,
} from "@/interfaces/api/user";
import { TargetRequestDTO } from "@/interfaces/api/target";
import { ProjectGroupConfigurationPartialFilterConfig } from "@/providers/project-group-configuration/types";

export const ProjectService = {
  getProjectVariant: async (
    target: TargetRequestDTO,
    gradableEventId: number
  ): Promise<ProjectVariantWithCategoryNameResponseDTO[]> => {
    return ApiClient.post(
      `/projects/variants?projectId=${gradableEventId}`,
      target
    );
  },

  getProjectCategories: async (
    gradableEventId: number
  ): Promise<ProjectCategoryWithVariantsResponseDTO[]> => {
    return ApiClient.get<ProjectCategoryWithVariantsResponseDTO[]>(
      `/projects/variants/categories?projectId=${gradableEventId}`
    );
  },

  getProjectGroup: async (
    studentId: number,
    gradableEventId: number
  ): Promise<StudentDetailsDTOWithType[]> => {
    return await ApiClient.get<StudentDetailsDTOWithType[]>(
      `/projects/group?studentId=${studentId}&projectId=${gradableEventId}`
    );
  },

  getProjectGroupConfiguration: async (
    target: TargetRequestDTO,
    gradableEventId: number
  ): Promise<ProjectGroupConfigurationResponseDTO> => {
    return await ApiClient.post<ProjectGroupConfigurationResponseDTO>(
      `/projects/group/configuration?projectId=${gradableEventId}`,
      target
    );
  },

  getProjectGroupConfigurationFilterConfigs: async (
    target: TargetRequestDTO | undefined,
    gradableEventId: number
  ): Promise<ProjectGroupConfigurationPartialFilterConfig> => {
    return await ApiClient.post<ProjectGroupConfigurationPartialFilterConfig>(
      `/projects/group/configuration/filters?projectId=${gradableEventId}`,
      target
    );
  },

  getProjectGroupConfigurationGroupPickStudents: async (
    target: TargetRequestDTO | null,
    groups: string[],
    gradableEventId: number
  ): Promise<StudentDetailsDTOWithName[]> => {
    return await ApiClient.post<StudentDetailsDTOWithName[]>(
      `/projects/group/students?projectId=${gradableEventId}`,
      {
        target,
        groups,
      }
    );
  },

  submitProjectGroupConfiguration: async (
    target: TargetRequestDTO | null,
    gradableEventId: number,
    configuration: ProjectGroupConfigurationResponseDTO
  ): Promise<void> => {
    return await ApiClient.put(
      `/projects/group/configuration?projectId=${gradableEventId}`,
      {
        target,
        projectGroupUpdate: configuration,
      }
    );
  },

  deleteProjectGroup: async (
    target: TargetRequestDTO,
    gradableEventId: number
  ): Promise<void> => {
    return await ApiClient.delete(
      `/projects/group?projectId=${gradableEventId}`,
      target
    );
  },

  getRandomProjectVariant: async (
    target: TargetRequestDTO | undefined,
    gradableEventId: number
  ): Promise<Record<number, number>> => {
    return await ApiClient.post<Record<number, number>>(
      `/projects/variants/suggestions?projectId=${gradableEventId}`,
      target
    );
  },
};
