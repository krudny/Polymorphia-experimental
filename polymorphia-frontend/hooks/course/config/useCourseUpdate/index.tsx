import { useMutation, useQueryClient } from "@tanstack/react-query";
import { CourseService } from "@/services/course";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import toast from "react-hot-toast";
import {
  UpdateCourseConfigVariables,
  UseCourseUpdate,
} from "@/hooks/course/config/useCourseUpdate/types";

export default function useCourseUpdate(): UseCourseUpdate {
  const { courseId } = useUserDetails();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ file }: UpdateCourseConfigVariables) => {
      return toast.promise(CourseService.updateCourseConfig(courseId, file), {
        loading: "Aktualizowanie kursu...",
        success: "Konfiguracja zaktualizowana pomyślnie!",
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["availableCourses"] });
    },
  });
}
