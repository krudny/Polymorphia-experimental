import { Accordion } from "@/components/accordion/Accordion";
import AccordionSection from "@/components/accordion/AccordionSection";
import ButtonWithBorder from "@/components/button";
import ErrorComponent from "@/components/error";
import { ErrorComponentSizes } from "@/components/error/types";
import Loading from "@/components/loading";
import useProjectGroupConfigurationContext from "@/hooks/contexts/useProjectGroupConfigurationContext";
import { useProjectCategories } from "@/hooks/course/projects/useProjectCategories";
import { ProjectGroupConfigurationSteps } from "@/providers/project-group-configuration/types";
import "./index.css";
import XPCardProjectVariant from "@/components/xp-card/components/XPCardProjectVariant";
import XPCardImage from "@/components/xp-card/components/XPCardImage";
import XPCard from "@/components/xp-card/XPCard";
import clsx from "clsx";
import useProjectGroupUpdate from "@/hooks/course/projects/useProjectGroupUpdate";
import useModalContext from "@/hooks/contexts/useModalContext";
import useRandomProjectVariant from "@/hooks/course/projects/useRandomProjectVariant";

export default function ProjectVariantPick() {
  const {
    setCurrentStep,
    currentProjectGroupConfiguration,
    setCurrentProjectGroupConfiguration,
    initialTarget,
  } = useProjectGroupConfigurationContext();
  const {
    data: projectCategories,
    isLoading: isProjectCategoriesLoading,
    isError: isProjectCategoriesError,
  } = useProjectCategories();

  const { mutation: submitMutation } = useProjectGroupUpdate();
  const { mutation: randomVariantMutation } = useRandomProjectVariant();
  const { closeModal } = useModalContext();

  const handleSelection = (categoryId: number, variantId: number) => {
    setCurrentProjectGroupConfiguration((previousConfiguration) => ({
      ...previousConfiguration,
      selectedVariants: {
        ...previousConfiguration.selectedVariants,
        [categoryId]: variantId,
      },
    }));
  };

  if (isProjectCategoriesLoading) {
    return (
      <div className="variant-pick-loading">
        <Loading />
      </div>
    );
  } else if (isProjectCategoriesError || projectCategories === undefined) {
    return (
      <ErrorComponent
        message="Nie udało się załadować wariantów."
        size={ErrorComponentSizes.COMPACT}
      />
    );
  }

  const isSelectionValid = projectCategories.every(
    (category) =>
      category.id in currentProjectGroupConfiguration.selectedVariants &&
      category.variants.some(
        (variant) =>
          variant.id ===
          currentProjectGroupConfiguration.selectedVariants[category.id]
      )
  );

  const handleSubmit = () => {
    if (!isSelectionValid) {
      return;
    }

    submitMutation.mutate(
      {
        target: initialTarget,
        configuration: currentProjectGroupConfiguration,
      },
      {
        onSuccess: () => {
          closeModal();
        },
      }
    );
  };

  const handleRandom = () => {
    randomVariantMutation.mutate(
      { target: initialTarget },
      {
        onSuccess: (newVariant) => {
          setCurrentProjectGroupConfiguration((previousConfiguration) => ({
            ...previousConfiguration,
            selectedVariants: newVariant,
          }));
        },
      }
    );
  };

  const sectionIds = new Set(
    projectCategories.map((category) => String(category.id))
  );

  const initiallyOpenedSectionIds =
    projectCategories.length > 0
      ? new Set([String(projectCategories[0].id)])
      : undefined;

  return (
    <div className="variant-pick">
      {projectCategories.length > 0 ? (
        <Accordion
          className="variant-pick-accordion-override"
          sectionIds={sectionIds}
          initiallyOpenedSectionIds={initiallyOpenedSectionIds}
          maxOpen={1}
          shouldAnimateInitialOpen={false}
        >
          {projectCategories.map((category) => {
            const selectedVariant =
              category.id in currentProjectGroupConfiguration.selectedVariants
                ? category.variants.find(
                    (variant) =>
                      variant.id ===
                      currentProjectGroupConfiguration.selectedVariants[
                        category.id
                      ]
                  )
                : undefined;

            return (
              <AccordionSection
                key={category.id}
                id={String(category.id)}
                title={
                  (selectedVariant ? `[${selectedVariant?.shortCode}] ` : "") +
                  category.name
                }
                headerClassName="variant-pick-accordion-header"
              >
                {category.variants.length > 0 ? (
                  <div className="variant-pick-list">
                    {category.variants.map((variant, index) => (
                      <div
                        key={index}
                        className={clsx(
                          currentProjectGroupConfiguration.selectedVariants[
                            category.id
                          ] === variant.id && "variant-pick-list-selected",
                          "cursor-pointer"
                        )}
                        onClick={() => handleSelection(category.id, variant.id)}
                      >
                        <XPCard
                          title={variant.name}
                          key={index}
                          forceWidth={true}
                          leftComponent={
                            <XPCardImage
                              imageUrl={variant.imageUrl}
                              alt={variant.name}
                            />
                          }
                          rightComponent={
                            <XPCardProjectVariant
                              shortCode={variant.shortCode}
                              color="gray"
                            />
                          }
                          size="xs"
                        />
                      </div>
                    ))}
                  </div>
                ) : (
                  <ErrorComponent message="Nie istnieją warianty dla tej kategorii." />
                )}
              </AccordionSection>
            );
          })}
        </Accordion>
      ) : (
        <ErrorComponent
          title="Brak wariantów"
          message="Ten projekt nie posiada wariantów"
          size={ErrorComponentSizes.COMPACT}
        />
      )}
      <div className="variant-pick-buttons">
        <div className="variant-pick-buttons-row">
          <ButtonWithBorder
            text={randomVariantMutation.isPending ? "Losowanie..." : "Wylosuj"}
            icon="ifl"
            className="!mx-0 !py-0 !w-full"
            onClick={handleRandom}
            isActive={
              projectCategories.length !== 0 && !randomVariantMutation.isPending
            }
          />
        </div>
        <div className="variant-pick-buttons-row">
          <ButtonWithBorder
            text="Powrót"
            className="!mx-0 !py-0 !w-full"
            onClick={() =>
              setCurrentStep(ProjectGroupConfigurationSteps.GROUP_PICK)
            }
          />
          <ButtonWithBorder
            text="Zapisz"
            className="!mx-0 !py-0 !w-full"
            isActive={
              (projectCategories.length === 0 || isSelectionValid) &&
              !submitMutation.isPending
            }
            onClick={handleSubmit}
          />
        </div>
      </div>
    </div>
  );
}
