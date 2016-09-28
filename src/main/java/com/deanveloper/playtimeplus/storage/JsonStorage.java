package com.deanveloper.playtimeplus.storage;

import com.deanveloper.playtimeplus.PlayTimePlus;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Dean
 */
public class JsonStorage implements Storage {
    private final File storage;
    private final Map<UUID, PlayerEntry> players;
    private final int version;

    JsonStorage() {
        storage = new File(PlayTimePlus.getInstance().getDataFolder(), "players.json");

        // Parse the file
        JsonObject root;
        try {
            String line = Files.readFirstLine(storage, Charset.defaultCharset());
            if (line == null || line.isEmpty()) {
                throw new FileNotFoundException("Just in case");
            }
            root = new JsonParser().parse(line).getAsJsonObject();
        } catch (FileNotFoundException e) {
            root = new JsonObject();
            root.addProperty("version", 1);
            root.add("players", new JsonObject());
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Now let's get our fields
        version = root.get("version").getAsInt();
        Type type = new TypeToken<Map<UUID, PlayerEntry>>(){}.getType();
        Map<UUID, PlayerEntry> temp = PlayTimePlus.GSON.fromJson(root.getAsJsonObject("players"), type);
        if(temp == null) {
            players = new HashMap<>();
        } else {
            players = temp;
        }
    }

    @Override
    public void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("version", version);
            root.add("players", PlayTimePlus.GSON.toJsonTree(players));
            Files.write(PlayTimePlus.GSON.toJson(root), storage, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerEntry get(UUID id) {
        return getPlayers().get(id);
    }

    @Override
    public Map<UUID, PlayerEntry> getPlayers() {
        return players;
    }
}
