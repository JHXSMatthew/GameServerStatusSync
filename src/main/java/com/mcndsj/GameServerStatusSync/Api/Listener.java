package com.mcndsj.GameServerStatusSync.Api;

import com.mcndsj.GameEvent.Events.GameInitReadyEvent;
import com.mcndsj.GameEvent.Events.GameStartEvent;
import org.bukkit.event.EventHandler;

/**
 * Created by Matthew on 11/06/2016.
 */
public class Listener implements org.bukkit.event.Listener{
    @EventHandler
    public void onGameStart(GameStartEvent evt){
        SyncApi.getApi().dropTrack();
    }
    @EventHandler
    public void onGameInit(GameInitReadyEvent evt){
        SyncApi.getApi().startTrack();
        System.out.println("GameStatus->GameInit received,ready to go!");
    }

}
