import {
  SupportedLanguages,
  type SupportedLanguage,
} from "@/interfaces/api/tasks/types";

export const mapSupportedLanguageToMonaco = (
  language: SupportedLanguage
): string => {
  switch (language) {
    case SupportedLanguages.JAVASCRIPT:
      return "javascript";
    case SupportedLanguages.PYTHON:
      return "python";
    case SupportedLanguages.JAVA:
      return "java";
    case SupportedLanguages.CPP:
      return "cpp";
    case SupportedLanguages.C:
      return "c";
    case SupportedLanguages.CSHARP:
      return "csharp";
    case SupportedLanguages.PLAINTEXT:
    default:
      return "plaintext";
  }
};
