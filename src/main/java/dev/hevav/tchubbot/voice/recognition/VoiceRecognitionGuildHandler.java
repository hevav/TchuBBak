package dev.hevav.tchubbot.voice.recognition;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.Listener;
import dev.hevav.tchubbot.helpers.DatabaseHelper;
import dev.hevav.tchubbot.i18n.Translator;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.rec_start;

public class VoiceRecognitionGuildHandler implements AudioReceiveHandler {
    public GuildChannel channel;
    private final HashMap<Long, Recognizer> recognizerHashMap = new HashMap<>();

    public VoiceRecognitionGuildHandler (GuildChannel channel){
        this.channel = channel;
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio audio){
        User user = audio.getUser();
        recognizerHashMap.get(user.getIdLong()).addBuffer(audio.getAudioData(1.0));
    }

    public void createUserVoiceRecognition(User user) {
        if(DatabaseHelper.getCustomString(channel.getGuild().getIdLong(), "premiumrec", "false").equals("true"))
            createUserPremiumRecognition(user);
        else
            createUserStandardRecognition(user);
    }

    public void createUserPremiumRecognition(User user){
        Recognizer recognizer = new VoskRecognizer(channel.getGuild().getMember(user), channel);
        recognizer.setHandler(recognizer1 -> {
            String tr_rec_start = Translator.translateString(rec_start, channel.getGuild());
            if(Arrays.asList(recognizer1.recognise()).contains(tr_rec_start)){
                try {
                    Recognizer premiumRecognizer = new GoogleRecognizer(recognizer1.member, recognizer1.channel);
                    premiumRecognizer.audioBuf.write(recognizer1.audioBuf.toByteArray());
                    String[] received = premiumRecognizer.recognise();

                    Listener.doRecognizeWork(received, channel.getGuild(), channel, user);
                } catch (IOException e) {
                    Config.logger.warn(e);
                }
            }
        });
        createUserVoiceRecognition(user, recognizer);
    }

    public void createUserStandardRecognition(User user){
        Recognizer recognizer = new VoskRecognizer(channel.getGuild().getMember(user), channel);
        recognizer.setHandler(recognizer1 -> {
            String[] received = recognizer1.recognise();
            GuildChannel channel = recognizer1.channel;

            Listener.doRecognizeWork(received, channel.getGuild(), channel, user);
        });
        createUserVoiceRecognition(user, recognizer);
    }

    private void createUserVoiceRecognition(User user, Recognizer recognizer){
        if(!recognizerHashMap.containsKey(user.getIdLong()))
            recognizerHashMap.put(user.getIdLong(), recognizer);
    }

    public void closeUserVoiceRecognition(User user){
        recognizerHashMap.remove(user.getIdLong());
    }
}
