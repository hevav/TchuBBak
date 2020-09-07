package dev.hevav.tchubbot.types;

public class Infraction {
    public enum InfractionType {
        BAN,
        WARN,
        MUTE,
        KICK
    }

    public InfractionType type;
    public String reason;
    public long lastDate;
    public long guildId;
    public long userId;

    public Infraction(InfractionType type, String reason, long lastDate, long guildId, long userId){
        this.type = type;
        this.reason = reason;
        this.lastDate = lastDate;
        this.guildId = guildId;
        this.userId = userId;
    }
}
