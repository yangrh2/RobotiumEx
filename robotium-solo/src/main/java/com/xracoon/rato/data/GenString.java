package com.xracoon.rato.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import com.xracoon.rato.DateUtil;

public class GenString {
	private final static String symbols="~!@#$%^&*()-_=+[{]};:'\"\\|,<.>/?";
	private final static String lowchars="abcdefghijklmnopqrstuvwxyz";
	private final static String upchars="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final static String alphabet=lowchars+upchars;
	private final static String digitals="0123456789";
	private final static String blanks=" \t\n";
	private final static int chardelta = 0x9fa5 - 0x4e00 + 1;
	/**
	 * @param length
	 * @param mark d:digital, a:alphabet chars, l:lower-case chars, u:upper-case chars, s:symbols, c:chinese character
	 * @return
	 */
	public static String getString(int min,int max, String mark)
	{
		if(min<=0||min>max)
			return "";
			
		mark=mark.toLowerCase().replaceAll("[^daluscb]", "");
		if(mark.contains("a"))
			mark=mark.replaceAll("l|u", "");
		
		List<Character> charTypes=new ArrayList<Character>();
		for(char c:mark.toCharArray())
			if(!charTypes.contains(c))
				charTypes.add(c);
		
		int types=charTypes.size();
		if(types==0)
			return "";
		
		StringBuilder getString=new StringBuilder();
		Random random = new Random();   
	    
	    int length=min;
	    if(min!=max)
	    	length=random.nextInt(max+1-min)+min;
	    
		for(int i=0; i<length; i++)
		{
			char type=charTypes.get(random.nextInt(types));
			if((i+1)==length && type=='c')
			{
				if(charTypes.size()==1)
					return "";

				charTypes.remove(charTypes.indexOf('c'));
				types=charTypes.size();
				type=charTypes.get(random.nextInt(types));
			}
			
			switch(type)
			{
				case 'a': 
					getString.append(alphabet.charAt(random.nextInt(alphabet.length()))); 
					break;
				case 'd':
					getString.append(digitals.charAt(random.nextInt(digitals.length()))); 
					break;
				case 'l':
					getString.append(lowchars.charAt(random.nextInt(lowchars.length()))); 
					break;
				case 'u':
					getString.append(upchars.charAt(random.nextInt(upchars.length()))); 
					break;
				case 's':
					getString.append(symbols.charAt(random.nextInt(symbols.length()))); 
					break;
				case 'c':
					getString.append((char)(0x4e00 + random.nextInt(chardelta))); 
					i++;
					break;
				case 'b':
					getString.append(blanks.charAt(random.nextInt(blanks.length()))); 
					break;
			}
		}
	
		return getString.toString();
	}
	
	public static String getStringBase(int length, String baseValue) 
	{   
	    String base = baseValue;   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) 
	    {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 } 
	
	public static String getStringBase(int min,int max, String baseValue) 
	{   
	    String base = baseValue;   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    int length=random.nextInt(max+1-min)+min;
	    for (int i = 0; i < length; i++) 
	    {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 } 
	
	/**
	 * generate fix length string by flags
	 * @param mark d:digital, a:chars, l:lowchars, u:upchars, s:symbols, c:chinese character
	 */
	public static String getString(int length, String mark)
	{
		return getString(length,length,mark);
	}

	public static String getNickName(String account)
	{
		if(account.matches("\\d{11}"))
			return "nick"+account;
		else
			return "nick"+account.substring(0,account.indexOf("@"));
	}
	
	public static void main(String[] args)
	{
		for(int i=0; i<10; i++)
			System.out.println(GenString.getString(4, "a"));
		
		for(int i=0; i<10; i++)
			System.out.println(GenString.getString(10, "d"));
		
		for(int i=0; i<10; i++)
			System.out.println(GenString.getString(5,8, "ad"));
	}
}
