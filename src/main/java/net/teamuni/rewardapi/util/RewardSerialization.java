package net.teamuni.rewardapi.util;

import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.teamuni.rewardapi.api.ItemReward;
import net.teamuni.rewardapi.api.Reward;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class RewardSerialization implements TypeSerializer<Reward> {

    @Override
    public @Nullable Reward deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        // TODO
        return null;
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Reward obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            return;
        }

        try {
            ConfigurationNode viewItemNode = ItemSerialization.serialize(obj.getViewItem());
            value.getNode("viewItem").setValue(viewItemNode);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (obj.isItemReward()) {
            ItemReward itemReward = (ItemReward) obj;
            ItemStackSnapshot[] rewardItems = itemReward.getRewardItems();
            List<ConfigurationNode> nodeList = new ArrayList<>();
            for (ItemStackSnapshot iss : rewardItems) {
                try {
                    nodeList.add(ItemSerialization.serialize(iss));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            value.getNode("rewardItems").setValue(nodeList); // TODO 작동 확인 테스트 필요
        } else {
            // TODO
        }
    }
}
