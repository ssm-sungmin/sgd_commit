package com.example.example1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	String total;
	ConnectivityManager manager;
	NetworkInfo wifi; // mobile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Load the ImageView that will host the animation and
		// set its background to our AnimationDrawable XML resource.
		LinearLayout img = (LinearLayout)findViewById(R.id.main_layout);
		img.setBackgroundResource(R.drawable.animation_main);
		// Get the background, which has been compiled to an AnimationDrawable object.
		AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
		// Start the animation (looped playback by default).
		frameAnimation.start();

		manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		// mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	}
	
	@Override
	protected void onDestroy() { super.onDestroy(); new NotiManager().notibar_remove(getApplicationContext()); };
	
	public void btnPost(View v) {
		wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (wifi.isConnected())
			new SendPost().execute(); // 서버와 자료 주고받기
		else
			Toast.makeText(getApplicationContext(), "Wifi 환경에서 동작 가능합니다. " , Toast.LENGTH_SHORT).show();
	}

	public void btnConnect(View v) {
		wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// Connect
		if (wifi.isConnected()){
			Intent intent = new Intent(MainActivity.this, Connect.class);
			startActivity(intent);
		}
		else
			Toast.makeText(getApplicationContext(), "Wifi 환경에서 동작 가능합니다. " , Toast.LENGTH_SHORT).show();
	}

	private class SendPost extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... unused) {
			try {
				HttpPost request = new HttpPost(
						"http://ss.issro.net/ServerCreate.php?ip="
								+ InetAddress.getLocalHost().getAddress());
				// 전달할 인자들
				Vector<NameValuePair> nameValue = new Vector<NameValuePair>();

				// 웹 접속 - utf-8 방식으로
				HttpEntity enty = new UrlEncodedFormEntity(nameValue,
						HTTP.UTF_8);
				request.setEntity(enty);

				HttpClient client = new DefaultHttpClient();
				HttpResponse res = client.execute(request);

				// 웹 서버에서 값받기
				HttpEntity entityResponse = res.getEntity();
				InputStream im = entityResponse.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(im, HTTP.UTF_8));

				total = "";
				String tmp = "";
				while ((tmp = reader.readLine()) != null) {
					if (tmp != null) {
						total += tmp;
					}
				}
				im.close();
				Log.e("check", total);
				return total;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String result) {
			// 모두 작업을 마치고 실행할 일 (메소드 등등)
			if (total !=null && total != "") {
				Intent intent = new Intent(MainActivity.this, Speaker.class);
				intent.putExtra("total", total);
				startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(), "네트워크가 안정적이지 않습니다.", Toast.LENGTH_SHORT).show();
			}
		}
	}
} // Activity
