package net.teamuni.rewardapi.data.database;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.object.Reward;
import org.checkerframework.checker.nullness.qual.NonNull;

public class JsonDatabase extends Database {

    private final Path dataFolder;

    public JsonDatabase(RewardAPI instance, Path dataFolder) {
        super(instance);
        this.dataFolder = dataFolder;
    }

    @Override
    protected @NonNull String loadJson(@NonNull UUID uuid) throws IOException {
        Path dataPath = dataFolder.resolve(uuid + ".json");
        if (!Files.exists(dataPath)) {
            return "";
        }

        return String.join("", Files.readAllLines(dataPath, UTF_8));
    }

    @Override
    protected void saveJson(@NonNull UUID uuid, @NonNull String json) throws IOException {
        Files.writeString(checkDataFile(uuid), json, UTF_8);
    }

    @Override
    protected long logReceived(UUID uuid, String json) {
        return -1;
    }

    @Override
    protected void logClaimed(UUID uuid, long receivedLogId) {

    }

    private Path checkDataFile(UUID uuid) throws IOException {
        if (!Files.exists(dataFolder)) {
            Files.createDirectories(dataFolder);
        }
        Path dataPath = dataFolder.resolve(uuid + ".json");
        if (!Files.exists(dataPath)) {
            Files.createFile(dataPath);
        }
        return dataPath;
    }
}
