import { useQuery } from "@tanstack/react-query";
import { UseTeachingRole } from "@/hooks/course/course-group/useTeachingRole/types";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import { CourseService } from "@/services/course";

export default function useTeachingRole(): UseTeachingRole {
  const { courseId } = useUserDetails();

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ["teachingRole", courseId],
    queryFn: () => CourseService.getTeachingRoleUsers(courseId),
  });

  return { data, isLoading, isError, refetch };
}
