package dev.hevav.pfbot.Modules;

import dev.hevav.pfbot.Boot;
import dev.hevav.pfbot.API.LocalizedString;
import dev.hevav.pfbot.API.Module;
import dev.hevav.pfbot.API.Trigger;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static dev.hevav.pfbot.API.EmbedHelper.sendEmbed;

/**
 * Help command module
 *
 * @author hevav
 * @since 1.0
 */
public class Help implements Module {
    @Override
    public Trigger[] triggers() {
        return new Trigger[]{new Trigger("help", helpDescription), new Trigger("h", helpDescription)};
    }

    ;

    private final Logger logger = LogManager.getLogger("PFbot");
    private Boot boot;
    private LocalizedString helpDescription = new LocalizedString(
            "Справка",
            "Help page",
            null,
            null,
            null,
            null);

    public Help(Boot _boot) {
        boot = _boot;
    }

    @Override
    public void onMessage(GuildMessageReceivedEvent event, String trigger) {
        switch (trigger) {
            case "help":
            case "h":
                List<List<MessageEmbed.Field>> fields = new ArrayList<>();
                int fieldCount = 0;
                int fieldListCount = 0;
                fields.add(new ArrayList<>());
                for (Module module : boot.modules) {
                    for (Trigger trigger1 : module.triggers()) {
                        fieldCount++;
                        fields.get(fieldListCount).add(new MessageEmbed.Field(boot.bot_prefix + trigger1.trigger, LocalizedString.getLocalizedString(trigger1.description, event.getGuild().getRegion()), true));
                        if(fieldCount == 25){
                            fieldCount = 0;
                            fieldListCount++;
                            fields.add(new ArrayList<>());
                        }
                    }
                }
                for(List<MessageEmbed.Field> field : fields)
                    sendEmbed(LocalizedString.getLocalizedString(helpDescription, event.getGuild().getRegion()), "", event.getChannel(), field);
                break;
        }
    }
}
