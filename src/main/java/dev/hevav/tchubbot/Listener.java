package dev.hevav.tchubbot;

import dev.hevav.tchubbot.helpers.DatabaseHelper;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.voice.VoiceRecognition;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.rec_start;
import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.rec_stop;

/**
 * Loads modules by trigger
 *
 * @author hevav
 * @since 1.0
 */
public class Listener extends ListenerAdapter{
    private static final Logger logger = Config.logger;

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String[] content = event.getMessage().getContentRaw().split(" ");
        if (!content[0].startsWith(Config.bot_prefix)) return;
        content[0] = content[0].substring(Config.bot_prefix.length());
        String msg_trigger = content[0];
        String[] disabledModulesString = DatabaseHelper.getDisabledModules(event.getGuild().getIdLong());
        List<String> disabledModules = new ArrayList<>();
        if(disabledModulesString != null)
            disabledModules = Arrays.asList(disabledModulesString);
        for (Module module : Config.modules) {
            if(module.triggers.stream().anyMatch(s -> s.trigger.equals(msg_trigger))){
                if(disabledModules.contains(module.shortName)) {
                    logger.trace("Trigger was found, but triggered module is disabled");
                    return;
                }
                module.onMessage(event, content);
                logger.trace("Proceeded " + module.getClass().getName());
                return;
            }
        }
    }

    @Override
    public void onReady(ReadyEvent event){
        event.getJDA().getGuilds().forEach(guild -> {
            if(!DatabaseHelper.guildExist(guild.getIdLong())) {
                logger.trace(String.format("Adding guild %s to db", guild.getName()));
                DatabaseHelper.addGuild(guild.getIdLong());
            }
        });
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event){
        Guild guild = event.getGuild();
        DatabaseHelper.addGuild(guild.getIdLong());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        DatabaseHelper.removeGuild(event.getGuild().getIdLong());
    }
}
