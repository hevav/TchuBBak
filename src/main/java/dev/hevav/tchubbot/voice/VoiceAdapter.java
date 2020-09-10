package dev.hevav.tchubbot.voice;

import com.sedmelluq.discord.lavaplayer.demo.jda.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.hevav.tchubbot.helpers.DatabaseHelper;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Collection;
import java.util.HashMap;

public class VoiceAdapter {
    public static HashMap<Long, VoiceChannel> voiceChannels = new HashMap<>();
    public static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private static final HashMap<Long, GuildMusicManager> musicManagers = new HashMap<>();
    private static final HashMap<Long, AudioReceiveHandler> prevReceiveHanlers = new HashMap<>();
    private static final HashMap<Long, AudioSendHandler> prevSendHanlers = new HashMap<>();
    
    public static void initAdapter(){
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public static VoiceChannel getChannel(Long guildId, VoiceChannel fallbackVoice, boolean reconnect){
        joinChannel(fallbackVoice, reconnect);
        return voiceChannels.get(guildId);
    }
    public static synchronized Collection<GuildMusicManager> getGuildAudioPlayers() {
        return musicManagers.values();
    }
    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        switchSendHandler(musicManager.getSendHandler(), guild);

        return musicManager;
    }
    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild, TextChannel channel) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, channel);
            musicManagers.put(guild.getIdLong(), musicManager);
        }

        switchSendHandler(musicManager.getSendHandler(), guild);

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
        String djrole = DatabaseHelper.getCustomString(member.getGuild().getIdLong(), "djrole");
        if(djrole == null)
            djrole = "DJ";
        String finalDjrole = djrole;
        return member.getRoles().stream().anyMatch(r -> r.getName().equals(finalDjrole));
    }

    public static void joinChannel(VoiceChannel channel, boolean reconnect){
        Guild guild = channel.getGuild();
        Long guildId = guild.getIdLong();
        if(reconnect || !isConnected(guildId))
            voiceChannels.put(guildId, channel);
        AudioManager audioManager = guild.getAudioManager();
        if(reconnect || !audioManager.isConnected())
            audioManager.openAudioConnection(voiceChannels.get(guildId));
    }

    public static void leaveChannel(Guild guild){
        if(musicManagers.containsKey(guild.getIdLong()))
            removeGuildAudioPlayer(guild);
    }

    public static void switchReceiveHandler(AudioReceiveHandler handler, Guild guild){
        AudioManager manager = guild.getAudioManager();
        prevReceiveHanlers.put(guild.getIdLong(), manager.getReceivingHandler());
        manager.setReceivingHandler(handler);
    }

    public static void returnReceiveHandler(Guild guild){
        AudioManager manager = guild.getAudioManager();
        manager.setReceivingHandler(prevReceiveHanlers.remove(guild.getIdLong()));
    }

    public static void switchSendHandler(AudioSendHandler handler, Guild guild){
        AudioManager manager = guild.getAudioManager();
        prevSendHanlers.put(guild.getIdLong(), manager.getSendingHandler());
        manager.setSendingHandler(handler);
    }

    public static void returnSendHandler(Guild guild){
        AudioManager manager = guild.getAudioManager();
        manager.setSendingHandler(prevSendHanlers.remove(guild.getIdLong()));
    }
}
