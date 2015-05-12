package com.xracoon.rato;

import java.util.Date;

import com.robotium.solo.Solo;

public class Timer {
	
	private static Date start=new Date();
	static public void start()
	{
		start=new Date();
	}
	static public int spend()
	{
		return (int)(new Date().getTime()-start.getTime());
	}

}
