package com.agh.polymorphia_backend.model.reward;

import com.agh.polymorphia_backend.model.reward.chest.ChestBehavior;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@SuperBuilder
@Table(name = "chests")
@PrimaryKeyJoinColumn(name = "reward_id")
@ToString(callSuper = true, exclude = {"items"})
public class Chest extends Reward {
    @ManyToMany(mappedBy = "chests", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Item> items = new ArrayList<>();
    @NotNull
    @Enumerated(EnumType.STRING)
    private ChestBehavior behavior;

    @Override
    public RewardType getRewardType() {
        return RewardType.CHEST;
    }
}
