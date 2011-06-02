package com.brandroid;

import java.lang.Thread.UncaughtExceptionHandler;

public class CustomExceptionHandler implements UncaughtExceptionHandler {
	public void uncaughtException(Thread t, Throwable e) {
		Logger.LogError("Uncaught Exception in Thread " + t.getName(), new Exception(e));
	}
}
