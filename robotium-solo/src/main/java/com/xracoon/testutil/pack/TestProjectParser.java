package com.xracoon.testutil.pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.xracoon.testutil.model.TestClass;
import com.xracoon.testutil.model.TestCase;
import com.xracoon.testutil.model.TestPack;
import com.xracoon.testutil.model.TestStatus;
import com.xracoon.testutil.model.TestSuite;

public class TestProjectParser {
	
	private final Pattern packNamePattern=Pattern.compile("^\\s*package\\s+(.+?);");
	private final Pattern suiteNamePattern=Pattern.compile("class\\s+(.*)\\s+extends\\s+.*?\\s*\\{");
	private final Pattern caseNamePattern=Pattern.compile("public\\s+void\\s+(test.*?)\\s*\\(");
	private final String comtPattern1="[\t ]*//.*";
	private final String comtPattern2="/\\*[\\s\\S]*?\\*/";
	
	public FileSet createFileSet(File baseDir, String includes, String excludes)
	{
		FileSet fs=new FileSet();
		fs.setDir(baseDir);
		fs.setProject(new Project());
		
		StringTokenizer tokens=null;
		if(includes!=null && includes.trim().length()>0)
		{
			tokens=new StringTokenizer(includes,",");
			while(tokens.hasMoreTokens())
			{
				String token=tokens.nextToken().trim();
				fs.createInclude().setName(token);
			}
		}
		if(excludes!=null && excludes.trim().length()>0)
		{
			tokens=new StringTokenizer(excludes);
			while(tokens.hasMoreTokens())
			{
				String token=tokens.nextToken().trim();
				fs.createExclude().setName(token);
			}
		}
		
		return fs;
	}
	
	public String[] searchTestCase(String path, String incs, String excs)
	{		
		FileSet fs=createFileSet(new File(path), incs, excs);
		DirectoryScanner ds=fs.getDirectoryScanner(new Project());
		String[] files=ds.getIncludedFiles();
		for(int i=0; i<files.length; i++)
			files[i]=path+File.separator+files[i];
			
		return files;
	}
	
	public boolean isTestCase(String file)
	{
		return file.toLowerCase().endsWith("testcase.java");
	}
	
	public TestClass parseFile(String file) throws IOException
	{
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		StringBuilder strBuilder=new StringBuilder();
		String line;
		while((line=reader.readLine())!=null)
			strBuilder.append(line+"\n");
		reader.close();
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(strBuilder.toString().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit javaFile = (CompilationUnit) (parser.createAST(null));
		String packName= javaFile.getPackage().getName().getFullyQualifiedName();
		
		for(Object itype: javaFile.types())
		{
			TypeDeclaration type=(TypeDeclaration)itype;
			String className=type.getName().getIdentifier();
			
			boolean isPublicClass=false;
			for(Object imodifer: type.modifiers())
			{
				if(imodifer instanceof Modifier)
				{
					Modifier m=(Modifier)imodifer;
					if(m.isPublic())
					{
						isPublicClass=true;
						break;
					}
				}
			}
			if(!isPublicClass)
				continue;
			
			TestClass tclass=new TestClass(packName,className);
			
			for(Object imethod : type.getMethods())
			{
				MethodDeclaration method=(MethodDeclaration)imethod;
				
				boolean isPublicMethod=false;
				boolean isStaticMethod=false;
				boolean ignoreAnnotation=false;
				boolean isTestAnnotation=false;
				boolean isTestMethodName=false;
				
				String level="";
				String[] points=null;
				List<String> passTags=new ArrayList<String>();
				
				
				String methodName=method.getName().getIdentifier();
				if(methodName.startsWith("test"))
					isTestMethodName=true;
				
				for(int i=0; i<method.modifiers().size(); i++)
				{
					Object imodifer=method.modifiers().get(i);
					if((imodifer instanceof Modifier) && ((Modifier)imodifer).isPublic())
						isPublicMethod=true;
					if((imodifer instanceof Modifier) && ((Modifier)imodifer).isStatic())
						isStaticMethod=true;
					
					//SuppressWarnings
					
					else if(imodifer instanceof MarkerAnnotation)
					{
						String annotName= ((MarkerAnnotation)imodifer).getTypeName().getFullyQualifiedName();
						if(annotName.equals("Ignore"))
							ignoreAnnotation=true;
						else if(annotName.equals("Test"))
							isTestAnnotation=true;
					}
									
					else if((imodifer instanceof NormalAnnotation) && ((NormalAnnotation)imodifer).getTypeName().getFullyQualifiedName().equals("TestFilter"))
					{
						NormalAnnotation testFilter=(NormalAnnotation)imodifer;
						for(Object v: testFilter.values())
						{
							MemberValuePair pair=(MemberValuePair)v;
							Object value=pair.getValue();
							if(pair.getName().getIdentifier().equals("level"))
								level=((StringLiteral)value).getLiteralValue();
							else if(pair.getName().getIdentifier().equals("point"))
							{
								if(value.getClass().equals(ArrayInitializer.class))
								{
									List exprs=((ArrayInitializer)value).expressions();
									if(exprs!=null && exprs.size()>0)
									{
										points=new String[exprs.size()];
										for(int ie=0; ie<exprs.size(); ie++)
											points[ie]=((StringLiteral)exprs.get(ie)).getLiteralValue();
									}
								}
								else 
								{
									points=new String[1];
									points[0]=((StringLiteral)value).getLiteralValue();
								}
							}
						}
					}
				}
				if(!isPublicMethod || isStaticMethod)
					continue;
				
				//����pass Tag
				for(Object obj : method.getBody().statements())
				{
					if((obj instanceof ExpressionStatement))
					{
						ExpressionStatement es=(ExpressionStatement)obj;
						Object exp= es.getExpression();
						if(exp instanceof MethodInvocation)
						{
							MethodInvocation invocExp=(MethodInvocation)exp;
							String name=invocExp.getName().getFullyQualifiedName();
							Object optionExp=invocExp.getExpression();
							if(name.equals("pass") && optionExp!=null && optionExp.toString().equals("LogEx"))
							{
								String param=((StringLiteral)invocExp.arguments().get(0)).getLiteralValue();
								passTags.add(param);
							}
						}
					}
				}
				
				if(isTestMethodName||isTestAnnotation)
				{
					TestCase test=new TestCase(packName,className,methodName);
					test.level=level;
					if(ignoreAnnotation)
						test.status=TestStatus.IGNORE;
					test.points=points;
					test.tags=passTags.toArray(new String[0]);
					tclass.put(test);
				}
			}
			tclass.count=tclass.testCases.size();
			return tclass;
		}
		return null;
	}
	
	public TestClass parseFile1(String file) throws IOException
	{
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		StringBuilder strBuilder=new StringBuilder();
		String line;
		while((line=reader.readLine())!=null)
			strBuilder.append(line+"\n");
		reader.close();
		
		//trim comment
		String fileContent=trimComment(strBuilder.toString());
		
		//package
		String packName="default";
		Matcher p=packNamePattern.matcher(fileContent);
		if(p.find())
			packName=p.group(1);
		
		//class
		String className;
		p=suiteNamePattern.matcher(fileContent);
		if(p.find())
			className=p.group(1);
		else return null;
		
		//method
		TestClass testClass=new TestClass(packName,className);
		p=caseNamePattern.matcher(fileContent);
		while(p.find())
		{
			TestCase tcase=new TestCase(packName,className,p.group(1));
//			if(p.group().contains("@Ignore"))
//				tcase.status=TestStatus.IGNORE;
			
			
			//parse annotation
			StringBuilder before=new StringBuilder();
			char c;
			for(int s=p.start()-1; s>-1; s--)
			{
				c=fileContent.charAt(s);
				
				//skip last black character
				while((c==' '||c=='\t'||c=='\n'||c=='\r') && s>-1)
					c=fileContent.charAt(--s);
				
				//no annotation
				if(c==';'||c=='}'||c=='{'||s<0)
					break;
				
				//read annotation
				while(c!='@' && s>-1)
				{
					before.append(c);
					s--;
					c=fileContent.charAt(s);
				}
				if(c=='@')
					before.append(fileContent.charAt(s--));
			}
			before.reverse();
			
			parseAnnotation(before.toString(),tcase);
			
			testClass.put(tcase);
		}
		
		
		testClass.count=testClass.testCases.size();
		return testClass;
	}
	
	public void parseAnnotation(String anotString, TestCase tcase)
	{
		String[] antos=anotString.split("@");
		Pattern levelPattern=Pattern.compile("level\\s*=\\s*\"(.+?)\"");
		//Pattern pointPattern=Pattern.compile("point\\s*=\\s*\\{\\s*(.+?)\\s*\\}");
		
		for(String a: antos)
		{
			if(a.trim().length()==0)
				continue;
			
			if(a.equals("Ignore"))
				tcase.status=TestStatus.IGNORE;
			
			else if(a.startsWith("TestFilter"))
			{
				Matcher m=levelPattern.matcher(a);
				if(m.find())
					tcase.level=m.group(1);
			}
		}
	}
	
	public String trimComment(String code)
	{
		return code.replaceAll(comtPattern1, "").replaceAll(comtPattern2, "");
	}
	
	/**
	 * 解析AndroidManifest.xml文件
	 * @param path
	 * @param args
	 * @throws IOException
	 * @throws DocumentException
	 */
	public ManifestInfo parseManifest(String path)
	{	
		try
		{
			String fileContent=FileUtils.readFileToString(new File(path+File.separator+"AndroidManifest.xml"), "UTF-8");
			Document doc = DocumentHelper.parseText(fileContent);
			
			ManifestInfo manifest=new ManifestInfo();
			String testPackage=doc.selectSingleNode("/manifest/@package").getStringValue();
			manifest.testPackageId=testPackage;
			
			String targetPackage=doc.selectSingleNode("/manifest/instrumentation/@android:targetPackage").getStringValue();
			manifest.targetPackageId=targetPackage;
			
			String testRunner=doc.selectSingleNode("/manifest/instrumentation/@android:name").getStringValue();
			manifest.testRunner=testRunner;
			
			return manifest;
		}catch(Exception e)
		{
			return null;
		}
	}
	
	public TestSuite parsePath(String path, String incs, String excs) throws IOException
	{
		System.out.println("===Analyse test project===");
		System.out.println("path: \t"+path);
		System.out.println("include: \t"+incs);
		System.out.println("exclude: \t"+excs);
		
		String[] files=searchTestCase(path,incs, excs);
		System.out.println("find files: "+files.length);
		Map<String,TestPack> testPacks=new LinkedHashMap<String,TestPack>();
		for(String file: files)
		{
			TestClass clss=parseFile(file);
			if(clss==null)
				continue;
			if(!testPacks.containsKey(clss.packName))
				testPacks.put(clss.packName, new TestPack(clss.packName));
			testPacks.get(clss.packName).testClasses.put(clss.name,clss);
			testPacks.get(clss.packName).count+=clss.count;
		}
		
		Map<String,TestPack> packs=new LinkedHashMap<String, TestPack>();
		int tsCount=0;
		for(TestPack tp: testPacks.values())
		{
			tsCount+=tp.count;
			packs.put(tp.name, tp);
		}
		
		TestSuite testSuite=new TestSuite("testSuite");
		testSuite.testPacks=packs;
		testSuite.count=tsCount;
		ManifestInfo info=parseManifest(path+File.separator+"../");
		if(info!=null)
		{
			testSuite.testRunner=info.testRunner;
			testSuite.targetApp=info.targetPackageId;
			testSuite.testApp=info.testPackageId;
		}
		
		System.out.println("RESULT:  \t"+testSuite.count+ " case"+(testSuite.count>1?"s":"")+" found.\n");
		return testSuite;
	}
	
	public void dump(TestSuite test)
	{
		System.out.println(test.name+"\t("+test.count+")");
		for(TestPack tp:test.testPacks.values())
		{
			System.out.println("\t"+tp.name+"\t("+tp.count+")");
			for(TestClass tc:tp.testClasses.values())
			{
				System.out.println("\t\t"+tc.name+"\t("+tc.count+")");
				for(TestCase tm:tc.testCases.values())
					System.out.println("\t\t\t"+tm.name+"\t["+tm.status+"]");
			}
		}
	}
}
