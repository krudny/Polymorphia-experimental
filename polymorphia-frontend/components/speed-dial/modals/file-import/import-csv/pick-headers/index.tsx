import ButtonWithBorder from "@/components/button";
import useImportCSVContext from "@/hooks/contexts/useImportCSVContext";
import toast from "react-hot-toast";
import "./index.css";
import "../index.css";
import Selector from "@/components/selector";
import React from "react";

export default function PickCSVHeaders() {
  const {
    csvHeadersMutation,
    csvPreviewMutation,
    selectedFile,
    headerMapping,
    setHeaderMapping,
    goBackToUpload,
  } = useImportCSVContext();

  const isAllMapped = csvHeadersMutation.data?.requiredCSVHeaders.every(
    (csvHeader) => headerMapping[csvHeader]
  );

  const handlePickHeaders = () => {
    if (!isAllMapped) {
      toast.error("Nie wszystkie nagłówki zostały dopasowane!");
      return;
    }

    if (selectedFile) {
      csvPreviewMutation.mutate({
        file: selectedFile,
        csvHeaders: headerMapping,
      });
    }
  };

  return (
    <div className="file-import">
      <div className="headers-mapping">
        {csvHeadersMutation.data?.requiredCSVHeaders.map((csvHeader) => {
          const columnOptions =
            csvHeadersMutation.data?.fileCSVHeaders.map((fileCSVHeader) => ({
              label: fileCSVHeader,
              value: fileCSVHeader,
            })) || [];

          return (
            <div className="csv-header-mapping" key={csvHeader}>
              <h3>{csvHeader}</h3>
              <div className="csv-header-selector-wrapper">
                <Selector
                  options={columnOptions}
                  value={headerMapping[csvHeader] || ""}
                  onChange={(value) => {
                    setHeaderMapping((prev) => ({
                      ...prev,
                      [csvHeader]: value,
                    }));
                  }}
                  placeholder="Kolumna"
                  size="2xl"
                  padding="xs"
                  centeredPlaceholder={true}
                  centeredOptions={true}
                  className="!border-b-2 !border-t-0 !border-x-0 !rounded-none !w-full"
                />
              </div>
            </div>
          );
        })}
      </div>

      <div className="file-import-button-wrapper">
        <ButtonWithBorder
          text="Powrót"
          className="!mx-0 !py-0 !w-full"
          onClick={goBackToUpload}
        />
        <ButtonWithBorder
          text="Zatwierdź"
          className="!mx-0 !py-0 !w-full"
          onClick={handlePickHeaders}
        />
      </div>
    </div>
  );
}
