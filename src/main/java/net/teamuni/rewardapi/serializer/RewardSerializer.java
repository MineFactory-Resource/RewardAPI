package net.teamuni.rewardapi.serializer;

import com.google.common.reflect.TypeToken;
import java.util.Arrays;
import java.util.List;
import net.teamuni.rewardapi.data.object.CommandReward;
import net.teamuni.rewardapi.data.object.ItemReward;
import net.teamuni.rewardapi.data.object.Reward;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class RewardSerializer implements TypeSerializer<Reward> {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable Reward deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        ItemStackSnapshot viewItem = value.getValue(TypeToken.of(ItemStackSnapshot.class));
        ConfigurationNode rewardNode = value.getNode("reward_items");
        if (!rewardNode.isVirtual()) {
            List<ItemStackSnapshot> items = rewardNode.getList(TypeToken.of(ItemStackSnapshot.class));
            return new ItemReward(viewItem, items.toArray(new ItemStackSnapshot[0]));
        } else {
            rewardNode = value.getNode("commands");
            String[] commands = rewardNode.getValue(TypeToken.of(String[].class));
            return new CommandReward(viewItem, commands);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Reward obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            return;
        }

        value.setValue(TypeToken.of(ItemStackSnapshot.class), obj.getViewItem());
        if (value.getNode("UnsafeDamage").getInt(0) == 0) {
            value.removeChild("UnsafeDamage");
        }

        if (obj.isItemReward()) {
            ItemReward itemReward = (ItemReward) obj;
            List<ItemStackSnapshot> items = Arrays.asList(itemReward.getRewardItems());
            value.getNode("reward_items").setValue(new TypeToken<List<ItemStackSnapshot>>() {}, items);
        } else {
            value.getNode("commands").setValue(TypeToken.of(String[].class), ((CommandReward) obj).getCommands());
        }
    }
}
