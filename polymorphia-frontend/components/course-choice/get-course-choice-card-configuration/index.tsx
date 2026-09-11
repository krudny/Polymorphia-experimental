import { GetCourseChoiceCardConfigurationProps } from "@/components/course-choice/get-course-choice-card-configuration/types";
import { NewCardProps } from "@/components/new-card/card/types";
import useHasValidAnimalInCourse from "@/hooks/course/animal/useHasValidAnimalInCourse";
import useCourseGroup from "@/hooks/course/course-group/useCourseGroup";
import { Roles, RoleTextMap } from "@/interfaces/api/user";
import toast from "react-hot-toast";
import NewCardImageAccessory from "@/components/new-card/card/accessory/image";

export default function getCourseChoiceCardConfiguration({
  availableCourse,
  currentCourseId,
  handleCourseSelection,
  setClickedDetails,
}: GetCourseChoiceCardConfigurationProps): Omit<NewCardProps, "mode"> {
  const { id, name, coordinatorName, imageUrl, userRole } = availableCourse;

  return {
    title: name,
    subtitle: `Koordynator: ${coordinatorName}`,
    details: `Twoja rola: ${RoleTextMap[userRole]}`,
    color:
      currentCourseId != null && currentCourseId === id ? "green" : "silver",
    useDynamicBehavior: () => {
      const { data: animal } = useHasValidAnimalInCourse(id);
      const { data: courseGroup } = useCourseGroup(
        id,
        userRole === Roles.STUDENT
      );

      return {
        onClick: () => {
          switch (userRole) {
            case Roles.COORDINATOR:
            case Roles.INSTRUCTOR:
              handleCourseSelection(id);
              break;
            case Roles.STUDENT:
              if (!courseGroup) {
                toast.error("Student nie został przypisany do żadnej grupy");
              } else if (!animal && courseGroup) {
                setClickedDetails({
                  courseId: id,
                  courseGroupId: courseGroup.courseGroupId,
                });
              } else {
                handleCourseSelection(id);
              }
              break;
            default:
              toast.error("Coś poszło nie tak!");
              break;
          }
        },
      };
    },
    leftComponent: () => (
      <NewCardImageAccessory imageUrl={imageUrl} alt={name} />
    ),
    sizeBonus: 3,
  };
}
