package dev.hevav.pfbot.api;

import dev.hevav.pfbot.types.LocalizedString;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;

public class Translator {
    /**
     * Get localized string
     *
     * @param string LocalizedString instance
     * @param guild Guild for overrides and region
     * @return translated string
     */
    public static String translateString(LocalizedString string, Guild guild){
        Region region = guild.getRegion();
        Region overriddenRegion = Database.getOverriddenRegion(guild.getIdLong());
        if(overriddenRegion != null)
            region = overriddenRegion;
        switch (region){
            case INDIA:
                return (string.indianString == null)? string.englishString : string.indianString;
            case BRAZIL:
                return (string.brazilianString == null)? string.englishString : string.brazilianString;
            case JAPAN:
                return (string.japaneseString == null)? string.englishString : string.japaneseString;
            case RUSSIA:
                return (string.russianString == null)? string.englishString : string.russianString;
            case HONG_KONG:
                return (string.chineseString == null)? string.englishString : string.chineseString;
            default:
                return string.englishString;
        }
    }
}
