package dev.hevav.tchubbot.modules;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.api.Database;
import dev.hevav.tchubbot.api.EmbedHelper;
import dev.hevav.tchubbot.api.Translator;
import dev.hevav.tchubbot.types.LocalizedString;
import dev.hevav.tchubbot.types.Module;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.hevav.tchubbot.translations.ModerationStrings.*;

/**
 * Admin features bot
 *
 * @author hevav
 * @since 1.0
 */
public class Moderation implements Module {

    private final Logger logger = LogManager.getLogger("TchuBBak");
    private JDA api;

    public List<Trigger> triggers(){
        return Arrays.asList(new Trigger("purge", "purge <int>", purgeDescription),
                new Trigger("ban", "ban <member> [time] [message]", banDescription),
                new Trigger("mute", "mute <member> [time] [message]", muteDescription),
                new Trigger("unmute", "unmute <member> [time] [message]", unmuteDescription),
                new Trigger("kick", "kick <member> [time] [message]", kickDescription));
    }

    @Override
    public String shortName() {
        return "moder";
    }

    @Override
    public LocalizedString description() {
        return moderationDescription;
    }

    @Override
    public List<Trigger> audioTriggers() {
        return new ArrayList<>();
    }

    public void onInit(WeakReference<Config> _boot) {
        api = _boot.get().api_ref.get();
        logger.debug("Module Moderation was initialized");
    }

    @Override
    public void onTick() {
        Database.getInfractions().forEach(infraction->{
            if(infraction.lastDate > System.currentTimeMillis()){
                switch (infraction.type){
                    case BAN:
                        api.getGuildById(infraction.guildId).unban(String.valueOf(infraction.userId));
                        break;
                    case MUTE:
                        Guild guild = api.getGuildById(infraction.guildId);
                        guild.removeRoleFromMember(infraction.userId, getMuteRole(guild));
                        break;
                }
            }
        });
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
                return;
        }
        Member member = event.getMessage().getMentionedMembers().get(0);
        String nickname = member.getUser().getName();
        String reason = String.join(" ", Arrays.copyOfRange(parsedText, 2, parsedText.length));
        try {
            switch (parsedText[0]){
                case "ban":
                    event.getGuild().ban(member, 0).reason(reason).complete();
                    break;
                case "kick":
                    event.getGuild().kick(member).reason(reason).complete();
                    break;
                case "mute":
                    event.getGuild().addRoleToMember(member, getMuteRole(event.getGuild())).complete();
                    break;
                case "unmute":
                    event.getGuild().removeRoleFromMember(member, getMuteRole(event.getGuild()));
                    break;
                default:
                    logger.warn(String.format("Proceeded strange trigger %s", parsedText[0]));
                    return;
            }
        }
        catch (Exception e){
            logger.debug("Admin exception",e);
            EmbedHelper.sendEmbed(Translator.translateString(errorBanKick, event.getGuild()),
                    e.getMessage(),
                    event.getChannel());
            return;
        }
        EmbedHelper.sendEmbed(Translator.translateString(moderationDescription, event.getGuild()),
                Translator.translateString(successfulBan, event.getGuild())+nickname,
                event.getChannel());
    }

    private static Role getMuteRole(Guild guild){
        Role role = guild.getRoleById(Database.getCustomString(guild.getIdLong(), "muterole"));
        if(role == null){
            role = guild.createRole().setName("MUTED").complete();
            Database.setCustomString(guild.getIdLong(), "muterole", role.getId());
        }
        return role;
    }

    @Override
    public void onVoice(VoiceChannel event, String trigger) {

    }
}
