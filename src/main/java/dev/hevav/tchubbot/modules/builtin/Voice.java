package dev.hevav.tchubbot.modules.builtin;

import dev.hevav.tchubbot.Config;
import dev.hevav.tchubbot.helpers.EmbedHelper;
import dev.hevav.tchubbot.i18n.LocalizedString;
import dev.hevav.tchubbot.i18n.Translator;
import dev.hevav.tchubbot.modules.Module;
import dev.hevav.tchubbot.types.LocalizedTrigger;
import dev.hevav.tchubbot.voice.VoiceAdapter;
import dev.hevav.tchubbot.types.Trigger;
import dev.hevav.tchubbot.voice.recognition.VoiceRecognition;
import dev.hevav.tchubbot.voice.recognition.VoiceRecognitionGuildHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.hevav.tchubbot.i18n.strings.VoiceStrings.*;

public class Voice extends Module {
    private boolean recognitionEnabled = false;
    private final HashMap<String, List<LocalizedTrigger>> moduleTriggers = new HashMap<>();
    private final HashMap<String, LocalizedString> modules = new HashMap<>();

    public Voice() {
        super("voice",
                voiceDescription,
                Arrays.asList(new Trigger("rejoin", rejoinDesctiption),
                    new Trigger("rj", rejoinDesctiption),
                    new Trigger("leave", leaveDescription),
                    new Trigger("l", leaveDescription),
                    new Trigger("vh", voiceHelpDescription),
                    new Trigger("voicehelp", voiceHelpDescription),
                    new Trigger("sr", recognizeDescription),
                    new Trigger("stopr", stopRecognizeDescription)),
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
                if(!recognitionEnabled){
                    VoiceChannel channel = event.getMember().getVoiceState().getChannel();
                    VoiceAdapter.joinChannel(channel, false);
                    VoiceAdapter.switchReceiveHandler(new VoiceRecognitionGuildHandler(event.getChannel()), event.getGuild());
                    channel.getMembers().forEach(member -> {
                        User user = member.getUser();
                        if(!user.isBot())
                            VoiceRecognition.createUserVoiceRecognition(user, event.getGuild());
                    });
                    recognitionEnabled = true;
                }
                break;
            case "stopr":
                if(recognitionEnabled){
                    VoiceChannel channel = event.getMember().getVoiceState().getChannel();
                    VoiceAdapter.returnReceiveHandler(event.getGuild());
                    channel.getMembers().forEach(member -> {
                        User user = member.getUser();
                        if(VoiceAdapter.hasDJ(member) && !user.isBot())
                            VoiceRecognition.closeUserVoiceRecognition(user);
                    });
                    recognitionEnabled = false;
                }
                break;
            case "vh":
            case "voicehelp":
                if(parsedText.length == 1) {
                    List<MessageEmbed.Field> moduleList = new ArrayList<>();
                    AtomicInteger fieldCount = new AtomicInteger();
                    modules.forEach((String shortName, LocalizedString description) -> {
                        fieldCount.getAndIncrement();
                        moduleList.add(new MessageEmbed.Field(String.format("%s%s %s", Config.bot_prefix, parsedText[0], shortName), Translator.translateString(description, event.getGuild()), false));
                        if (fieldCount.get() == 25) {
                            fieldCount.set(0);
                            moduleList.clear();
                            EmbedHelper.sendEmbed(Translator.translateString(voiceHelpDescription, event.getGuild()), "", event.getChannel(), moduleList);
                        }
                    });
                    EmbedHelper.sendEmbed(Translator.translateString(voiceHelpDescription, event.getGuild()), "", event.getChannel(), moduleList);
                }
                else{
                    List<MessageEmbed.Field> triggerList = new ArrayList<>();
                    int fieldCount = 0;
                    for(LocalizedTrigger trigger : moduleTriggers.get(parsedText[1])){
                        fieldCount++;
                        triggerList.add(new MessageEmbed.Field(String.format("%s %s", Translator.translateString(rec_start, event.getGuild()), Translator.translateString(trigger.show_trigger, event.getGuild())), Translator.translateString(trigger.description, event.getGuild()), false));
                        if (fieldCount == 25) {
                            fieldCount = 0;
                            triggerList.clear();
                            EmbedHelper.sendEmbed(Translator.translateString(voiceHelpDescription, event.getGuild()), "", event.getChannel(), triggerList);
                        }
                    }
                    EmbedHelper.sendEmbed(Translator.translateString(voiceHelpDescription, event.getGuild()), "", event.getChannel(), triggerList);
                }
                break;
        }
    }

    @Override
    public void onInit() {
        VoiceAdapter.initAdapter();
        Config.api.addEventListener(new VoiceListener());
        for (Module module : Config.modules) {
            if(module.audioTriggers.size() > 0){
                modules.put(module.shortName, module.description);
                moduleTriggers.put(module.shortName, module.audioTriggers);
            }
        }
    }

    private class VoiceListener extends ListenerAdapter {
        @Override
        public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
            User user = event.getMember().getUser();
            if(user.getIdLong() == Config.api.getSelfUser().getIdLong())
                VoiceAdapter.leaveChannel(event.getGuild());
            else if(recognitionEnabled && VoiceAdapter.hasDJ(event.getMember()) && !user.isBot())
                VoiceRecognition.closeUserVoiceRecognition(user);
        }

        @Override
        public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
            User user = event.getMember().getUser();
            if(recognitionEnabled && VoiceAdapter.hasDJ(event.getMember()) && !user.isBot())
                VoiceRecognition.createUserVoiceRecognition(user, event.getGuild());
        }
    } 
}
