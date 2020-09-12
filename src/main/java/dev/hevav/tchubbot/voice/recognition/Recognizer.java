package dev.hevav.tchubbot.voice.recognition;

import dev.hevav.tchubbot.Config;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public abstract class Recognizer {
    public final ByteArrayOutputStream audioBuf = new ByteArrayOutputStream();
    public final Member member;
    public final GuildChannel channel;

    private long latestPing = Long.MAX_VALUE;
    private Consumer<Recognizer> handler;

    public Recognizer(Member member, GuildChannel channel){
        this.member = member;
        this.channel = channel;

        Recognizer instance = this;

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(System.currentTimeMillis() - latestPing >= 500) {
                    latestPing = Long.MAX_VALUE;
                    if (System.currentTimeMillis() - latestPing >= 15000) {
                        audioBuf.reset();
                        return;
                    }

                    latestPing = Long.MAX_VALUE;
                    handler.accept(instance);
                    audioBuf.reset();
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 500);
    }

    public void setHandler(Consumer<Recognizer> handler){
        this.handler = handler;
    }

    public void addBuffer(byte[] audio20ms){
        try {
            latestPing = System.currentTimeMillis();
            audioBuf.write(audio20ms);
        } catch (IOException e) {
            Config.logger.warn(e);
        }
    }

    public abstract String[] recognise();
}
