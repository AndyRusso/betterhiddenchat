package io.github.andyrusso.betterhiddenchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BetterHiddenChatConfig {
    private static final Path file = FabricLoader.getInstance().getConfigDir().resolve("betterhiddenchat.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BetterHiddenChatConfig instance;

    public boolean persist = false;
    public boolean chatHideState = false;

    public void save() {
        try {
            Files.writeString(file, GSON.toJson(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BetterHiddenChatConfig getInstance() {
        if (instance == null) {
            try {
                instance = GSON.fromJson(Files.readString(file), BetterHiddenChatConfig.class);
            } catch (IOException exception) {
                instance = new BetterHiddenChatConfig();
            }
        }

        return instance;
    }
}
