package com.example.example1;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;

// Application work - 강제종료
public class MyApplication extends Application {

	private UncaughtExceptionHandler mUncaughtExceptionHandler;

	@Override
	public void onCreate(){
		mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerApplication());
		super.onCreate();
	}

    private String getStackTrace(Throwable th) {
        final Writer result = new StringWriter();

        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
    
	class UncaughtExceptionHandlerApplication implements Thread.UncaughtExceptionHandler{
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			//예외상황이 발행 되는 경우 작업
			new Speaker().db_delete.execute();
			Log.e("Error", getStackTrace(ex)); 
			//예외처리를 하지 않고 DefaultUncaughtException으로 넘긴다.
			mUncaughtExceptionHandler.uncaughtException(thread, ex);
		}
	}
}