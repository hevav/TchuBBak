package dev.hevav.tchubbot.api;

import com.sedmelluq.discord.lavaplayer.demo.jda.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class VoiceAdapter {
    public static Map<Long, VoiceChannel> voiceChannels = new HashMap<>();
    public static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private static final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
    
    public static void initAdapter(){
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public static VoiceChannel getChannel(Long guildId, VoiceChannel fallbackVoice, boolean reconnect){
        joinChannel(fallbackVoice, reconnect);
        return voiceChannels.get(guildId);
    }
    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild, TextChannel channel) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, channel);
            musicManagers.put(guild.getIdLong(), musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public static synchronized void removeGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        if (musicManager != null) {
            musicManagers.remove(guild.getIdLong());
        }
    }
    public static boolean isConnected(Long id){
        return voiceChannels.containsKey(id);
    }
    
    public static boolean hasDJ(Member member){
        if(member.hasPermission(Permission.MESSAGE_MANAGE))
            return true;
        return member.getRoles().stream().anyMatch(r -> r.getName().equals(Database.getCustomString(member.getGuild().getIdLong(), "djrole")));
    }

    public static void joinChannel(VoiceChannel channel, boolean reconnect){
        Guild guild = channel.getGuild();
        Long guildId = guild.getIdLong();
        if(reconnect || !isConnected(guildId))
            voiceChannels.put(guildId, channel);
        AudioManager audioManager = guild.getAudioManager();
        if(reconnect || (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()))
            audioManager.openAudioConnection(voiceChannels.get(guildId));
    }

    public static void leaveChannel(Guild guild){
        if(getGuildAudioPlayer(guild) != null)
            removeGuildAudioPlayer(guild);
    }
}
