package dev.hevav.tchubbot.modules.builtin;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.helpers.EmbedHelper;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import static dev.hevav.tchubbot.i18n.strings.StatusStrings.*;

public class Status extends Module {

    public Status() {
        super(
            "status",
            logDescription,
            Arrays.asList(new Trigger("sl", shortLogDescription), new Trigger("status", logDescription)),
            new ArrayList<>());
    }

    @Override
    public void onMessage(GuildMessageReceivedEvent event, String[] parsedText) {
        Guild guild = event.getGuild();
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptime = rb.getUptime();
        String stringUptime = LocalTime.MIN.plus(
                Duration.ofMillis(uptime)
        ).toString();
        switch (parsedText[0]){
            case "sl":
                EmbedHelper.sendEmbed(Translator.translateString(shortLogDescription, guild), "", event.getChannel(), Arrays.asList(
                        new MessageEmbed.Field(Translator.translateString(upTimeString, guild), stringUptime, true)
                ));
                break;
            case "status":
                EmbedHelper.sendEmbed(Translator.translateString(logDescription, guild), "", event.getChannel(), Arrays.asList(
                        new MessageEmbed.Field(Translator.translateString(upTimeString, guild), stringUptime, true),
                        new MessageEmbed.Field(Translator.translateString(pingString, guild), String.valueOf(Config.api.getGatewayPing()), true),
                        new MessageEmbed.Field(Translator.translateString(guildsString, guild), String.valueOf(Config.api.getGuilds().size()), true)
                        ));
                break;
            default:
                logger.warn(String.format("Proceeded strange trigger %s", parsedText[0]));
                break;
        }
    }
}
