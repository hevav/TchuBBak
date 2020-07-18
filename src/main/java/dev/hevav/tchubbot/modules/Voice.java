package dev.hevav.tchubbot.modules;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.api.VoiceAdapter;
import dev.hevav.tchubbot.types.LocalizedString;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import static dev.hevav.tchubbot.api.VoiceAdapter.removeGuildAudioPlayer;
import static dev.hevav.tchubbot.translations.VoiceStrings.*;

public class Voice implements dev.hevav.tchubbot.types.Module {
    @Override
    public String shortName() {
        return "voice";
    }

    @Override
    public LocalizedString description() {
        return voiceDescription;
    }

    @Override
    public List<Trigger> triggers() {
        return Arrays.asList(new Trigger("rejoin", rejoinDesctiption),
                new Trigger("rj", rejoinDesctiption),
                new Trigger("leave", leaveDescription),
                new Trigger("l", leaveDescription));
    }

    @Override
    public List<Trigger> audioTriggers() {
        return null;
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
    public void onInit(WeakReference<Config> _boot) {
        VoiceAdapter.initAdapter();
        _boot.get().api_ref.get().addEventListener(new VoiceListener());
    }
    
    private class VoiceListener extends ListenerAdapter {
        @Override
        public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
            VoiceAdapter.leaveChannel(event.getGuild());
        }
    } 
}
