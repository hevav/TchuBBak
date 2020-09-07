package dev.hevav.tchubbot.modules.builtin;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.Listener;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.voice.VoiceAdapter;
import dev.hevav.tchubbot.types.Trigger;
import dev.hevav.tchubbot.voice.VoiceRecognition;
import dev.hevav.tchubbot.voice.VoiceRecognitionGuildHandler;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.*;

public class Voice extends Module {
    private boolean recognitionEnabled = false;

    public Voice() {
        super("voice",
                voiceDescription,
                Arrays.asList(new Trigger("rejoin", rejoinDesctiption),
                    new Trigger("rj", rejoinDesctiption),
                    new Trigger("leave", leaveDescription),
                    new Trigger("l", leaveDescription),
                    new Trigger("sr", recogniseDescription)),
                new ArrayList<>());
    }

    @Override
    public void onMessage(GuildMessageReceivedEvent event, String[] parsedText) {
        switch (parsedText[0]){
            case "rejoin":
            case "rj":
                if(VoiceAdapter.hasDJ(event.getMember()) && event.getMember().getVoiceState().inVoiceChannel())
                    VoiceAdapter.joinChannel(event.getMember().getVoiceState().getChannel(), true);
                break;
            case "leave":
            case "l":
                if(VoiceAdapter.hasDJ(event.getMember()))
                    event.getGuild().getAudioManager().closeAudioConnection();
                break;
            case "sr":
                VoiceAdapter.joinChannel(event.getMember().getVoiceState().getChannel(), false);
                VoiceAdapter.switchReceiveHandler(new VoiceRecognitionGuildHandler(event.getGuild()), event.getGuild());
                recognitionEnabled = true;
                break;
            case "stopr":
                VoiceAdapter.returnReceiveHandler(event.getGuild());
                recognitionEnabled = false;
                break;
        }
    }

    @Override
    public void onInit() {
        VoiceAdapter.initAdapter();
        Config.api.addEventListener(new VoiceListener());
    }

    private class VoiceListener extends ListenerAdapter {
        @Override
        public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
            if(event.getMember().getIdLong() == Config.api.getSelfUser().getIdLong())
                VoiceAdapter.leaveChannel(event.getGuild());
            else if(recognitionEnabled)
                VoiceRecognition.closeUserVoiceRecognition(event.getMember().getUser());
        }

        @Override
        public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
            if(recognitionEnabled)
                VoiceRecognition.createUserVoiceRecognition(event.getMember().getUser());
        }
    } 
}
