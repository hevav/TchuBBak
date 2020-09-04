package dev.hevav.tchubbot.helpers;

import dev.hevav.tchubbot.modules.Module;

public class TickHelper {
    private final Thread tickThread;

    public TickHelper(Module[] modules){
        tickThread = new Thread(()->{
            try {
                for(Module module : modules)
                    module.onTick();
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void doTicks(){
        tickThread.start();
    }
}
