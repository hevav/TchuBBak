package dev.hevav.tchubbot.modules;

import dev.hevav.tchubbot.i18n.LocalizedString;
import dev.hevav.tchubbot.types.LocalizedTrigger;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

/**
 * Interface to create modules
 *
 * @author hevav
 * @since 1.0
 */
public class Module {
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
    public List<LocalizedTrigger> audioTriggers;

    public Module(String shortName, LocalizedString description, List<Trigger> triggers, List<LocalizedTrigger> audioTriggers){
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
     * @param parsedText trigger and other text
     */
    public void onVoice(Member event, String[] parsedText){

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
