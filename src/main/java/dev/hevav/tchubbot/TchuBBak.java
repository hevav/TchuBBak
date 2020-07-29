package dev.hevav.tchubbot;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.api.Database;
import dev.hevav.tchubbot.types.Module;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;

/**
 * Main class
 *
 * @author hevav
 * @since 1.0
 */
public class TchuBBak {
    private static final Logger logger = LogManager.getLogger("TchuBBak");
    public static final String VERSION = "2.0.0";

    public static void main(String[] args) {
        Config config = new Config(args);
        if(config.log_level == null)
            config.log_level = "WARN";
        switch (config.log_level) {
            case "OFF":
                Configurator.setLevel("TchuBBak", Level.OFF);
                break;
            case "FATAL":
                Configurator.setLevel("TchuBBak", Level.FATAL);
                break;
            case "ERROR":
                Configurator.setLevel("TchuBBak", Level.ERROR);
                break;
            case "WARN":
                Configurator.setLevel("TchuBBak", Level.WARN);
                break;
            case "DEBUG":
                Configurator.setLevel("TchuBBak", Level.DEBUG);
                break;
            case "TRACE":
                Configurator.setLevel("TchuBBak", Level.TRACE);
                break;
            default:
                Configurator.setLevel("TchuBBak", Level.INFO);
                break;
        }
        JDA api;
        try {
            api = JDABuilder.createDefault(config.bot_token).build();
        } catch (javax.security.auth.login.LoginException e) {
            logger.fatal("Wrong credentials", e);
            return;
        }
        Database.initializeDatabase(config.db_string);
        config.api_ref = new WeakReference<>(api);
        WeakReference<Config> _config = new WeakReference<>(config);
        for(Module module : config.modules)
            module.onInit(_config);
        api.addEventListener(new Listener(_config));
    }
}
