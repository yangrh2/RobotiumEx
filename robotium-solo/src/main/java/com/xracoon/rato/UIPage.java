package com.xracoon.rato;

import com.robotium.solo.SoloEx;
import com.robotium.solo.Timeout;

import android.os.SystemClock;
import junit.framework.Assert;

public abstract class UIPage extends Assert {
	protected SoloEx solo;
	
	public UIPage(SoloEx solo, boolean wait)
	{
		this.solo=solo;
		solo.sleep(1000);
		if(wait)
		{
			assertTrue(this.getClass().getSimpleName()+"页面未载入",waitforPage());
		}
	}
	
	public UIPage(SoloEx solo)
	{
		this(solo,true);
	}
	
	public abstract boolean verifyPage();
	public boolean waitforPage()
	{
		final long startTime= SystemClock.uptimeMillis();
		final long endTime = startTime + Timeout.getLargeTimeout();
		while (SystemClock.uptimeMillis() < endTime) {
			solo.sleep(500);
			if(verifyPage())
			{
				//LogEx.i("["+(SystemClock.uptimeMillis()-startTime)/1000.0+"] "+this.getClass().getSimpleName()+"已载入");
				return true;
			}
		}
		//LogEx.i("["+(SystemClock.uptimeMillis()-startTime)/1000.0+"][ERROR] "+this.getClass().getSimpleName()+"未载入");
		return false;
	}
	public final void assertPage()
	{
		solo.sleep(1000);
		assertTrue("非"+this.getClass().getSimpleName()+"页面",verifyPage()); 
	}
}
