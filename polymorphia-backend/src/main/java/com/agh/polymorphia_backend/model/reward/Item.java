package com.agh.polymorphia_backend.model.reward;


import com.agh.polymorphia_backend.model.event_section.EventSection;
import com.agh.polymorphia_backend.model.reward.item.ItemType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "items")
@ToString(callSuper = true, exclude = {"chests"})
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "reward_id")
public abstract class Item extends Reward {
    @NotNull
    @Column(name = "\"limit\"")
    private Integer limit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_section_id")
    private EventSection eventSection;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chests_items",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "chest_id")
    )
    @Builder.Default
    private List<Chest> chests = new ArrayList<>();

    public abstract ItemType getItemType();

    @Override
    public RewardType getRewardType() {
        return RewardType.ITEM;
    }
}
