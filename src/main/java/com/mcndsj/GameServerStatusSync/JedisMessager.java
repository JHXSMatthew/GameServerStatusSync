package com.mcndsj.GameServerStatusSync;

import com.mcndsj.GameServerStatusSync.Utils.JSONUtils;
import com.mcndsj.GameServerStatusSync.Utils.JedisUtils;
import com.mcndsj.GameServerStatusSync.Utils.WordUtils;
import me.konsolas.aac.api.AACAPIProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Matthew on 2016/4/18.
 */
public class JedisMessager implements Listener{

    private String typeName = null;
    private int id = -1;
    private ExecutorService thread;
    private final Core core;

    private State state = State.setup;
    //need to be close
    private JedisPubSub ps;

    private boolean isCountChanged = false;
    /**
     * Full - 5 seconds
     * not full - 2 seconds
     */
    private int cooldown = 0;
    private int maxTimeLimit = 0;
    // implement a pending queue
    private boolean shouldDirectlyStartUp = false;
    private BukkitTask sendRunnable = null;

    public JedisMessager(final Core core){
        this.core = core;
        thread = Executors.newCachedThreadPool();

        thread.execute(new Runnable() {
            public void run() {
                try (Jedis j  =  JedisUtils.get()){
                   j.subscribe(new ServerNameQuery(),"ServerManage.ServerNameQuery." + core.getLocalIp());

                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        new BukkitRunnable(){
            @Override
            public void run() {
                if(state == State.preActive || typeName != null)
                    cancel();

                Jedis j = JedisUtils.get();
                try {
                    j.publish("ServerManage.ServerNameQuery",core.getLocalIp());
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                j.close();
            }
        }.runTaskTimerAsynchronously(Core.get(),0,20 * 5);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt){
        this.isCountChanged = true;
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt){
        this.isCountChanged = true;
    }


    public void setShouldDirectlyStartUp(){
        this.shouldDirectlyStartUp = true;
    }


    public State getState(){
        return state;
    }
    /**
     *  Channel- ServerInvoke.typeName
     *  Message- serverNumber|ServerMaxCount
     */
    public void sendStartMsg(){
        if(typeName == null){
            System.out.print("ERROR,No Server Type defined yet!");
            Bukkit.getServer().shutdown();
            return;
        }
        if(Bukkit.getPluginManager().getPlugin("AAC") != null){
            //handle AAC
            if (!AACAPIProvider.isAPILoaded()) {
                new BukkitRunnable() {
                    int count = 0;

                    @Override
                    public void run() {
                        count++;
                        if (AACAPIProvider.isAPILoaded()) {
                            sendStartMsg();
                            cancel();
                        } else if (count > 120) {
                            System.out.print("ERROR,AAC Time out 120 seconds!");
                            Bukkit.getServer().shutdown();
                        }
                    }
                }.runTaskTimer(Core.get(), 0, 20);
                return;
            }
        }

        state = State.active;

        Core.get().getServer().getPluginManager().registerEvents(this,Core.get());

        thread.execute(new Runnable() {
            public void run() {
                JedisPubSub temp = new onPlayerSend();
                ps = temp;
                try (Jedis j  =  JedisUtils.get()){
                    j.subscribe(temp, "PlayerSend." + typeName + id);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        thread.execute(new Runnable() {
            public void run() {
                heartBeats();
            }
        });


        sendRunnable = new BukkitRunnable(){
            public void run() {
                maxTimeLimit++;
                if(cooldown <= 0) {
                    if ((isCountChanged || maxTimeLimit > 30) && Bukkit.getOnlinePlayers().size() != Bukkit.getMaxPlayers()) {
                        heartBeats();
                        isCountChanged = false;
                        maxTimeLimit = 0;
                    }
                }else{
                    cooldown --;
                    isCountChanged = true;
                }
            }
        }.runTaskTimer(Core.get(),0,20);

    }

    /**
     * send ack packet to server
     */
    private void heartBeats(){
        System.out.println("heartBeats." + typeName + id);
        JedisUtils.publish("GameACK." + typeName, JSONUtils.getDynamicString(typeName,id));
    }

    /**
     * send Drop
     */
    public void sendDrop(){
        thread.execute(new Runnable() {
            public void run() {
                state = State.dropped;
                try {
                    HandlerList.unregisterAll(get());
                }catch(Exception e){
                }
                try {
                    sendRunnable.cancel();
                }catch(Exception e){
                }
                try {
                    if (ps.isSubscribed())
                        ps.unsubscribe();
                }catch(Exception e){

                }
                JedisUtils.publish("GameACK." + typeName, JSONUtils.getDropTrackString(typeName,id));
                System.out.println("DropConnection." + typeName + id);
            }
        });


    }

    public String getType(){
        return typeName + id;
    }

    public void sendDropSync(){
        state = State.dropped;
        try {
            HandlerList.unregisterAll(get());
        }catch(Exception e){
        }
        try {
            sendRunnable.cancel();
        }catch(Exception e){
        }
        try {
            if (ps.isSubscribed())
                ps.unsubscribe();
        }catch(Exception e){

        }
        JedisUtils.publish("GameACK." + typeName, JSONUtils.getDropTrackString(typeName,id));
    }





    public class onPlayerSend  extends JedisPubSub{
        @Override
        public void onMessage(String channel, String message) {
            int coming = Integer.parseInt(message);
            if(core.getServer().getOnlinePlayers().size() + coming >= core.getServer().getMaxPlayers()){
                cooldown = 1;
            }else{
                cooldown = 1;
            }
        }
    }

    public class ServerNameQuery extends JedisPubSub{
        boolean isReceived = false;
        @Override
        public void onMessage(String channel, String message) {
            if(isReceived){
                return;
            }
            isReceived  = true;
            try {
                System.out.print(message);
                if(channel.equals("ServerManage.ServerNameQuery." + core.getLocalIp())){
                    typeName = message.replace(String.valueOf(WordUtils.getIntFromString(message)),"");
                    id = WordUtils.getIntFromString(message);
                    state = State.preActive;
                    unsubscribe("ServerManage.ServerNameQuery."+core.getLocalIp());
                    if(shouldDirectlyStartUp) {
                        sendStartMsg();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public JedisMessager get(){
        return this;
    }

    public enum State{
        setup,preActive,active,dropped;
    }
}
