package com.mcndsj.GameServerStatusSync;

import com.mcndsj.GameServerStatusSync.Api.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Matthew on 2016/4/18.
 */
public class Core extends JavaPlugin {

    private JedisMessager manager = null;
    private static Core instance;
    public void onEnable(){
        instance = this;
        manager = new JedisMessager(this);
        getServer().getPluginManager().registerEvents(new Listener(),this);
    }

    public void onDisable(){
        manager.sendDropSync();

    }

    public static Core get(){
        return instance;
    }


    public JedisMessager getManager(){
        return manager;
    }

    public String getLocalIp() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements()){
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()){
                InetAddress i = (InetAddress) ee.nextElement();
                if(i.getHostAddress().contains("192")){
                    return i.getHostAddress()+":"+getServer().getPort();
                }
            }
        }
        return null;
    }

    public static String getType(){
        return instance.getManager().getType();
    }
}
