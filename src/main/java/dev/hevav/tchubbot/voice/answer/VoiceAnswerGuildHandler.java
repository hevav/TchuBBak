package dev.hevav.tchubbot.voice.answer;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public class VoiceAnswerGuildHandler implements AudioSendHandler {
    private String answerKey;
    private Guild guild;
    private ByteBuffer buffer;

    public VoiceAnswerGuildHandler(String answerKey, Guild guild){
        this.answerKey = answerKey;
        this.guild = guild;
    }

    @Override
    public boolean canProvide() {
        return false;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return null;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
