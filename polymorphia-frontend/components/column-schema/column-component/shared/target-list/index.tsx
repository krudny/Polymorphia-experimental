import XPCard from "@/components/xp-card/XPCard";
import XPCardImage from "@/components/xp-card/components/XPCardImage";
import XPCardPoints from "@/components/xp-card/components/XPCardPoints";
import "./index.css";
import { Fragment } from "react";
import useTargetContext from "@/hooks/contexts/useTargetContext";
import { TargetTypes } from "@/interfaces/api/target";
import TargetListTopBar from "@/components/column-schema/column-component/shared/target-list/top-bar";
import Loading from "@/components/loading";
import ColumnComponent from "@/components/column-schema/column-component";
import { isTargetSelected } from "@/providers/target/utils/is-selected";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { getTargetListErrorComponent } from "@/components/column-schema/column-component/shared/target-list/utils/get-target-list-error-component";
import { getKeyForTarget } from "@/providers/grading/utils/getKeyForTarget";

export default function TargetList() {
  const {
    targets,
    isTargetsLoading,
    onTargetSelect,
    selectedTarget,
    appliedFilters,
  } = useTargetContext();
  const { eventType } = useEventParams();

  const topComponent = () => <TargetListTopBar />;

  const mainComponent =
    isTargetsLoading || !targets
      ? () => (
          <div className="target-list-loading">
            <Loading />
          </div>
        )
      : () => (
          <div className="group-list custom-scrollbar">
            {targets.length === 0 && getTargetListErrorComponent(eventType)}
            {targets.map((target) => (
              <Fragment key={getKeyForTarget(target)}>
                <div className="group-record">
                  {(target.type === TargetTypes.STUDENT
                    ? [target.student]
                    : target.members
                  ).map((student, studentIndex) => {
                    const isSelected = isTargetSelected(
                      target,
                      student,
                      selectedTarget
                    );
                    const color = isSelected
                      ? student.gainedXp != null
                        ? "green"
                        : "sky"
                      : "gray";

                    const handleSelection = () => {
                      onTargetSelect(target, student);
                    };

                    return (
                      <XPCard
                        key={studentIndex}
                        title={
                          appliedFilters["searchBy"][0] === "animalName"
                            ? student.animalName
                            : student.fullName
                        }
                        color={color}
                        subtitle={student.group}
                        size="xs"
                        leftComponent={
                          <XPCardImage
                            imageUrl={student.imageUrl}
                            alt={student.evolutionStage}
                          />
                        }
                        rightComponent={
                          <XPCardPoints
                            points={student.gainedXp}
                            color={color}
                            isSumLabelVisible={true}
                            isXPLabelVisible={student.gainedXp != null}
                          />
                        }
                        onClick={handleSelection}
                      />
                    );
                  })}
                </div>
                {target.type === TargetTypes.STUDENT_GROUP && (
                  <div className="divider"></div>
                )}
              </Fragment>
            ))}
          </div>
        );

  return (
    <ColumnComponent
      topComponent={topComponent}
      mainComponent={mainComponent}
    />
  );
}
