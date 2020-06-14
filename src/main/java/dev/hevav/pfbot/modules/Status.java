package dev.hevav.pfbot.modules;

import dev.hevav.pfbot.api.Config;
import dev.hevav.pfbot.api.EmbedHelper;
import dev.hevav.pfbot.api.Translator;
import dev.hevav.pfbot.types.Module;
import dev.hevav.pfbot.types.Trigger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.hevav.pfbot.translations.StatusStrings.*;

public class Status implements Module {

    private WeakReference<JDA> api_ref;
    private final Logger logger = LogManager.getLogger("PFbot");

    public void onInit(WeakReference<Config> _boot) {
        Config config = _boot.get();
        assert config != null;
        api_ref = config.api_ref;
        logger.debug("Module Status was initialized");
    }

    @Override
    public String shortName() {
        return "status";
    }

    @Override
    public List<Trigger> triggers() {
        return Arrays.asList(new Trigger("sl", shortLogDescription), new Trigger("status", logDescription));
    }

    @Override
    public List<Trigger> audioTriggers() {
        return new ArrayList<Trigger>();
    }

    public void onMessage(GuildMessageReceivedEvent event, String trigger) {
        Guild guild = event.getGuild();
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptime = rb.getUptime();
        String stringUptime = LocalTime.MIN.plus(
                Duration.ofMillis(uptime)
        ).toString();
        switch (trigger){
            case "sl":
                EmbedHelper.sendEmbed(Translator.translateString(shortLogDescription, guild), "", event.getChannel(), Arrays.asList(
                        new MessageEmbed.Field(Translator.translateString(upTimeString, guild), stringUptime, true)
                ));
                break;
            case "status":
                JDA api = api_ref.get();
                assert api != null;
                EmbedHelper.sendEmbed(Translator.translateString(logDescription, guild), "", event.getChannel(), Arrays.asList(
                        new MessageEmbed.Field(Translator.translateString(upTimeString, guild), stringUptime, true),
                        new MessageEmbed.Field(Translator.translateString(pingString, guild), String.valueOf(api.getGatewayPing()), true),
                        new MessageEmbed.Field(Translator.translateString(guildsString, guild), String.valueOf(api.getGuilds().size()), true)
                        ));
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
