package dev.hevav.tchubbot.helpers;

import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import dev.hevav.tchubbot.types.Infraction;
import net.dv8tion.jda.api.Region;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class to interact with Database
 *
 * @author hevav
 * @since 2.0.0
 */
public class DatabaseHelper {
    private static MongoDatabase database;
    private static final Gson gson = new Gson();
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
    public static String getCustomString(Long guildId, String fieldName, String defaultValue){
        Object answer = database.getCollection("guilds").find(new Document().append("guildId", guildId)).first().get(fieldName);
        if(answer == null)
            return defaultValue;
        return answer.toString();
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
        object.remove(fieldName);
        object.put(fieldName, fieldValue);
        database.getCollection("guilds").findOneAndUpdate(new Document().append("guildId", guildId), object);
    }

    public static void addGuild(Long guildId){
        database.getCollection("guilds").insertOne(new Document().append("guildId", guildId));
    }

    public static void removeGuild(Long guildId){
        database.getCollection("guilds").deleteOne(new Document().append("guildId", guildId));
    }

    public static boolean guildExist(Long guildId){
        return database.getCollection("guilds").countDocuments(new Document().append("guildId", guildId)) > 0;
    }

    public static List<Infraction> getInfractions(Long guildId){
        List<Infraction> infractions = new ArrayList<>();
        database.getCollection("infractions").find(new Document().append("guildId", guildId)).forEach((Consumer<Document>) document -> {
             infractions.add(gson.fromJson(document.toJson(), Infraction.class));
        });
        return infractions;
    }

    public static List<Infraction> getInfractions(){
        List<Infraction> infractions = new ArrayList<>();
        database.getCollection("infractions").find().forEach((Consumer<Document>) document -> {
             infractions.add(gson.fromJson(document.toJson(), Infraction.class));
        });
        return infractions;
    }

    public static void addInfraction(Infraction infraction){
        database.getCollection("infractions").insertOne(Document.parse(gson.toJson(infraction)));
    }

    public static void removeInfraction(Infraction infraction){
        database.getCollection("infractions").deleteOne(Document.parse(gson.toJson(infraction)));
    }
}
