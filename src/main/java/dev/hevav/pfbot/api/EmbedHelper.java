package dev.hevav.pfbot.api;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class EmbedHelper {
    private static LocalizedString DJDescription = new LocalizedString(
            "Управление доступно только DJ",
            "Controlling is available only for DJ",
            null,
            null,
            null,
            null);
    private static LocalizedString trackLengthString = new LocalizedString(
            "Продолжительность",
            "Track length",
            null,
            null,
            null,
            null);
    private static LocalizedString queuePosString = new LocalizedString(
            "Очередь",
            "Queue",
            null,
            null,
            null,
            null);

    public static void sendEmbed(String title, String msg, TextChannel textChannel) {
        textChannel.sendMessage(new MessageEmbed(null, title, msg, EmbedType.UNKNOWN, null, 16711680, null, null, new MessageEmbed.AuthorInfo("PFbot", null, "https://cdn.discordapp.com/avatars/538670331938865163/bc903e523601f6535ea6f6909e51ff5c.png", null), null, new MessageEmbed.Footer("PFbot by hevav and OSS community", "https://cdn.discordapp.com/avatars/538670331938865163/bc903e523601f6535ea6f6909e51ff5c.png", null), null, null)).complete();
    }

    public static void sendEmbed(String title, String msg, TextChannel textChannel, List<MessageEmbed.Field> fields) {
        textChannel.sendMessage(new MessageEmbed(null, title, msg, EmbedType.UNKNOWN, null, 16711680, null, null, new MessageEmbed.AuthorInfo("PFbot", null, "https://cdn.discordapp.com/avatars/538670331938865163/bc903e523601f6535ea6f6909e51ff5c.png", null), null, new MessageEmbed.Footer("PFbot by hevav and OSS community", "https://cdn.discordapp.com/avatars/538670331938865163/bc903e523601f6535ea6f6909e51ff5c.png", null), null, fields)).complete();
    }
    public static void sendEmbed(String trackName, String queuePos, long length, String trackUrl, String author, PlayType type, TextChannel textChannel) {
        String stringLength = LocalTime.MIN.plus(
                Duration.ofSeconds( length )
        ).toString();
        Message msg = textChannel.sendMessage(new MessageEmbed(
                trackUrl,
                trackName,
                DJDescription.getLocalizedString(textChannel.getGuild().getRegion()),
                EmbedType.UNKNOWN,
                null,
                typeToColor(type),
                null,
                null,
                new MessageEmbed.AuthorInfo(author, null, null, null),
                null,
                new MessageEmbed.Footer("PFbot by hevav and OSS community", "https://cdn.discordapp.com/avatars/538670331938865163/bc903e523601f6535ea6f6909e51ff5c.png", null),
                null,
                Arrays.asList(
                        new MessageEmbed.Field(trackLengthString.getLocalizedString(textChannel.getGuild().getRegion()), stringLength,true),
                        new MessageEmbed.Field(queuePosString.getLocalizedString(textChannel.getGuild().getRegion()), queuePos, true))
                )
        ).complete();
        msg.addReaction("⏯").complete(); //play pause
        msg.addReaction("⏭").complete(); //skip
        msg.addReaction("\uD83D\uDD07").complete(); //mute
        msg.addReaction("\uD83D\uDD09").complete(); //sound
        msg.addReaction("\uD83D\uDD0A").complete(); //loud
    }

    private static int typeToColor(PlayType type) {
        switch (type) {
            case Added:
                return 16776960;
            case Playing:
                return 65280;
            case Streaming:
                return 8388863;
        }
        return 0;
    }

    public enum PlayType {
        Playing,
        Added,
        Streaming,
        Playlist
    }
}
