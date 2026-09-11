import ErrorComponent from "@/components/error";
import { ErrorComponentSizes } from "@/components/error/types";

const errorMessage = "Nie udało się załadować wariantów projektu.";
export const errorComponentStudent = <ErrorComponent message={errorMessage} />;

export const errorComponentCompact = (
  <ErrorComponent message={errorMessage} size={ErrorComponentSizes.COMPACT} />
);

const noProjectVariantsErrorTitle = "Brak wariantów projektu";

export const noProjectVariantsErrorComponentStudent = (
  <ErrorComponent
    title={noProjectVariantsErrorTitle}
    message="Nie należysz do żadnej grupy lub Twojej grupie nie zostały przypisane żadne warianty."
  />
);

export const noProjectVariantsErrorComponentCompact = (
  <ErrorComponent
    title={noProjectVariantsErrorTitle}
    message="Tej grupie nie zostały przypisane żadne warianty projektu."
    size={ErrorComponentSizes.COMPACT}
  />
);
