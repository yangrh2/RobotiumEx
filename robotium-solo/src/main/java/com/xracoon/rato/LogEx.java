package com.xracoon.rato;

import java.util.Date;

import org.dom4j.Document;

import android.util.Log;

public class LogEx{
	static private String tag;
	static private Date lastTime;
	static private Date startTime;
	
	static public void setTag(String t)
	{
		tag=t;
	}
	
	static public void d(String msg)
	{
		Log.d(tag,msg);
	}
	static public void e(String msg)
	{
		Log.e(tag,msg);
	}
	static public void i(String msg)
	{
		Log.i(tag,msg);
	}
	static public void v(String msg)
	{
		Log.v(tag,msg);
	}
	static public void w(String msg)
	{
		Log.w(tag,msg);
	}
	static public void wtf(String msg)
	{
		Log.wtf(tag,msg);
	}
	
	
	static public void iStart(String msg)
	{
		lastTime=new Date();
		startTime=lastTime;
		i("[0 s]"+msg);
	}
	
	/**
	 * info级别，输出中包含距离上次调用的时间间隔
	 */
	static public void iStep(String msg)
	{
		if(lastTime==null)
			i("[null]"+msg);
		else
		{
			Date now=new Date();
			i("["+((now.getTime()-lastTime.getTime())/1000.0)+" s]"+msg);
			lastTime=now;
		}
	}
	
	/**
	 * 标记某个测试点通过
	 */
	public static void pass(String testPoint)
	{
		StackTraceElement[] stacks = new Throwable().getStackTrace();
		String location=stacks.length>1?stacks[1].toString():"";
		
		i("PASS [["+testPoint+"]] @[["+location+"]]");
	}
	
	public static void logLongText(String text)
	{
		//3500
		int pack=3500;
		int len=text.length();
		for(int i=0; i<len; i=i+pack)
		{
			int end=Math.min(i+pack, len);
			LogEx.i(text.substring(i,end));
		}
	}
	public static void LogXml(Document doc)
	{
		logLongText(doc.asXML());
	}
	
	public static void diff(String real, String expect)
	{
		LogEx.i(real+"<<=real, expect=>>"+expect);
	}
}
