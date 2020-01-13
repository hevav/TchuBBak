package dev.hevav.pfbot.API;

import net.dv8tion.jda.api.Region;

/**
 * Strings localized by server's region
 *
 * @author hevav
 * @since 1.0
 */
public class LocalizedString {
    public String russianString;
    public String englishString;
    public String brazilianString;
    public String indianString;
    public String japaneseString;
    public String chineseString;

    /**
     * Create localized string
     *
     * @param _russianString Russian string
     * @param _englishString English string
     * @param _brazilianString Brazilian string
     * @param _indianString Indian string
     * @param _japaneseString Japanese string
     * @param _chineseString Chinese string
     */
    public LocalizedString (String _russianString, String _englishString, String _brazilianString, String _indianString, String _japaneseString, String _chineseString){
        russianString = _russianString;
        englishString = _englishString;
        brazilianString = _brazilianString;
        indianString = _indianString;
        japaneseString = _japaneseString;
        chineseString = _chineseString;
    }

    /**
     * Get localized string
     *
     * @param localizedString LocalizedString instance
     * @param region Server region
     * @return localized string
     */
    public static String getLocalizedString(LocalizedString localizedString, Region region){
        switch (region){
            case INDIA:
                return (localizedString.indianString == null)? localizedString.englishString : localizedString.indianString;
            case BRAZIL:
                return (localizedString.brazilianString == null)? localizedString.englishString : localizedString.brazilianString;
            case JAPAN:
                return (localizedString.japaneseString == null)? localizedString.englishString : localizedString.japaneseString;
            case RUSSIA:
                return (localizedString.russianString == null)? localizedString.englishString : localizedString.russianString;
            case HONG_KONG:
                return (localizedString.chineseString == null)? localizedString.englishString : localizedString.chineseString;
            default:
                return localizedString.englishString;
        }
    }
}
