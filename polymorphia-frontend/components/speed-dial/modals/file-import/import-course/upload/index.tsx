"use client";

import ButtonWithBorder from "@/components/button";
import { ReactNode, useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import "../../import-csv/upload/index.css";
import "../../import-csv/index.css";
import Loading from "@/components/loading";
import { fileImportError } from "@/components/speed-dial/modals/file-import/import-csv/upload/fileImportError";
import useCourseUpdate from "@/hooks/course/config/useCourseUpdate";
import useModalContext from "@/hooks/contexts/useModalContext";
import useCourseCreate from "@/hooks/course/config/useCourseCreate";
import {
  CourseFileActions,
  UseCourseUploadProps,
} from "@/components/speed-dial/modals/file-import/import-course/upload/types";
import { FileType } from "@/components/speed-dial/modals/file-import/import-csv/upload/types";

export default function UploadCourseConfig({
  courseFileAction,
}: UseCourseUploadProps): ReactNode {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const courseUpdateMutation = useCourseUpdate();
  const courseCreateMutation = useCourseCreate();
  const { closeModal } = useModalContext();

  const goBackToUpload = () => {
    setSelectedFile(null);
    courseUpdateMutation.reset();
    courseCreateMutation.reset();
  };

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
      "application/json": [".json"],
    },
    maxSize: 200 * 1024,
    multiple: false,
    disabled: courseUpdateMutation.isPending || courseCreateMutation.isPending,
    onDropRejected: (rejectedFiles) =>
      fileImportError({ rejectedFiles, expectedFileType: FileType.JSON }),
  });

  const handleUpload = () => {
    if (selectedFile) {
      switch (courseFileAction) {
        case CourseFileActions.UPDATE:
          courseUpdateMutation.mutate(
            { file: selectedFile },
            {
              onSuccess: () => {
                closeModal();
              },
            }
          );
          break;
        case CourseFileActions.CREATE:
          courseCreateMutation.mutate(
            { file: selectedFile },
            {
              onSuccess: () => {
                closeModal();
              },
            }
          );
          break;
      }
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

        {courseUpdateMutation.isPending || courseCreateMutation.isPending ? (
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
              Przeciągnij lub wgraj plik JSON tutaj
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
            text="Zatwierdź"
            className="!mx-0 !py-0 !w-full"
            onClick={handleUpload}
          />
        </div>
      )}
    </div>
  );
}
