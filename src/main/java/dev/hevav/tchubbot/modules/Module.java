package dev.hevav.tchubbot.modules;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.i18n.LocalizedString;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Interface to create modules
 *
 * @author hevav
 * @since 1.0
 */
public class Module {
    protected final Logger logger = Config.logger;
    /**
     * Short name of module
     */
    public String shortName;
    /**
     * Description of module
     */
    public LocalizedString description;
    /**
     * Trigger to load this module
     */
    public List<Trigger> triggers;

    /**
     * AudioTrigger to load this module
     */
    public List<Trigger> audioTriggers;

    public Module(String shortName, LocalizedString description, List<Trigger> triggers, List<Trigger> audioTriggers){
        this.shortName = shortName;
        this.description = description;
        this.triggers = triggers;
        this.audioTriggers = audioTriggers;
    }

    /**
     * Process event for this module
     *
     * @param event event
     * @param parsedText trigger with excluded prefix and other text
     */
    public void onMessage(GuildMessageReceivedEvent event, String[] parsedText){

    }

    /**
     * Process event for this module
     *
     * @param event event
     * @param trigger trigger with excluded prefix
     */
    public void onVoice(VoiceChannel event, String trigger){

    }

    /**
     * Initialize module after API was created
     */
    public void onInit(){

    }

    /**
     * Executes any operation every tick(1m)
     */
    public void onTick(){

    }
}
