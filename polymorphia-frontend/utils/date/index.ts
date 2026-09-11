export const formatDate = (
  isoDateString: string | null | undefined
): string => {
  if (!isoDateString) {
    return "";
  }
  const dateObject = new Date(isoDateString);
  const monthString = String(dateObject.getMonth() + 1).padStart(2, "0");
  const dayString = String(dateObject.getDate()).padStart(2, "0");
  const yearString = dateObject.getFullYear();
  const hoursString = String(dateObject.getHours()).padStart(2, "0");
  const minutesString = String(dateObject.getMinutes()).padStart(2, "0");
  return `${monthString}/${dayString}/${yearString} ${hoursString}:${minutesString}`;
};
