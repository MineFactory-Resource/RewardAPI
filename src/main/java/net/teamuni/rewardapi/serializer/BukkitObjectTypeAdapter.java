package net.teamuni.rewardapi.serializer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.Yaml;
/*
버킷의 직렬화 가능 클래스들은 모두 Yaml 기반으로 이루어져 있기 때문에 Json 에서 사용하려면 따로 작업이 필요함
아래의 소스코드를 수정함
https://github.com/Shopkeepers/Shopkeepers/blob/master/modules/main/src/main/java/com/nisovin/shopkeepers/util/json/BukkitAwareObjectTypeAdapter.java
https://github.com/Shopkeepers/Shopkeepers/blob/master/modules/main/src/main/java/com/nisovin/shopkeepers/util/json/YamlLikeObjectTypeAdapter.java
 */
public class BukkitObjectTypeAdapter extends TypeAdapter<Object> {

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (ConfigurationSerializable.class.isAssignableFrom(type.getRawType())) {
                return unsafeCast(new BukkitObjectTypeAdapter(gson));
            }
            return null;
        }
    };
    private static final Yaml yaml = new Yaml();

    private final Gson gson;

    private BukkitObjectTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    private static Map<String, Object> serialize(ConfigurationSerializable object) {
        return ImmutableMap.<String, Object>builder()
            .put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
                ConfigurationSerialization.getAlias(object.getClass()))
            .putAll(object.serialize())
            .build();
    }

    private static <T extends ConfigurationSerializable> T deserialize(Map<String, Object> map) {
        return unsafeCast(ConfigurationSerialization.deserializeObject(map));
    }

    @SuppressWarnings("unchecked")
    private static <T> T unsafeCast(Object o) {
        return (T) o;
    }

    @Override
    public Object read(JsonReader in) throws IOException {
        Object value;
        JsonToken token = in.peek();
        switch (token) {
            case BEGIN_ARRAY:
                List<Object> list = new ArrayList<>();
                in.beginArray();
                while (in.hasNext()) {
                    // This recursively uses this custom Object TypeAdapter:
                    list.add(this.read(in));
                }
                in.endArray();
                value = list;
                break;
            case BEGIN_OBJECT:
                // We use a LinkedHashMap instead of Gson's LinkedTreeMap:
                Map<String, Object> map = new LinkedHashMap<>();
                in.beginObject();
                while (in.hasNext()) {
                    // This recursively uses this custom Object TypeAdapter:
                    map.put(in.nextName(), this.read(in));
                }
                in.endObject();
                value = map;
                break;
            case STRING:
                String string = in.nextString();
                if (in.isLenient()) {
                    // Check if we can parse the String as one of the special numbers (NaN and infinities):
                    try {
                        double number = Double.parseDouble(string);
                        if (!Double.isFinite(number)) {
                            value = number;
                            break;
                        } // Finite numbers are not expected to be represented as String
                    } catch (NumberFormatException ignored) {
                    }
                }
                value = string;
                break;
            case NUMBER:
                // We delegate the number parsing to the Yaml parser:
                String numberString = in.nextString();
                Object number = yaml.load(numberString);
                if (!(number instanceof Number)) {
                    throw new IllegalStateException("Could not parse number: " + numberString);
                }
                value = number;
                break;
            case BOOLEAN:
                value = in.nextBoolean();
                break;
            case NULL:
                in.nextNull();
                value = null;
                break;
            default:
                throw new IllegalStateException();
        }

        if (value instanceof Map map &&
            map.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            value = deserialize(unsafeCast(map));
        }
        return value;
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        if (value instanceof ConfigurationSerializable serializable) {
            value = serialize(serializable);
        }

        TypeAdapter<Object> typeAdapter = unsafeCast(gson.getAdapter(value.getClass()));
        if (typeAdapter instanceof BukkitObjectTypeAdapter) {
            out.beginObject();
            out.endObject();
            return;
        }

        typeAdapter.write(out, value);
    }
}
