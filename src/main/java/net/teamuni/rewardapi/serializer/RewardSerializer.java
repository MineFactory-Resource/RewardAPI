package net.teamuni.rewardapi.serializer;

import com.google.common.reflect.TypeToken;
import java.util.Arrays;
import java.util.List;
import net.teamuni.rewardapi.api.CommandReward;
import net.teamuni.rewardapi.api.ItemReward;
import net.teamuni.rewardapi.api.Reward;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class RewardSerializer implements TypeSerializer<Reward> {

    @Override
    public @Nullable Reward deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        ItemStackSnapshot iss = value.getNode("viewItem").getValue(TypeToken.of(ItemStackSnapshot.class));

        ConfigurationNode rewardNode = value.getNode("rewardItems");
        if (rewardNode.isVirtual()) {
            List<ItemStackSnapshot> items = rewardNode.getValue(new TypeToken<List<ItemStackSnapshot>>() {});
            return new ItemReward(iss, items.toArray(new ItemStackSnapshot[0]));
        } else {
            rewardNode = value.getNode("commands");
            String[] cmds = rewardNode.getValue(TypeToken.of(String[].class));
            return new CommandReward(iss, cmds);
        }
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Reward obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            return;
        }

        value.getNode("viewItem").setValue(TypeToken.of(ItemStackSnapshot.class), obj.getViewItem());

        if (obj.isItemReward()) {
            ItemReward itemReward = (ItemReward) obj;
            List<ItemStackSnapshot> items = Arrays.asList(itemReward.getRewardItems());
            value.getNode("rewardItems").setValue(new TypeToken<List<ItemStackSnapshot>>() {}, items); // TODO 작동 확인 테스트 필요
        } else {
            value.getNode("commands").setValue(TypeToken.of(String[].class), ((CommandReward) obj).getCommands());
        }
    }
}
