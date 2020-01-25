package dev.hevav.pfbot.modules;

import dev.hevav.pfbot.Boot;
import dev.hevav.pfbot.api.EmbedHelper;
import dev.hevav.pfbot.api.LocalizedString;
import dev.hevav.pfbot.api.Module;
import dev.hevav.pfbot.api.Trigger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;

/**
 * Admin features bot
 *
 * @author hevav
 * @since 1.0
 */
public class Admin implements Module {

    private LocalizedString noPermissions = new LocalizedString(
            "No permissions",
            "Запрещено",
            null,
            null,
            null,
            null);
    private LocalizedString noPermissionsFull = new LocalizedString(
            "You don't have permissions to manage messages",
            "У вас нету права на удаление сообщений",
            null,
            null,
            null,
            null);
    private LocalizedString purgeDescription = new LocalizedString(
            "Удалить <int> сообщений",
            "Remove <int> messages",
            null,
            null,
            null,
            null
    );
    private LocalizedString errorPurgeDescription = new LocalizedString(
            "Ошибка при удалении. Проверьте права бота или число",
            "Removing error. Check bot's permissions or number",
            null,
            null,
            null,
            null
    );

    private final Logger logger = LogManager.getLogger("PFbot");

    public Trigger[] triggers(){
        return new Trigger[]{new Trigger("purge", "purge <int>", purgeDescription)};
    }

    public void onInit(WeakReference<Boot> _boot) {
        logger.debug("Module Admin was initialized");
    }

    @Override
    public void onMessage(GuildMessageReceivedEvent event, String trigger){
        String[] msg_split = event.getMessage().getContentRaw().split(" ");
        Region region = event.getGuild().getRegion();
        switch (trigger){
            case "purge":
                if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)){
                    EmbedHelper.sendEmbed(noPermissions.getLocalizedString(region),
                            noPermissionsFull.getLocalizedString(region),
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
                    EmbedHelper.sendEmbed(errorPurgeDescription.getLocalizedString(region),
                            e.toString(),
                            event.getChannel());
                }
                break;
            default:
                logger.warn(String.format("Proceeded strange trigger %s", trigger));
                break;
        }
    }
}
