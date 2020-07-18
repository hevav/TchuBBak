package dev.hevav.tchubbot.modules;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.api.EmbedHelper;
import dev.hevav.tchubbot.api.Translator;
import dev.hevav.tchubbot.types.LocalizedString;
import dev.hevav.tchubbot.types.Module;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.hevav.tchubbot.translations.AdminStrings.*;

/**
 * Admin features bot
 *
 * @author hevav
 * @since 1.0
 */
public class Admin implements Module {

    private final Logger logger = LogManager.getLogger("TchuBBak");

    public List<Trigger> triggers(){
        return Arrays.asList(new Trigger("purge", "purge <int>", purgeDescription),
                new Trigger("ban", "ban [ip] <member> [message]", banDescription),
                new Trigger("kick", "kick <member> [message]", kickDescription));
    }

    @Override
    public String shortName() {
        return "admin";
    }

    @Override
    public LocalizedString description() {
        return adminDescription;
    }

    @Override
    public List<Trigger> audioTriggers() {
        return new ArrayList<>();
    }

    public void onInit(WeakReference<Config> _boot) {
        logger.debug("Module Admin was initialized");
    }

    @Override
    public void onMessage(GuildMessageReceivedEvent event, String[] parsedText){
        String[] msg_split = event.getMessage().getContentRaw().split(" ");
        switch (parsedText[0]){
            case "purge":
                if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)){
                    EmbedHelper.sendEmbed(Translator.translateString(noPermissions, event.getGuild()),
                            Translator.translateString(noPermissionsFull, event.getGuild()),
                            event.getChannel());
                    return;
                }
                try {
                    int count = Integer.parseInt(msg_split[1]);
                    while (count > 0) {
                        MessageChannel channel = event.getChannel();
                        int deleteCount = count%100;
                        if(deleteCount == 0)
                            deleteCount = 100;
                        logger.trace("removing "+deleteCount+" messages");
                        channel.purgeMessages(channel.getHistory().retrievePast(deleteCount).complete());
                        count -= deleteCount;
                    }
                }
                catch (Exception e){
                    logger.debug("Admin exception",e);
                    EmbedHelper.sendEmbed(Translator.translateString(errorPurgeDescription, event.getGuild()),
                            e.getMessage(),
                            event.getChannel());
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
