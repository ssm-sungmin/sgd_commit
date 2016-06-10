package com.example.example1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NotiManager{

	int mNotificationId = 1;
	NotificationCompat.Builder mBuilder;
	NotificationManager mNotifyMgr;
	Intent resultIntent;
	
	@SuppressWarnings("static-access")
	void notibar_create(Context context, int check, String title, String text,String input) {
		
		// Build Notification , setOngoing keeps the notification always in  status bar
		mBuilder= new NotificationCompat.Builder(context).setSmallIcon(R.drawable.notibar).setContentTitle(title)
				.setContentText(text).setOngoing(true);

		// Creates an explicit intent for an Activity in your app
		resultIntent = check == 1 ? new Intent(context, Speaker.class) : new Intent(context, Client.class);
		
		resultIntent.putExtra("total", input);

		if(check == -1){ // 임시 제거
			// The stack builder object will contain an artificial back stack for the started Activity.
			// This ensures that navigating backward from the Activity leads out of your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class); // error ? 
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
			/*resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
        	PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);*/
        
			mBuilder.setContentIntent(resultPendingIntent);
			
			// 행동 최대 3개 등록
			mBuilder.addAction(R.drawable.ic_launcher, "Show", resultPendingIntent);
			mBuilder.addAction(R.drawable.ic_launcher, "Hide", resultPendingIntent);
		}
        
		// Gets an instance of the NotificationManager service
		mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		// Build the notification and issues it.
		mNotifyMgr.notify(mNotificationId, mBuilder.build());
	}
	
	@SuppressWarnings("static-access")
	void notibar_remove(Context context){
		mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		mNotifyMgr.cancel(mNotificationId);
	}
}
