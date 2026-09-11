"use client";
import { SpeedDialModalProps } from "@/components/speed-dial/modals/types";
import Modal from "@/components/modal";
import "./index.css";
import { ReactNode } from "react";
import { ImportCSVProvider } from "@/providers/import-csv";
import UploadCSV from "@/components/speed-dial/modals/file-import/import-csv/upload";
import useImportCSVContext from "@/hooks/contexts/useImportCSVContext";
import PickCSVHeaders from "@/components/speed-dial/modals/file-import/import-csv/pick-headers";
import PreviewCSV from "@/components/speed-dial/modals/file-import/import-csv/preview";
import {
  ImportCSVModalProps,
  ImportCSVModalResult,
} from "@/components/speed-dial/modals/file-import/import-csv/types";

const ImportCSVModalContent = ({ onClosedAction }: SpeedDialModalProps) => {
  const { csvHeadersMutation, csvPreviewMutation } = useImportCSVContext();

  const renderData = (): ImportCSVModalResult => {
    if (csvPreviewMutation.isSuccess && csvPreviewMutation.data) {
      return {
        content: <PreviewCSV />,
        subtitle: "Sprawdź, czy dane się zgadzają:",
      };
    }
    if (csvHeadersMutation.isSuccess && csvHeadersMutation.data) {
      return {
        content: <PickCSVHeaders />,
        subtitle: "Dopasuj kolumny z pliku do wymaganych pól:",
      };
    }
    return {
      content: <UploadCSV />,
      subtitle: "",
    };
  };

  const { content, subtitle } = renderData();

  return (
    <Modal
      isDataPresented={true}
      onClosed={onClosedAction}
      title="Import CSV"
      subtitle={subtitle}
    >
      {content}
    </Modal>
  );
};

export default function ImportCSVModal({
  onClosedAction,
  importType,
}: ImportCSVModalProps): ReactNode {
  return (
    <ImportCSVProvider importType={importType}>
      <ImportCSVModalContent onClosedAction={onClosedAction} />
    </ImportCSVProvider>
  );
}
