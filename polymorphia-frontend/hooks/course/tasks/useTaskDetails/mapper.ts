import {
  SupportedLanguages,
  type SupportedLanguage,
  type TaskDetailsResponseDTO,
} from "@/interfaces/api/tasks/types";
import type { LanguageInfo, TaskDetailsView } from "@/providers/task/types";

export const mapBackendToSupportedLanguage = (
  backendLanguage: string
): SupportedLanguage => {
  switch (backendLanguage.trim().toUpperCase()) {
    case "JAVASCRIPT":
    case "JS":
      return SupportedLanguages.JAVASCRIPT;
    case "PYTHON":
    case "PY":
      return SupportedLanguages.PYTHON;
    case "JAVA":
      return SupportedLanguages.JAVA;
    case "CPP":
    case "C++":
      return SupportedLanguages.CPP;
    case "C":
      return SupportedLanguages.C;
    case "CSHARP":
    case "C_SHARP":
    case "C#":
      return SupportedLanguages.CSHARP;
    default:
      return SupportedLanguages.PLAINTEXT;
  }
};

export const mapTaskDetailsResponse = (
  data: TaskDetailsResponseDTO
): TaskDetailsResponseDTO => ({
  ...data,
  allowedLanguages: data.allowedLanguages.map((allowedLanguage) => ({
    ...allowedLanguage,
    taskLanguage: mapBackendToSupportedLanguage(allowedLanguage.taskLanguage),
  })),
});

export const toTaskDetailsView = (
  dto: TaskDetailsResponseDTO
): TaskDetailsView => {
  const languages = new Map<SupportedLanguage, LanguageInfo>(
    dto.allowedLanguages.map((allowedLanguage) => {
      const normalizedLanguage = mapBackendToSupportedLanguage(
        allowedLanguage.taskLanguage
      );
      return [
        normalizedLanguage,
        {
          language: normalizedLanguage,
          sampleCode: allowedLanguage.sampleCode,
          isDefault: allowedLanguage.isDefault,
        },
      ];
    })
  );

  const defaultAllowedLanguage =
    dto.allowedLanguages.find((allowedLanguage) => allowedLanguage.isDefault) ??
    dto.allowedLanguages[0];

  const defaultLanguage = mapBackendToSupportedLanguage(
    defaultAllowedLanguage.taskLanguage
  );

  return {
    testCases: dto.testCases,
    languages,
    defaultLanguage,
  };
};
