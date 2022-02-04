package net.teamuni.rewardapi.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Map;
import net.teamuni.rewardapi.data.object.CommandReward;
import net.teamuni.rewardapi.data.object.ItemReward;
import net.teamuni.rewardapi.data.object.Reward;
import org.bukkit.inventory.ItemStack;

public class RewardSerializer implements JsonSerializer<Reward>, JsonDeserializer<Reward> {

    @Override
    public JsonElement serialize(Reward src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = context.serialize(src.getViewItem()).getAsJsonObject();
        if (src.isItemReward()) {
            ItemReward itemReward = (ItemReward) src;
            jsonObject.add("reward_items", context.serialize(itemReward.getRewardItems()));
        } else {
            jsonObject.add("commands", context.serialize(((CommandReward) src).getCommands()));
        }
        return jsonObject;
    }

    @Override
    public Reward deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        if (!json.isJsonObject()) {
            return null;
        }

        JsonObject jsonObject = json.getAsJsonObject();
        ItemStack viewItem = ItemStack.deserialize(context.deserialize(json, Map.class));
        if (jsonObject.has("reward_items")) {
            ItemStack[] items = context.deserialize(jsonObject.get("reward_items"), ItemStack[].class);
        }

        return null;
    }
}
