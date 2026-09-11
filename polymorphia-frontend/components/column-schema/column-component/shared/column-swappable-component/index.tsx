import { ColumnSwappableComponentProps } from "@/components/column-schema/column-component/shared/column-swappable-component/types";
import clsx from "clsx";
import {
  baseSwapAnimationWrapperProps,
  SwapAnimationWrapper,
} from "@/animations/SwapAnimationWrapper";
import { getKeyForTarget } from "@/providers/grading/utils/getKeyForTarget";
import Loading from "@/components/loading";

export default function ColumnSwappableComponent<T>({
  data,
  isDataLoading,
  isDataError,
  renderComponent,
  renderDataErrorComponent,
  renderEmptyDataErrorComponent,
  minHeightClassName,
  className,
  selectedTarget,
}: ColumnSwappableComponentProps<T>) {
  const isDataLoadingUtil = isDataLoading;
  const isDataErrorUtil = isDataError || data === undefined;
  const isDataEmpty =
    renderEmptyDataErrorComponent !== undefined &&
    Array.isArray(data) &&
    data.length === 0;

  const targetKey = getKeyForTarget(selectedTarget);
  const targetKeySuffix = isDataLoadingUtil
    ? "_loading"
    : isDataErrorUtil
      ? "_error"
      : isDataEmpty
        ? "_empty"
        : "";
  const key =
    selectedTarget === null ? "noTarget" : targetKey + targetKeySuffix;

  let content;
  switch (true) {
    case selectedTarget === null:
      content = undefined;
      break;

    case isDataLoadingUtil:
      content = (
        <div
          className={clsx("mt-2", "relative", minHeightClassName, className)}
        >
          <Loading />
        </div>
      );
      break;

    case isDataErrorUtil:
      content = renderDataErrorComponent();
      break;

    case isDataEmpty:
      content = renderEmptyDataErrorComponent();
      break;

    default:
      content = renderComponent(data, key);
      break;
  }

  return (
    <SwapAnimationWrapper {...baseSwapAnimationWrapperProps} keyProp={key}>
      <div className={clsx("mt-2", minHeightClassName, className)}>
        {content}
      </div>
    </SwapAnimationWrapper>
  );
}
