package com.mcndsj.GameServerStatusSync.Utils;

import java.util.regex.Pattern;

/**
 * Created by Matthew on 2016/4/18.
 */
public class WordUtils {

    public static int getIntFromString(String input){
       return  Integer.parseInt(Pattern.compile("[^0-9]").matcher(input).replaceAll(""));
    }
}
