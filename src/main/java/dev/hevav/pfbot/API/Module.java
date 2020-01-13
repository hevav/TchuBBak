package dev.hevav.pfbot.API;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * Interface to create modules
 *
 * @author hevav
 * @since 1.0
 */
public interface Module {
    /**
     * Trigger to load this module
     *
     * @return trigger to load this module
     */
    Trigger[] triggers();

    /**
     * Process event for this module
     *
     * @param event event
     * @param trigger trigger with excluded prefix
     */
    void onMessage(GuildMessageReceivedEvent event, String trigger);
}
