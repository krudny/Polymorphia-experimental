import { API_STATIC_URL } from "@/services/api";
import Image from "next/image";
import "./index.css";
import ImageBadge from "@/components/image-badge/ImageBadge";

export default function HallOfFameImage({
  position,
  imageUrl,
}: {
  position: number;
  imageUrl: string;
}) {
  return (
    <div className="hof-image-wrapper">
      <Image
        src={`${API_STATIC_URL}/${imageUrl}`}
        alt="User profile"
        fill
        className="hof-image"
        sizes="(max-width: 768px) 64px, (max-width: 1920px) 150px, 165px"
      />
      <ImageBadge
        text={position < 10 ? "0" + position : position.toString()}
        className={"hof-image-badge"}
      />
    </div>
  );
}
