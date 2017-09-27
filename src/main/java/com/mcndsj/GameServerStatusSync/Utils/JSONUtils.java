package com.mcndsj.GameServerStatusSync.Utils;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

/**
 * Created by Matthew on 2016/4/20.
 */
public class JSONUtils {

    //start
    public static String getDropTrackString(String type, int id){
        JSONObject object = new JSONObject();
        object.put("type",type);
        object.put("id",id);
        object.put("online", -999);
        object.put("max", Bukkit.getMaxPlayers());
        return object.toJSONString();

    }

    public static String getDynamicString(String type, int id){
        JSONObject object = new JSONObject();
        object.put("type",type);
        object.put("id",id);
        object.put("online", Bukkit.getOnlinePlayers().size());
        object.put("max", Bukkit.getMaxPlayers());
        return object.toJSONString();
    }
}
