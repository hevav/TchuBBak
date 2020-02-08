package dev.hevav.pfbot.modules;

import dev.hevav.pfbot.Boot;
import dev.hevav.pfbot.api.EmbedHelper;
import dev.hevav.pfbot.api.LocalizedString;
import dev.hevav.pfbot.api.Module;
import dev.hevav.pfbot.api.Trigger;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Help command module
 *
 * @author hevav
 * @since 1.0
 */
public class Help implements Module {
    private final Logger logger = LogManager.getLogger("PFbot");
    private String bot_prefix;
    private WeakReference<Module[]> modules_ref;
    private LocalizedString helpDescription = new LocalizedString(
            "Справка",
            "Help page",
            null,
            null,
            null,
            null);

    public Trigger[] triggers() {
        return new Trigger[]{new Trigger("help", helpDescription), new Trigger("h", helpDescription)};
    }

    public void onInit(WeakReference<Boot> _boot){
        Boot boot = _boot.get();
        boot.api_ref.get().getPresence().setActivity(Activity.listening(String.format("%shelp", boot.bot_prefix)));
        bot_prefix = boot.bot_prefix;
        modules_ref = boot.modules_ref;
        logger.debug("Module Help was initialized");
    }

    public void onMessage(GuildMessageReceivedEvent event, String trigger) {
        switch (trigger) {
            case "help":
            case "h":
                List<List<MessageEmbed.Field>> modules = new ArrayList<>();
                int fieldCount = 0;
                int fieldListCount = 0;
                modules.add(new ArrayList<>());
                for (Module module : modules_ref.get()) {
                    for (Trigger trigger1 : module.triggers()) {
                        fieldCount++;
                        modules.get(fieldListCount).add(new MessageEmbed.Field(bot_prefix + trigger1.trigger, trigger1.description.getLocalizedString(event.getGuild().getRegion()), true));
                        if(fieldCount == 25){
                            fieldCount = 0;
                            fieldListCount++;
                            modules.add(new ArrayList<>());
                        }
                    }
                }
                for(List<MessageEmbed.Field> field : modules)
                    EmbedHelper.sendEmbed(helpDescription.getLocalizedString(event.getGuild().getRegion()), "", event.getChannel(), field);
                break;
            default:
                logger.warn(String.format("Proceeded strange trigger %s", trigger));
                break;
        }
    }
}
