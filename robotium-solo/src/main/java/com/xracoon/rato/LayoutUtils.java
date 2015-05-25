package com.xracoon.rato;

import android.view.View;

public class LayoutUtils {
	
	static public int[] getCenter(View view)
	{
		int [] size=new int[2];
		view.getLocationOnScreen(size);
		size[0]=size[0]+view.getWidth()/2;
		size[1]=size[1]+view.getHeight()/2;
		return size;
	}
	
	static public int[] getOrigin(View view)
	{
		int [] size=new int[2];
		view.getLocationOnScreen(size);
		return size;
	}
	
	static public boolean betweenV(View up, View mid, View down)
	{
		int[] pos1=getOrigin(up);
		int[] pos2=getOrigin(mid);
		int[] pos3=getOrigin(down);
		
		boolean v1=(pos1[1]+up.getHeight())<=pos2[1];
		boolean v2=(pos2[1]+mid.getHeight())<=pos3[1];
		
		return v1 && v2;
	}
	
	static public boolean betweenH(View left, View mid, View right)
	{
		int[] pos1=getOrigin(left);
		int[] pos2=getOrigin(mid);
		int[] pos3=getOrigin(right);
		
		boolean v1=(pos1[0]+left.getHeight())<=pos2[0];
		boolean v2=(pos2[0]+mid.getHeight())<=pos3[0];
		
		return v1 && v2;
	}
	
	static public boolean vertical(View up, View down)
	{
		int[] pos1=getOrigin(up);

		int[] pos2=getOrigin(down);
		
		boolean v1=(pos1[1]+up.getHeight())<=pos2[1];
		
		return v1 ;
	}
	
}
