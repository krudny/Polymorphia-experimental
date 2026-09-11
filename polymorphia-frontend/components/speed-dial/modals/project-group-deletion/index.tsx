import { ProjectGroupConfigurationModalProps } from "@/components/speed-dial/modals/project-group-configuration/types";
import { ReactNode } from "react";
import Modal from "@/components/modal";
import ButtonWithBorder from "@/components/button";
import useModalContext from "@/hooks/contexts/useModalContext";
import useProjectGroupDelete from "@/hooks/course/projects/useProjectGroupDelete";
import { ProjectGroupDeletionModalContentProps } from "@/components/speed-dial/modals/project-group-deletion/types";
import "./index.css";

function ProjectGroupDeletionModalContent({
  initialTarget,
}: ProjectGroupDeletionModalContentProps) {
  const { closeModal } = useModalContext();
  const { mutation } = useProjectGroupDelete();

  const handleSubmit = () => {
    if (initialTarget !== null) {
      mutation.mutate(
        {
          target: initialTarget,
        },
        {
          onSuccess: closeModal,
        }
      );
    }
  };

  return (
    <div className="project-group-deletion">
      <h1>Czy na pewno chcesz usunąć aktualnie wybraną grupę projektową?</h1>
      <div className="project-group-deletion-buttons">
        <ButtonWithBorder
          text="Anuluj"
          className="!mx-0 !py-0 !w-full"
          onClick={closeModal}
        />
        <ButtonWithBorder
          text="Potwierdź"
          className="!mx-0 !py-0 !w-full"
          isActive={!mutation.isPending}
          onClick={handleSubmit}
        />
      </div>
    </div>
  );
}

export default function ProjectGroupDeletionModal({
  onClosedAction,
  initialTarget,
}: ProjectGroupConfigurationModalProps): ReactNode {
  return (
    <Modal
      isDataPresented={true}
      onClosed={onClosedAction}
      title="Usunięcie grupy"
    >
      <ProjectGroupDeletionModalContent initialTarget={initialTarget} />
    </Modal>
  );
}
