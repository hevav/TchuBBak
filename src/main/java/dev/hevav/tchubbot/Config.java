package dev.hevav.tchubbot;

import dev.hevav.tchubbot.i18n.LocalizedString;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.modules.builtin.*;
import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration for TchuBBak
 *
 * @author hevav
 * @since 1.0
 */
public class Config {
    //Modules to load globally
    public static Module[] modules = new Module[]{
            new Moderation(),
            new Voice(),
            new Music(),
            new Help(),
            new Status()
    };

    //YouTube v3 api token
    public static String yt_token;
    //Discord bot token
    public static String bot_token;
    //Prefix to trigger
    public static String bot_prefix;
    public static String log_level;
    public static String db_string;
    public static String tts_voice_dir;
    public static LocalizedString vosk_api = new LocalizedString();
    public static JDA api;

    public static Logger logger;

    public static void fillConfig(String[] args){
        logger = LogManager.getLogger("TchuBBak");
        yt_token = System.getenv("pf_yt_token");
        bot_token = System.getenv("pf_bot_token");
        bot_prefix = System.getenv("pf_bot_prefix");
        log_level = System.getenv("pf_log_level");
        db_string = System.getenv("pf_db_string");
        tts_voice_dir = System.getenv("pf_tts_voice_dir");

        vosk_api.russianString = System.getenv("pf_vosk_ru");
        vosk_api.englishString = System.getenv("pf_vosk_en");
        vosk_api.brazilianString = System.getenv("pf_vosk_br");
        vosk_api.indianString = System.getenv("pf_vosk_in");
        vosk_api.japaneseString = System.getenv("pf_vosk_jp");
        vosk_api.chineseString = System.getenv("pf_vosk_ch");

        for(String arg : args){
            String[] arg_split = arg.split("=");
            switch(arg_split[0]){
                case "bot_token":
                    bot_token = arg_split[1];
                    break;
                case "bot_prefix":
                    bot_prefix = arg_split[1];
                    break;
                case "log_level":
                    log_level = arg_split[1];
                    break;
                case "yt_token":
                    yt_token = arg_split[1];
                    break;
                case "db_string":
                    db_string = arg_split[1];
                    break;
                case "tts_voice_dir":
                    tts_voice_dir = arg_split[1];
                    break;
                case "vosk_ru":
                    vosk_api.russianString = arg_split[1];
                    break;
                case "vosk_en":
                    vosk_api.englishString = arg_split[1];
                    break;
                case "vosk_br":
                    vosk_api.brazilianString = arg_split[1];
                    break;
                case "vosk_in":
                    vosk_api.indianString = arg_split[1];
                    break;
                case "vosk_jp":
                    vosk_api.japaneseString = arg_split[1];
                    break;
                case "vosk_ch":
                    vosk_api.chineseString = arg_split[1];
                    break;
                default:
                    logger.warn(String.format("Wrong variable %s", arg_split[0]));
                    break;
            }
        }
    }
}
