package dev.hevav.pfbot;

import dev.hevav.pfbot.API.Module;
import dev.hevav.pfbot.API.Trigger;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads modules by trigger
 *
 * @author hevav
 * @since 1.0
 */
public class Listener extends ListenerAdapter {
    private Boot boot;
    private static final Logger logger = LogManager.getLogger(Listener.class.getName());

    public Listener(Boot _boot) {
        boot = _boot;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String content = event.getMessage().getContentRaw();
        logger.debug("New message");
        if(!content.startsWith(boot.bot_prefix)) return;
        new Thread(() -> {
            String msg_trigger = content.split(" ")[0].substring(boot.bot_prefix.length());
            for (Module module : boot.modules) {
                for (Trigger trigger : module.triggers()) {
                    if (trigger.trigger.equals(msg_trigger)) {
                        module.onMessage(event, msg_trigger);
                        logger.trace("Proceeded " + module.getClass().getName());
                        return;
                    }
                }
                logger.trace("No trigger found");
            }
        }).start();
        logger.debug("End message");
    }
}
