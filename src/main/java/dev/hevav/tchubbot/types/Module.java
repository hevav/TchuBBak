package dev.hevav.tchubbot.types;

import dev.hevav.tchubbot.api.Config;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Interface to create modules
 *
 * @author hevav
 * @since 1.0
 */
public interface Module {
    /**
     * Short name of module
     *
     * @return short name
     */
    String shortName();
    /**
     * Description of module
     *
     * @return description
     */
    LocalizedString description();
    /**
     * Trigger to load this module
     *
     * @return trigger to load this module
     */
    List<Trigger> triggers();

    /**
     * AudioTrigger to load this module
     *
     * @return trigger to load this module
     */
    List<Trigger> audioTriggers();

    /**
     * Process event for this module
     *
     * @param event event
     * @param parsedText trigger with excluded prefix and other text
     */
    void onMessage(GuildMessageReceivedEvent event, String[] parsedText);

    /**
     * Process event for this module
     *
     * @param event event
     * @param trigger trigger with excluded prefix
     */
    void onVoice(VoiceChannel event, String trigger);

    /**
     * Initialize module after API was created
     */
    void onInit(WeakReference<Config> _boot);

    /**
     * Executes any operation every tick(1m)
     */
    void onTick();
}
