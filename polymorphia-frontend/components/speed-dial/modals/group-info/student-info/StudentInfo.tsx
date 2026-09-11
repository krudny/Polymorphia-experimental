import Loading from "@/components/loading";
import XPCard from "@/components/xp-card/XPCard";
import XPCardImage from "@/components/xp-card/components/XPCardImage";
import ErrorComponent from "@/components/error";
import "./index.css";
import { useProjectGroup } from "@/hooks/course/projects/useProjectGroup";

export default function StudentInfo() {
  const { data: projectGroupStudents, isLoading, isError } = useProjectGroup();

  if (isLoading) {
    return (
      <div className="gradable-event-section h-50 relative">
        <Loading />
      </div>
    );
  }
  if (isError || !projectGroupStudents) {
    return (
      <ErrorComponent message="Nie udało się załadować informacji o składzie grupy." />
    );
  }

  if (projectGroupStudents.length === 0) {
    return <ErrorComponent message="Nie należysz do żadnej grupy." />;
  }

  return (
    <div className="student-info-scrollable">
      <div className="gradable-event-section">
        {projectGroupStudents.map((student) => {
          const { animalName, fullName, evolutionStage, imageUrl } =
            student.userDetails;

          if (!fullName) {
            throw new Error("No userName defined!");
          }

          return (
            <XPCard
              key={animalName}
              title={fullName}
              subtitle={evolutionStage}
              leftComponent={
                <XPCardImage imageUrl={imageUrl} alt={evolutionStage} />
              }
              size="xs"
            />
          );
        })}
      </div>
    </div>
  );
}
