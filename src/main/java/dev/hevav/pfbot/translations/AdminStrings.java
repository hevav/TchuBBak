package dev.hevav.pfbot.translations;

import dev.hevav.pfbot.types.LocalizedString;

public class AdminStrings {
    public static LocalizedString noPermissions = new LocalizedString(
            "No permissions",
            "Запрещено",
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
    public static LocalizedString errorPurgeDescription = new LocalizedString(
            "Ошибка при удалении. Проверьте права бота или число",
            "Removing error. Check bot's permissions or number",
            null,
            null,
            null,
            null
    );
}
