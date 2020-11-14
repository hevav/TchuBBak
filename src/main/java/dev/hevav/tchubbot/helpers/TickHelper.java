package dev.hevav.tchubbot.helpers;

import dev.hevav.tchubbot.modules.Module;

/**
 * Class to do per tick tasks
 *
 * @author hevav
 * @since 2.0.0
 */
public class TickHelper {
    private final Thread tickThread;

    public TickHelper(Module[] modules){
        tickThread = new Thread(()->{
            try {
                while (true){
                    for(Module module : modules)
                        module.onTick();
                    Thread.sleep(60000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void doTicks(){
        tickThread.start();
    }
}
