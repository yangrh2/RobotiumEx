package com.xracoon.rato;

import com.robotium.solo.SoloEx;

import android.view.View;

public class By {
	public static final int ID=0;
	public static final int TEXT=1;
	
	public int type;
	public Object val;
	public View parent;
	
	public By id(String id)
	{
		By by=new By();
		by.type=ID;
		by.val=id;
		
		return by;
	}
	
	public By text(String text)
	{
		By by=new By();
		by.type=TEXT;
		by.val=text;
		
		return by;
	}
	
	public By id(String id, View parent)
	{
		By by=new By();
		by.type=ID;
		by.val=id;
		by.parent=parent;
		
		return by;
	}
	
	public By text(String text, View parent)
	{
		By by=new By();
		by.type=TEXT;
		by.val=text;
		by.parent=parent;
		
		return by;
	}
	
	public View getView(SoloEx solo,By by)
	{
		if(by.type==ID)
		{
			
		}
		else if(by.type==TEXT)
		{
			
		}
			
		return null;
	}
}
