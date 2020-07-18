package dev.hevav.tchubbot.api;

import net.dv8tion.jda.api.Region;

/**
 * Class to interact with Database
 *
 * @author hevav
 * @since 2.0.0
 */
public class Database {
    /**
     * Initialize Database
     *
     * @param mongoString MongoDB connection string
     */
    public static void initializeDatabase(String mongoString){

    }

    /**
     * Gets overridden(if was) Region for translations
     *
     * @param guildId Discord Guild id
     * @return Overridden region
     */
    public static Region getOverriddenRegion(Long guildId){
        return null;
    }

    /**
     * Gets disabled module for module toggles
     *
     * @param guildId Discord Guild id
     * @return Disabled modules array
     */
    public static String[] getDisabledModules(Long guildId){
        return new String[0];
    }

    /**
     * Gets custom field
     *
     * @param guildId Discord Guild id*
     * @return Custom field as String
     */
    public static String getCustomString(Long guildId, String fieldName){
        return "false";
    }

    /**
     * Gets disabled module for module toggles
     *
     * @param guildId Discord Guild id
     * @param fieldName Custom field name
     * @param fieldValue Custom field value
     */
    public static void setCustomString(Long guildId, String fieldName, String fieldValue){

    }
}
