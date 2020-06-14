package dev.hevav.pfbot.modules;

import dev.hevav.pfbot.api.Config;
import dev.hevav.pfbot.api.EmbedHelper;
import dev.hevav.pfbot.api.Translator;
import dev.hevav.pfbot.types.Module;
import dev.hevav.pfbot.types.Trigger;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.hevav.pfbot.translations.HelpStrings.helpDescription;

/**
 * Help command module
 *
 * @author hevav
 * @since 1.0
 */
public class Help implements Module {
    private final Logger logger = LogManager.getLogger("PFbot");
    private String bot_prefix;
    private final List<Trigger> triggers = new ArrayList<>();

    @Override
    public String shortName() {
        return "help";
    }

    public List<Trigger> triggers() {
        return Arrays.asList(new Trigger("help", helpDescription), new Trigger("h", helpDescription));
    }

    @Override
    public List<Trigger> audioTriggers() {
        return new ArrayList<>();
    }

    public void onInit(WeakReference<Config> _boot){
        Config config = _boot.get();
        assert config != null;
        Objects.requireNonNull(config.api_ref.get()).getPresence().setActivity(Activity.listening(String.format("%shelp", config.bot_prefix)));
        bot_prefix = config.bot_prefix;
        for (Module module : config.modules) {
            triggers.addAll(module.triggers());
        }
        logger.debug("Module Help was initialized");
    }

    public void onMessage(GuildMessageReceivedEvent event, String trigger) {
        switch (trigger) {
            case "help":
            case "h":
                List<MessageEmbed.Field> modules = new ArrayList<>();
                int fieldCount = 0;
                for (Trigger trigger1 : triggers) {
                    fieldCount++;
                    modules.add(new MessageEmbed.Field(bot_prefix + trigger1.trigger, Translator.translateString(trigger1.description, event.getGuild()), true));
                    if(fieldCount == 25){
                        fieldCount = 0;
                        modules.clear();
                        EmbedHelper.sendEmbed(Translator.translateString(helpDescription, event.getGuild()), "", event.getChannel(), modules);
                    }
                }
                EmbedHelper.sendEmbed(Translator.translateString(helpDescription, event.getGuild()), "", event.getChannel(), modules);
                break;
            default:
                logger.warn(String.format("Proceeded strange trigger %s", trigger));
                break;
        }
    }

    @Override
    public void onVoice(VoiceChannel event, String trigger) {

    }
}
