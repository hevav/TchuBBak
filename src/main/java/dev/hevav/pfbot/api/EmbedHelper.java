package dev.hevav.pfbot.api;

import net.dv8tion.jda.api.entities.*;

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
        SelfUser bot = textChannel.getJDA().getSelfUser();
        textChannel.sendMessage(new MessageEmbed(null, title, msg, EmbedType.UNKNOWN, null, 0xFFCC66, null, null, new MessageEmbed.AuthorInfo(bot.getName(), null, bot.getAvatarUrl(), null), null, new MessageEmbed.Footer("#stayhome", null, null), null, null)).complete();
    }

    public static void sendEmbed(String title, String msg, TextChannel textChannel, List<MessageEmbed.Field> fields) {
        SelfUser bot = textChannel.getJDA().getSelfUser();
        textChannel.sendMessage(new MessageEmbed(null, title, msg, EmbedType.UNKNOWN, null, 0xFFCC66, null, null, new MessageEmbed.AuthorInfo(bot.getName(), null, bot.getAvatarUrl(), null), null, new MessageEmbed.Footer("#stayhome", null, null), null, fields)).complete();
    }
    public static void sendEmbed(String trackName, String queuePos, long length, String trackUrl, String author, PlayType type, TextChannel textChannel) {

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
                new MessageEmbed.Footer("#stayhome", null, null),
                null,
                Arrays.asList(
                        new MessageEmbed.Field(trackLengthString.getLocalizedString(textChannel.getGuild().getRegion()), formatTiming(length, length),true),
                        new MessageEmbed.Field(queuePosString.getLocalizedString(textChannel.getGuild().getRegion()), queuePos, true))
                )
        ).complete();
        msg.addReaction("⏯").complete(); //play pause
        msg.addReaction("⏭").complete(); //skip
        msg.addReaction("\uD83D\uDD07").complete(); //mute
        msg.addReaction("\uD83D\uDD09").complete(); //sound
        msg.addReaction("\uD83D\uDD0A").complete(); //loud
    }

    private static String formatTiming(long timing, long maximum) {
        timing = Math.min(timing, maximum) / 1000;

        long seconds = timing % 60;
        timing /= 60;
        long minutes = timing % 60;
        timing /= 60;
        long hours = timing;

        if (maximum >= 3600000L) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    private static int typeToColor(PlayType type) {
        switch (type) {
            case Added:
                return 0xFFCC66;
            case Playing:
                return 0x33CC33;
            case Streaming:
                return 8388863;
            default:
                return 0;
        }
    }

    public enum PlayType {
        Playing,
        Added,
        Streaming,
        Playlist
    }
}
