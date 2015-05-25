package com.xracoon.rato;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class PixSampler {
	
	private View view;
	private Bitmap bitmap;
	private double [] rectMargin;
	private int [] rectCoord;
	
	private int count;
	
	public PixSampler(View view, double[] margin)
	{
		this.view=view;
		this.bitmap=getBitmap(view);
		
		this.rectMargin=margin;
		this.rectCoord=margin2Coord(view.getWidth(),view.getHeight(),margin);
	}
	
	/**
	 * 根据边距值，计算出坐标范围
	 * @param width    width of bitmap
	 * @param height   height of bitmap
	 * @param margin  边距值，类型为double[4]，数组中0-3元素分别为左上右下。小于1按百分比计算，大于1按像素计算。负数表示按相反的方向计算（左右相反，上下相反）。
	 * @return  坐标范围, 类型为int[4], 数组中0-3元素分别为左上右下，4-5分别为范围区域的宽高
	 */
	private int[] margin2Coord(int w, int h, double[] margin)
	{
		int left=0;
		int right=0;
		int up=0;
		int down=0;
		if(margin!=null && margin.length>=4)
		{
			if(Math.abs(margin[0])>1)
				left=(int) margin[0];
			else
				left=(int) (margin[0]*w);
			
			if(Math.abs(margin[1])>1)
				up=(int) margin[1];
			else
				up=(int) (margin[1]*h);
			
			if(Math.abs(margin[2])>1)
				right=(int) margin[2];
			else
				right=(int) (margin[2]*w);
			
			if(Math.abs(margin[3])>1)
				down=(int) margin[3];
			else
				down=(int) (margin[3]*h);
		
			
			if(left<0)
				left=w+left;
			if(up<0)
				up=h+up;
			if(right<0)
				right=w+right;
			if(down<0)
				down=w+down;
			//LogEx.i("width:"+w+",  height: "+h);
			//LogEx.i("margin:  left: "+left+", top: "+up+",  right:"+right+", down:"+down);
			
			int [] coord=new int[]{left,up,w-right,h-down, w-right-left, h-down-up};
			//LogEx.i("range: width: "+coord[4]+",  height: "+coord[5]);
			LogEx.i("coord range:  left: "+coord[0]+", top: "+coord[1]+",  right:"+coord[2]+", down:"+coord[3]+"\nrange: width: "+coord[4]+",  height: "+coord[5]);
			return coord;
		}
		return null;
	}
	
	static public Bitmap getBitmap(View v)
	{
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    v.draw(canvas);
	    return bitmap;
	}
	
	public int getPix(int x, int y)
	{
		return bitmap.getPixel(x, y);
	}
	
	public int getPixRandom()
	{
		Random rand=new Random();
		int x=rectCoord[0]+rand.nextInt(rectCoord[4]);
		int y=rectCoord[1]+rand.nextInt(rectCoord[5]);
		
		//LogEx.i(x+","+y);
		
		return bitmap.getPixel(x, y);
	}

}
