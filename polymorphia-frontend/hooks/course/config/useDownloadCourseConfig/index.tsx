import { useMutation } from "@tanstack/react-query";
import { CourseService } from "@/services/course";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import toast from "react-hot-toast";
import { UseCourseDownload } from "@/hooks/course/config/useDownloadCourseConfig/types";

export default function useDownloadCourseConfig(): UseCourseDownload {
  const { courseId } = useUserDetails();

  return useMutation<void, Error, void>({
    mutationFn: () => {
      return toast.promise(CourseService.downloadCourseConfig(courseId), {
        loading: "Generowanie konfiguracji...",
        success: "Konfiguracja pobrana pomyślnie!",
      });
    },
  });
}
