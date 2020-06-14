package dev.hevav.pfbot.types;


/**
 * Class to represent audio triggers for loading modules
 *
 * @author hevav
 * @since 2.0.0
 */
public class AudioTrigger {
    public String trigger;
    public String show_trigger;
    public LocalizedString description;

    /**
     * Class to represent audio triggers for loading modules
     *
     * @param _trigger Trigger to load bot
     * @param _show_trigger Trigger showed in help
     * @param _description Description showed in help
     */
    public AudioTrigger(String _trigger, String _show_trigger, LocalizedString _description){
        trigger = _trigger;
        show_trigger = _show_trigger;
        description = _description;
    }

    /**
     * Class to represent audio triggers for loading modules
     *
     * @param _trigger Trigger to load bot
     * @param _description Description showed in help
     */
    public AudioTrigger(String _trigger, LocalizedString _description){
        trigger = _trigger;
        show_trigger = _trigger;
        description = _description;
    }
}
