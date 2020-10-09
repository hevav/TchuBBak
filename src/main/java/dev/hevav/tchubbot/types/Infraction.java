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
    public String lastDate;
    public String guildId;
    public String userId;

    public Infraction(InfractionType type, String reason, String lastDate, String guildId, String userId){
        this.type = type;
        this.reason = reason;
        this.lastDate = lastDate;
        this.guildId = guildId;
        this.userId = userId;
    }
}
