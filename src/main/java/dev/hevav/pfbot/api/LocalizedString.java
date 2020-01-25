package dev.hevav.pfbot.api;

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
     * @param region Server region
     * @return localized string
     */
    public String getLocalizedString(Region region){
        switch (region){
            case INDIA:
                return (this.indianString == null)? this.englishString : this.indianString;
            case BRAZIL:
                return (this.brazilianString == null)? this.englishString : this.brazilianString;
            case JAPAN:
                return (this.japaneseString == null)? this.englishString : this.japaneseString;
            case RUSSIA:
                return (this.russianString == null)? this.englishString : this.russianString;
            case HONG_KONG:
                return (this.chineseString == null)? this.englishString : this.chineseString;
            default:
                return this.englishString;
        }
    }
}
