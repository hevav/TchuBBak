package dev.hevav.tchubbot.voice;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import dev.hevav.tchubbot.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

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

    public static void createUserVoiceRecognition(User user){
        try {
            if(!wordMap.containsKey(user.getIdLong()))
                wordMap.put(user.getIdLong(), new VoiceRecognition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> recognise(User user, byte[] audioBuf){
        VoiceRecognition rec = wordMap.get(user.getIdLong());
        rec.webSocket.sendBinary(audioBuf);
        return rec.words;
    }

    public static void clearWords(User user){
        VoiceRecognition rec = wordMap.get(user.getIdLong());
        rec.words.clear();
    }

    public static void closeUserVoiceRecognition(User user){
        if(wordMap.containsKey(user.getIdLong())) {
            VoiceRecognition rec = wordMap.get(user.getIdLong());
            rec.webSocket.sendText("{\"eof\" : 1}");
            wordMap.remove(user.getIdLong());
        }
    }
}
