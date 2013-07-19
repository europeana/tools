/*
 * Copyright 2005-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.annocultor.xconverter.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.velocity.VelocityContext;

import eu.annocultor.xconverter.api.GeneratedConverterInt;

public class Converter {

	public static void main(String... args) 
	throws Exception
	{
		if (args.length != 2)
		{
			System.err.println("Error in parameters, expected <profile> <work dir>");
			System.exit(1);
		}
		int result = run(new File(args[0]), new File(args[1]), new PrintWriter(System.out));
		System.exit(result);
	}

	public static int run(File profileFile, File workDir, PrintWriter console)
	throws Exception
	{
		OutputStream javaOutputStream = new StringOutputStream();
		return run(profileFile, workDir, console, javaOutputStream);
	}

	static File compile(File profileFile, File workDir, PrintWriter console, OutputStream javaOutputStream)
	throws Exception
	{
		if (!profileFile.exists())
		{
			throw new IOException("Cannot find xconverter: " + profileFile.getCanonicalPath());
		}
		
		// everything happens in the working dir named according to the profile file name
		workDir = new File(workDir, profileFile.getName());
		workDir.mkdir();
		
		// find a suitable place to store the generated Java code
		// should not be on the classpath!!!
		File work = new File(workDir, "xconverterwork");
		if (!work.exists())
		{
			work.mkdir();
		}

		
		// run Velocity to convert XML definition of a converter to Java code
		XConverter2Java converter = new XConverter2Java();
		VelocityContext context = converter.run(
				profileFile.getParentFile(),
				new FileInputStream(profileFile), 
				"xconverter/XConverterGenerator.vm", 
				javaOutputStream,
				work
		);		
		javaOutputStream.close();

		// there is also repositoryId set in the template
		File profile = new File(work, context.get("profileId").toString());
		if (!profile.exists())
		{
			profile.mkdir();
		}
		if (profile.listFiles() != null)
		{
			for (File file : profile.listFiles())
			{
				file.delete();
			}
		}

		File javaFile = new File(profile, XConverter2Java.GENERATED_CONVERTER_CLASS_NAME + ".java");
		if (javaFile.exists())
		{
			javaFile.delete();
		}


		Writer fos = new FileWriter(javaFile, true);
		fos.write(javaOutputStream.toString());
		fos.close();

		// compile it
		String cpJars = 
			System.getProperty("java.class.path") + File.pathSeparatorChar + 
			System.getProperty("sun.boot.class.path") + File.pathSeparatorChar + 
			System.getenv("ANNOCULTOR_TOOLS_JAR") + File.pathSeparatorChar + 
			"mavenbin/";

		String [] jars = cpJars.split(File.pathSeparatorChar+"");
		URL[] urls = new URL[jars.length];
		for (int i = 0; i < jars.length; i++) {
			urls[i] = new File(jars[i]).toURI().toURL();
		}

		console.println("Running generated converter");//, classpath: " + cpJars);
		console.flush();
		int compileReturnCode = -1;

		try 
		{
//						Class javac = Class.forName("com.sun.tools.javac.Main", true, new URLClassLoader(urls));
//						Method method = javac.getMethod("compile", String[].class, PrintWriter.class);
//			
//						compileReturnCode =	(Integer)method.invoke(null, 
//								new String[] {
//								javaFile.getCanonicalPath(),
//								"-cp",
//								cpJars},
//								console);
//						return compileReturnCode == 0 ? profile : null;
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				throw new Exception("Cannot find Java compiler. Are you running Java 6 JDK? Detected " 
						+ System.getProperty("java.version") + " running at " + System.getProperty("java.home"));
			}
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

			Iterable<? extends JavaFileObject> cu = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile));
			boolean success = compiler.getTask(console, fileManager, null, Arrays.asList("-cp", cpJars), null, cu).call();

			fileManager.close();

			return success ? profile : null;
		}
		catch (Exception e) 
		{
			throw new Exception("Exception compiling converter", e);
		}
	}

	static int run(File profileFile, File workDir, PrintWriter console, OutputStream javaOutputStream)
	throws Exception
	{
		try 
		{
			File profile = compile(profileFile, workDir, console, javaOutputStream);
			if (profile != null) 
			{
				// run it
				Object objectParameters[] = {new String[]{}};
				Class classParameters[] = {objectParameters[0].getClass()};
				// Create a new class loader with the directory
				ClassLoader cl = new URLClassLoader(new URL[]{profile.toURI().toURL()});

				// Load in the class
				Class cls = cl.loadClass(XConverter2Java.GENERATED_CONVERTER_CLASS_NAME);

				// Create a new instance of the new class
				GeneratedConverterInt instance = (GeneratedConverterInt)cls.newInstance();
				if (instance == null)
				{
					throw new Exception("Failed to create converter instance");
				}
				try 
				{
					int r = instance.run(console);
					console.flush();
					return r;
				}
				catch (Exception e) {
					throw new Exception("Exception launching converter", e);
				}
			} 
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new Exception("Exception compiling converter", e);
		}

		return -1;
	}

}
