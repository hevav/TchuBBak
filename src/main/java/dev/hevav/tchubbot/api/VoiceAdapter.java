package dev.hevav.tchubbot.api;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Map;

public class VoiceAdapter {
    public static Map<Long, VoiceChannel> voiceChannels;

    public static VoiceChannel getChannel(Long guildId, VoiceChannel fallbackVoice){
        joinChannel(fallbackVoice);
        return voiceChannels.get(guildId);
    }

    public static boolean isConnected(Long id){
        return voiceChannels.containsKey(id);
    }

    public static void joinChannel(VoiceChannel channel){
        Guild guild = channel.getGuild();
        Long guildId = guild.getIdLong();
        if(!isConnected(guildId))
            voiceChannels.put(guildId, channel);
        AudioManager audioManager = guild.getAudioManager();
        if(!audioManager.isConnected() && !audioManager.isAttemptingToConnect())
            audioManager.openAudioConnection(voiceChannels.get(guildId));
    }

}
