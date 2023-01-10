package net.teamuni.rewardapi.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.teamuni.rewardapi.data.object.CommandReward;
import net.teamuni.rewardapi.data.object.ItemReward;
import net.teamuni.rewardapi.data.object.Reward;
import org.bukkit.inventory.ItemStack;

public class RewardSerializer implements JsonSerializer<Reward>, JsonDeserializer<Reward> {

    @Override
    public JsonElement serialize(Reward src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = context.serialize(src.getViewItem(), ItemStack.class).getAsJsonObject();
        if (src instanceof ItemReward itemReward) {
            JsonArray jsonArray = new JsonArray();
            for (final ItemStack item : itemReward.getRewardItems()) {
                jsonArray.add(context.serialize(item, ItemStack.class));
            }
            jsonObject.add("reward_items", jsonArray);
        } else if (src instanceof CommandReward cmdReward) {
            jsonObject.add("commands", context.serialize(cmdReward.getCommands()));
        }
        if (src.getReceivedLogId() != -1) {
            jsonObject.add("received_log_id", new JsonPrimitive(src.getReceivedLogId()));
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
        ItemStack viewItem = context.deserialize(json, ItemStack.class);
        Reward reward = null;
        if (jsonObject.has("reward_items")) {
            JsonArray jsonArray = jsonObject.getAsJsonArray("reward_items");
            ItemStack[] items = new ItemStack[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); ++i) {
                items[i] = context.deserialize(jsonArray.get(i), ItemStack.class);
            }
            reward = new ItemReward(viewItem, items);
        } else if (jsonObject.has("commands")) {
            String[] commands = context.deserialize(jsonObject.get("commands"), String[].class);
            reward = new CommandReward(viewItem, commands);
        }
        if (reward != null && jsonObject.has("received_log_id")) {
            reward.setReceivedLogId(jsonObject.get("received_log_id").getAsLong());
        }
        return reward;
    }
}
