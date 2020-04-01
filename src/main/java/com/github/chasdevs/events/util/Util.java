package com.github.chasdevs.events.util;

public class Util {
    
    @SafeVarargs
    public static <T> T coalesce(T... params)
    {
        for (T param : params)
            if (param != null)
                return param;
        return null;
    }
    
}
