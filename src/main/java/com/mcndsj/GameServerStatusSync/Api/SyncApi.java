package com.mcndsj.GameServerStatusSync.Api;

import com.mcndsj.GameServerStatusSync.Core;
import com.mcndsj.GameServerStatusSync.JedisMessager;

/**
 * Created by Matthew on 2016/4/18.
 */
public class SyncApi {
    private static SyncApi api = null;

   public static SyncApi getApi(){
       if(api == null){
           api = new SyncApi();
       }
       return api;
   }

    /**
     *  will drop when GameStartEvent
     * @return
     */
    public boolean dropTrack(){
        if(Core.get().getManager().getState() == JedisMessager.State.active){
            Core.get().getManager().sendDrop();
            return true;
        }
        return false;
    }

    /**
     *  will start when GameInitReadyEvent
     * @return
     */
    public boolean startTrack(){
        if(Core.get().getManager().getState() == JedisMessager.State.dropped
                || Core.get().getManager().getState()== JedisMessager.State.preActive) {
            Core.get().getManager().sendStartMsg();
            return true;
        }else if(Core.get().getManager().getState() == JedisMessager.State.setup){
            Core.get().getManager().setShouldDirectlyStartUp();
        }
        return false;
    }

}
