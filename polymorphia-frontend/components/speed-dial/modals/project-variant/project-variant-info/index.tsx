import XPCard from "@/components/xp-card/XPCard";
import XPCardImage from "@/components/xp-card/components/XPCardImage";
import XPCardProjectVariant from "@/components/xp-card/components/XPCardProjectVariant";
import { ProjectVariantInfoProps } from "@/components/speed-dial/modals/project-variant/project-variant-info/types";
import "./index.css";

export default function ProjectVariantInfo({
  size = "sm",
  color = "gray",
  projectVariants,
}: ProjectVariantInfoProps) {
  return (
    <div className="project-variant-info">
      {projectVariants.map((projectVariant, index) => (
        <XPCard
          title={projectVariant.name}
          subtitle={projectVariant.categoryName}
          key={index}
          leftComponent={
            <XPCardImage
              imageUrl={projectVariant.imageUrl}
              alt={projectVariant.name}
            />
          }
          rightComponent={
            <XPCardProjectVariant
              shortCode={projectVariant.shortCode}
              color={color}
            />
          }
          size={size}
        />
      ))}
    </div>
  );
}
