import Image from "next/image";
import "./index.css";
import { API_STATIC_URL } from "@/services/api";
import useUserContext from "@/hooks/contexts/useUserContext";
import { Roles, RoleTextMap } from "@/interfaces/api/user";

export default function UserSection() {
  const userContext = useUserContext();
  const { userDetails } = userContext;

  const title =
    userContext.userRole === Roles.STUDENT
      ? userContext.userDetails.animalName
      : userContext.userDetails.fullName;

  const subtitle =
    userContext.userRole === Roles.STUDENT
      ? userContext.userDetails.evolutionStage
      : RoleTextMap[userContext.userRole];

  return (
    <div className="user-section">
      <div className="user-section-image-wrapper">
        <Image
          src={`${API_STATIC_URL}/${userDetails.imageUrl}`}
          alt="Zwierzak użytkownika"
          fill
          priority
          fetchPriority="high"
          className="object-cover rounded-lg"
          sizes="(max-width: 1024px) 65px, 100px"
        />
      </div>
      <div className="profile-block user-section-content">
        <h1>{title}</h1>
        <h3>{subtitle}</h3>
      </div>
    </div>
  );
}
