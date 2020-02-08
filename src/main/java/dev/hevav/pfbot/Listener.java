package dev.hevav.pfbot;

import dev.hevav.pfbot.api.Module;
import dev.hevav.pfbot.api.Trigger;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;

/**
 * Loads modules by trigger
 *
 * @author hevav
 * @since 1.0
 */
public class Listener extends ListenerAdapter {
    private WeakReference<Module[]> modules_ref;
    private String bot_prefix;
    private static final Logger logger = LogManager.getLogger(Listener.class.getName());

    public Listener(WeakReference<Boot> _boot) {
        Boot boot = _boot.get();
        modules_ref = boot.modules_ref;
        bot_prefix = boot.bot_prefix;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String content = event.getMessage().getContentRaw();
        logger.debug("New message");
        if (!content.startsWith(bot_prefix)) return;
        String msg_trigger = content.split(" ")[0].substring(bot_prefix.length());
        for (Module module : modules_ref.get()) {
            for (Trigger trigger : module.triggers()) {
                if (trigger.trigger.equals(msg_trigger)) {
                    module.onMessage(event, msg_trigger);
                    logger.trace("Proceeded " + module.getClass().getName());
                    return;
                }
            }
            logger.trace("No trigger found");
        }
        logger.debug("End message");
    }
}
