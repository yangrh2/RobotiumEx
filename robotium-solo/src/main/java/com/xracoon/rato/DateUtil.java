package com.xracoon.rato;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class DateUtil {
	/**
	 * 获取当前时间, 并用fmtStr参数指定的形式格式化成字符串
	 * @param fmtStr 字符串格式，默认格式为yyyy-MM-dd HH:mm:ss.SSS
	 */
	static public String format(String fmtStr)
	{
		if(fmtStr==null||fmtStr.trim().length()==0)
			fmtStr="yyyy-MM-dd HH:mm:ss.SSS";
		return new SimpleDateFormat(fmtStr).format(new Date());
	}
	
	/**
	 * 将Date对象用fmtStr参数指定的形式格式化成字符串
	 * @param fmtStr 字符串格式，默认格式为为yyyy-MM-dd HH:mm:ss.SSS
	 */
	static public String  format(Date time, String fmtStr)
	{
		if(fmtStr==null||fmtStr.trim().length()==0)
			fmtStr="yyyy-MM-dd HH:mm:ss.SSS";
		return new SimpleDateFormat(fmtStr).format(time);
	}
	
	/**
	 * 将字符串解析为Date对象
	 * @param timeStr
	 * @param format  yyyy-MM-dd HH:mm:ss.SSS
	 */
	static public Date parse(String timeStr, String format) throws ParseException
	{
			return new SimpleDateFormat(format).parse(timeStr);
	}
	
	
	/**
	 * 验证到当前时间的时间差是否小于某一范围，dateStr和fmtStr为一个时间点，当前时间为第二个时间点
	 * @param dateStr 时间字符串
	 * @param fmtStr 格式字符串
	 * @param timeDiff 允许的时间范围（毫秒）
	 * @return
	 * @throws ParseException
	 */
	public static boolean checkDate(String dateStr, String fmtStr, int timeDiff) throws ParseException
	{
		SimpleDateFormat fmt=new SimpleDateFormat(fmtStr); 
		Date date=fmt.parse(dateStr);
		Date now=new Date();
		
		LogEx.i(now.toLocaleString()+"  -  "+date.toLocaleString());
		LogEx.i((now.getTime()-date.getTime())+" < "+timeDiff+"?");
		return (now.getTime()-date.getTime())<timeDiff;
	}
	
	
	/**
	 * 验证两个时间点的时间差是否小于某一范围
	 * @param before 时间点1
	 * @param after 时间点2
	 * @param timeDiff 允许的时间范围（毫秒）
	 * @return
	 * @throws ParseException
	 */
	public static boolean checkDate(Date before, Date after, int timeDiff) throws ParseException
	{	
		LogEx.i(DateUtil.format(before,null));
		LogEx.i(DateUtil.format(after,null));
		Log.i("AAAAAA",(Math.abs(after.getTime()-before.getTime()))+" < "+timeDiff+"?");
		return (Math.abs(after.getTime()-before.getTime()))<timeDiff;
	}
}
