package dev.hevav.tchubbot.voice;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import dev.hevav.tchubbot.Config;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VoiceRecognition {
    private static final WebSocketFactory factory = new WebSocketFactory();
    private static final HashMap<Long, VoiceRecognition> wordMap = new HashMap<>();

    public List<String> words = new ArrayList<>();
    public WebSocket webSocket = factory.createSocket(Config.vosk_api);

    public VoiceRecognition() throws Exception {
        webSocket.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String message) {
                words.add(message);
            }
        });
        webSocket.connect();
    }

    public static void createGuildVoiceRecognition(Guild guild){
        try {
            if(!wordMap.containsKey(guild.getIdLong()))
                wordMap.put(guild.getIdLong(), new VoiceRecognition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> recognise(Guild guild, byte[] audioBuf){
        VoiceRecognition rec = wordMap.get(guild.getIdLong());
        rec.webSocket.sendBinary(audioBuf);
        return rec.words;
    }

    public static void clearWords(Guild guild){
        VoiceRecognition rec = wordMap.get(guild.getIdLong());
        rec.words.clear();
    }

    public static void closeGuildVoiceRecognition(Guild guild){
        if(wordMap.containsKey(guild.getIdLong())) {
            VoiceRecognition rec = wordMap.get(guild.getIdLong());
            rec.webSocket.sendText("{\"eof\" : 1}");
            wordMap.remove(guild.getIdLong());
        }
    }
}
