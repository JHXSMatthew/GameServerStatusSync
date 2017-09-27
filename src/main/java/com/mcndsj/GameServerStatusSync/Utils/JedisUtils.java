package com.mcndsj.GameServerStatusSync.Utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Matthew on 2016/4/20.
 */
public class JedisUtils {
    private static JedisPool pool =null;
    public static redis.clients.jedis.Jedis get(){
        if(pool == null){
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);
            pool = new JedisPool(config,"192.168.123.2",6379,0,"l8VZl0HZ#fYKERkhfjyv(aezzN__WT^b");
        }
        //System.out.print("Wait : " + pool.getNumWaiters() + " " + " Active:" + pool.getNumActive());
        return pool.getResource();
    }



    public static void publish(String channel, String msg){
        Jedis jedis = get();
        jedis.publish(channel,msg);
        jedis.close();
    }

}
