package dev.hevav.pfbot;

import dev.hevav.pfbot.api.Config;
import dev.hevav.pfbot.types.Module;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.lang.ref.WeakReference;

/**
 * Main class
 *
 * @author hevav
 * @since 1.0
 */
public class PFbot {

    private static final Logger logger = LogManager.getLogger("PFbot");

    public static void main(String[] args) {
        Config config = new Config(args);
        if(config.log_level == null)
            config.log_level = "WARN";
        switch (config.log_level) {
            case "OFF":
                Configurator.setLevel("PFbot", Level.OFF);
                break;
            case "FATAL":
                Configurator.setLevel("PFbot", Level.FATAL);
                break;
            case "ERROR":
                Configurator.setLevel("PFbot", Level.ERROR);
                break;
            case "WARN":
                Configurator.setLevel("PFbot", Level.WARN);
                break;
            case "DEBUG":
                Configurator.setLevel("PFbot", Level.DEBUG);
                break;
            case "TRACE":
                Configurator.setLevel("PFbot", Level.TRACE);
                break;
            default:
                Configurator.setLevel("PFbot", Level.INFO);
                break;
        }
        JDA api;
        try {
            api = JDABuilder.createLight(config.bot_token).build();
        } catch (javax.security.auth.login.LoginException e) {
            logger.fatal("Wrong credentials", e);
            return;
        }
        config.api_ref = new WeakReference<>(api);
        WeakReference<Config> _config = new WeakReference<>(config);
        for(Module module : config.modules)
            module.onInit(_config);
        api.addEventListener(new Listener(_config));
        _config = null;
    }
}
