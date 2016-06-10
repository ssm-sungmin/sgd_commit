package com.example.example1;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;

// Application work - ��������
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
			//���ܻ�Ȳ�� ���� �Ǵ� ��� �۾�
			new Speaker().db_delete.execute();
			Log.e("Error", getStackTrace(ex)); 
			//����ó���� ���� �ʰ� DefaultUncaughtException���� �ѱ��.
			mUncaughtExceptionHandler.uncaughtException(thread, ex);
		}
	}
}