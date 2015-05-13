package com.xracoon.rato;

import android.os.Bundle;
import android.test.AndroidTestRunner;
import android.test.InstrumentationTestRunner;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import org.xmlpull.v1.XmlSerializer;

public class PolideaInstrumentationTestRunner extends InstrumentationTestRunner
{
  private static final String TESTSUITES = "testsuites";
  private static final String TESTSUITE = "testsuite";
  private static final String ERRORS = "errors";
  private static final String FAILURES = "failures";
  private static final String ERROR = "error";
  private static final String FAILURE = "failure";
  private static final String NAME = "name";
  private static final String PACKAGE = "package";
  private static final String TESTS = "tests";
  private static final String TESTCASE = "testcase";
  private static final String CLASSNAME = "classname";
  private static final String TIME = "time";
  private static final String TIMESTAMP = "timestamp";
  private static final String PROPERTIES = "properties";
  private static final String SYSTEM_OUT = "system-out";
  private static final String SYSTEM_ERR = "system-err";
  private static final String SPLIT_LEVEL_NONE = "none";
  private static final String SPLIT_LEVEL_CLASS = "class";
  private static final String SPLIT_LEVEL_PACKAGE = "package";
  private static final String TAG = PolideaInstrumentationTestRunner.class.getSimpleName();
  private static final String DEFAULT_JUNIT_FILE_POSTFIX = "-TEST.xml";
  private static final String DEFAULT_NO_PACKAGE_PREFIX = "NO_PACKAGE";
  private static final String DEFAULT_SINGLE_FILE_NAME = "ALL-TEST.xml";
  private static final String DEFAULT_SPLIT_LEVEL = "package";
  private String junitOutputDirectory;
  private String junitOutputFilePostfix;
  private String junitNoPackagePrefix;
  private String junitSplitLevel;
  private String junitSingleFileName;
  private boolean junitOutputEnabled;
  private boolean justCount;
  private XmlSerializer currentXmlSerializer;
  private final LinkedHashMap<Package, TestCaseInfo> caseMap;
  private boolean outputEnabled;
  private AndroidTestRunner runner;
  private boolean logOnly;
  private PrintWriter currentFileWriter;

  public PolideaInstrumentationTestRunner()
  {
    this.junitOutputDirectory = null;
    this.junitOutputFilePostfix = null;

    this.caseMap = new LinkedHashMap();
  }

  private synchronized TestInfo getTestInfo(TestCase testCase)
  {
    Class clazz = testCase.getClass();
    Package thePackage = clazz.getPackage();
    String name = testCase.getName();
    StringBuilder sb = new StringBuilder();
    sb.append(thePackage).append(".").append(clazz.getSimpleName()).append(".").append(name);
    String mapKey = sb.toString();
    TestCaseInfo caseInfo = (TestCaseInfo)this.caseMap.get(thePackage);
    if (caseInfo == null) {
      caseInfo = new TestCaseInfo();
      caseInfo.testCaseClass = testCase.getClass();
      caseInfo.thePackage = thePackage;
      this.caseMap.put(thePackage, caseInfo);
    }
    TestInfo ti = (TestInfo)caseInfo.testMap.get(mapKey);
    if (ti == null) {
      ti = new TestInfo();
      ti.name = name;
      ti.testCase = testCase.getClass();
      ti.thePackage = thePackage;
      caseInfo.testMap.put(mapKey, ti);
    }
    return ti;
  }

  private void startFile(File outputFile) throws IOException {
    Log.d(TAG, "Writing to file " + outputFile);
    this.currentXmlSerializer = Xml.newSerializer();
    this.currentFileWriter = new PrintWriter(outputFile, "UTF-8");
    this.currentXmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
    this.currentXmlSerializer.setOutput(this.currentFileWriter);
    this.currentXmlSerializer.startDocument("UTF-8", null);
    this.currentXmlSerializer.startTag(null, "testsuites");
  }

  private void endFile() throws IOException {
    Log.d(TAG, "closing file");
    this.currentXmlSerializer.endTag(null, "testsuites");
    this.currentXmlSerializer.endDocument();
    this.currentFileWriter.flush();
    this.currentFileWriter.close();
  }

  private String getTimestamp() {
    long time = System.currentTimeMillis();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    return sdf.format(Long.valueOf(time));
  }

  private void writeClassToFile(TestCaseInfo tci) throws IllegalArgumentException, IllegalStateException, IOException
  {
    Package thePackage = tci.thePackage;
    Class clazz = tci.testCaseClass;
    int tests = tci.testMap.size();
    String timestamp = getTimestamp();
    int errors = 0;
    int failures = 0;
    int time = 0;
    for (TestInfo testInfo : tci.testMap.values()) {
      if (testInfo.error != null) {
        errors++;
      }
      if (testInfo.failure != null) {
        failures++;
      }
      time = (int)(time + testInfo.time);
    }
    this.currentXmlSerializer.startTag(null, "testsuite");
    this.currentXmlSerializer.attribute(null, "errors", Integer.toString(errors));
    this.currentXmlSerializer.attribute(null, "failures", Integer.toString(failures));
    this.currentXmlSerializer.attribute(null, "name", clazz.getName());
    this.currentXmlSerializer.attribute(null, "package", thePackage == null ? "" : thePackage.getName());
    this.currentXmlSerializer.attribute(null, "tests", Integer.toString(tests));
    this.currentXmlSerializer.attribute(null, "time", Double.toString(time / 1000.0D));
    this.currentXmlSerializer.attribute(null, "timestamp", timestamp);
    for (TestInfo testInfo : tci.testMap.values()) {
      writeTestInfo(testInfo);
    }
    this.currentXmlSerializer.startTag(null, "properties");
    this.currentXmlSerializer.endTag(null, "properties");
    this.currentXmlSerializer.startTag(null, "system-out");
    this.currentXmlSerializer.endTag(null, "system-out");
    this.currentXmlSerializer.startTag(null, "system-err");
    this.currentXmlSerializer.endTag(null, "system-err");
    this.currentXmlSerializer.endTag(null, "testsuite");
  }

  private void writeTestInfo(TestInfo testInfo) throws IllegalArgumentException, IllegalStateException, IOException
  {
    this.currentXmlSerializer.startTag(null, "testcase");
    this.currentXmlSerializer.attribute(null, "classname", testInfo.testCase.getName());
    this.currentXmlSerializer.attribute(null, "name", testInfo.name);
    this.currentXmlSerializer.attribute(null, "time", Double.toString(testInfo.time / 1000.0D));
    if (testInfo.error != null) {
      this.currentXmlSerializer.startTag(null, "error");
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      testInfo.error.printStackTrace(pw);
      this.currentXmlSerializer.text(sw.toString());
      this.currentXmlSerializer.endTag(null, "error");
    }
    if (testInfo.failure != null) {
      this.currentXmlSerializer.startTag(null, "failure");
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      testInfo.failure.printStackTrace(pw);
      this.currentXmlSerializer.text(sw.toString());
      this.currentXmlSerializer.endTag(null, "failure");
    }
    this.currentXmlSerializer.endTag(null, "testcase");
  }

  private File getJunitOutputFile(Package p) {
    return new File(this.junitOutputDirectory, (p == null ? this.junitNoPackagePrefix : p.getName()) + this.junitOutputFilePostfix);
  }

  private File getJunitOutputFile() {
    return new File(this.junitOutputDirectory, this.junitSingleFileName);
  }

  private File getJunitOutputFile(Class<? extends TestCase> clazz) {
    return new File(this.junitOutputDirectory, clazz.getName() + this.junitOutputFilePostfix);
  }

  private void setDefaultParameters() {
    if (this.junitOutputDirectory == null) {
      this.junitOutputDirectory = getTargetContext().getFilesDir().getAbsolutePath();
    }
    if (this.junitOutputFilePostfix == null) {
      this.junitOutputFilePostfix = "-TEST.xml";
    }
    if (this.junitNoPackagePrefix == null) {
      this.junitNoPackagePrefix = "NO_PACKAGE";
    }
    if (this.junitSplitLevel == null) {
      this.junitSplitLevel = "package";
    }
    if (this.junitSingleFileName == null)
      this.junitSingleFileName = "ALL-TEST.xml";
  }

  private boolean getBooleanArgument(Bundle arguments, String tag, boolean defaultValue)
  {
    String tagString = arguments.getString(tag);
    if (tagString == null) {
      return defaultValue;
    }
    return Boolean.parseBoolean(tagString);
  }

  public void onCreate(Bundle arguments)
  {
    Log.d(TAG, "Creating the Test Runner with arguments: " + arguments.keySet());
    if (arguments != null) {
      this.junitOutputEnabled = getBooleanArgument(arguments, "junitXmlOutput", true);
      this.junitOutputDirectory = arguments.getString("junitOutputDirectory");
      this.junitOutputFilePostfix = arguments.getString("junitOutputFilePostfix");
      this.junitNoPackagePrefix = arguments.getString("junitNoPackagePrefix");
      this.junitSplitLevel = arguments.getString("junitSplitLevel");
      this.junitSingleFileName = arguments.getString("junitSingleFileName");
      this.justCount = getBooleanArgument(arguments, "count", false);
      this.logOnly = getBooleanArgument(arguments, "log", false);
    }
    setDefaultParameters();
    logParameters();
    createDirectoryIfNotExist();
    deleteOldFiles(); 
    super.onCreate(arguments);
  }

  private void logParameters() {
    Log.d(TAG, "Test runner is running with the following parameters:");
    Log.d(TAG, "junitOutputDirectory: " + this.junitOutputDirectory);
    Log.d(TAG, "junitOutputFilePostfix: " + this.junitOutputFilePostfix);
    Log.d(TAG, "junitNoPackagePrefix: " + this.junitNoPackagePrefix);
    Log.d(TAG, "junitSplitLevel: " + this.junitSplitLevel);
    Log.d(TAG, "junitSingleFileName: " + this.junitSingleFileName);
  }

  private boolean createDirectoryIfNotExist() {
    boolean created = false;
    Log.d(TAG, "Creating output directory if it does not exist");
    File directory = new File(this.junitOutputDirectory);
    if (!directory.exists()) {
      created = directory.mkdirs();
    }
    Log.d(TAG, "Created directory? " + created);
    return created;
  }

  private void deleteOldFiles() {
    Log.d(TAG, "Deleting old files");
    File[] filesToDelete = new File(this.junitOutputDirectory).listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String filename) {
        return (filename.endsWith(PolideaInstrumentationTestRunner.this.junitOutputFilePostfix)) || (filename.equals(PolideaInstrumentationTestRunner.this.junitSingleFileName));
      }
    });
    if (filesToDelete != null) {
      Log.d(TAG, "Deleting: " + Arrays.toString(filesToDelete));
      for (File f : filesToDelete)
        f.delete();
    }
  }

  public void finish(int resultCode, Bundle results)
  {
    if (this.outputEnabled) {
      Log.d(TAG, "Post processing");
      if ("package".equals(this.junitSplitLevel)) {
        processPackageLevelSplit();
      } else if ("class".equals(this.junitSplitLevel)) {
        processClassLevelSplit();
      } else if ("none".equals(this.junitSplitLevel)) {
        processNoSplit();
      } else {
        Log.d(TAG, "Invalid split level " + this.junitSplitLevel + ", falling back to package level split.");
        processPackageLevelSplit();
      }
    }
    super.finish(resultCode, results);
  }

  private void processNoSplit() {
    try {
      File f = getJunitOutputFile();
      startFile(f);
      try {
        for (Package p : this.caseMap.keySet())
          try {
            TestCaseInfo tc = (TestCaseInfo)this.caseMap.get(p);
            writeClassToFile(tc);
          } catch (IOException e) {
            Log.e(TAG, "Error: " + e, e);
          }
      }
      finally {
        endFile();
      }
    } catch (IOException e) {
      Log.e(TAG, "Error: " + e, e);
    }
  }

  private void processPackageLevelSplit() {
    Log.d(TAG, "Packages: " + this.caseMap.size());
    for (Package p : this.caseMap.keySet()) {
      Log.d(TAG, "Processing package " + p);
      try {
        File f = getJunitOutputFile(p);
        startFile(f);
        try {
          TestCaseInfo tc = (TestCaseInfo)this.caseMap.get(p);
          writeClassToFile(tc);
        } finally {
          endFile();
        }
      } catch (IOException e) {
        Log.e(TAG, "Error: " + e, e);
      }
    }
  }

  private void processClassLevelSplit() {
    for (Package p : this.caseMap.keySet())
      try {
        TestCaseInfo tc = (TestCaseInfo)this.caseMap.get(p);
        File f = getJunitOutputFile(tc.testCaseClass);
        startFile(f);
        try {
          writeClassToFile(tc);
        } finally {
          endFile();
        }
      } catch (IOException e) {
        Log.e(TAG, "Error: " + e, e);
      }
  }

  protected AndroidTestRunner getAndroidTestRunner()
  {
    Log.d(TAG, "Getting android test runner");
    this.runner = super.getAndroidTestRunner(); 
    if ((this.junitOutputEnabled) && (!this.justCount) && (!this.logOnly)) {
      Log.d(TAG, "JUnit test output enabled");
      this.outputEnabled = true;
      this.runner.addTestListener(new JunitTestListener());
    } else {
      this.outputEnabled = false;
      Log.d(TAG, "JUnit test output disabled: [ junitOutputEnabled : " + this.junitOutputEnabled + ", justCount : " + this.justCount + ", logOnly : " + this.logOnly + " ]");
    }

    return this.runner;
  }

  private class JunitTestListener implements TestListener
  {
    private static final int MINIMUM_TIME = 100;
    private final ThreadLocal<Long> startTime = new ThreadLocal();

    private JunitTestListener() {
    }
    public void startTest(Test test) { Log.d(PolideaInstrumentationTestRunner.TAG, "Starting test: " + test);
      if ((test instanceof TestCase)) {
        Thread.currentThread().setContextClassLoader(test.getClass().getClassLoader());
        this.startTime.set(Long.valueOf(System.currentTimeMillis()));
      }
    }

    public void endTest(Test t)
    {
      if ((t instanceof TestCase)) {
        TestCase testCase = (TestCase)t;
        cleanup(testCase);

        long timeTaken = System.currentTimeMillis() - ((Long)this.startTime.get()).longValue();
        PolideaInstrumentationTestRunner.this.getTestInfo(testCase).time = timeTaken;
        if (timeTaken < 100L)
          try {
            Thread.sleep(100L - timeTaken);
          }
          catch (InterruptedException ignored)
          {
          }
      }
      Log.d(PolideaInstrumentationTestRunner.TAG, "Finished test: " + t);
    }

    public void addError(Test test, Throwable t)
    {
      if ((test instanceof TestCase))
        PolideaInstrumentationTestRunner.this.getTestInfo((TestCase)test).error = t;
    }

    public void addFailure(Test test, AssertionFailedError f)
    {
      if ((test instanceof TestCase))
        PolideaInstrumentationTestRunner.this.getTestInfo((TestCase)test).failure = f;
    }

    private void cleanup(TestCase test)
    {
      Class clazz = test.getClass();

      while (clazz != TestCase.class) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
          Field f = field;
          if ((!f.getType().isPrimitive()) && (!Modifier.isStatic(f.getModifiers()))) {
            try {
              f.setAccessible(true);
              f.set(test, null);
            }
            catch (Exception ignored)
            {
            }
          }
        }
        clazz = clazz.getSuperclass();
      }
    }
  }

  public static class TestPackageInfo
  {
    public Package thePackage;
    public Map<Class<? extends TestCase>, PolideaInstrumentationTestRunner.TestCaseInfo> testCaseList = new LinkedHashMap();
  }

  public static class TestCaseInfo
  {
    public Package thePackage;
    public Class<? extends TestCase> testCaseClass;
    public Map<String, PolideaInstrumentationTestRunner.TestInfo> testMap = new LinkedHashMap();
  }

  public static class TestInfo
  {
    public Package thePackage;
    public Class<? extends TestCase> testCase;
    public String name;
    public Throwable error;
    public AssertionFailedError failure;
    public long time;

    public String toString()
    {
      return this.name + "[" + this.testCase.getClass() + "] <" + this.thePackage + ">. Time: " + this.time + " ms. E<" + this.error + ">, F <" + this.failure + ">";
    }
  }
}