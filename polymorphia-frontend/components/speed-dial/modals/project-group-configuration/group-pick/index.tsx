import ButtonWithBorder from "@/components/button";
import ErrorComponent from "@/components/error";
import { ErrorComponentSizes } from "@/components/error/types";
import Loading from "@/components/loading";
import XPCardImage from "@/components/xp-card/components/XPCardImage";
import XPCard from "@/components/xp-card/XPCard";
import useProjectGroupConfigurationContext from "@/hooks/contexts/useProjectGroupConfigurationContext";
import { useProjectGroupConfigurationGroupPickStudents } from "@/hooks/course/projects/useProjectGroupConfigurationGroupPickStudents";
import { ProjectGroupConfigurationSteps } from "@/providers/project-group-configuration/types";
import { useEffect, useMemo, useState } from "react";
import { useDebounce } from "use-debounce";
import "./index.css";
import Search from "@/components/search";
import { useMediaQuery } from "react-responsive";

export default function ProjectGroupPick() {
  const {
    initialTarget,
    currentProjectGroupConfiguration,
    setCurrentProjectGroupConfiguration,
    setCurrentStep,
    filters,
    setAreFiltersOpen,
  } = useProjectGroupConfigurationContext();
  const [search, setSearch] = useState("");
  const [debouncedSearch] = useDebounce(search, 300);

  const isMd = useMediaQuery({ minWidth: 768 });
  const isLg = useMediaQuery({ minWidth: 1024 });
  const buttonVariant = isLg ? "md" : isMd ? "base" : "sm";

  const groups = useMemo(
    () => filters.getAppliedFilterValues("groups"),
    [filters]
  );

  const {
    data: studentsForGroupPick,
    isLoading: isStudentsForGroupPickLoading,
    isError: isStudentsForGroupPickError,
  } = useProjectGroupConfigurationGroupPickStudents({
    target: initialTarget,
    groups,
  });

  // Update selection based on groups
  useEffect(() => {
    if (
      !studentsForGroupPick ||
      isStudentsForGroupPickLoading ||
      isStudentsForGroupPickError
    ) {
      return;
    }

    setCurrentProjectGroupConfiguration((previousConfiguration) => ({
      ...previousConfiguration,
      studentIds: previousConfiguration.studentIds.filter((studentId) =>
        studentsForGroupPick.some(({ id }) => studentId === id)
      ),
    }));
  }, [
    isStudentsForGroupPickError,
    isStudentsForGroupPickLoading,
    setCurrentProjectGroupConfiguration,
    studentsForGroupPick,
  ]);

  const handleStudentSelection = (selectedStudentId: number) => {
    setCurrentProjectGroupConfiguration((previousConfiguration) => ({
      ...previousConfiguration,
      studentIds: previousConfiguration.studentIds.some(
        (studentId) => selectedStudentId === studentId
      )
        ? previousConfiguration.studentIds
        : [...previousConfiguration.studentIds, selectedStudentId],
    }));
  };

  const handleStudentDeselection = (deselectedStudentId: number) => {
    setCurrentProjectGroupConfiguration((previousConfiguration) => ({
      ...previousConfiguration,
      studentIds: previousConfiguration.studentIds.filter(
        (studentId) => studentId !== deselectedStudentId
      ),
    }));
  };

  const handleSubmit = () => {
    if (selectedStudents.length === 0) {
      return;
    }

    setCurrentStep(ProjectGroupConfigurationSteps.VARIANT_PICK);
  };

  if (isStudentsForGroupPickLoading) {
    return (
      <div className="group-pick-loading">
        <Loading />
      </div>
    );
  } else if (
    isStudentsForGroupPickError ||
    studentsForGroupPick === undefined
  ) {
    return (
      <ErrorComponent
        message="Nie udało się załadować studentów."
        size={ErrorComponentSizes.COMPACT}
      />
    );
  }

  let selectableStudents = studentsForGroupPick.filter(({ id }) =>
    currentProjectGroupConfiguration.studentIds.every(
      (studentId) => id !== studentId
    )
  );

  if (debouncedSearch !== "") {
    selectableStudents = selectableStudents.filter(
      ({ fullName }) =>
        fullName.toLowerCase().indexOf(debouncedSearch.toLowerCase()) !== -1
    );
  }

  const selectedStudents = studentsForGroupPick.filter(({ id }) =>
    currentProjectGroupConfiguration.studentIds.some(
      (studentId) => id === studentId
    )
  );

  return (
    <div className="group-pick">
      <div
        className="group-pick-body"
        onSubmit={(event) => event.preventDefault()}
      >
        <div className="group-pick-header">
          <Search
            search={search}
            setSearch={setSearch}
            placeholder="Znajdź studenta..."
          />
          <ButtonWithBorder
            text="Filtry"
            className="group-pick-filter-btn"
            size={buttonVariant}
            onClick={() => setAreFiltersOpen(true)}
          />
        </div>

        <div className="group-pick-list">
          {selectableStudents.length > 0 ? (
            selectableStudents.map((student) => (
              <div key={student.id} className="group-pick-card-wrapper">
                <XPCard
                  title={student.fullName}
                  subtitle={`${student.group} | ${student.evolutionStage}`}
                  leftComponent={
                    <XPCardImage
                      imageUrl={student.imageUrl}
                      alt={student.evolutionStage}
                    />
                  }
                  size="xs"
                  onClick={() => handleStudentSelection(student.id)}
                />
              </div>
            ))
          ) : (
            <ErrorComponent
              title="Brak studentów"
              message="Brak studentów możliwych do przypisania."
              size={ErrorComponentSizes.COMPACT}
            />
          )}
        </div>

        <h3 className="group-pick-title">Skład grupy</h3>

        <div className="group-pick-list">
          {selectedStudents.length > 0 ? (
            selectedStudents.map((student) => (
              <div key={student.id} className="group-pick-card-wrapper">
                <XPCard
                  title={student.fullName}
                  subtitle={`${student.group} | ${student.evolutionStage}`}
                  leftComponent={
                    <XPCardImage
                      imageUrl={student.imageUrl}
                      alt={student.evolutionStage}
                    />
                  }
                  size="xs"
                  onClick={() => handleStudentDeselection(student.id)}
                />
              </div>
            ))
          ) : (
            <ErrorComponent
              title="Pusta grupa"
              message="Nie wybrano jeszcze żadnych studentów."
              size={ErrorComponentSizes.COMPACT}
            />
          )}
        </div>
      </div>

      <div className="group-pick-footer">
        <ButtonWithBorder
          text="Dalej"
          className="!mx-0 !py-0 !w-full"
          isActive={selectedStudents.length > 0}
          onClick={handleSubmit}
        />
      </div>
    </div>
  );
}
