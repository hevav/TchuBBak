package dev.hevav.pfbot.modules;

import com.sedmelluq.discord.lavaplayer.demo.jda.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.hevav.pfbot.api.EmbedHelper;
import dev.hevav.pfbot.api.Translator;
import dev.hevav.pfbot.api.VoiceAdapter;
import dev.hevav.pfbot.types.Module;
import dev.hevav.pfbot.types.Trigger;
import dev.hevav.pfbot.api.Config;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

import static dev.hevav.pfbot.translations.MusicStrings.*;

/**
 * Music module
 * Originally by sedmelluq
 *
 * @author hevav
 * @since 1.0
 */
public class Music implements Module {

    private final Logger logger = LogManager.getLogger("PFbot");
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private String yt_token;


    public Music() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    @Override
    public String shortName() {
        return "music";
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
                new Trigger("leave", leaveDescription));
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

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild, TextChannel channel) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, channel);
            musicManagers.put(guild.getIdLong(), musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private synchronized void removeGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());

        if (musicManager != null) {
            musicManagers.remove(guild.getIdLong());
        }
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl, final VoiceChannel voiceChannel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild(), channel);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                int queueSize = musicManager.scheduler.queue.size();
                EmbedHelper.sendEmbed(track.getInfo().title, String.valueOf(queueSize),track.getDuration(), trackUrl, track.getInfo().author, EmbedHelper.PlayType.Added, channel);
                if(queueSize == 0 && musicManager.player.getPlayingTrack() == null)
                    EmbedHelper.sendEmbed(track.getInfo().title, String.valueOf(queueSize),track.getDuration(), trackUrl, track.getInfo().author, EmbedHelper.PlayType.Playing, channel);
                play(channel.getGuild(), musicManager, track, voiceChannel);
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
                    play(channel.getGuild(), musicManager, track, voiceChannel);
            }

            @Override
            public void noMatches() {
                EmbedHelper.sendEmbed("404", Translator.translateString(error404Description, channel.getGuild()) + trackUrl, channel);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, channel.getGuild()),
                        exception.toString(),
                        channel);
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, VoiceChannel voiceChannel) {
        AudioManager audioManager = guild.getAudioManager();
        if(!audioManager.isConnected() && !audioManager.isAttemptingToConnect())
            audioManager.openAudioConnection(voiceChannel);

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
                    e.toString().replace(yt_token, "YT_TOKEN"),
                    channel);
        }
        catch (IndexOutOfBoundsException e){
            logger.debug(e);
            EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, channel.getGuild()),
                    String.format("%s %s", Translator.translateString(error404Description, channel.getGuild()), search),
                    channel);
        }
    }

    public void onMessage(GuildMessageReceivedEvent event, String trigger) {
        String[] msg_split = event.getMessage().getContentRaw().split(" ");
        boolean notHasDJ = true;
        for (Role role : Objects.requireNonNull(event.getMember()).getRoles()){
            logger.trace(role.getName());
            if (role.getName().toLowerCase().contains("dj")) {
                notHasDJ = false;
                break;
            }
        }
        switch (trigger) {
            case "play":
            case "p":
                if (msg_split.length == 0) {
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()),
                            "Link to video is wrong",
                            event.getChannel());
                    return;
                }
                if (msg_split[1].startsWith("http://") || msg_split[1].startsWith("https://") || msg_split[1].startsWith("www."))
                    loadAndPlay(event.getChannel(), msg_split[1], VoiceAdapter.getChannel(event.getGuild().getIdLong()));
                else
                    youtubeSearch(event.getMessage().getContentRaw().replaceFirst(msg_split[0]+" ", ""), event.getChannel(), VoiceAdapter.getChannel(event.getGuild().getIdLong()));
                break;
            case "volume":
            case "v":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()), Translator.translateString(DJDescription, event.getGuild()), event.getChannel());
                    return;
                }
                try {
                    setVolume(event.getChannel(), Integer.parseInt(msg_split[1]));
                } catch (Exception e) {
                    logger.debug(e);
                    EmbedHelper.sendEmbed(Translator.translateString(errorMusicDescription, event.getGuild()),
                            e.toString(),
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
                if(msg_split.length > 1)
                    queue.remove(Integer.parseInt(msg_split[1])-1);
                else
                    queue.remove(queue.size()-1);
                break;
            default:
                logger.warn(String.format("Proceeded strange trigger %s", trigger));
                break;
        }
    }

    @Override
    public void onVoice(VoiceChannel event, String trigger) {

    }

    private class MusicListener extends ListenerAdapter{
        @Override
        public void onMessageReactionAdd(MessageReactionAddEvent event){
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
            if(Objects.requireNonNull(event.getUser()).isBot())
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

        @Override
        public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
            if(event.getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId()))
                removeGuildAudioPlayer(event.getGuild());
        }
    }
}
