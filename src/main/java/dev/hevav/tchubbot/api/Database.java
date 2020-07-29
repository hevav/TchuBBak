package dev.hevav.tchubbot.api;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.Region;
import org.bson.Document;

import java.net.UnknownHostException;

/**
 * Class to interact with Database
 *
 * @author hevav
 * @since 2.0.0
 */
public class Database {
    private static MongoDatabase database;
    /**
     * Initialize Database
     *
     * @param mongoString MongoDB connection string
     */
    public static void initializeDatabase(String mongoString) {
        database = new MongoClient(new MongoClientURI(mongoString)).getDatabase("tchubbase");
    }

    /**
     * Gets overridden(if was) Region for translations
     *
     * @param guildId Discord Guild id
     * @return Overridden region
     */
    public static Region getOverriddenRegion(Long guildId){
        String regionString = (String)database.getCollection("guilds").find(new Document().append("guildId", guildId)).first().get("global_region");
        if(regionString == null)
            return null;
        switch (regionString){
            default:
                return Region.US_CENTRAL;
            case "ru":
                return Region.RUSSIA;
        }
    }

    /**
     * Gets disabled module for module toggles
     *
     * @param guildId Discord Guild id
     * @return Disabled modules array
     */
    public static String[] getDisabledModules(Long guildId){
        return (String[])database.getCollection("guilds").find(new Document().append("guildId", guildId)).first().get("disabled_modules");
    }

    /**
     * Gets custom field
     *
     * @param guildId Discord Guild id*
     * @return Custom field as String
     */
    public static String getCustomString(Long guildId, String fieldName){
        return (String)database.getCollection("guilds").find(new Document().append("guildId", guildId)).first().get(fieldName);
    }

    /**
     * Gets disabled module for module toggles
     *
     * @param guildId Discord Guild id
     * @param fieldName Custom field name
     * @param fieldValue Custom field value
     */
    public static void setCustomString(Long guildId, String fieldName, String fieldValue){
        Document object = database.getCollection("guilds").find(new Document().append("guildId", guildId)).first();
        assert object != null;
        object.remove(fieldName);
        object.put(fieldName, fieldValue);
        database.getCollection("guilds").findOneAndUpdate(new Document().append("guildId", guildId), object);
    }

    public static void addGuild(Long guildId, String guildName, String guildPhoto){
        Document dbObject = new Document();
        dbObject.append("guildId", guildId);
        dbObject.append("guildName", guildName);
        dbObject.append("guildPhoto", guildPhoto);
        database.getCollection("guilds").insertOne(dbObject);
    }

    public static void removeGuild(Long guildId){
        database.getCollection("guilds").deleteOne(new Document().append("guildId", guildId));
    }

    public static boolean guildExist(Long guildId){
        return database.getCollection("guilds").countDocuments(new Document().append("guildId", guildId)) > 0;
    }
}
