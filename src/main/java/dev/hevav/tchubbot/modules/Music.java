package dev.hevav.tchubbot.modules;

import com.sedmelluq.discord.lavaplayer.demo.jda.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.hevav.tchubbot.api.EmbedHelper;
import dev.hevav.tchubbot.api.Translator;
import dev.hevav.tchubbot.api.VoiceAdapter;
import dev.hevav.tchubbot.translations.VoiceStrings;
import dev.hevav.tchubbot.types.LocalizedString;
import dev.hevav.tchubbot.types.Module;
import dev.hevav.tchubbot.types.Trigger;
import dev.hevav.tchubbot.api.Config;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.ArrayUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sun.security.util.ArrayUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

import static dev.hevav.tchubbot.api.VoiceAdapter.getGuildAudioPlayer;
import static dev.hevav.tchubbot.api.VoiceAdapter.removeGuildAudioPlayer;
import static dev.hevav.tchubbot.translations.MusicStrings.*;
import static java.lang.Math.pow;

/**
 * Music module
 * Originally by sedmelluq
 *
 * @author hevav
 * @since 1.0
 */
public class Music implements Module {

    private final Logger logger = LogManager.getLogger("TchuBBak");
    private String yt_token;

    @Override
    public String shortName() {
        return "music";
    }

    @Override
    public LocalizedString description() {
        return moduleDescription;
    }

    public List<Trigger> triggers() {
        return Arrays.asList(new Trigger("stop", stopDescription),
                new Trigger("play", "play <track>", playDescription),
                new Trigger("p", "p <track>", playDescription),
                new Trigger("volume", "volume <int>", volumeDescription),
                new Trigger("v", "v <int>", volumeDescription),
                new Trigger("queue", queueDescription),
                new Trigger("q", queueDescription),
                new Trigger("remove", "remove <number>", removeDescription),
                new Trigger("r", "r <number>", removeDescription),
                new Trigger("skip", skipDescription),
                new Trigger("pause", pauseDescription),
                new Trigger("seek", "seek [to] <time>", seekDescription),
                new Trigger("s", "s [to] <time>", seekDescription));
    }

    @Override
    public List<Trigger> audioTriggers() {
        return new ArrayList<>();
    }

    public void onInit(WeakReference<Config> _boot){
        Config config = _boot.get();
        assert config != null;
        yt_token = config.yt_token;
        Objects.requireNonNull(config.api_ref.get()).addEventListener(new MusicListener());
        logger.debug("Module Music was initialized");
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl, final VoiceChannel voiceChannel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild(), channel);

        VoiceAdapter.playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                int queueSize = musicManager.scheduler.queue.size();
                EmbedHelper.sendEmbed(track.getInfo().title, String.valueOf(queueSize),track.getDuration(), trackUrl, track.getInfo().author, EmbedHelper.PlayType.Added, channel);
                if(queueSize == 0 && musicManager.player.getPlayingTrack() == null)
                    EmbedHelper.sendEmbed(track.getInfo().title, String.valueOf(queueSize),track.getDuration(), trackUrl, track.getInfo().author, EmbedHelper.PlayType.Playing, channel);
                play(musicManager, track, voiceChannel);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                EmbedHelper.sendEmbed(playlist.getName(), String.format("%d (+%d)", musicManager.scheduler.queue.size() + 1, playlist.getTracks().size()), 0, trackUrl, "Playlist", EmbedHelper.PlayType.Playlist, channel);
                EmbedHelper.sendEmbed(firstTrack.getInfo().title, "N/A", firstTrack.getDuration(), firstTrack.getInfo().uri, firstTrack.getInfo().author, EmbedHelper.PlayType.Added, channel);

                for(AudioTrack track : playlist.getTracks())
                    play(musicManager, track, voiceChannel);
            }

            @Override
            public void noMatches() {
                EmbedHelper.sendEmbed("404", Translator.translateString(error404Description, channel.getGuild()) + trackUrl, channel);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, channel.getGuild()),
                        exception.getMessage(),
                        channel);
            }
        });
    }

    private void play(GuildMusicManager musicManager, AudioTrack track, VoiceChannel voiceChannel) {
        VoiceAdapter.joinChannel(voiceChannel, false);
        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();
    }

    private void pause(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.setPaused(!musicManager.player.isPaused());
    }

    private void leaveVoiceChannel(TextChannel channel) {
        channel.getGuild().getAudioManager().closeAudioConnection();
    }
    private void stop(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.onTrackEnd(musicManager.player, null, AudioTrackEndReason.CLEANUP);
        musicManager.player.startTrack(null, false);
        musicManager.player.destroy();
        removeGuildAudioPlayer(channel.getGuild());
    }

    private void setVolume(TextChannel channel, int percentage) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.setVolume(percentage);
    }
    private int getVolume(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        return musicManager.player.getVolume();
    }

    private void youtubeSearch(String search, TextChannel channel, VoiceChannel voiceChannel){
        String url = String.format("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=%s&key=%s", search.replace(" ", "+"), yt_token);
        try {
            Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(10 * 1000).get();
            String getJson = doc.text();
            String jsonObject = (String) ((HashMap) ((HashMap) ((JSONObject) new JSONTokener(getJson).nextValue()).getJSONArray("items").toList().get(0)).get("id")).get("videoId");
            loadAndPlay(channel, String.format("https://youtube.com/watch?v=%s", jsonObject), voiceChannel);
        } catch (IOException e) {
            logger.debug(e);
            EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, channel.getGuild()),
                    e.getMessage().replace(yt_token, "YT_TOKEN"),
                    channel);
        }
        catch (IndexOutOfBoundsException e){
            logger.debug(e);
            EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, channel.getGuild()),
                    String.format("%s %s", Translator.translateString(error404Description, channel.getGuild()), search),
                    channel);
        }
    }
    
    private long parseTime(String t){
        String[] time = t.split(":");
        long lenSec = 0;
        for(int i = 0; i<time.length; i++){
            lenSec += Integer.parseInt(time[i])*pow(60, time.length-1-i);
        }
        return lenSec*1000;
    }

    public void onMessage(GuildMessageReceivedEvent event, String[] parsedText) {
        boolean notHasDJ = true;
        for (Role role : Objects.requireNonNull(event.getMember()).getRoles()){
            logger.trace(role.getName());
            if (role.getName().toLowerCase().contains("dj")) {
                notHasDJ = false;
                break;
            }
        }
        switch (parsedText[0]) {
            case "play":
            case "p":
                if (parsedText.length == 1) {
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()),
                            "Link to video is wrong",
                            event.getChannel());
                    return;
                }
                GuildVoiceState voiceState = event.getMember().getVoiceState();
                assert voiceState != null;
                if(!voiceState.inVoiceChannel()){
                    return;
                }
                boolean needToRejoin = !event.getGuild().getMemberById(event.getJDA().getSelfUser().getId()).getVoiceState().inVoiceChannel();
                if (parsedText[1].startsWith("http://") || parsedText[1].startsWith("https://") || parsedText[1].startsWith("www."))
                    loadAndPlay(event.getChannel(), parsedText[1], VoiceAdapter.getChannel(event.getGuild().getIdLong(), voiceState.getChannel(), needToRejoin));
                else
                    youtubeSearch(String.join(" ", Arrays.copyOfRange(parsedText, 1, parsedText.length)), event.getChannel(), VoiceAdapter.getChannel(event.getGuild().getIdLong(), voiceState.getChannel(), needToRejoin));
                break;
            case "volume":
            case "v":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()), Translator.translateString(DJDescription, event.getGuild()), event.getChannel());
                    return;
                }
                try {
                    int vol = Integer.parseInt(parsedText[1]);
                    if(vol > 250)
                        throw new Exception("Too loud");
                    setVolume(event.getChannel(), vol);
                } catch (Exception e) {
                    logger.debug(e);
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()),
                            e.getMessage(),
                            event.getChannel());
                }
                break;
            case "skip":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()), Translator.translateString(DJDescription, event.getGuild()), event.getChannel());
                    return;
                }
                skipTrack(event.getChannel());
                break;
            case "stop":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()), Translator.translateString(DJDescription, event.getGuild()), event.getChannel());
                    return;
                }
                stop(event.getChannel());
                break;
            case "pause":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()), Translator.translateString(DJDescription, event.getGuild()), event.getChannel());
                    return;
                }
                pause(event.getChannel());
                break;
            case "leave":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()), Translator.translateString(DJDescription, event.getGuild()), event.getChannel());
                    return;
                }
                leaveVoiceChannel(event.getChannel());
                break;
            case "queue":
            case "q":
                List<MessageEmbed.Field> queueFields = new ArrayList<>();
                int trackQuantity = 0;
                for(AudioTrack track : getGuildAudioPlayer(event.getGuild()).scheduler.queue){
                    trackQuantity++;
                    AudioTrackInfo info = track.getInfo();
                    queueFields.add(new MessageEmbed.Field(info.author, String.format("%d) %s", trackQuantity, info.title), false));
                    if(trackQuantity == 15)
                        break;
                }
                EmbedHelper.sendEmbed(Translator.translateString(queueDescription, event.getGuild()), "", event.getChannel(), queueFields);
                break;
            case "remove":
            case "r":
                LinkedList<AudioTrack> queue = getGuildAudioPlayer(event.getGuild()).scheduler.queue;
                if(parsedText.length > 1)
                    queue.remove(Integer.parseInt(parsedText[1])-1);
                else
                    queue.remove(queue.size()-1);
                break;
            case "seek":
            case "s":
                if(parsedText.length == 1){
                    EmbedHelper.sendEmbed("Seek", Translator.translateString(seekDescription, event.getGuild()), event.getChannel());
                    return;
                }
                GuildMusicManager player = getGuildAudioPlayer(event.getGuild());
                AudioTrack track = player.player.getPlayingTrack();
                long setTime = 0;
                if(parsedText[1].toLowerCase().equals("to"))
                    setTime = parseTime(parsedText[2]);
                else
                    setTime = track.getPosition() + parseTime(parsedText[1]);
                if(setTime > track.getDuration())
                    setTime = track.getDuration();
                track.setPosition(setTime);
                break;
            default:
                logger.warn(String.format("Proceeded strange trigger %s", parsedText[0]));
                break;
        }
    }

    @Override
    public void onVoice(VoiceChannel event, String trigger) {

    }

    private class MusicListener extends ListenerAdapter{
        @Override
        public void onMessageReactionAdd(MessageReactionAddEvent event){
            if(Objects.requireNonNull(event.getUser()).isBot())
                return;
            boolean notHasDJ = true;
            for (Role role : Objects.requireNonNull(event.getMember()).getRoles()){
                logger.trace(role.getName());
                if (role.getName().toLowerCase().contains("dj")) {
                    notHasDJ = false;
                    break;
                }
            }
            if(notHasDJ)
                return;
            switch (event.getReactionEmote().getEmoji()){
                case "⏯":
                    pause(event.getTextChannel());
                    break;
                case "⏭":
                    skipTrack(event.getTextChannel());
                    break;
                case "\uD83D\uDD07":
                    setVolume(event.getTextChannel(), 0);
                    break;
                case "\uD83D\uDD09":
                    setVolume(event.getTextChannel(), getVolume(event.getTextChannel()) - 10);
                    break;
                case "\uD83D\uDD0A":
                    setVolume(event.getTextChannel(), getVolume(event.getTextChannel()) + 10);
                    break;
                default:
                    return;
            }
            event.getReaction().removeReaction(event.getUser()).complete();
        }
    }
}
