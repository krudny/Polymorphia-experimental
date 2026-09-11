import { useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import {
  UseProjectGroupConfigurationUpdate,
  UseProjectGroupConfigurationUpdateParams,
} from "@/hooks/course/projects/useProjectGroupUpdate/types";
import { ProjectService } from "@/services/project";

export default function useProjectGroupUpdate(): UseProjectGroupConfigurationUpdate {
  const { gradableEventId } = useEventParams();
  const queryClient = useQueryClient();

  const mutation = useMutation<
    void,
    Error,
    UseProjectGroupConfigurationUpdateParams
  >({
    mutationFn: async ({ target, configuration }) => {
      return toast.promise(
        ProjectService.submitProjectGroupConfiguration(
          target,
          gradableEventId,
          configuration
        ),
        {
          loading:
            "Trwa " +
            (target !== null ? "aktualizowanie" : "tworzenie") +
            " grupy projektowej...",
          success:
            "Pomyślnie " +
            (target !== null ? "zaktualizowano" : "utworzono") +
            " grupę projektową.",
        }
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["gradingTargets"],
      });
      queryClient.invalidateQueries({
        queryKey: ["grade"],
      });
      queryClient.invalidateQueries({
        queryKey: ["submissionDetails"],
      });
      queryClient.invalidateQueries({
        queryKey: ["projectVariant"],
      });
      queryClient.invalidateQueries({
        queryKey: ["projectGroupConfigurationStudents"],
      });
      queryClient.invalidateQueries({
        queryKey: ["projectGroupConfiguration"],
      });
    },
  });

  return { mutation };
}
