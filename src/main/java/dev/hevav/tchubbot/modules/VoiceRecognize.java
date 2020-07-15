package dev.hevav.tchubbot.modules;

import dev.hevav.tchubbot.api.Config;
import dev.hevav.tchubbot.types.LocalizedString;
import dev.hevav.tchubbot.types.Trigger;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.lang.ref.WeakReference;
import java.util.List;

import static dev.hevav.tchubbot.translations.VoiceStrings.voiceDescription;

public class VoiceRecognize implements dev.hevav.tchubbot.types.Module {
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
        return null;
    }

    @Override
    public List<Trigger> audioTriggers() {
        return null;
    }

    @Override
    public void onMessage(GuildMessageReceivedEvent event, String[] parsedText) {

    }

    @Override
    public void onVoice(VoiceChannel event, String trigger) {

    }

    @Override
    public void onInit(WeakReference<Config> _boot) {

    }
}
