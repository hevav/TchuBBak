package dev.hevav.tchubbot.translations;

import dev.hevav.tchubbot.types.LocalizedString;

public class AdminStrings {
    public static LocalizedString noPermissions = new LocalizedString(
            "No permissions",
            "Запрещено",
            null,
            null,
            null,
            null);
    public static LocalizedString adminDescription = new LocalizedString(
            "Модерация",
            "Moderation",
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
    public static LocalizedString banDescription = new LocalizedString(
            "Забанить (по [ip]) <member> по причине [message]",
            "Ban ([ip] for ip-ban) <member> with message [message]",
            null,
            null,
            null,
            null
    );
    public static LocalizedString kickDescription = new LocalizedString(
            "Кикнуть <member> по причине [message]",
            "Kick <member> with message [message]",
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
}
