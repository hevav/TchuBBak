package dev.hevav.pfbot.api;

import net.dv8tion.jda.api.Region;

public class Database {
    public static void initializeDatabase(String mongoString){

    }

    public static Region getOverriddenRegion(Long id){
        return null;
    }

    public static String[] getDisabledModules(Long id){
        return new String[0];
    }
}
