package dev.hevav.tchubbot.modules.builtin;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.helpers.DatabaseHelper;
import dev.hevav.tchubbot.helpers.EmbedHelper;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.types.Infraction;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.hevav.tchubbot.i18n.strings.ModerationStrings.*;

/**
 * Admin features bot
 *
 * @author hevav
 * @since 1.0
 */
public class Moderation extends Module {
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy O");

    public Moderation() {
        super(
            "moder",
            moderationDescription,
            Arrays.asList(
                new Trigger("purge", "purge <int>", purgeDescription),
                new Trigger("infr", "infr <member>", infrDescription),
                new Trigger("ban", "ban <member> [time] [message]", banDescription),
                new Trigger("mute", "mute <member> [time] [message]", muteDescription),
                new Trigger("warn", "warn <member> [message]", warnDescription),
                new Trigger("unmute", "unmute <member> [message]", unmuteDescription),
                new Trigger("kick", "kick <member> [message]", kickDescription)),
            new ArrayList<>());
    }

    @Override
    public void onTick() {
        DatabaseHelper.getInfractions().forEach(infraction->{
            if(Long.parseLong(infraction.lastDate) <= System.currentTimeMillis()){
                DatabaseHelper.removeInfraction(infraction);

                switch (infraction.type){
                    case BAN:
                        Config.api.getGuildById(infraction.guildId).unban(String.valueOf(infraction.userId)).queue();
                        break;
                    case MUTE:
                        Guild guild = Config.api.getGuildById(infraction.guildId);
                        guild.removeRoleFromMember(infraction.userId, getMuteRole(guild)).queue();
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
                        Config.logger.trace("removing "+deleteCount+" messages");
                        channel.purgeMessages(channel.getHistory().retrievePast(deleteCount).complete());
                        count -= deleteCount;
                    }
                }
                catch (Exception e){
                    Config.logger.debug("Admin exception",e);
                    EmbedHelper.sendEmbed(Translator.translateString(errorPurgeDescription, event.getGuild()),
                            e.getMessage(),
                            event.getChannel());
                }
                return;
            case "infr":
                Member member = event.getMessage().getMentionedMembers().get(0);
                List<MessageEmbed.Field> fields = new ArrayList<>();
                for (Infraction infraction : DatabaseHelper.getInfractions(event.getGuild().getId(), member.getId())){
                    String reason = infraction.reason;
                    Long lastDate = Long.parseLong(infraction.lastDate);
                    if (lastDate != Long.MAX_VALUE){
                        reason += "\n" + Instant.ofEpochMilli(lastDate).atZone(ZoneId.systemDefault()).format(formatter);
                    }
                    fields.add(new MessageEmbed.Field(infraction.type.toString(), reason, false));
                }
                EmbedHelper.sendEmbed(Translator.translateString(moderationDescription, event.getGuild()), "", event.getChannel(), fields);
                return;
        }
        Member member = event.getMessage().getMentionedMembers().get(0);
        String nickname = member.getUser().getName();
        Long time = null;
        if(parsedText.length > 2)
            time = millisFromString(parsedText[2]);
        String reason = "";
        if(time == null && parsedText.length > 2)
            reason = String.join(" ", Arrays.copyOfRange(parsedText, 2, parsedText.length));
        else if(parsedText.length > 3)
            reason = String.join(" ", Arrays.copyOfRange(parsedText, 3, parsedText.length));
        long lastDateLong = Long.MAX_VALUE;
        if(time != null){
            lastDateLong = System.currentTimeMillis() + time;
        }
        String lastDate = String.valueOf(lastDateLong);
        try {
            switch (parsedText[0]){
                case "ban":
                    if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.BAN_MEMBERS)){
                        EmbedHelper.sendEmbed(Translator.translateString(noPermissions, event.getGuild()),
                                Translator.translateString(noPermissionsFull, event.getGuild()),
                                event.getChannel());
                        return;
                    }
                    event.getGuild().ban(member, 0).reason(reason).complete();
                    DatabaseHelper.addInfraction(new Infraction(Infraction.InfractionType.BAN, reason, lastDate, event.getGuild().getId(), member.getId()));
                    break;
                case "kick":
                    if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.KICK_MEMBERS)){
                        EmbedHelper.sendEmbed(Translator.translateString(noPermissions, event.getGuild()),
                                Translator.translateString(noPermissionsFull, event.getGuild()),
                                event.getChannel());
                        return;
                    }
                    event.getGuild().kick(member).reason(reason).complete();
                    DatabaseHelper.addInfraction(new Infraction(Infraction.InfractionType.KICK, reason, lastDate, event.getGuild().getId(), member.getId()));
                    break;
                case "mute":
                    if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)){
                        EmbedHelper.sendEmbed(Translator.translateString(noPermissions, event.getGuild()),
                                Translator.translateString(noPermissionsFull, event.getGuild()),
                                event.getChannel());
                        return;
                    }
                    event.getGuild().addRoleToMember(member, getMuteRole(event.getGuild())).complete();
                    DatabaseHelper.addInfraction(new Infraction(Infraction.InfractionType.MUTE, reason, lastDate, event.getGuild().getId(), member.getId()));
                    break;
                case "warn":
                    if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)){
                        EmbedHelper.sendEmbed(Translator.translateString(noPermissions, event.getGuild()),
                                Translator.translateString(noPermissionsFull, event.getGuild()),
                                event.getChannel());
                        return;
                    }
                    DatabaseHelper.addInfraction(new Infraction(Infraction.InfractionType.WARN, reason, lastDate, event.getGuild().getId(), member.getId()));
                    break;
                case "unmute":
                    if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)){
                        EmbedHelper.sendEmbed(Translator.translateString(noPermissions, event.getGuild()),
                                Translator.translateString(noPermissionsFull, event.getGuild()),
                                event.getChannel());
                        return;
                    }
                    event.getGuild().removeRoleFromMember(member, getMuteRole(event.getGuild())).complete();
                    break;
                default:
                    Config.logger.warn(String.format("Proceeded strange trigger %s", parsedText[0]));
                    return;
            }
        }
        catch (Exception e){
            Config.logger.debug("Admin exception",e);
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
        String roleId = DatabaseHelper.getCustomString(guild.getIdLong(), "muterole", null);
        if(roleId == null){
            roleId = guild.createRole().setName("MUTED").complete().getId();
            DatabaseHelper.setCustomString(guild.getIdLong(), "muterole", roleId);
        }
        return guild.getRoleById(roleId);
    }

    private static Long millisFromString(String string){
        try {
            long num = Long.parseLong(string.replaceAll("[^0-9]+", ""));
            String typeOfNum = string.substring((int) Math.log10(num) + 1);
            num*=1000;
            switch (typeOfNum){
                case "y":
                    num *= 12;
                case "mo":
                    num *= 30;
                case "w":
                    num *= 7;
                case "d":
                    num *= 24;
                case "h":
                    num *= 60;
                case "m":
                    num *= 60;
                    break;
                default:
                    return null;
            }
            return num;
        }
        catch (NumberFormatException exception) {
            return null;
        }
    }
}
