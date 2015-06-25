package com.xracoon.testutil.model;

import java.util.List;


public class TestCase extends TestBase {
	public int id;
	
	public String packName;
	public String className;
	
	public TestStatus status=TestStatus.WAIT;
	public String info="";
	
	public String cause="";
	
	public String inTime;

	public String level="";
	public String[] points; 
	public String[] tags;
	public List<String> passTags;
	
	public TestCase()
	{
		count=1;
	}
	
	public void  copy(TestCase o)
	{
		id=o.id;
		packName=o.packName;
		className=o.className;
		status=o.status;
		info=o.info;
		inTime=o.inTime;
		
		name=o.name;
		start=o.start;
		end=o.end;
		
		count=o.count;
		pass=o.pass;
		fail=o.fail;
	}
	
	public TestCase(String pName, String cName, String mName)
	{
		packName=pName;
		className=cName;
		name=mName;
		
		count=1;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s/%s#%s", packName,className,name); 
	}
	
	public String getSummary()
	{
		String str=String.format("%s\t%s\t%s/%s#%s", status,inTime,packName,className,name);
		if(status.isFail())
			str+="\n"+info;
		return str; 
	}

	public String getFullName()
	{
		StringBuilder builder=new StringBuilder();
		builder.append(packName);
		builder.append("/");
		builder.append(className);
		builder.append("#");
		builder.append(name);
		return builder.toString();
	}
	@Override
	public void update() {
		count=1;
		fail=0;
		pass=0;
		
		if(status.isFail())
			fail=1;
		else if(status==TestStatus.PASS)
			pass=1;
	}
}
