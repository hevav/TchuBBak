package dev.hevav.tchubbot;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.api.Database;
import dev.hevav.tchubbot.types.Module;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        String[] content = event.getMessage().getContentRaw().split(" ");
        logger.debug("New message");
        if (!content[0].startsWith(bot_prefix)) return;
        content[0] = content[0].substring(bot_prefix.length());
        String msg_trigger = content[0];
        List<String> disabledModules = Arrays.asList(Database.getDisabledModules(event.getGuild().getIdLong()));
        for (Module module : modules) {
            if(module.triggers().stream().anyMatch(s -> s.trigger.equals(msg_trigger))){
                if(disabledModules.contains(module.shortName())) {
                    logger.trace("Trigger was found, but triggered module is disabled");
                    return;
                }
                module.onMessage(event, content);
                logger.trace("Proceeded " + module.getClass().getName());
                return;
            }
            logger.trace("No trigger was found");
        }
        logger.trace("End message");
    }
}
