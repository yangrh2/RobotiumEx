package com.xracoon.testutil.pack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.xmlpull.v1.XmlPullParserException;

import com.xracoon.testutil.model.TestSuite;

public class TestSuiteDumper {
	public static void main(String[] args) throws IOException, XmlPullParserException
	{
		System.out.println("args: "+args.length);
		for(String str: args)
			System.out.println(str);
		
		String inPath=args[0];
		File file=new File(args[1]);
		String outPath=file.getParent();
		String fileName=file.getName();
		
		TestProjectParser parser=new TestProjectParser();
		TestSuite suite=parser.parsePath(inPath, "**/*TestCase.java", null);
		ReportCreator reporter=new ReportCreator();
		reporter.createReport(suite, outPath, fileName);
	}
}
