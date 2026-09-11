import { Accordion } from "@/components/accordion/Accordion";
import AccordionSection from "@/components/accordion/AccordionSection";
import ButtonWithBorder from "@/components/button";
import Loading from "@/components/loading";
import Modal from "@/components/modal";
import { AccordionRef } from "@/providers/accordion/types";
import {
  FilterActions,
  FilterOption,
} from "@/hooks/course/filters/useFilters/types";
import { useRef } from "react";
import toast from "react-hot-toast";
import "./index.css";
import { FiltersModalProps } from "@/components/filters-modals/types";

export default function FiltersModal<FilterIdType extends string>({
  filters,
  isModalOpen,
  setIsModalOpen,
  isFiltersLoading,
  isFiltersError,
  onFiltersApplied,
}: FiltersModalProps<FilterIdType>) {
  const {
    dispatch,
    configs,
    applyFilters,
    getFilterValues,
    resetFiltersToApplied,
  } = filters;

  const accordionRef = useRef<AccordionRef>(null);

  const handleSelect = (filterId: FilterIdType, option: FilterOption) => {
    dispatch({
      type: FilterActions.TOGGLE,
      id: filterId,
      value: option.value,
    });
  };

  const handleConfirm = () => {
    const result = applyFilters();

    if (result.ok) {
      onFiltersApplied?.();
      setIsModalOpen(false);
    } else if (result.errors !== undefined) {
      (Object.keys(result.errors) as FilterIdType[]).forEach(
        (configId: FilterIdType) => {
          toast.error(result.errors![configId]);
        }
      );
    }
  };

  const getModalContent = () => {
    if (isFiltersLoading) {
      return <Loading />;
    }

    if (isFiltersError) {
      return <h1>Nie udało się załadować filtrów.</h1>;
    }

    const filterIds = configs.map(({ id }) => id);
    const accordionSections = new Set(filterIds);
    const initiallyOpenedAccordionSections = new Set(
      filterIds.length > 0 ? [filterIds[0]] : []
    );

    return (
      <>
        <Accordion
          ref={accordionRef}
          sectionIds={accordionSections}
          initiallyOpenedSectionIds={initiallyOpenedAccordionSections}
          maxOpen={1}
          className="filters-modal-wrapper"
        >
          {configs.map((filterConfig) => (
            <AccordionSection
              key={filterConfig.id}
              id={filterConfig.id}
              title={filterConfig.title}
            >
              <div>
                <div className="filters-modal-filter-grid">
                  {filterConfig.options.map((option) => (
                    <div
                      key={option.value}
                      onClick={() => handleSelect(filterConfig.id, option)}
                      className={`filters-modal-filter-option ${
                        (getFilterValues(filterConfig.id) ?? []).includes(
                          option.value
                        )
                          ? "filters-modal-filter-option-selected"
                          : "filters-modal-filter-option-unselected"
                      }`}
                    >
                      {option.label ?? option.value}
                    </div>
                  ))}
                </div>
                {filterConfig.additionalInfo && (
                  <h3 className="filters-modal-filter-additional-info">
                    {filterConfig.additionalInfo}
                  </h3>
                )}
              </div>
            </AccordionSection>
          ))}
        </Accordion>
        <div className="filters-modal-button-wrapper">
          <ButtonWithBorder
            text="Potwierdź zmiany"
            className="w-full rounded-md"
            size="sm"
            onClick={handleConfirm}
          />
        </div>
      </>
    );
  };

  return (
    <Modal
      isDataPresented={isModalOpen}
      title="Filtry"
      onClosed={() => {
        accordionRef.current?.resetToInitial();
        resetFiltersToApplied();
        setIsModalOpen(false);
      }}
      shouldUnmountWhenClosed={true}
    >
      <div className="filters-modal">{getModalContent()}</div>
    </Modal>
  );
}
