"use client";

import { RewardProps } from "@/components/column-schema/column-component/grading/grade/types";
import AssignRewardModal from "@/views/grading/modals/assign-reward";
import { useState } from "react";
import "./index.css";
import { API_STATIC_URL } from "@/services/api";
import ImageBadge from "@/components/image-badge/ImageBadge";
import Image from "next/image";
import { CriterionAssignableRewardResponseDTO } from "@/interfaces/api/grade/criteria";

export default function AssignReward({
  criterion,
  criterionGrade,
}: RewardProps) {
  const [assignableRewards, setAssignableRewards] = useState<
    CriterionAssignableRewardResponseDTO[] | null
  >(null);

  return (
    <>
      <div className="grade-assign-reward">
        {criterionGrade?.assignedRewards?.map((assignedReward, index) => {
          if (assignedReward.quantity === 0) {
            return;
          }

          return (
            <div key={index} className="grade-assign-reward-wrapper">
              <div className="grade-assign-reward-image-wrapper">
                <Image
                  src={`${API_STATIC_URL}/${assignedReward.imageUrl}`}
                  alt={assignedReward.name}
                  fill
                  className="object-cover"
                  sizes="(min-width: 1024px) 25vw, 50vw"
                  onClick={() => {}}
                />
                <ImageBadge
                  text={assignedReward.quantity.toString()}
                  className="grade-assign-reward-image-badge"
                />
              </div>
            </div>
          );
        })}
        <div
          className="grade-assign-reward-edit"
          onClick={() => setAssignableRewards(criterion?.assignableRewards)}
        >
          <span>edit</span>
        </div>
      </div>
      <AssignRewardModal
        criterionId={criterion.id}
        assignableRewards={assignableRewards}
        onClosedAction={() => setAssignableRewards(null)}
      />
    </>
  );
}
