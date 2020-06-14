package dev.hevav.pfbot.api;

import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.Map;

public class VoiceAdapter {
    public static Map<Long, VoiceChannel> voiceChannels;

    public static VoiceChannel getChannel(Long id){
        return voiceChannels.get(id);
    }

    public static boolean isConnected(Long id){
        return voiceChannels.containsKey(id);
    }

    public static void joinChannel(VoiceChannel channel){
        if(!isConnected(channel.getGuild().getIdLong()))
            voiceChannels.put(channel.getGuild().getIdLong(), channel);
    }

}
