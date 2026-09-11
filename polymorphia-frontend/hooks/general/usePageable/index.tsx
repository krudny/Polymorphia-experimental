import { useRef, useState, useEffect } from "react";

interface UsePageableProps {
  initialPage?: number;
  // Optional dependency the change of which will reset the page to 0
  resetDependency?: unknown;
}

export function usePageable({
  initialPage = 0,
  resetDependency,
}: UsePageableProps = {}) {
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [direction, setDirection] = useState<"left" | "right">("right");
  const prevPageRef = useRef(initialPage);

  useEffect(() => {
    if (resetDependency !== undefined) {
      setCurrentPage(0);
      prevPageRef.current = 0;
    }
  }, [resetDependency]);

  const handlePageChange = (selectedItem: { selected: number }) => {
    const newPage = selectedItem.selected;
    setDirection(newPage > prevPageRef.current ? "right" : "left");
    prevPageRef.current = newPage;
    setCurrentPage(newPage);
  };

  return {
    currentPage,
    direction,
    handlePageChange,
    setCurrentPage,
  };
}
