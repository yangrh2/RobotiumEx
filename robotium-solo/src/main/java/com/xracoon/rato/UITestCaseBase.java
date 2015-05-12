/**
 * UITestCaseBase可以作为各个项目中TestCase类的基类
 * 
 * public class UITestCase extends UITestCaseBase {
 *	public static Class<?> targetClass;
 *	static {
 *		try {
＊			targetClass = Class.forName("com.wanmei.demo.laohuSDK.MainActivity");
＊		} catch (ClassNotFoundException e) {
＊			throw new RuntimeException(e);
＊		}
＊	}
＊	
＊	public UITestCase() {
＊		super(targetClass);
＊		LogEx.setTag("TAG_UITEST_LAOHUSDK");
＊	}
}
 * 
*/

package com.xracoon.rato;

import com.robotium.solo.SoloEx;
import com.robotium.solo.Solo.Config;
import com.robotium.solo.Solo.Config.ScreenshotFileType;

import android.test.ActivityInstrumentationTestCase2;

@SuppressWarnings("rawtypes")
public class UITestCaseBase<T extends Class> extends ActivityInstrumentationTestCase2
{
	public SoloEx solo;
	
	@SuppressWarnings("unchecked")
	public UITestCaseBase(T v)
	{
		super(v);
		LogEx.setTag("AAAAAA");
	}
	
	@Override
	public void setUp() throws Exception
	{
		LogEx.i("setUp");
		super.setUp();
		Config config = new Config();
		config.screenshotFileType =ScreenshotFileType.PNG; //JPEG/PNG
		config.screenshotSavePath ="/data/local/tmp/Robotium-Screenshots";
		config.shouldScroll = true;	
		config.timeout_large = 10000;	
		config.timeout_small = 5000; 
		config.useJavaScriptToClickWebElements = true;	
		solo = new SoloEx(getInstrumentation(),config); 

		try 
		{	
			solo = new SoloEx(getInstrumentation(), getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void tearDown() throws Exception
	{
		LogEx.i("tearDown");
		try 
		{	
			solo.finishOpenedActivities();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
