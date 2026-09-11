import { useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import CourseGroupsService from "@/services/course-groups";
import { CourseGroupTypes } from "@/services/course-groups/types";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import useModalContext from "@/hooks/contexts/useModalContext";
import { UseDeleteCourseGroup } from "@/hooks/course/course-group/useDeleteCourseGroup/types";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { useRouter } from "next/navigation";

export default function useDeleteCourseGroup(): UseDeleteCourseGroup {
  const queryClient = useQueryClient();
  const { courseId } = useUserDetails();
  const { courseGroupId } = useEventParams();
  const { closeModal } = useModalContext();
  const router = useRouter();

  const mutation = useMutation<void, Error, void>({
    mutationFn: () => {
      if (!courseGroupId) {
        throw new Error("Nie znaleziono ID grupy zajęciowej w parametrach.");
      }

      return toast.promise(
        CourseGroupsService.deleteCourseGroup(courseGroupId),
        {
          loading: "Usuwanie...",
          success: "Usunięto grupę zajęciową!",
        }
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["courseGroups", courseId, CourseGroupTypes.INDIVIDUAL_FULL],
      });
      queryClient.removeQueries({
        queryKey: ["courseGroupTargets"],
      });
      queryClient.invalidateQueries({
        queryKey: ["gradingTargets"],
      });
      closeModal();
      router.push(`/course/groups`);
    },
  });

  return { mutation };
}
