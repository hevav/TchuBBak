package dev.hevav.tchubbot.voice;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.modules.Module;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.rec_start;
import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.rec_stop;

public class VoiceRecognitionGuildHandler implements AudioReceiveHandler {
    private final Guild guild;

    public VoiceRecognitionGuildHandler(Guild guild){
        this.guild = guild;
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio audio){
        List<String> received = VoiceRecognition.recognise(audio.getUser(), audio.getAudioData(1.0));
        if(received.contains(rec_start)){
            VoiceRecognition.clearWords(audio.getUser());
        }
        if(received.contains(rec_stop)){
            if(received.size() > 15){
                Config.logger.warn("Very long phrase, probably rec_start didn't start");
                VoiceRecognition.clearWords(audio.getUser());
                return;
            }
            received.remove(0);
            for (Module module : Config.modules) {
                if(module.triggers.stream().anyMatch(s -> s.trigger.equals(received.get(0)))){
                    module.onVoice(guild, (String[]) received.toArray());
                }
            }
        }
    }
}
