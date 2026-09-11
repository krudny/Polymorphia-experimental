import { ProjectGroupConfigurationContext } from "@/providers/project-group-configuration";
import { useContext } from "react";

export default function useProjectGroupConfigurationContext() {
  const context = useContext(ProjectGroupConfigurationContext);

  if (!context) {
    throw new Error(
      "useProjectGroupConfigurationContext must be used within ProjectGroupConfigurationProvider"
    );
  }

  return context;
}
