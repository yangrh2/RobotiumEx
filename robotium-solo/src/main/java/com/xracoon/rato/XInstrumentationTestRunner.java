package com.xracoon.rato;

import android.os.Bundle;
import android.os.Environment;
import android.test.InstrumentationTestRunner;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class XInstrumentationTestRunner extends InstrumentationTestRunner {
	private Writer mWriter;
	private XmlSerializer mTestSuiteSerializer;
	private long mTestStarted;
	private String tag="XInstrumentationTestRunner";
	
	/**
	 * @return
	 */
	private File getReportRoot()
	{
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
		   	return new File("/sdcard/xTest");
		else
		   return getTargetContext().getFilesDir();
	}
	
	public void onStart() {
		try {
			String xmlName = "Test" + DateUtil.format("yyyy-MM-dd-kk-mm") + ".xml";
			File file=new File(getReportRoot(),xmlName);

			if(file.exists())
				file.delete();

			else if(!file.getParentFile().exists())
				file.getParentFile().mkdirs(); 

			file.createNewFile();
			if(!file.exists())
				Log.e(tag,"can't create result report file!");
			else
				Log.i(tag,"result report: "+file.toString());
			
			startJUnitOutput(new FileWriter(file));
		} catch (Exception e) {
			Log.i(tag,e.getMessage());
			throw new RuntimeException(e);
		}
		super.onStart();
	}

	void startJUnitOutput(Writer writer) {
		try {
			this.mWriter = writer;
			this.mTestSuiteSerializer = newSerializer(this.mWriter);
			this.mTestSuiteSerializer.startDocument(null, null);
			this.mTestSuiteSerializer.startTag(null, "testsuites");
			this.mTestSuiteSerializer.startTag(null, "testsuite");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private XmlSerializer newSerializer(Writer writer) {
		try {
			XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
			XmlSerializer serializer = pf.newSerializer();
			serializer.setOutput(writer);
			return serializer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void sendStatus(int resultCode, Bundle results) {
		super.sendStatus(resultCode, results);
		switch (resultCode) {
		case -2:
		case -1:
		case 0:
			try {
				recordTestResult(resultCode, results);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		case 1:
			recordTestStart(results);
		}
	}
	
	void recordTestStart(Bundle results) {
		this.mTestStarted = System.currentTimeMillis();
	}

	void recordTestResult(int resultCode, Bundle results) throws IOException {
		float time = (float) (System.currentTimeMillis() - this.mTestStarted) / 1000.0F;
		String className = results.getString("class");
		String testMethod = results.getString("test");
		String stack = results.getString("stack");
		int current = results.getInt("current");
		int total = results.getInt("numtests");

		this.mTestSuiteSerializer.startTag(null, "testcase");
		this.mTestSuiteSerializer.attribute(null, "ID", current + "");
		this.mTestSuiteSerializer.attribute(null, "classname", className);
		this.mTestSuiteSerializer.attribute(null, "casename", testMethod);
		// Log.v("myInfor", current + "");
		if (resultCode != 0) {
			this.mTestSuiteSerializer
					.attribute(
							null,
							"time",
							String.format("%.3f",
									new Object[] { Float.valueOf(time) }));
			this.mTestSuiteSerializer.startTag(null, "result");
			if (stack != null) {
				String reason = stack.substring(0, stack.indexOf('\n'));
				String message = "";
				int index = reason.indexOf(':');
				if (index > -1) {
					message = reason.substring(index + 1);
					reason = reason.substring(0, index);
				}
				this.mTestSuiteSerializer.attribute(null, "message", message);
				// this.mTestSuiteSerializer.attribute(null, "type", reason);
				// this.mTestSuiteSerializer.text(stack);
				this.mTestSuiteSerializer.text("failure");
			}
			this.mTestSuiteSerializer.endTag(null, "result");
		} else {
			this.mTestSuiteSerializer
					.attribute(
							null,
							"time",
							String.format("%.3f",
									new Object[] { Float.valueOf(time) }));
			this.mTestSuiteSerializer.startTag(null, "result");
			this.mTestSuiteSerializer.attribute(null, "message", "pass");
			this.mTestSuiteSerializer.text("success");
			this.mTestSuiteSerializer.endTag(null, "result");
		}
		this.mTestSuiteSerializer.endTag(null, "testcase");
		if (current == total) {
			// this.mTestSuiteSerializer.startTag(null, "system-out");
			// this.mTestSuiteSerializer.endTag(null, "system-out");
			// this.mTestSuiteSerializer.startTag(null, "system-err");
			// this.mTestSuiteSerializer.endTag(null, "system-err");
			this.mTestSuiteSerializer.endTag(null, "testsuite");
			this.mTestSuiteSerializer.flush();
		}
	}

	public void finish(int resultCode, Bundle results) {
		endTestSuites();
		super.finish(resultCode, results);
	}

	void endTestSuites() {
		try {
			this.mTestSuiteSerializer.endTag(null, "testsuites");
			this.mTestSuiteSerializer.endDocument();
			this.mTestSuiteSerializer.flush();
			this.mWriter.flush();
			this.mWriter.close();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}