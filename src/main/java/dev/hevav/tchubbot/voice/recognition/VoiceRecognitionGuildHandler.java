package dev.hevav.tchubbot.voice.recognition;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.jetbrains.annotations.NotNull;

public class VoiceRecognitionGuildHandler implements AudioReceiveHandler {
    public GuildChannel channel;

    public VoiceRecognitionGuildHandler (GuildChannel channel){
        this.channel = channel;
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio audio){
        VoiceRecognition.recognize(audio.getUser(), audio.getAudioData(1.0));
    }
}
