import { XPCardImageProps } from "@/components/xp-card/components/types";
import Image from "next/image";
import "./index.css";
import { API_STATIC_URL } from "@/services/api";

export default function XPCardImage({ imageUrl, alt }: XPCardImageProps) {
  return (
    <div className="xp-card-image">
      <Image
        src={`${API_STATIC_URL}/${imageUrl}`}
        alt={alt}
        fill
        className="object-cover"
        sizes="200px"
      />
    </div>
  );
}
