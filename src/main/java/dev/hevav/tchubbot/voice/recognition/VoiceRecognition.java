package dev.hevav.tchubbot.voice.recognition;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.modules.Module;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.rec_start;

public class VoiceRecognition {
    private static final HashMap<Long, VoiceRecognition> wordMap = new HashMap<>();

    private long latestPing = Long.MAX_VALUE;
    private final SpeechClient speechClient = SpeechClient.create();
    private final RecognitionConfig config;
    private final ByteArrayOutputStream audioBuf = new ByteArrayOutputStream();

    public VoiceRecognition(Guild guild, User user) throws IOException {
        config =
            RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode(Translator.lanCodeByGuild(guild))
                .build();

        VoiceRecognition instance = this;

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(System.currentTimeMillis() - latestPing >= 500) {
                    latestPing = Long.MAX_VALUE;
                    if(System.currentTimeMillis() - latestPing >= 15000){
                        audioBuf.reset();
                        return;
                    }

                    String[] received = VoiceRecognition.getResponse(instance);
                    if (received != null) {
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
                                if (module.audioTriggers.stream().anyMatch(s -> Translator.translateString(s.trigger, guild).contains(receivedFinal[0]))) {
                                    module.onVoice(guild.getMember(user), receivedFinal);
                                }
                            }
                        }
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 500);
    }

    public static void createUserVoiceRecognition(User user, Guild guild){
        try {
            if(!wordMap.containsKey(user.getIdLong()))
                wordMap.put(user.getIdLong(), new VoiceRecognition(guild, user));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void recognize(User user, byte[] audioBuf){
        VoiceRecognition rec = wordMap.get(user.getIdLong());

        try {
            rec.latestPing = System.currentTimeMillis();
            rec.audioBuf.write(audioBuf);
        } catch (Exception e) {
            Config.logger.warn(e);
        }
    }

    public static String[] getResponse(VoiceRecognition rec){
        try {
            if(rec != null){
                Config.logger.trace("lol");
                if(rec.audioBuf.size() < 3840*50)
                    throw new Exception("Very short phrase");
                RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(fromDiscord(rec.audioBuf.toByteArray()))).build();
                RecognizeResponse response = rec.speechClient.recognize(rec.config, audio);

                rec.latestPing = Long.MAX_VALUE;
                rec.audioBuf.reset();

                return response.getResults(0).getAlternatives(0).getTranscript().toLowerCase().split(" ");
            }
        } catch (Exception e) {
            Config.logger.warn(e);
        }
        return null;
    }

    public static byte[] fromDiscord(byte[] discordBytes) {
        try {
            AudioFormat target = new AudioFormat(16000f, 16, 1, true, false);
            AudioInputStream is = AudioSystem.getAudioInputStream(target,
                new AudioInputStream(new ByteArrayInputStream(discordBytes), AudioReceiveHandler.OUTPUT_FORMAT, discordBytes.length));

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            IOUtils.copy(is, os);

            return os.toByteArray();
        } catch(Exception e) {
            Config.logger.warn(e);
            return new byte[]{};
        }
    }

    public static void closeUserVoiceRecognition(User user){
        wordMap.remove(user.getIdLong());
    }
}
