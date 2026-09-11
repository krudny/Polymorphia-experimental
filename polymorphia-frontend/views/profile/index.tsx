import Image from "next/image";
import { useScaleShow } from "@/animations/ScaleShow";
import { API_STATIC_URL } from "@/services/api";
import "./index.css";
import UserPoints from "@/components/user-points/UserPoints";
import { useMediaQuery } from "react-responsive";
import Loading from "@/components/loading";
import useUserContext from "@/hooks/contexts/useUserContext";
import FiltersModal from "@/components/filters-modals/FiltersModal";
import ProfileProgressBar from "@/components/progressbar/profile";
import useProfileContext from "@/hooks/contexts/useProfileContext";
import { distributeTo100 } from "@/components/progressbar/profile/distributeTo100";
import ErrorComponent from "@/components/error";
import { Roles } from "@/interfaces/api/user";
import { Sizes } from "@/interfaces/general";
import { SpeedDialKeys } from "@/components/speed-dial/types";
import { LazySpeedDial } from "@/components/speed-dial/lazy";
import { getUserPointsProps } from "./util";
import { ProfileFilterId } from "@/providers/profile/types";

export default function ProfileView() {
  const {
    areFiltersOpen,
    setAreFiltersOpen,
    isLoading,
    isError,
    profile,
    filteredXpDetails,
    maxPoints,
    isFiltersLoading,
    isFiltersError,
    filters,
  } = useProfileContext();
  const userContext = useUserContext();
  const wrapperRef = useScaleShow(!isLoading);
  const isMd = useMediaQuery({ minWidth: 768 });
  const isXl = useMediaQuery({ minWidth: 1280 });
  const userPointsProps = getUserPointsProps(isMd, isXl);

  if (isLoading || !userContext.userRole) {
    return <Loading />;
  }

  if (isError || !profile || userContext.userRole !== Roles.STUDENT) {
    return <ErrorComponent message="Nie udało się załadować profilu." />;
  }
  const { imageUrl, fullName, animalName, position } = userContext.userDetails;

  return (
    <div ref={wrapperRef} className="profile">
      <LazySpeedDial speedDialKey={SpeedDialKeys.PROFILE_STUDENT} />
      <div className="profile-wrapper">
        <div className="profile-content-wrapper">
          <div className="profile-image-wrapper">
            <Image
              src={`${API_STATIC_URL}/${imageUrl}`}
              alt="User profile"
              fill
              className="profile-img"
              priority
              sizes="(min-width: 1024px) 25vw, 50vw"
            />
          </div>
          <div className="profile-content">
            <div className="profile-content-text">
              <h1>{fullName}</h1>
              <h2>{animalName}</h2>
              <h3>
                Jesteś {position} na {profile.totalStudentsInCourse} zwierzaków!
              </h3>
            </div>
            <div className={userPointsProps.className}>
              <UserPoints
                separators
                titleSize={userPointsProps.titleSize}
                xpSize={userPointsProps.xpSize}
                xpDetails={filteredXpDetails}
              />
            </div>
          </div>
        </div>
        <div className="profile-progress-bar-mobile">
          <ProfileProgressBar
            totalXp={
              (profile.totalXp / profile.rightEvolutionStage.minXp) * 100
            }
            maxPoints={maxPoints}
            evolutionStages={[
              profile.leftEvolutionStage,
              profile.rightEvolutionStage,
            ]}
            numSquares={2}
            segmentSizes={[0, 100, 0]}
            size={Sizes.SM}
          />
        </div>

        <div className="profile-progress-bar-desktop">
          <ProfileProgressBar
            totalXp={profile.totalXp}
            maxPoints={maxPoints}
            evolutionStages={profile.evolutionStageThresholds}
            numSquares={profile.evolutionStageThresholds.length}
            segmentSizes={distributeTo100(profile.evolutionStageThresholds)}
            size={isXl ? Sizes.MD : Sizes.SM}
          />
        </div>
        <FiltersModal<ProfileFilterId>
          filters={filters}
          isModalOpen={areFiltersOpen}
          setIsModalOpen={setAreFiltersOpen}
          isFiltersLoading={isFiltersLoading}
          isFiltersError={isFiltersError}
        />
      </div>
    </div>
  );
}
