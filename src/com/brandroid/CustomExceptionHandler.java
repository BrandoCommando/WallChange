package com.brandroid;

import java.lang.Thread.UncaughtExceptionHandler;

import android.os.Environment;

public class CustomExceptionHandler implements UncaughtExceptionHandler {
	public void uncaughtException(Thread t, Throwable e) {
		Logger.LogError("Uncaught Exception in Thread: " + t.toString(), new Exception(e));
		System.exit(1);
	}
}
