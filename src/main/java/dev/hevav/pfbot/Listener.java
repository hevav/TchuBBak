package dev.hevav.pfbot;

import dev.hevav.pfbot.api.Config;
import dev.hevav.pfbot.api.Database;
import dev.hevav.pfbot.types.Module;
import dev.hevav.pfbot.types.Trigger;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.crypto.Data;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Loads modules by trigger
 *
 * @author hevav
 * @since 1.0
 */
public class Listener extends ListenerAdapter {
    private final Module[] modules;
    private final String bot_prefix;
    private static final Logger logger = LogManager.getLogger(Listener.class.getName());

    public Listener(WeakReference<Config> _boot) {
        Config config = _boot.get();
        modules = Objects.requireNonNull(config).modules;
        bot_prefix = config.bot_prefix;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String content = event.getMessage().getContentRaw();
        logger.debug("New message");
        if (!content.startsWith(bot_prefix)) return;
        String msg_trigger = content.split(" ")[0].substring(bot_prefix.length());
        List<String> disabledModules = Arrays.asList(Database.getDisabledModules(event.getGuild().getIdLong()));
        for (Module module : modules) {
            if(module.triggers().stream().anyMatch(s -> s.trigger.equals(msg_trigger))){
                if(disabledModules.contains(module))
                    break;
                module.onMessage(event, msg_trigger);
                logger.trace("Proceeded " + module.getClass().getName());
                return;
            }
            logger.trace("No trigger found");
        }
        logger.debug("End message");
    }
}
