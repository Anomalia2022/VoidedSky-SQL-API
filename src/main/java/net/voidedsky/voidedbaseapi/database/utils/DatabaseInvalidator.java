package net.voidedsky.voidedbaseapi.database.utils;

import net.cybercake.cyberapi.spigot.chat.Broadcast;
import net.cybercake.cyberapi.spigot.chat.Log;
import net.voidedsky.voidedbaseapi.Main;
import net.voidedsky.voidedbaseapi.database.DatabaseHandler;

import java.sql.SQLException;

public class DatabaseInvalidator implements Runnable {
    @Override
    public void run() {
        try {
            // getConnection returns true if connection is good and will then return false canceling the if; isClosed is redundancy and will return false if the connection is valid making both false
            if (!DatabaseHandler.getConnection().isValid(Main.getConf().getInt("database.timeout")) || DatabaseHandler.getConnection().isClosed()) {
                // If it were previously connected and just now detected a disconnect then alert console and privileged staff in game.
                if(DatabaseHandler.isConnected) {
                    Broadcast.chat(Main.getConf().getString("errorPrefix") + "&cDatabase is down! Please contact the Development Team! Database will attempt to reconnect!", "sysadmin.database");
                }

                // Now set isConnected to false.
                DatabaseHandler.isConnected = false;

                // Set databaseDown to true (Used bellow to determine revive of connection)
                DatabaseHandler.isDatabaseDown = true;

                DatabaseHandler.databaseEndTime = System.currentTimeMillis();

                // Attempt to refresh the connection
                DatabaseHandler.refreshDatabase(false);
            } else {
                // This will run if the Database is up or has reconnected (fires 10 minutes after being true above once before)
                if(DatabaseHandler.isDatabaseDown) {
                    // Runs only once as the isDatabaseDown was set true once above. This will now set it back to false now that it is back online.
                    Broadcast.chat(Main.getConf().getString("prefix") + "&aDatabase has since been restored!", "sysadmin.database");
                    DatabaseHandler.isDatabaseDown = false;
                }

                // Finally set the database isConnected to true. Normally wouldn't change unless the else fires post reconnect.
                DatabaseHandler.isConnected = true;
            }
        } catch (SQLException e) {
            Log.error("Error occurred while processing DatabaseInvalidator!");
            throw new RuntimeException(e);
        }
    }
}