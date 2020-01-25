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
import dev.hevav.pfbot.api.EmbedHelper;
import dev.hevav.pfbot.api.LocalizedString;
import dev.hevav.pfbot.api.Module;
import dev.hevav.pfbot.api.Trigger;
import dev.hevav.pfbot.Boot;
import net.dv8tion.jda.api.Region;
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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Music module
 * Originally by sedmelluq
 *
 * @author hevav
 * @since 1.0
 */
public class Music implements Module {

    public Trigger[] triggers() {
        return new Trigger[]{
                new Trigger("stop", stopDescription),
                new Trigger("play", "play <track>", playDescription),
                new Trigger("p", "p <track>", playDescription),
                new Trigger("volume", "volume <int>", volumeDescription),
                new Trigger("v", "v <int>", volumeDescription),
                new Trigger("skip", skipDescription),
                new Trigger("pause", pauseDescription),
        };
    }

    private final Logger logger = LogManager.getLogger("PFbot");
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private String yt_token;

    private LocalizedString playDescription = new LocalizedString(
            "Сыграть <track> или добавить очередь",
            "Play <track> or add to queue",
            null,
            null,
            null,
            null);
    private LocalizedString errorMusicDescription = new LocalizedString(
            "Ошибка в музыкальном боте. Проверьте права бота или число",
            "Music bot error. Check bot's permissions or number",
            null,
            null,
            null,
            null);
    private LocalizedString error404Description = new LocalizedString(
            "Ничего не найдено по запросу ",
            "Nothing found by ",
            null,
            null,
            null,
            null);
    private LocalizedString volumeDescription = new LocalizedString(
            "Поставить громкость на <int>%",
            "Set volume to <int>%",
            null,
            null,
            null,
            null);
    private LocalizedString skipDescription = new LocalizedString(
            "Пропустить трек",
            "Skip track",
            null,
            null,
            null,
            null);
    private LocalizedString stopDescription = new LocalizedString(
            "Остановить проигрывание",
            "Stop and clear queue",
            null,
            null,
            null,
            null);
    private LocalizedString pauseDescription = new LocalizedString(
            "Поставить/убрать трек с паузы",
            "Pause/resume track",
            null,
            null,
            null,
            null);
    private LocalizedString DJDescription = new LocalizedString(
            "Управление доступно только DJ",
            "Controlling is available only for DJ",
            null,
            null,
            null,
            null);

    public Music() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public void onInit(WeakReference<Boot> _boot){
        Boot boot = _boot.get();
        yt_token = boot.yt_token;
        boot.api.addEventListener(new MusicListener());
        logger.debug("Module Music was initialized");
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild, TextChannel channel) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, channel);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private synchronized void removeGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager != null) {
            musicManagers.remove(guildId);
        }
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl, final VoiceChannel voiceChannel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild(), channel);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                EmbedHelper.sendEmbed(track.getInfo().title, "+1" ,track.getDuration(), trackUrl, track.getInfo().author, EmbedHelper.PlayType.Added, channel);

                play(channel.getGuild(), musicManager, track, voiceChannel);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                EmbedHelper.sendEmbed(playlist.getName(), "+"+playlist.getTracks().size() , 0, trackUrl, "Playlist", EmbedHelper.PlayType.Playlist, channel);
                EmbedHelper.sendEmbed(firstTrack.getInfo().title, "N/A", firstTrack.getDuration(), firstTrack.getInfo().uri, firstTrack.getInfo().author, EmbedHelper.PlayType.Added, channel);

                for(AudioTrack track : playlist.getTracks())
                    play(channel.getGuild(), musicManager, track, voiceChannel);
            }

            @Override
            public void noMatches() {
                EmbedHelper.sendEmbed("404",error404Description.getLocalizedString(channel.getGuild().getRegion()) + trackUrl, channel);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(channel.getGuild().getRegion()),
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
            EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(channel.getGuild().getRegion()),
                    e.toString().replace(yt_token, "YT_TOKEN"),
                    channel);
        }
        catch (IndexOutOfBoundsException e){
            logger.debug(e);
            EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(channel.getGuild().getRegion()),
                    String.format("%s %s", error404Description.getLocalizedString(channel.getGuild().getRegion()), search),
                    channel);
        }
    }

    public void onMessage(GuildMessageReceivedEvent event, String trigger) {
        String[] msg_split = event.getMessage().getContentRaw().split(" ");
        Region region = event.getGuild().getRegion();
        boolean notHasDJ = true;
        for (Role role : event.getMember().getRoles()){
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
                    EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(region),
                            "Link to video is wrong",
                            event.getChannel());
                    return;
                }
                GuildVoiceState voiceState = event.getMember().getVoiceState();
                if(!voiceState.inVoiceChannel()){
                    return;
                }
                if (msg_split[1].startsWith("http://") || msg_split[1].startsWith("https://") || msg_split[1].startsWith("www."))
                    loadAndPlay(event.getChannel(), msg_split[1], voiceState.getChannel());
                else
                    youtubeSearch(event.getMessage().getContentRaw().replaceFirst(msg_split[0]+" ", ""), event.getChannel(), voiceState.getChannel());
                break;
            case "volume":
            case "v":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(region), DJDescription.getLocalizedString(region), event.getChannel());
                    return;
                }
                try {
                    setVolume(event.getChannel(), Integer.parseInt(msg_split[1]));
                } catch (Exception e) {
                    logger.debug(e);
                    EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(region),
                            e.toString(),
                            event.getChannel());
                }
                break;
            case "skip":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(region), DJDescription.getLocalizedString(region), event.getChannel());
                    return;
                }
                skipTrack(event.getChannel());
                break;
            case "stop":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(region), DJDescription.getLocalizedString(region), event.getChannel());
                    return;
                }
                stop(event.getChannel());
                break;
            case "pause":
                if(notHasDJ){
                    EmbedHelper.sendEmbed(errorMusicDescription.getLocalizedString(region), DJDescription.getLocalizedString(region), event.getChannel());
                    return;
                }
                pause(event.getChannel());
                break;
        }
    }

    private class MusicListener extends ListenerAdapter{
        @Override
        public void onMessageReactionAdd(MessageReactionAddEvent event){
            boolean notHasDJ = true;
            for (Role role : event.getMember().getRoles()){
                logger.trace(role.getName());
                if (role.getName().toLowerCase().contains("dj")) {
                    notHasDJ = false;
                    break;
                }
            }
            if(notHasDJ)
                return;
            if(event.getUser().isBot())
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
