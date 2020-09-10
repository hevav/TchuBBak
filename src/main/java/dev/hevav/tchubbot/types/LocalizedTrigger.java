package dev.hevav.tchubbot.types;


import dev.hevav.tchubbot.i18n.LocalizedString;

/**
 * Class to represent audio triggers for loading modules
 *
 * @author hevav
 * @since 2.0.0
 */
public class LocalizedTrigger {
    public LocalizedString trigger;
    public LocalizedString show_trigger;
    public LocalizedString description;

    /**
     * Class to represent audio triggers for loading modules
     *
     * @param _trigger Trigger to load bot
     * @param _show_trigger Trigger showed in help
     * @param _description Description showed in help
     */
    public LocalizedTrigger(LocalizedString _trigger, LocalizedString _show_trigger, LocalizedString _description){
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
    public LocalizedTrigger(LocalizedString _trigger, LocalizedString _description){
        trigger = _trigger;
        show_trigger = _trigger;
        description = _description;
    }
}
