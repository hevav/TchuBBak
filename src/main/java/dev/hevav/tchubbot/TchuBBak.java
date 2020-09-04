package dev.hevav.tchubbot;

import dev.hevav.tchubbot.helpers.DatabaseHelper;
import dev.hevav.tchubbot.helpers.TickHelper;
import dev.hevav.tchubbot.modules.Module;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Main class
 *
 * @author hevav
 * @since 1.0
 */
public class TchuBBak {
    public static final String VERSION = "2.0.0";

    public static void main(String[] args) {
        Config.fillConfig(args);
        if(Config.log_level == null)
            Config.log_level = "WARN";
        switch (Config.log_level) {
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
            api = JDABuilder.createDefault(Config.bot_token).build();
        } catch (javax.security.auth.login.LoginException e) {
            Config.logger.fatal("Wrong credentials", e);
            return;
        }
        DatabaseHelper.initializeDatabase(Config.db_string);
        TickHelper tickHelper = new TickHelper(Config.modules);
        Config.api = api;
        for(Module module : Config.modules)
            module.onInit();
        api.addEventListener(new Listener());
        tickHelper.doTicks();
    }
}
