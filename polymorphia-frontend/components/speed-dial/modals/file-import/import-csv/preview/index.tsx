import ButtonWithBorder from "@/components/button";
import useImportCSVContext from "@/hooks/contexts/useImportCSVContext";
import useModalContext from "@/hooks/contexts/useModalContext";
import "./index.css";
import "../index.css";

export default function PreviewCSV() {
  const {
    selectedFile,
    importType,
    csvPreviewMutation,
    csvProcessMutation,
    goBackToUpload,
  } = useImportCSVContext();
  const { closeModal } = useModalContext();

  if (!csvPreviewMutation.data || !selectedFile) {
    closeModal();
    return;
  }

  const handleConfirm = () => {
    csvProcessMutation.mutate(
      {
        type: importType,
        csvHeaders: csvPreviewMutation.data.csvHeaders,
        data: csvPreviewMutation.data.data,
      },
      {
        onSuccess: closeModal,
      }
    );
  };

  const { csvHeaders, data } = csvPreviewMutation.data;
  const gridTemplateColumns = `repeat(${csvHeaders.length}, minmax(150px, 1fr))`;

  return (
    <div className="file-import">
      <div className="table-wrapper">
        <div
          className="table"
          style={{
            gridTemplateColumns: gridTemplateColumns,
          }}
        >
          {csvHeaders.map((csvHeader, index) => (
            <div className="table-header" key={`header-${index}`}>
              {csvHeader}
            </div>
          ))}

          {data.flatMap((row, rowIndex) =>
            row.map((cell, cellIndex) => (
              <div className="table-cell" key={`${rowIndex}-${cellIndex}`}>
                {cell || "—"}
              </div>
            ))
          )}
        </div>
      </div>

      <div
        className={`file-import-button-wrapper ${data.length > 10 && "preview-buttons-override"}`}
      >
        <ButtonWithBorder
          text="Powrót"
          className="!mx-0 !py-0 !w-full"
          onClick={goBackToUpload}
        />
        <ButtonWithBorder
          text="Zatwierdź"
          className="!mx-0 !py-0 !w-full"
          onClick={handleConfirm}
        />
      </div>
    </div>
  );
}
