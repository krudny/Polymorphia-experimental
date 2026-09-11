import { FileRejection } from "react-dropzone";
export enum FileType {
  JSON = "JSON",
  CSV = "CSV",
}

export interface UseFileImportError {
  rejectedFiles: FileRejection[];
  expectedFileType: FileType;
}
