"use client";

import { useState } from "react";
import { InviteUserModalProps } from "@/components/speed-dial/modals/invite-user/types";
import ImportCSVModal from "@/components/speed-dial/modals/file-import/import-csv";
import {
  ImportCSVTypes,
  InviteSpecificType,
  InviteSpecificTypes,
  InviteTypes,
} from "@/interfaces/general";
import ButtonWithBorder from "@/components/button";
import "./index.css";
import InviteUserToCourseModal from "@/components/speed-dial/modals/invite-user/invite-to-course";
import Modal from "@/components/modal";

export default function InviteUserModal({
  onClosedAction,
  inviteType,
}: InviteUserModalProps) {
  const [activeModal, setActiveModal] = useState<InviteSpecificType | null>(
    null
  );

  const handleManualClick = () => {
    switch (inviteType) {
      case InviteTypes.COURSE:
        setActiveModal(InviteSpecificTypes.COURSE_MANUAL);
        break;
      case InviteTypes.GROUP:
        setActiveModal(InviteSpecificTypes.GROUP_MANUAL);
        break;
    }
  };

  const handleCSVClick = () => {
    switch (inviteType) {
      case InviteTypes.COURSE:
        setActiveModal(InviteSpecificTypes.COURSE_CSV);
        break;
      case InviteTypes.GROUP:
        setActiveModal(InviteSpecificTypes.GROUP_CSV);
        break;
    }
  };

  switch (activeModal) {
    case InviteSpecificTypes.COURSE_MANUAL:
      return <InviteUserToCourseModal onClosedAction={onClosedAction} />;

    case InviteSpecificTypes.GROUP_MANUAL:
      return (
        <Modal
          isDataPresented={true}
          onClosed={onClosedAction}
          title="Zaproś użytkownika do grupy"
        >
          <div className="text-2xl my-4">
            Ta funkcjonalność nie została jeszcze zaimplementowana.
          </div>
        </Modal>
      );

    case InviteSpecificTypes.COURSE_CSV:
      return (
        <ImportCSVModal
          onClosedAction={onClosedAction}
          importType={ImportCSVTypes.STUDENT_INVITE}
        />
      );

    case InviteSpecificTypes.GROUP_CSV:
      return (
        <ImportCSVModal
          onClosedAction={onClosedAction}
          importType={ImportCSVTypes.GROUP_INVITE}
        />
      );

    default:
      return (
        <Modal
          isDataPresented={true}
          onClosed={onClosedAction}
          title="Zaproś użytkownika"
        >
          <div className="invite-user-wrapper">
            <ButtonWithBorder
              text="Manualnie"
              onClick={handleManualClick}
              className="!w-full"
            />
            <ButtonWithBorder
              text="Z CSV"
              onClick={handleCSVClick}
              className="!w-full"
            />
          </div>
        </Modal>
      );
  }
}
