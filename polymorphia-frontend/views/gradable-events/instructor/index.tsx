import { useScaleShow } from "@/animations/ScaleShow";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Loading from "@/components/loading";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import useInstructorGradableEvents from "@/hooks/course/gradable-event/useInstructorGradableEvents";
import "./index.css";
import ErrorComponent from "@/components/error";
import { GradableEventDTO } from "@/interfaces/api/gradable_event/types";
import NewCardTextAccessory from "@/components/new-card/card/accessory/text";
import NewCardGridView from "@/components/new-card/grid";

export default function InstructorView() {
  const { eventType, eventSectionId } = useEventParams();
  const router = useRouter();
  const {
    data: gradableEvents,
    isLoading,
    isError,
  } = useInstructorGradableEvents();

  const containerRef = useScaleShow(!isLoading);

  useEffect(() => {
    if (!isLoading && gradableEvents && gradableEvents.length === 1) {
      router.replace(
        `/course/${eventType.toLowerCase()}/${eventSectionId}/${gradableEvents[0].id}/grading`
      );
    }
  }, [isLoading, gradableEvents, eventType, eventSectionId, router]);

  if (isLoading || (gradableEvents && gradableEvents.length < 2)) {
    return <Loading />;
  }

  if (isError || !gradableEvents) {
    return <ErrorComponent message="Nie udało się załadować wydarzeń." />;
  }

  if (gradableEvents.length === 0) {
    return (
      <ErrorComponent
        title="Brak wydarzeń"
        message="W tej sekcji nie ma żadnych wydarzeń."
      />
    );
  }

  const handleClick = (gradableEvent: GradableEventDTO) => {
    router.push(
      `/course/${eventType.toLowerCase()}/${eventSectionId}/${gradableEvent.id}/grading`
    );
  };

  return (
    <NewCardGridView
      ref={containerRef}
      cardConfigurations={gradableEvents.map((gradableEvent) => ({
        title: gradableEvent.name,
        subtitle: gradableEvent.topic,
        rightComponent: () => (
          <NewCardTextAccessory
            topText={gradableEvent.ungradedStudents.toString()}
            bottomText="Nieocenionych"
            backgroundColor="gray"
          />
        ),
        color: gradableEvent.ungradedStudents === 0 ? "green" : "sky",
        onClick: () => handleClick(gradableEvent),
      }))}
      usesPointsSummary={false}
    />
  );
}
