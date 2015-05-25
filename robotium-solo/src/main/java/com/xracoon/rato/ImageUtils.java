package com.xracoon.rato;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

public class ImageUtils {
	
	static public int getAvgColor(View view,int sampleCount, double[] rect)
	{
		long alpha=0;
		long red=0;
		long green=0;
		long blue=0;
		int realCount=0;
		
		PixSampler sampler=new PixSampler(view,rect);
		
		for(int i=0; i<sampleCount; i++)
		{
			int c=sampler.getPixRandom();
			int ialpha=c >>> 24 ;
			int ired=(c >> 16) & 0xFF ;
			int igreen=(c >> 8) & 0xFF ;
			int iblue=c & 0xFF;
			
			alpha+=ialpha;
			red+=ired;
			green+=igreen;
			blue+=iblue;
			realCount++;
		}
		alpha=alpha/realCount;
		red=red/realCount;
		green=green/realCount;
		blue=blue/realCount;

		int color=Color.argb((int)alpha, (int)red, (int)green, (int)blue);
		LogEx.i("avarge color: "+getColor(color));
		return color;
	}
	
	
	static public int getMainColor(View view, int sampleCount, double[] rect, int[] excColor)
	{
		PixSampler sampler=new PixSampler(view,rect);

		int diff=32;
		int shift=16;
		Map<Integer, Integer> colorMap=new TreeMap<Integer,Integer>();
		Set<Integer> excSet=null;
		if(excColor!=null && excColor.length>0)
		{	
			excSet=new TreeSet<Integer>();
			for(int c: excColor)
			{
				int c1=normailizeColor(c, diff, shift);
				excSet.add(c1);
				//LogEx.i("exclude: "+getColor(c)+"=>"+getColor(c1));
			}
		}
		
		for(int i=0; i<sampleCount; i++)
		{
			int c=sampler.getPixRandom();
//			if((c >>>24 )<255)
//				continue;
			
			int c1=normailizeColor(c,diff,shift);
			//LogEx.i("skiped : "+getColor(c)+"=>"+getColor(c1));
			if(excSet!=null&& excSet.contains(c1))
				continue;
			
			if(colorMap.containsKey(c1))
				colorMap.put(c1, colorMap.get(c1)+1);
			else
				colorMap.put(c1, 1);
		}
		
		int max=-1;
		int maxc=-1;
		//LogEx.i("color groups: "+colorMap.size());
		for(int i: colorMap.keySet())
		{
			int icount=colorMap.get(i);
			LogEx.i(getColor(i)+"==> "+icount);
			if(icount>max)
			{
				max=colorMap.get(i);
				maxc=i;
			}
		}
		
		return maxc;
	}
	

	static public boolean existColor(View view, int color,int points, COLORRANGE range,  int sample,  int diff,  double [] rect)
	{
		PixSampler sampler=new PixSampler(view,rect);
		
		int pcount=1;
		for(int i=0; i<sample; i++)
		{
			int c=sampler.getPixRandom();
			
			if(nearColor(color,c,diff))
			{
				//LogEx.i("equal "+getColor(color)+": "+getColor(c));
				pcount++;
			}
			else if(range==COLORRANGE.DARKKER && isAllDarker(c,color))
			{
				//LogEx.i("dark "+getColor(color)+": "+getColor(c) );
				pcount++;
			}
			else if(range==COLORRANGE.LIGHTTER && isAllLightter(c,color))
			{
				//LogEx.i("light "+getColor(color)+": "+getColor(c) );
				pcount++;
			}
			
			if(pcount>=points)
				break;
		}
		
		LogEx.i("exist points: "+pcount+",  need "+points);
		return pcount>=points;
	}
	
	static public boolean nearColor(View view, int color, int sample,  int diff,  double [] rect, int [] excColors)
	{
		int mcolor=getMainColor(view,sample,rect,excColors);
		return nearColor(mcolor,color,diff);
	}
	
	static public boolean nearColor(int mcolor,int color, int diff)
	{
		int ired=(mcolor >> 16) & 0xFF ;
		int igreen=(mcolor >> 8) & 0xFF ;
		int iblue=mcolor & 0xFF;
			
		int tred=(color >> 16) & 0xFF ;
		int tgreen=(color >> 8) & 0xFF ;
		int tblue=color & 0xFF;
		
		int dr=Math.abs(ired-tred);
		int dg=Math.abs(igreen-tgreen);
		int db=Math.abs(iblue-tblue);
		//LogEx.i("main reg: "+ired+",  main green: "+igreen+",  main blue: "+iblue);
		//LogEx.i("deta reg: "+dr+",  deta green: "+dg+",  deta blue: "+db);
		return dr<=diff && dg<=diff && db<=diff;
	}
	
	static public boolean isAllDarker(int c1, int c2)
	{
		int ired=(c1 >> 16) & 0xFF ;
		int igreen=(c1 >> 8) & 0xFF ;
		int iblue=c1 & 0xFF;
			
		int tred=(c2 >> 16) & 0xFF ;
		int tgreen=(c2 >> 8) & 0xFF ;
		int tblue=c2 & 0xFF;
		return  (ired-tred)<=0 && (igreen-tgreen)<=0 && (iblue-tblue)<=0;
	}
	static public boolean isAllLightter(int c1, int c2)
	{
		int ired=(c1 >> 16) & 0xFF ;
		int igreen=(c1 >> 8) & 0xFF ;
		int iblue=c1 & 0xFF;
			
		int tred=(c2 >> 16) & 0xFF ;
		int tgreen=(c2 >> 8) & 0xFF ;
		int tblue=c2 & 0xFF;
		return  (ired-tred)>=0 && (igreen-tgreen)>=0 && (iblue-tblue)>=0;
	}
	
	static public int normailizeElem(int icolor, int diff,int shift)
	{
		return Math.max(Math.min(((int)((icolor+shift)/diff))*diff,255),0);
	}
	static public int normailizeColor(int c,int diff, int shift)
	{
		int ired=(c >> 16) & 0xFF ;
		int igreen=(c >> 8) & 0xFF ;
		int iblue=c & 0xFF;
		
		ired=normailizeElem(ired,32,16);
		igreen=normailizeElem(igreen,32,16);
		iblue=normailizeElem(iblue,32,16);
		
		c=Color.argb(255, ired, igreen, iblue);
		return c;
	}
	
	static public int getBaseColor(View view,int sampleCount, double[] rect)
	{	
		long red=1;
		long green=1;
		long blue=1;

		PixSampler sampler=new PixSampler(view,rect);
		
		for(int i=0; i<sampleCount; i++)
		{
			int c=sampler.getPixRandom();
			int ialpha=c >>> 24 ;
			int ired=(c >> 16) & 0xFF ;
			int igreen=(c >> 8) & 0xFF ;
			int iblue=c & 0xFF;
			
			int min=Math.min(Math.min(ired, igreen),iblue);
			//LogEx.i(pos+", "+x+", "+y+":  "+ired+","+igreen+","+iblue+"=>"+min);
			red+=ired-min;
			green+=igreen-min;
			blue+=iblue-min;
		}
		long max=Math.max(Math.max(red, green),blue);
		int color=0;
		if(max>255)
			color=Color.rgb((int)(red*255/max), (int)(green*255/max), (int)(blue*255/max));
		else
			color=Color.rgb((int)(red), (int)(green), (int)(blue));
			
		return color;
	}
	
	static public String getColor(int c)
	{
		String color="(a: "+(c>>>24)+", r: "+((c>>16) &0xFF)+", g: "+((c>>8) &0xFF)+", b: "+(c & 0xFF)+")";
		return color;
	}
	
	static public boolean nearGray(View view,int sampes,int diff, double[] rect)
	{
		int c=ImageUtils.getBaseColor(view, sampes, rect);
		LogEx.i("near gray: "+getColor(c));
		return ImageUtils.nearGray(c,diff);
	}

	
	static public boolean nearGray(int c,int diff)
	{
		int r=((c>>16) &0xFF);
		int g=(c>>8) &0xFF;
		int b=c & 0xFF;
		
		return (r-b)<=diff && (r-g)<=diff && (b-g)<=diff;
	}
	
	static public boolean nearRed(View view,int sampes,int diff, double[] rect)
	{
		int c=ImageUtils.getBaseColor(view, sampes,rect);
		LogEx.i("near read: "+getColor(c));
		return ImageUtils.nearRed(c,diff);
	}
	
	static public boolean nearRed(int c,int diff)
	{
		int r=((c>>16) &0xFF)-diff;
		int g=(c>>8) &0xFF;
		int b=c & 0xFF;
		
		return r>=b&&r>=g;
	}
	
	static public boolean nearGreen(View view,int sampes,int diff, double[] rect)
	{
		int c=ImageUtils.getBaseColor(view, sampes,rect);
		LogEx.i("near green: "+getColor(c));
		return ImageUtils.nearGreen(c,diff);
	}
	
	static public boolean nearGreen(int c, int diff)
	{
		int r=(c>>16) &0xFF;
		int g=((c>>8) &0xFF)-diff;
		int b=c & 0xFF;
		
		return g>=r&&g>=b;
	}
	
	static public boolean nearBlue(View view,int sampes,int diff,  double[] rect)
	{
		int c=ImageUtils.getBaseColor(view, sampes,rect);
		LogEx.i("near blue: "+getColor(c));
		return ImageUtils.nearBlue(c,diff);
	}
	
	static public boolean nearBlue(int c, int diff)
	{
		int r=(c>>16) &0xFF;
		int g=(c>>8) &0xFF;
		int b=(c & 0xFF)-diff;
		
		return b>=r&&b>=g;
	}
	

}


