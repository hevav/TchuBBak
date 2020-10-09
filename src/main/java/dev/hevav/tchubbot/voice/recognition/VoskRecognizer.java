package dev.hevav.tchubbot.voice.recognition;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.voice.VoiceAdapter;
import dev.hevav.tchubbot.voice.WAVHeader;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CountDownLatch;

public class VoskRecognizer extends Recognizer {
    public static WebSocketFactory factory = new WebSocketFactory();

    private CountDownLatch latch = new CountDownLatch(1);
    private final Gson gson = new Gson();
    private String[] words = new String[]{};

    public VoskRecognizer(Member member, GuildChannel channel) {
        super(member, channel);
    }

    @Override
    public String[] recognise() {
        try {
            WebSocket webSocket = factory.createSocket(Translator.translateString(Config.vosk_api, channel.getGuild()));

            webSocket.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) {
                    Answer answer = gson.fromJson(message, Answer.class);
                    if(answer.text != null){
                        words = answer.text.split(" ");
                        latch.countDown();
                    }
                }
            });

            webSocket.connect();

            webSocket.sendText("{\"config\" : {\"sample_rate\" : 16000.0}}");

            byte[] pcmData = VoiceAdapter.fromDiscord(audioBuf.toByteArray(), 16000f);
            webSocket.sendBinary(WAVHeader.build(16000, pcmData.length));
            webSocket.sendBinary(pcmData);

            webSocket.sendText("{\"eof\" : 1}");

            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        latch = new CountDownLatch(1);

        return words;
    }

    private static class Answer{
        String text;
    }
}
