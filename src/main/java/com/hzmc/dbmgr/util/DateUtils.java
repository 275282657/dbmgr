package com.hzmc.dbmgr.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	
	static SimpleDateFormat format;
	static{
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        format.setTimeZone(timeZone);
	}
	
	public static void main(String argv[]) throws ParseException{
		long a = 1528699202142l;
		long b = a-(a + 28800000l)%86400000l;
		System.out.println(new Date(a));
		System.out.println(new Date(b));
		System.out.println(b);
		System.out.println(dateToStamp("2018-06-11 00:00:00"));
		System.out.println(stampToDate(String.valueOf(a)));
		System.out.println(TimeZone.getDefault());
	}
	
	/* 
     * 将时间转换为时间戳
     */    
    public static String dateToStamp(String s) throws ParseException{
        String res;
        Date date = format.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }    
    /* 
     * 将时间转换为时间戳
     */ 
    public static long dateToStampRlong(String s) throws ParseException{
        Date date = format.parse(s);
        long ts = date.getTime();
        return ts;
    }
    /* 
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = format.format(date);
        return res;
    }
    /* 
     * 将时间戳转换为时间
     */
    public static String stampToDate(long s){
        String res;
        Date date = new Date(s);
        res = format.format(date);
        return res;
    }
    
    public static String dateToString(Date date){
    	return format.format(date);
    }
}
