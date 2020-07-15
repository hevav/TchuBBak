package dev.hevav.tchubbot.types;

/**
 * Class to represent triggers for loading modules
 *
 * @author hevav
 * @since 1.0
 */
public class Trigger {
    public String trigger;
    public String show_trigger;
    public LocalizedString description;

    /**
     *
     * @param _trigger Trigger to load bot
     * @param _show_trigger Trigger showed in help
     * @param _description Description showed in help
     */
    public Trigger(String _trigger, String _show_trigger, LocalizedString _description){
        trigger = _trigger;
        show_trigger = _show_trigger;
        description = _description;
    }

    /**
     *
     * @param _trigger Trigger to load bot
     * @param _description Description showed in help
     */
    public Trigger(String _trigger, LocalizedString _description){
        trigger = _trigger;
        show_trigger = _trigger;
        description = _description;
    }
}
