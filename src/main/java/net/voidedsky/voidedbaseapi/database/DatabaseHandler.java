package net.voidedsky.voidedbaseapi.database;


import net.cybercake.cyberapi.spigot.basic.BetterStackTraces;
import net.cybercake.cyberapi.spigot.chat.Log;
import net.voidedsky.voidedbaseapi.Main;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Class to house and manage all high-importance parts of the Database; Double redundant
public class DatabaseHandler {

    // High-importance variables

    private static Connection connection; // Connection to the database

    public static boolean isConnected = false; // Not connected by default

    public static boolean isSQL = true; // Set false if system has been set to filetree or has fallen back to filetree

    // Call to restart, refresh, or reconnect to the Database
    public static void refreshDatabase(boolean isSQL) {
        // Don't run either Database if it is disabled or if the server is in developer mode
        if (Main.getConf().getBoolean("server.isDev") || !Main.getConf().getBoolean("database.enabled")) {
            Log.info("Database is disabled! Set database.enabled to true or isDev to false in the plugin's config.yml to enable!");
            isConnected = false;
            return;
        }

        if(isSQL) {
            // If true connect to mySQL
            try {
                setConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        // Else it will connect to back up filetree database
        DatabaseHandler.isSQL = filetreeRefresh();

    }


    // Low scope class to handle connection ot the database
    private static void setConnection() throws SQLException {

        // Get start time of connect attempt; Used to calculate time to connect
        long mssDatabase = System.currentTimeMillis();

        // If database is enabled it will now attempt to connect. Failure to connect will fall back to filetree; Run async as to not freeze the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            // Gather all login info from the config.yml
            String host = Main.getConf().getString("database.host");
            String port = Main.getConf().getString("database.port");
            String username = Main.getConf().getString("database.username");
            String database = Main.getConf().getString("database.database");
            String password = Main.getConf().getString("database.password");

            // Attempt to connect to the database; fall back to filetree if failure occurs
            try {
                // Connect and log status
                Log.info("Attempting to connect to DataSource");
                DriverManager.setLoginTimeout(Main.getConf().getInt("database.timeout"));
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
                Log.info("SQL Database Connection Created! Time elapsed: " + (System.currentTimeMillis() - mssDatabase) + "ms!");
                // Check if the connection is fully successful or ignore if it failed and falls back to filetree
                if(!connection.isClosed() && connection.isValid(Main.getConf().getInt("database.timeout"))) {
                    Log.info("SQL Database Connection Valid!");
                    isSQL = true;
                    isConnected = true;
                } else {
                    Log.error("SQL Database Connection is invalid or didn't connect! Killing SQL attempts and falling back to filetree. Run \"/database reload\" to attempt a reconnect");
                    Bukkit.broadcast("SQL Database Connect Invalid! Falling back to Filetree! Run \"/database reload\" to attempt a reconnect", "sysadmin.database");
                    isSQL = filetreeRefresh();
                }
            } catch (SQLException e) {
                Log.error("SQL Database Connect Failed! Falling back to Filetree! Run \"/database reload\" to attempt a reconnect\"");
                Bukkit.broadcast("SQL Database Connect Failed! Falling back to Filetree! Run \"/database reload\" to attempt a reconnect", "sysadmin.database");
                isSQL = filetreeRefresh();
            }
        });
    }

    public static void closeConnection() {
        // Only close if the database connection is valid; return if connection is null
        if(connection == null) {
            return;
        }

        try {
            if(!connection.isClosed()) {
                connection.close();
                Log.info("Database Connection closed successfully!");
            } else {
                Log.info("Database Connection is already closed or never existed!");
            }
        } catch (SQLException e) {
            Log.error("Database Connection failed to close properly!!!");
            throw new RuntimeException(e);
        }
    }


    private static boolean filetreeRefresh() {

        return true; // filetree was successful.
    }

}
