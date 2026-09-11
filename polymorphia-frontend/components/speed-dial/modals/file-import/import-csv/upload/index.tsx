"use client";

import ButtonWithBorder from "@/components/button";
import { ReactNode, useCallback } from "react";
import { useDropzone } from "react-dropzone";
import useImportCSVContext from "@/hooks/contexts/useImportCSVContext";
import "./index.css";
import "../index.css";
import Loading from "@/components/loading";
import { fileImportError } from "@/components/speed-dial/modals/file-import/import-csv/upload/fileImportError";
import { FileType } from "@/components/speed-dial/modals/file-import/import-csv/upload/types";

export default function UploadCSV(): ReactNode {
  const {
    selectedFile,
    importType,
    setSelectedFile,
    csvHeadersMutation,
    goBackToUpload,
  } = useImportCSVContext();

  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      const file = acceptedFiles[0];
      setSelectedFile(file);
    },
    [setSelectedFile]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      "text/csv": [".csv"],
    },
    maxSize: 5 * 1024 * 1024,
    multiple: false,
    disabled: csvHeadersMutation.isPending,
    onDropRejected: (rejectedFiles) =>
      fileImportError({ rejectedFiles, expectedFileType: FileType.CSV }),
  });

  const handleUpload = () => {
    if (selectedFile) {
      csvHeadersMutation.mutate({ file: selectedFile, type: importType });
    }
  };

  return (
    <div className="file-import">
      <div
        {...getRootProps()}
        className={`
            file-import-upload-wrapper
            ${isDragActive ? "drag-active" : ""}
          `}
      >
        <input {...getInputProps()} />

        {csvHeadersMutation.isPending ? (
          <div className="file-import-loading">
            <Loading />
          </div>
        ) : selectedFile ? (
          <>
            <span className="file-import-upload-icon">description</span>
            <span className="file-import-text">{selectedFile.name}</span>
          </>
        ) : isDragActive ? (
          <>
            <span className="file-import-upload-icon">cloud_upload</span>
            <span className="file-import-text">Upuść plik tutaj</span>
          </>
        ) : (
          <>
            <span className="file-import-upload-icon">cloud_upload</span>
            <span className="file-import-text">
              Przeciągnij lub wgraj plik CSV tutaj
            </span>
          </>
        )}
      </div>

      {selectedFile && (
        <div className="file-import-button-wrapper">
          <ButtonWithBorder
            text="Usuń"
            className="!mx-0 !py-0 !w-full"
            onClick={goBackToUpload}
          />
          <ButtonWithBorder
            text={
              csvHeadersMutation.isPending ? "Przesyłanie" : "Analizuj plik"
            }
            className="!mx-0 !py-0 !w-full"
            onClick={handleUpload}
          />
        </div>
      )}
    </div>
  );
}
