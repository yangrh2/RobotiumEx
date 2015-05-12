package com.robotium.solo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;

public class FileUtils {
	public static void chmod(String filename, int permissions) {
		// This was a bit problematic - there is an
		// android.os.FileUtils.setPermissions()
		// function, but apparently that is not a part of the supported
		// interface.
		// I found some other options:
		// - java.io.setReadOnly() exists, but seems limited.
		// - java.io.File.setWritable() is a part of Java 1.6, but doesn't seem
		// to exist in Android.
		// - java.nio.file.attribute.PosixFilePermission also doesn't seem to
		// exist under Android.
		// - doCommand("/system/bin/chmod", permissions, filename) was what I
		// used to do, but it was crashing for some.
		// I don't think these permissions are actually critical for anything in
		// the application,
		// so for now, we will try to use the undocumented function and just be
		// careful to catch any exceptions
		// and print some output spew. /FF

		try
		{
		    Class<?> fileUtils = Class.forName("android.os.FileUtils");
		    Method setPermissions =
		        fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
		    int a = (Integer) setPermissions.invoke(null, filename, permissions, -1, -1);
		    if(a != 0)
		    {
				Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() returned " + a + " for '" + filename + "', probably didn't work.");
		    }
		}
		catch(Exception e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - ClassNotFoundException.");
		}
	}
}
