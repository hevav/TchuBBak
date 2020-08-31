package dev.hevav.tchubbot;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.api.Database;
import dev.hevav.tchubbot.types.Module;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Loads modules by trigger
 *
 * @author hevav
 * @since 1.0
 */
public class Listener extends ListenerAdapter {
    private final Module[] modules;
    private final String bot_prefix;
    private static final Logger logger = LogManager.getLogger(Listener.class.getName());

    public Listener(WeakReference<Config> _boot) {
        Config config = _boot.get();
        modules = Objects.requireNonNull(config).modules;
        bot_prefix = config.bot_prefix;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String[] content = event.getMessage().getContentRaw().split(" ");
        if (!content[0].startsWith(bot_prefix)) return;
        content[0] = content[0].substring(bot_prefix.length());
        String msg_trigger = content[0];
        String[] disabledModulesString = Database.getDisabledModules(event.getGuild().getIdLong());
        List<String> disabledModules = new ArrayList<>();
        if(disabledModulesString != null)
            disabledModules = Arrays.asList(disabledModulesString);
        for (Module module : modules) {
            if(module.triggers().stream().anyMatch(s -> s.trigger.equals(msg_trigger))){
                if(disabledModules.contains(module.shortName())) {
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
            if(!Database.guildExist(guild.getIdLong())) {
                logger.trace(String.format("Adding guild %s to db", guild.getName()));
                Database.addGuild(guild.getIdLong());
            }
        });
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event){
        Guild guild = event.getGuild();
        Database.addGuild(guild.getIdLong());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event){
        Database.removeGuild(event.getGuild().getIdLong());
    }
}
