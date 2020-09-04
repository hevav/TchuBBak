package dev.hevav.tchubbot.modules.builtin;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.voice.VoiceAdapter;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.*;

public class Voice extends Module {
    public Voice() {
        super("voice",
                voiceDescription,
                Arrays.asList(new Trigger("rejoin", rejoinDesctiption),
                    new Trigger("rj", rejoinDesctiption),
                    new Trigger("leave", leaveDescription),
                    new Trigger("l", leaveDescription)),
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
        }
    }

    @Override
    public void onVoice(VoiceChannel event, String trigger) {

    }

    @Override
    public void onInit() {
        VoiceAdapter.initAdapter();
        Config.api.addEventListener(new VoiceListener());
    }

    @Override
    public void onTick() {

    }

    private class VoiceListener extends ListenerAdapter {
        @Override
        public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
            VoiceAdapter.leaveChannel(event.getGuild());
        }
    } 
}
