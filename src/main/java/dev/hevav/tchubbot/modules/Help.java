package dev.hevav.tchubbot.modules;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.api.EmbedHelper;
import dev.hevav.tchubbot.api.Translator;
import dev.hevav.tchubbot.types.LocalizedString;
import dev.hevav.tchubbot.types.Module;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.hevav.tchubbot.translations.HelpStrings.helpDescription;

/**
 * Help command module
 *
 * @author hevav
 * @since 1.0
 */
public class Help implements Module {
    private final Logger logger = LogManager.getLogger("TchuBBak");
    private String bot_prefix;
    private final HashMap<String, List<Trigger>> triggers = new HashMap<>();
    private final HashMap<String, LocalizedString> modules = new HashMap<>();

    @Override
    public String shortName() {
        return "help";
    }

    @Override
    public LocalizedString description() {
        return helpDescription;
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
            modules.put(module.shortName(), module.description());
            triggers.put(module.shortName(), module.triggers());
        }
        logger.debug("Module Help was initialized");
    }

    public void onMessage(GuildMessageReceivedEvent event, String[] parsedText) {
        switch (parsedText[0]) {
            case "help":
            case "h":
                if(parsedText.length == 1) {
                    List<MessageEmbed.Field> moduleList = new ArrayList<>();
                    AtomicInteger fieldCount = new AtomicInteger();
                    modules.forEach((String shortName, LocalizedString description) -> {
                        fieldCount.getAndIncrement();
                        moduleList.add(new MessageEmbed.Field(String.format("%s%s %s", bot_prefix, parsedText[0], shortName), Translator.translateString(description, event.getGuild()), false));
                        if (fieldCount.get() == 25) {
                            fieldCount.set(0);
                            moduleList.clear();
                            EmbedHelper.sendEmbed(Translator.translateString(helpDescription, event.getGuild()), "", event.getChannel(), moduleList);
                        }
                    });
                    EmbedHelper.sendEmbed(Translator.translateString(helpDescription, event.getGuild()), "", event.getChannel(), moduleList);
                }
                else{
                    List<MessageEmbed.Field> triggerList = new ArrayList<>();
                    int fieldCount = 0;
                    for(Trigger trigger : triggers.get(parsedText[1])){
                        fieldCount++;
                        triggerList.add(new MessageEmbed.Field(bot_prefix + trigger.show_trigger, Translator.translateString(trigger.description, event.getGuild()), false));
                        if (fieldCount == 25) {
                            fieldCount = 0;
                            triggerList.clear();
                            EmbedHelper.sendEmbed(Translator.translateString(helpDescription, event.getGuild()), "", event.getChannel(), triggerList);
                        }
                    }
                    EmbedHelper.sendEmbed(Translator.translateString(helpDescription, event.getGuild()), "", event.getChannel(), triggerList);
                }
                break;
            default:
                logger.warn(String.format("Proceeded strange trigger %s", parsedText[0]));
                break;
        }
    }

    @Override
    public void onVoice(VoiceChannel event, String trigger) {

    }
}
