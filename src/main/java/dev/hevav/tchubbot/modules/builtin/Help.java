package dev.hevav.tchubbot.modules.builtin;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.helpers.EmbedHelper;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.i18n.LocalizedString;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.hevav.tchubbot.i18n.strings.HelpStrings.helpDescription;

/**
 * Help command module
 *
 * @author hevav
 * @since 1.0
 */
public class Help extends Module {
    private final HashMap<String, List<Trigger>> moduleTriggers = new HashMap<>();
    private final HashMap<String, LocalizedString> modules = new HashMap<>();

    public Help() {
        super("help",
                helpDescription,
                Arrays.asList(new Trigger("help", helpDescription), new Trigger("h", helpDescription)),
                new ArrayList<>());
    }

    public void onInit(){
        Config.api.getPresence().setActivity(Activity.listening(String.format("%shelp", Config.bot_prefix)));
        for (Module module : Config.modules) {
            modules.put(module.shortName, module.description);
            moduleTriggers.put(module.shortName, module.triggers);
        }
        logger.debug("Module Help was initialized");
    }

    @Override
    public void onTick() {

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
                        moduleList.add(new MessageEmbed.Field(String.format("%s%s %s", Config.bot_prefix, parsedText[0], shortName), Translator.translateString(description, event.getGuild()), false));
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
                    for(Trigger trigger : moduleTriggers.get(parsedText[1])){
                        fieldCount++;
                        triggerList.add(new MessageEmbed.Field(Config.bot_prefix + trigger.show_trigger, Translator.translateString(trigger.description, event.getGuild()), false));
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
