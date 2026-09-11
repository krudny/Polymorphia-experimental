import { useMutation, useQueryClient } from "@tanstack/react-query";
import { CourseService } from "@/services/course";
import toast from "react-hot-toast";
import { UseCourseCreate } from "@/hooks/course/config/useCourseCreate/types";
import { UpdateCourseConfigVariables } from "@/hooks/course/config/useCourseUpdate/types";

export default function useCourseCreate(): UseCourseCreate {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ file }: UpdateCourseConfigVariables) => {
      return toast.promise(CourseService.createCourse(file), {
        loading: "Tworzenie kursu...",
        success: "Kurs utworzony pomyślnie!",
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["availableCourses"] });
    },
  });
}
