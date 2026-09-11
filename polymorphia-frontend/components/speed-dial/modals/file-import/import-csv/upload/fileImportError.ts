import toast from "react-hot-toast";
import { UseFileImportError } from "@/components/speed-dial/modals/file-import/import-csv/upload/types";

export function fileImportError({
  rejectedFiles,
  expectedFileType,
}: UseFileImportError) {
  rejectedFiles.forEach((rejectedFile) => {
    const { file, errors } = rejectedFile;

    errors.forEach((error) => {
      switch (error.code) {
        case "file-too-large":
          const fileSizeMB = (file.size / (1024 * 1024)).toFixed(2);
          toast.error(
            `Plik "${file.name}" jest za duży (${fileSizeMB} MB). Maksymalny rozmiar to 5 MB.`
          );
          break;

        case "file-invalid-type":
          toast.error(
            `Plik "${file.name}" nie jest w formacie ${expectedFileType}.`
          );
          break;

        case "too-many-files":
          toast.error("Można przesłać tylko jeden plik naraz");
          break;

        default:
          toast.error(
            `Błąd przesyłania pliku "${file.name}": ${error.message}`
          );
      }
    });
  });
}
