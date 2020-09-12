package dev.hevav.tchubbot;

import dev.hevav.tchubbot.helpers.DatabaseHelper;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.modules.Module;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.rec_start;

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

    public static void doRecognizeWork(String[] received, Guild guild, GuildChannel channel, User user){
        if (received != null && received.length > 1) {

            int receivedInd = 0;
            String tr_rec_start = Translator.translateString(rec_start, guild);
            while(receivedInd < received.length-1){
                if(received[receivedInd].equals(tr_rec_start))
                    break;
                ++receivedInd;
            }
            if(received[receivedInd].equals(tr_rec_start)){
                String[] receivedFinal = Arrays.copyOfRange(received, receivedInd + 1, received.length);
                for (String oneWord : receivedFinal)
                    Config.logger.trace(oneWord);
                for (Module module : Config.modules) {
                    if (module.audioTriggers.size() > 0 && module.audioTriggers.stream().anyMatch(s -> receivedFinal[0].contains(Translator.translateString(s.trigger, guild)))) {
                        module.onVoice(guild.getMember(user), channel, receivedFinal);
                    }
                }
            }
        }
    }
}
