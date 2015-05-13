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
	
	static public int getAvgColor(Bitmap bitmap,int sampleCount, double[] rect)
	{
		long alpha=0;
		long red=0;
		long green=0;
		long blue=0;
		int realCount=0;
		
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		Random rand=new Random();
		
		int left=0;
		int right=0;
		int up=0;
		int down=0;
		if(rect!=null && rect.length>=4)
		{
			left=(int) (rect[0]*w);
			up=(int) (rect[1]*h);
			right=(int) (rect[2]*w);
			down=(int) (rect[3]*h);
		}
		for(int i=0; i<sampleCount; i++)
		{
			int x=left+rand.nextInt(w-left-right);
			int y=up+rand.nextInt(h-up-down);
			
			int c=bitmap.getPixel(x, y);
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
	
	
	static public int getMainColor(Bitmap bitmap, int sampleCount, double[] rect, int[] excColor)
	{
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		Random rand=new Random();
		
		int left=0;
		int right=0;
		int up=0;
		int down=0;
		if(rect!=null && rect.length>=4)
		{
			if(Math.abs(rect[0])>1)
				left=(int) rect[0];
			else
				left=(int) (rect[0]*w);
			
			if(Math.abs(rect[1])>1)
				up=(int) rect[1];
			else
				up=(int) (rect[1]*h);
			
			if(Math.abs(rect[2])>1)
				right=(int) rect[2];
			else
				right=(int) (rect[2]*w);
			
			if(Math.abs(rect[3])>1)
				down=(int) rect[3];
			else
				down=(int) (rect[3]*h);
		
			
			if(left<0)
				left=w+left;
			if(up<0)
				up=h+up;
			LogEx.i("width:"+w+",  height: "+h+",  left: "+left+", top: "+up+",  right:"+right+", down:"+down);
		}
		
		int diff=32;
		int shift=16;
		Map<Integer, Integer> colorMap=new TreeMap<Integer,Integer>();
		Set<Integer> excSet=null;
		if(excColor!=null && excColor.length>0)
		{	
			excSet=new TreeSet<Integer>();
			for(int c: excColor)
				excSet.add(normailizeColor(c, diff, shift));
		}
		
		for(int i=0; i<sampleCount; i++)
		{
			int x=left+rand.nextInt(w-left-right);
			int y=up+rand.nextInt(h-up-down);
			
			int c=bitmap.getPixel(x, y);
//			if((c >>>24 )<255)
//				continue;
			
			c=normailizeColor(c,diff,shift);
			if(excSet!=null&& excSet.contains(c))
				continue;
			
			if(colorMap.containsKey(c))
				colorMap.put(c, colorMap.get(c)+1);
			else
				colorMap.put(c, 1);
		}
		
		int max=-1;
		int maxc=-1;
		
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
	
	static public boolean nearColor(View view, int color, int sample,  int diff,  double [] rect, int [] excColors)
	{
		int mcolor=getMainColor(getBitmap(view),sample,rect,excColors);
		int ired=(mcolor >> 16) & 0xFF ;
		int igreen=(mcolor >> 8) & 0xFF ;
		int iblue=mcolor & 0xFF;
			
		int tred=(color >> 16) & 0xFF ;
		int tgreen=(color >> 8) & 0xFF ;
		int tblue=color & 0xFF;
		
		int dr=Math.abs(ired-tred);
		int dg=Math.abs(igreen-tgreen);
		int db=Math.abs(iblue-tblue);
		LogEx.i("deta reg: "+dr+",  deta green: "+dg+",  deta blue: "+db);
		return dr<=diff && dg<=diff && db<=diff;
	}
	
	static public int normailizeElem(int icolor, int diff,int shift)
	{
		return Math.max(Math.min(icolor+shift,255),0)/diff*diff;
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
	
	static public int getBaseColor(Bitmap bitmap,int sampleCount, double[] rect)
	{	
		long red=1;
		long green=1;
		long blue=1;

		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		Random rand=new Random();
		
		int left=0;
		int right=0;
		int up=0;
		int down=0;
		if(rect!=null && rect.length>=4)
		{
			left=(int) (rect[0]*w);
			up=(int) (rect[1]*h);
			right=(int) (rect[2]*w);
			down=(int) (rect[3]*h);
		}
		
		for(int i=0; i<sampleCount; i++)
		{
			int x=left+rand.nextInt(w-left-right);
			int y=up+rand.nextInt(h-up-down);
			
			int c=bitmap.getPixel(x, y);
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
		int c=ImageUtils.getBaseColor(ImageUtils.getBitmap(view), sampes, rect);
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
		int c=ImageUtils.getBaseColor(ImageUtils.getBitmap(view), sampes,rect);
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
		int c=ImageUtils.getBaseColor(ImageUtils.getBitmap(view), sampes,rect);
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
		int c=ImageUtils.getBaseColor(ImageUtils.getBitmap(view), sampes,rect);
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
	
	static public Bitmap getBitmap(View v)
	{
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    v.draw(canvas);
	    return bitmap;
	}
}
