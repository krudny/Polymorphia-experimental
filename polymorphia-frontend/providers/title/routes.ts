import useEventSectionTitle from "@/hooks/app/title/useEventSectionTitle";
import { TitleRule } from "@/providers/title/types";
import useCourseGroupTitle from "@/hooks/app/title/useCourseGroupTitle";
import useGradableEventTitle from "@/hooks/app/title/useGradableEventTitle";

export const GENERAL_APPLICATION_ROUTES: TitleRule[] = [
  { pattern: /^\/$/, useTitleHook: () => "" },
  { pattern: /^\/welcome$/, useTitleHook: () => "" },
  { pattern: /^\/course-choice$/, useTitleHook: () => "" },
];

export const LOGGED_IN_APPLICATION_ROUTES: TitleRule[] = [
  { pattern: /^\/settings$/, useTitleHook: () => "Ustawienia" },
  { pattern: /^\/roadmap$/, useTitleHook: () => "Roadmapa" },
  { pattern: /^\/tasks$/, useTitleHook: () => "Zadania" },
  { pattern: /^\/profile$/, useTitleHook: () => "Profil" },
  { pattern: /^\/knowledge-base\/rules$/, useTitleHook: () => "Zasady" },
  {
    pattern: /^\/knowledge-base\/evolution-stages$/,
    useTitleHook: () => "Postacie",
  },
  { pattern: /^\/knowledge-base\/items$/, useTitleHook: () => "Przedmioty" },
  { pattern: /^\/knowledge-base\/chests$/, useTitleHook: () => "Skrzynki" },
  {
    pattern: /^\/hall-of-fame$/,
    useTitleHook: () => "Hall of Fame",
  },
  { pattern: /^\/equipment$/, useTitleHook: () => "Ekwipunek" },
  {
    pattern: /^\/course\/groups$/,
    useTitleHook: () => "Grupy zajęciowe",
  },
  {
    pattern: /^\/course\/groups\/(\d+)$/,
    useTitleHook: useCourseGroupTitle,
  },
  {
    pattern: /^\/course\/([a-zA-Z-]+)\/(\d+)$/,
    useTitleHook: useEventSectionTitle,
  },
  {
    pattern: /^\/course\/([a-zA-Z-]+)\/(\d+)\/(\d+)(?:\/grading)?$/,
    useTitleHook: useGradableEventTitle,
  },
];
