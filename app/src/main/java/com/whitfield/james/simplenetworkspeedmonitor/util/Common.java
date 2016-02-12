package com.whitfield.james.simplenetworkspeedmonitor.util;

/**
 * Created by jwhit on 05/02/2016.
 */
public class Common {

    public static String bytesToKbs(Long bytes){

        Long tmp = bytes/1024;
        return String.valueOf(tmp);
    }

    public static String stringDownNotificationOutput(long value){

        if(value <(1024*1024)){
            return  "\u25bc " + convertBytesToKbs(value) + " Kbs";
        }else{
            return  "\u25bc " + convertBytesToMbs(value) + " Mbs";
        }

    }

    public static String stringUpNotificationOutput(long value){

        if(value <(1024*1024)){
            return  "\u25b2 " + convertBytesToKbs(value) + " Kbs";
        }else{
            return  "\u25b2 " + convertBytesToMbs(value) + " Mbs";
        }

    }

    public static String stringDownTotalOutput(long value){



        if(value < 1024){
            return  "\u25bc " + value + " Bytes";
        }else if(value <(1024*1024)){
            return  "\u25bc " + convertBytesToKbs(value) + " Kb";
        }else if(value < (1024*1024*1024)){
            return  "\u25bc " + convertBytesToMbs(value) + " Mb";
        }else{
            return  "\u25bc " + convertBytesToGbs(value) + " Gb";
        }

    }

    public static String stringUpTotalOutput(long value){

        if(value < 1024){
            return  "\u25b2 " + value + " Bytes";
        }else if(value <(1024*1024)){
            return  "\u25b2 " + convertBytesToKbs(value) + " Kb";
        }else if(value < (1024*1024*1024)){
            return  "\u25b2 " + convertBytesToMbs(value) + " Mb";
        }else{
            return  "\u25b2 " + convertBytesToGbs(value) + " Gb";
        }
    }

    public static String convertBytesToGbs(long value) {

        Double megaValue = (double)value / (1024*1024*1024);
        megaValue = megaValue *100;
        value = Math.round(megaValue);
        megaValue = (double)value/100;
        return String.valueOf(megaValue);
    }

    public static String convertBytesToMbs(long value) {

        Double megaValue = (double)value / (1024*1024);
        megaValue = megaValue *100;
        value = Math.round(megaValue);
        megaValue = (double)value/100;
        return String.valueOf(megaValue);
    }

    public static String convertBytesToKbs(Long bytes){

        if(bytes > 0){
            Long kbs = bytes/1024;
            if(kbs == 0){
                return " \u22450";
            }else{
                return String.valueOf(kbs);
            }
        }else{
            return "0";
        }
    }
}
