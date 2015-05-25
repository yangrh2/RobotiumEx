package com.robotium.solo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.robotium.solo.Solo;
import com.xracoon.rato.LayoutUtils;
import com.xracoon.rato.LogEx;
import com.xracoon.rato.Timer;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class SoloEx extends Solo {
	
	public int widthPix;
	public int heightPix;
	
	public SoloEx(Instrumentation instrumentation, Activity activity) {
		super(instrumentation, activity);
	}
	
	public SoloEx(Instrumentation instrumentation, Config config) {
		super(instrumentation,config);	
	}
	
	public SoloEx(Instrumentation instrumentation, Config config, Activity activity) {
		super(instrumentation, config, activity);	
	}
	
	public void waitLimit(int msTime)
	{
		int spend=Timer.spend();
		if(msTime>spend)
		{
			LogEx.i("limited time:  wait...");
			sleep(msTime-spend);
		}
	}
	public void startLimit()
	{
		Timer.start();
	}
	
	public void nap()
	{
		this.sleep(2500);
	}
	
	/**
	 * 获取资源Id对应的id
	 * @param resId 资源id
	 * @return 
	 */
	public int getId(String resId)
	{
		Activity act=getCurrentActivity();
		return act.getResources().getIdentifier(resId, "id", act.getPackageName());
	}
	
	public boolean searchId(String id)
	{
		return getViewT(id)!=null;
	}
	
	/**
	 * in 1s, no scroll, only visiable
	 * @param text
	 * @return
	 */
	public boolean hasText(String text)
	{
		int TIMEOUT=1000;
		final long endTime = SystemClock.uptimeMillis() + TIMEOUT;
		TextView foundAnyMatchingView = null;

		while (SystemClock.uptimeMillis() < endTime) {
			sleeper.sleep();
			foundAnyMatchingView = searcher.searchFor(TextView.class, text, 0, 0, false, true);
			if (foundAnyMatchingView !=null){
				return true;
			}
		}
		return false;
	}
	

	
	/**
	 * 在Top Frame中查找View 
	 * @return
	 */
	public View getViewT(String id)
	{
		View frame=this.getTopFrame();
		View target=this.getViewById(id,frame);
		return target;
	}
	

	public View getViewById(String id, View parent)
	{
		boolean scroll=true;
		return getView(Method.ID,id,parent,scroll);
	}
	
	public View getViewByText(String text, View parent)
	{	
		boolean scroll=true;
		return getView(Method.REGEX_TEXT,text,parent,scroll);
	}
	
	public View getTopFrame()
	{
		View[] frames=getViews(Method.CLASS,"NoSaveStateFrameLayout",null,false);
		if(frames!=null&&frames.length>0)
			return frames[frames.length-1];
		return null;
	}
	
	
	public View getUpperViewIn(View view, String listId)
	{
		ViewGroup p=(ViewGroup) view.getParent();
		ViewGroup t=null;
		while(p!=null)
		{
			LogEx.i(p.toString());
			t=(ViewGroup) p.getParent();
			if(t!=null && t.getId()==this.getId(listId))
				return p;
			p=t;
		}
		
		return null;
	}
	
	
	public View[] getViews(Method method, String value,View parent, boolean scroll)
	{
		return getViews(method,value,parent,scroll,Timeout.getSmallTimeout(),null);
	}
	public View[] getViews(Method method, String value,View parent, boolean scroll,long timeout, View scroller)
	{
			if(timeout<=0)
				timeout=Timeout.getSmallTimeout();
			long endTime = SystemClock.uptimeMillis() + timeout;
			
			Set<View> uniqueViewsMatchingId = new LinkedHashSet<View>();
			Pattern targetTextPattern=null;
			int targetId =0;  
			if(Method.REGEX_TEXT==method)
				targetTextPattern=Pattern.compile(value);
			else if(Method.ID==method)
			{
				Context targetContext = instrumentation.getTargetContext(); 
				String packageName = targetContext.getPackageName(); 
				targetId=targetContext.getResources().getIdentifier(value, "id", packageName);
			}
			while (SystemClock.uptimeMillis() <= endTime) 
			{
				sleeper.sleep();
				List<View> list=null;
				if(parent==null)
					list=viewFetcher.getAllViews(true);
				else
					list= viewFetcher.getViews(parent, true);
				
				for (View view : list) {
					if(!view.isShown() || view.getWidth()==0 || view.getHeight()==0)
						continue;
					
					if (method==Method.REGEX_TEXT &&  (view instanceof TextView) && targetTextPattern.matcher(((TextView)view).getText()).find())
						uniqueViewsMatchingId.add(view);
					else if(method==Method.PLAIN_TEXT && (view instanceof TextView) && ((TextView)view).getText().toString().contains(value))
						uniqueViewsMatchingId.add(view);
					else if(method==Method.CLASS && view.getClass().getSimpleName().matches(value))
						uniqueViewsMatchingId.add(view);
					else if(method==Method.ID && view.getId()==targetId)
						uniqueViewsMatchingId.add(view);
				}
				
				if(scroll && scrollEx(Scroller.DOWN,false,scroller))
					continue;
				
				break;
			}
			
			return uniqueViewsMatchingId.toArray(new View[0]);
	}
	
	public View getView(Method method, String value,View parent, boolean scroll)
	{
		return getView(method,value,parent,scroll,Timeout.getSmallTimeout(),null);
	}
	
	public View getView(Method method, String value, View parent, View scroller)
	{
		return getView(method,value,parent,true,Timeout.getSmallTimeout(),scroller);
	}
	
	public View getView(Method method, String value,View parent, boolean scroll, long timeout, View scroller)
	{
			if(timeout<=0)
				timeout=Timeout.getSmallTimeout();
			long endTime = SystemClock.uptimeMillis() + timeout;
			
			Pattern targetTextPattern=null;
			int targetId =0;  
			if(Method.REGEX_TEXT==method)
				targetTextPattern=Pattern.compile(value);
			else if(Method.ID==method)
			{
				Context targetContext = instrumentation.getTargetContext(); 
				String packageName = targetContext.getPackageName(); 
				targetId=targetContext.getResources().getIdentifier(value, "id", packageName);
			}
				
			while (SystemClock.uptimeMillis() <= endTime) {
				sleeper.sleep();
				List<View> list=null;
				if(parent==null)
					list=viewFetcher.getAllViews(true);
				else
					list= viewFetcher.getViews(parent, true);
					
				for (View view : list) {
					if(!view.isShown() || view.getWidth()==0 || view.getHeight()==0)
						continue;
			
					if (method==Method.REGEX_TEXT &&  (view instanceof TextView) && targetTextPattern.matcher(((TextView)view).getText()).find())
						return view;
					else if(method==Method.PLAIN_TEXT && (view instanceof TextView) && ((TextView)view).getText().toString().contains(value))
						return view;
					else if(method==Method.CLASS && view.getClass().getSimpleName().matches(value))
						return view;
					else if(method==Method.ID && view.getId()==targetId)
						return view;
				}
				if(scroll && scrollEx(Scroller.DOWN,false,scroller))
					continue;
				
				break;
			}
			return null;
	}
	
	public View getView(String id, int timeout)
	{
		View viewToReturn = getter.getView(getId(id), 0,timeout);
		return viewToReturn;
	}
	
	public boolean scrollEx(int direction, boolean allTheWay, View scroll) 
	{
		View parent=this.getTopFrame();
		final ArrayList<View> viewList = RobotiumUtils.removeInvisibleViews(viewFetcher.getViews(parent, true));
		@SuppressWarnings("unchecked")
		ArrayList<View> views = RobotiumUtils.filterViewsToSet(new Class[] { ListView.class,
				ScrollView.class, GridView.class, WebView.class}, viewList);
		
		View view = viewFetcher.getFreshestView(views);
		if(scroll!=null)
			view=scroll;
		//LogEx.i("scroller: "+view);
		if (view == null)
		{
			return false;
		}

		if (view instanceof AbsListView) {
			return scroller.scrollList((AbsListView)view, direction, allTheWay);
		}

		if (view instanceof ScrollView) {
			if (allTheWay) {
				scroller.scrollViewAllTheWay((ScrollView) view, direction);
				return false;
			} else {
				return scroller.scrollView((ScrollView)view, direction);
			}
		}
		if(view instanceof WebView){
			return scroller.scrollWebView((WebView)view, direction, allTheWay);
		}
		return false;
	}
	
	public View getViewIdx(int index, String id)
	{
		View view = null;
		int i=0;
		
		while(view==null && (i++)<10)
		{
			//LogEx.i("try get by id "+id+": "+i);
			
			sleep(2000);
			view = getter.getView(getId(id), index);
		}
		
		if(view != null && view.isShown() )
			return view;
		
		return null;
	}
	
	public String getText(View view)
	{
		if(view==null)
			return null;
		if(view instanceof TextView)
			return ((TextView)view).getText().toString();
		return null;
	}

	
	public void clickOnId(String id)
	{
		clickOnView(getView(id));
	}
	

	public void clickLongOnId(String id,int time)
	{
		clickLongOnView(getView(id),time);
	}
	
	public void clickOnScreenPercent(double w,double h)
	{
		DisplayMetrics dm = new DisplayMetrics();
		getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm); 
		double width=Math.abs(w)<1?w*dm.widthPixels:w;
		double height=Math.abs(h)<1?h*dm.heightPixels:h;
		
		if(width<0)
			width=dm.widthPixels+width;
		if(height<0)
			height=dm.heightPixels+height;
		
		clickOnScreen((float)width, (float)height);
	}
	
	public int[] getScreen()
	{
		int [] size=new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm); 
		size[0]=dm.widthPixels;
		size[1]=dm.heightPixels;
		return size;
	}
	
	public int[] getPos(View view)
	{
		return LayoutUtils.getCenter(view);
	}
	
	public boolean nearPos(int x1,int y1,int x2, int y2,int diffx,int diffy)
	{
		int dx=Math.abs(x2-x1);
		int dy=Math.abs(y2-y1);
		LogEx.i(x2+"-"+x1+",   "+y2+"-"+y1);
		LogEx.i("dx: "+dx+",  diffx: "+diffx+",    dy:"+dy+",  diffy:"+diffy);
		return dx<diffx && dy<diffy;
	}
		
	public View[] getViews(String id)
	{
		return getViews(id, true, null);
	}
	
	public View getLastView(String id)
	{
		View[] views=getViews(id);
		if(views!=null && views.length>0)
			return views[views.length-1];
		return null;
	}
	
	public void waitForSkip(String unexpectedId, int loop, int delay)
	{
		this.sleep(1000);
		int i=0;
		while(searchId(unexpectedId)&&(i++)<loop)
			sleep(delay);
	}
	
	public View[] getViews(String id, boolean scroll, View parent)
	{
		Context targetContext = instrumentation.getTargetContext(); 
		String packageName = targetContext.getPackageName(); 
		int viewId = targetContext.getResources().getIdentifier(id, "id", packageName);
		//Log.i("AAAAAA","id:"+viewId);
		
		Set<View> uniqueViewsMatchingId = new LinkedHashSet<View>();
		long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();
		
		while (SystemClock.uptimeMillis() <= endTime) {
			sleeper.sleep();
			List<View> list=null;
			if(parent==null)
				list=viewFetcher.getAllViews(true);
			else
				list= viewFetcher.getViews(parent, true);
			
			//LogEx.i("list in getViews: "+Arrays.deepToString(list.toArray()));
			
			for (View view : list) {
				Integer idOfView = Integer.valueOf(view.getId());
				if (idOfView.equals(viewId)  && view.isShown() &&  view.getWidth()!=0 && view.getHeight()!=0) {
					uniqueViewsMatchingId.add(view);
				}
			}
			if(scroll && scroller.scrollDown())
				continue;
			break;
		}
		
		return uniqueViewsMatchingId.toArray(new View[0]);
	}
	
	public View[] getViews(View parent, String text, boolean scroll)
	{		
		text=".*"+text+".*";
		
		Set<View> uniqueViewsMatchingId = new LinkedHashSet<View>();
		long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();
		
		while (SystemClock.uptimeMillis() <= endTime) {
			sleeper.sleep();
			List<View> list=null;
			if(parent==null)
				list=viewFetcher.getAllViews(true);
			else
				list= viewFetcher.getViews(parent, true);
			
			for (View view : list) {
				if ((view instanceof TextView) && view.isShown() && ((TextView)view).getText().toString().matches(text) && view.getWidth()!=0 && view.getHeight()!=0) {
					uniqueViewsMatchingId.add(view);
				}
			}
			if(scroll && scroller.scrollDown())
				continue;
			
			break;
		}
		
		return uniqueViewsMatchingId.toArray(new View[0]);
	}
	
//	public void dumpAllView()
//	{
//		LogEx.i("===== dump view =====");
//		for(View v: getViews())
//		{
//			LogEx.i(v.getId()+":"+v.getClass().getSimpleName()+",["+(v.isShown()?"[Show]":"")+(v.isClickable()?"[Click]":"")+(v.isEnabled()?"[Enable]":"")+"]");
//		}
//	}
//	
//	public void dumpAllView(View parent)
//	{
//		LogEx.i("===== dump target view =====");
//		for(View v: getViews(parent))
//		{
//			LogEx.i(v.getId()+":"+v.getClass().getSimpleName()+","+(v.isShown()?"[Show]":"")+(v.isClickable()?"[Click]":"")+(v.isEnabled()?"[Enable]":""));
//		}
//	}
	
	/**
	 * 根据匹配参考元素获取同序号的目标元素
	 * @param idTarget
	 * @param idRef
	 * @param refText
	 * @return
	 */
	public View getRelateView(String idTarget, String idRef, String refText,View parent)
	{
		//先滚动定位到refText位置
		if(!this.searchText(refText,true))
			return null;

		View[] refs=getViews(idRef,false,parent);
		View[] targets=getViews(idTarget,false,parent);
		
		LogEx.i(refs.length+" refs");
		LogEx.i(targets.length+" targets");
		
		int index=-1;
		int i=0;
		for(View v:refs)
		{
			if(((TextView)v).getText().toString().matches(refText))
			{
				index=i;
				break;
			}
			i++;
		}
		
		if(index==-1)
			return null;
			
		View view=targets[index];
		return view;
	}
	
	public View getRelateView(String idTarget,String targetText, String idRef, String refText,View parent)
	{
		//先滚动定位到refText位置
		if(!this.searchText(refText,true))
			return null;
		
		View[] refs=getViews(idRef,false,parent);
//		for(View v: refs)
//			LogEx.i("ref:"+((TextView)v).getText().toString());
		
		View[] targets=getViews(idTarget,false,parent);
//		for(View v: targets)
//			LogEx.i("target:"+((TextView)v).getText().toString());
		
		int index=-1;
		int i=0;
		for(View v:refs)
		{
			if(((TextView)v).getText().toString().matches(refText) && ((TextView)targets[i]).getText().toString().matches(targetText))
			{
				index=i;
				break;
			}
			i++;
		}
		
		if(index==-1)
			return null;
			
		View view=targets[index];
		return view;
	}
	
	public void execute(Runnable run)
	{
		this.instrumentation.runOnMainSync(run);
		
//		new Runnable()
//		{
//			public void run()
//			{
//				editText.setInputType(InputType.TYPE_NULL); 
//				editText.performClick();
//				dialogUtils.hideSoftKeyboard(editText, false, false);
//				if(text.equals(""))
//					editText.setText(text);
//				else{
//					editText.setText(previousText + text);
//					editText.setCursorVisible(false);
//				}
//			}
//		}
	}
	
	
	/*----------------------------------------------------
	*  Dump View to XML Tree
	*---------------------------------------------------*/
	public void clickOnElem(Element elem)
	{
		int x=Integer.parseInt(elem.attribute("x").getValue());
		int y=Integer.parseInt(elem.attribute("y").getValue());
		int w=Integer.parseInt(elem.attribute("w").getValue());
		int h=Integer.parseInt(elem.attribute("h").getValue());
		
		this.clickOnScreen(x+w/2, y+h/2);
	}
	
	public void clickLongOnElem(Element elem)
	{
		int x=Integer.parseInt(elem.attribute("x").getValue());
		int y=Integer.parseInt(elem.attribute("y").getValue());
		int w=Integer.parseInt(elem.attribute("w").getValue());
		int h=Integer.parseInt(elem.attribute("h").getValue());
		
		this.clickLongOnScreen(x+w/2, y+h/2);
	}
	
	/**
	 * dump view to XML
	 * @throws IOException 
	 */
	public Document dumpView(View parent)
	{
		//SAXReader reader = new SAXReader();
		Document doc = DocumentHelper.createDocument();
		Element root=doc.addElement("root");
		dumpSubView(parent,root);
		
//		LogEx.i(doc.asXML());
//		OutputFormat format = OutputFormat.createPrettyPrint();
//        format.setEncoding("GBK");
//		XMLWriter writer = new XMLWriter(new FileWriter("/data/local/tmp/ViewTree.xml"), format);
//		writer.write(doc);
//		writer.close();
	
		return doc;
	}
	
	public Document dumpAllViews()
	{
		Document doc = DocumentHelper.createDocument();
		Element root=doc.addElement("root");
		
		final View[] decorViews = this.viewFetcher.getWindowDecorViews();
		for(View view: decorViews)
		{
			dumpSubView(view,root);
		}
		
		return doc;
	}
	
	public boolean invokeJs(String js)
	{
		return webUtils.invokeJs(js);
	}
	
	public String getHtml()
	{
		return webUtils.getHtml();
	}
	
	private void dumpSubView(View view,Element pElem)
	{
		if(view==null)
		{
			final View[] views = this.viewFetcher.getWindowDecorViews();
			view=viewFetcher.getRecentDecorView(views);
		}
		
		if(!view.isShown())
			return;
		
		Element elem=pElem.addElement(view.getClass().getSimpleName());
		int[] local=new int[2];
		view.getLocationOnScreen(local);
		elem.addAttribute("x",local[0]+"");
		elem.addAttribute("y", local[1]+"");
		elem.addAttribute("w", view.getWidth()+"");
		elem.addAttribute("h", view.getHeight()+"");
		
		int id=view.getId();
		if(id!=-1)
			elem.addAttribute("id", id+"");
		
		Matcher match=Pattern.compile("(?<=:id/).*?(?=\\})").matcher(view.toString());
		if(match.find())
			elem.addAttribute("resId", match.group());
		
		if(view instanceof TextView)
			elem.addAttribute("text", ((TextView)view).getText().toString());
		
		if(view instanceof ViewGroup)
		{
			ViewGroup group=(ViewGroup)view;
			for(int i=0; i<group.getChildCount(); i++)
			{
				View v=group.getChildAt(i);
				dumpSubView(v,elem);
			}
		}
	}
	
	
	public void startApp(String packageName, String mainActivity) throws IOException, InterruptedException
	{
		Runtime.getRuntime().exec("am start -n "+packageName+"/"+mainActivity);
		Thread.sleep(3000);
	}
	
	public void stopApp(String packageName) throws IOException, InterruptedException
	{
		Process p=Runtime.getRuntime().exec("ps "+(packageName.length()>15?packageName.substring(packageName.length()-15):packageName));
		BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line=null;
		boolean skipTitle=false;
		while ((line = reader.readLine()) != null) {
			if(!skipTitle) {skipTitle=true; continue;}
			StringTokenizer st= new StringTokenizer(line);
			if(st.countTokens()>8){
				st.nextToken();
				Runtime.getRuntime().exec("kill "+st.nextToken());
			}
		} 
	}
	
}
