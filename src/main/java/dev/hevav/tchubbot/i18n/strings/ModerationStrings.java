package dev.hevav.tchubbot.i18n.strings;

import dev.hevav.tchubbot.i18n.LocalizedString;

public class ModerationStrings {
    public static LocalizedString noPermissions = new LocalizedString(
            "No permissions",
            "Запрещено",
            null,
            null,
            null,
            null);
    public static LocalizedString moderationDescription = new LocalizedString(
            "Плагин модерации",
            "Moderation plugin",
            null,
            null,
            null,
            null);
    public static LocalizedString noPermissionsFull = new LocalizedString(
            "You don't have permissions to manage messages",
            "У вас нету права на удаление сообщений",
            null,
            null,
            null,
            null);
    public static LocalizedString purgeDescription = new LocalizedString(
            "Удалить <int> сообщений",
            "Remove <int> messages",
            null,
            null,
            null,
            null
    );
    public static LocalizedString infrDescription = new LocalizedString(
            "Посмотреть нарушения <member>",
            "View <member>'s infractions",
            null,
            null,
            null,
            null
    );
    public static LocalizedString banDescription = new LocalizedString(
            "Забанить <member> по причине [message] на время [time]",
            "Ban <member> with message [message] for time [time]",
            null,
            null,
            null,
            null
    );
    public static LocalizedString kickDescription = new LocalizedString(
            "Кикнуть <member> по причине [message] на время [time]",
            "Kick <member> with message [message] for time [time]",
            null,
            null,
            null,
            null
    );
    public static LocalizedString muteDescription = new LocalizedString(
            "Замутить <member> по причине [message] на время [time]",
            "Mute <member> with message [message] for time [time]",
            null,
            null,
            null,
            null
    );
    public static LocalizedString warnDescription = new LocalizedString(
            "Выдать варн <member> по причине [message]",
            "Set warn to <member> with message [message]",
            null,
            null,
            null,
            null
    );
    public static LocalizedString unmuteDescription = new LocalizedString(
            "Размутить <member>",
            "Unmute <member>",
            null,
            null,
            null,
            null
    );
    public static LocalizedString errorPurgeDescription = new LocalizedString(
            "Ошибка при удалении. Проверьте права бота или число",
            "Removing error. Check bot's permissions or number",
            null,
            null,
            null,
            null
    );
    public static LocalizedString successfulBan = new LocalizedString(
            "Плагин на модерацию успешно завершил работу с ",
            "Moderation plugin successfully changed state of ",
            null,
            null,
            null,
            null
    );
    public static LocalizedString errorBanKick = new LocalizedString(
            "Невозможно выполнить операцию, проверьте права бота\n",
            "Cannot complete this operation, check bot permissions\n",
            null,
            null,
            null,
            null
    );
}
