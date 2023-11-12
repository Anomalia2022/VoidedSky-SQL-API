package net.voidedsky.voidedbaseapi.commands.database;

import net.cybercake.cyberapi.spigot.basic.BetterStackTraces;
import net.cybercake.cyberapi.spigot.chat.Broadcast;
import net.cybercake.cyberapi.spigot.chat.Log;
import net.cybercake.cyberapi.spigot.chat.TabCompleteType;
import net.cybercake.cyberapi.spigot.chat.UChat;
import net.cybercake.cyberapi.spigot.server.commands.CommandInformation;
import net.cybercake.cyberapi.spigot.server.commands.SpigotCommand;
import net.voidedsky.voidedbaseapi.Main;
import net.voidedsky.voidedbaseapi.database.DatabaseHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class Database extends SpigotCommand {

    public Database() {
        super(
                newCommand("database")
                        .setDescription("Debug command.")
                        .setUsage("/database <value>")
                        .setPermission("sysadmin.database")
                        .setTabCompleteType(TabCompleteType.SEARCH)
        );
    }
    @Override
    public boolean perform(@org.jetbrains.annotations.NotNull CommandSender sender, @org.jetbrains.annotations.NotNull String command, CommandInformation information, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("&cPlease provide a valid <value> for the command usage: &a[/database <value>]");
            return true;
        }


        switch (args[0]) {
            case "reload" -> {
                if(!Main.getConf().getBoolean("database.isSQL")) { // SQL is set to false
                    sender.sendMessage(Main.getConf().getString("errorPrefix") + "&cCannot reload Database if SQL is disabled! Please enabled SQL to use this command!");
                    return true;
                } else if(Main.getConf().getBoolean("server.isDev")) { // isDev is set to true
                    sender.sendMessage(Main.getConf().getString("errorPrefix") + "&cCannot reload Database if Developer Mode is enabled! Please disable Developer Mode to use this command!");
                    return true;
                } else if (!Main.getConf().getBoolean("database.enabled")) { // Database is set to false (SQL is obviously going to be disabled)
                    sender.sendMessage(Main.getConf().getString("errorPrefix") + "&cCannot reload Database if Database is disabled! Please enable Database and SQL to use this command!");
                    return true;
                }

                // If Database is enabled and active, then run the reload command
                DatabaseHandler.closeConnection(); // Close the connection if it is still open
                DatabaseHandler.refreshDatabase(true); // Attempt to reopen the connection

                return true;
            }
            case "uptime" -> {
                if(DatabaseHandler.isDatabaseDown && DatabaseHandler.databaseStartTime != 0L && DatabaseHandler.databaseEndTime != 0L) {
                    sender.sendMessage(Main.getConf().getString("errorPrefix") + "&cDatabase is currently down! Uptime before Database went down down: " + (double)Math.round (((double) (DatabaseHandler.databaseEndTime - DatabaseHandler.databaseStartTime) / 60000) * 100000d)  / 100000d + " minutes!");
                    return true;
                } else if(!DatabaseHandler.isDatabaseDown && DatabaseHandler.databaseStartTime != 0L) {
                    sender.sendMessage(Main.getConf().getString("prefix") + "&aDatabase has been online for " + (double)Math.round (((double) (System.currentTimeMillis() - DatabaseHandler.databaseStartTime) / 60000) * 100000d)  / 100000d + " minutes!");
                } else {
                    sender.sendMessage(Main.getConf().getString("errorPrefix") + "&cAn error occurred while running this command!");
                    return true;
                }

                return true;
            }
            default -> {
                sender.sendMessage("&cPlease provide a valid <value> for the command usage: &a[/database <value>]");
                return true;
            }
        }
    }

    @Override
    public List<String> tab(@org.jetbrains.annotations.NotNull CommandSender sender, @org.jetbrains.annotations.NotNull String command, CommandInformation information, String[] args) {
        if (args.length == 1)
            return List.of("reload", "uptime"); // Only one command for now
        return null;
    }
}
