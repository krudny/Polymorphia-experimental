import Modal from "@/components/modal";
import { SpeedDialModalProps } from "@/components/speed-dial/modals/types";
import StudentInfo from "@/components/speed-dial/modals/group-info/student-info/StudentInfo";

export default function GroupModal({ onClosedAction }: SpeedDialModalProps) {
  return (
    <Modal
      isDataPresented={true}
      onClosed={onClosedAction}
      title="Grupa"
      subtitle="Oto skład twojej grupy:"
    >
      <StudentInfo />
    </Modal>
  );
}
