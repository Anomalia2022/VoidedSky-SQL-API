package net.voidedsky.voidedbaseapi;

import net.cybercake.cyberapi.common.builders.settings.FeatureSupport;
import net.cybercake.cyberapi.common.builders.settings.Settings;
import net.cybercake.cyberapi.spigot.CyberAPI;
import net.cybercake.cyberapi.spigot.chat.Log;
import net.voidedsky.voidedbaseapi.database.DatabaseHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends CyberAPI {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }
    @Override
    public void onEnable() {
        // Start SQL
        instance = this;


        startCyberAPI(
                Settings.builder()
                        .mainPackage("net.voidedsky.voidedbaseapi")
                        .prefix("Database")
                        .showPrefixInLogs(true)
                        .luckPermsSupport(FeatureSupport.SUPPORTED)
                        .build()
        );

        copyDefaultConfig();
        saveDefaultConfig();
        reloadConfig();
        Log.info("Loaded main configuration!");

        DatabaseHandler.refreshDatabase(false);
    }

    @Override
    public void onDisable() {
        // Upload and Shutdown SQL properly
        DatabaseHandler.closeConnection();

    }
}
