package dev.hevav.tchubbot.voice.recognition;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.protobuf.ByteString;
import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.voice.VoiceAdapter;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;

import java.io.IOException;

public class GoogleRecognizer extends Recognizer {
    private final SpeechClient speechClient = SpeechClient.create();
    private final RecognitionConfig config;

    public GoogleRecognizer(Member member, GuildChannel channel) throws IOException {
        super(member, channel);

        config =
            RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode(Translator.lanCodeByGuild(channel.getGuild()))
                .build();
    }

    @Override
    public String[] recognise() {
        try {
            if (audioBuf.size() < 3840 * 50)
                throw new Exception("Very short phrase");
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(VoiceAdapter.fromDiscord(audioBuf.toByteArray(), 16000f))).build();
            RecognizeResponse response = speechClient.recognize(config, audio);

            return response.getResults(0).getAlternatives(0).getTranscript().toLowerCase().split(" ");
        } catch (Exception e) {
            Config.logger.info(e);
        }
        return null;
    }
}
