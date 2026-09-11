"use client";

import useCourseGroups from "@/hooks/course/course-group/useCourseGroups";
import Loading from "@/components/loading";
import "./index.css";
import { useScaleShow } from "@/animations/ScaleShow";
import { useRouter } from "next/navigation";
import useUserContext, {
  useUserDetails,
} from "@/hooks/contexts/useUserContext";
import { CourseGroupTypes } from "@/services/course-groups/types";
import ErrorComponent from "@/components/error";
import { Roles } from "@/interfaces/api/user";
import { SpeedDialKeys } from "@/components/speed-dial/types";
import { LazySpeedDial } from "@/components/speed-dial/lazy";
import NewCardGridView from "@/components/new-card/grid";
import NewCardTextAccessory from "@/components/new-card/card/accessory/text";

export default function CourseGroupsPage() {
  const router = useRouter();
  const { courseId } = useUserDetails();
  const { userRole } = useUserContext();
  const {
    data: courseGroups,
    isLoading,
    isError,
  } = useCourseGroups({ courseId, type: CourseGroupTypes.INDIVIDUAL_FULL });

  const containerRef = useScaleShow(!isLoading);
  const speedDialRef = useScaleShow(!isLoading);

  if (isLoading || !userRole) {
    return <Loading />;
  }

  if (isError || !courseGroups || userRole === Roles.STUDENT) {
    return (
      <ErrorComponent message="Nie udało się załadować grup zajęciowych." />
    );
  }

  const handleClick = (id: number) => {
    router.push(`/course/groups/${id}`);
  };

  return (
    <>
      {courseGroups.length > 0 ? (
        <NewCardGridView
          ref={containerRef}
          cardConfigurations={courseGroups.map((courseGroup) => ({
            title: courseGroup.name.toUpperCase(),
            subtitle: courseGroup.room,
            color: "sky",
            rightComponent: () => (
              <NewCardTextAccessory
                topText={courseGroup.studentCount.toString()}
                bottomText="Studentów"
                backgroundColor="gray"
              />
            ),
            onClick: () => handleClick(courseGroup.id),
          }))}
          usesPointsSummary={false}
        />
      ) : (
        <ErrorComponent
          title="Brak grup"
          message="W tym kursie nie istnieją jeszcze żadne grupy."
        />
      )}
      {userRole === Roles.COORDINATOR && (
        <div ref={speedDialRef}>
          <LazySpeedDial speedDialKey={SpeedDialKeys.COURSE_GROUP_GRID} />
        </div>
      )}
    </>
  );
}
