import Modal from "@/components/modal";
import UploadCourseConfig from "@/components/speed-dial/modals/file-import/import-course/upload";
import { UseUploadCourseModalProps } from "@/components/speed-dial/modals/file-import/import-course/types";

export const UploadCourseModal = ({
  onClosedAction,
  courseFileAction,
}: UseUploadCourseModalProps) => {
  return (
    <Modal isDataPresented={true} onClosed={onClosedAction} title="Import JSON">
      <UploadCourseConfig courseFileAction={courseFileAction} />
    </Modal>
  );
};
