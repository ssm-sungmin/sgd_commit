package com.example.example1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.gc.materialdesign.views.ButtonRectangle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class Client extends Activity {

	String total, name;
	DatagramSocket sock, sock_rec, socket , socket_vol;
	Thread thrd, thrd_msg, thrd_rec, thrd_vol, thrd_port;
	AudioManager manager;
	AudioRecord audio_recorder;
	BroadcastReceiver vol_broad;
	int Port;
	boolean translate;

 	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState != null){
			finish();
			return ;
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);

		Intent intent = new Intent(this.getIntent());
		total = intent.getStringExtra("total");
		name = intent.getStringExtra("name");

		manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		
		info_Send(); // first udp msg 통신
		if(thrd_msg!=null) thrd_msg.interrupt();
		ReceivePort();
		Log.i("test", "recive port start");
		while(Port < 2048){ // connect wait 
			if(Port==-1){ // connect reject
				Toast.makeText(getApplicationContext(),"연결을 거부하였습니다.", Toast.LENGTH_SHORT).show();
				socket.close();
				thrd_port.interrupt();
				finish();
				return ;
			}
		}
		if(socket!=null) socket.close();
		if(thrd_port!=null) thrd_port.interrupt();
		
		// SendAudio(); // udp audio send
		if(thrd_rec==null){ SendMicAudio(Port); RecvVolume();}// capture by udp audio recorder
		new NotiManager().notibar_create(this, 2, "Connect", "스피커가 재생중 입니다.", null);

		ButtonRectangle btn = (ButtonRectangle) findViewById(R.id.btndis);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				info_Send(); // disconnect !!
				all_interrupt();
				finish();
			}
		});
		
		translate = true;
		vol_broad= new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// receive 하면서 send 되는부분 !! -> translate로 차단
				if(translate){
					SendVoulme(total,manager.getStreamVolume(AudioManager.STREAM_MUSIC)+"");
					Log.d("Music Stream", "has changed / "+ intent.getAction());
				}
				translate = true;
			}
		};
	}
	@Override
	public void onBackPressed() { 
		super.onBackPressed(); 
		info_Send(); // disconnect !!
		all_interrupt();
		finish(); 
	}; // back button
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
	    registerReceiver(vol_broad, filter);
	};
	@Override
	protected void onDestroy() { super.onDestroy(); all_interrupt(); new NotiManager().notibar_remove(getApplicationContext());
							if(vol_broad!=null) unregisterReceiver(vol_broad);}; // Destroy의 경우, broad 해제
	@Override
	protected void onPause() { super.onPause();};
	protected void all_interrupt(){
		if(socket_vol!=null) socket_vol.close();
		if(thrd_msg!=null) thrd_msg.interrupt();
		if(thrd_rec!=null) thrd_rec.interrupt();
		if(thrd_vol!=null) thrd_vol.interrupt();
	}

	// static final String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/ost.wav";
	static final int CONNECT_PORT = 11000;
	static final int SAMPLE_RATE = 44100;
	static final int SAMPLE_INTERVAL = 0; // milliseconds --> speed sleep. minsize/100 + 5!

	public void SendMicAudio(final int AUDIO_PORT) {
		thrd_rec = new Thread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Log.e("start audio send", "start SendMicAudio thread, thread id: "
						+ Thread.currentThread().getId());
				
				int minsize = 7056; // AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
				audio_recorder = new AudioRecord(
						MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, // AudioSource -> Mic
						AudioFormat.CHANNEL_CONFIGURATION_STEREO,
						AudioFormat.ENCODING_PCM_16BIT,
						minsize * 5);
				@SuppressWarnings("unused")
				int bytes_read = 0;
				// int bytes_count = 0;
				byte[] buf = new byte[minsize];
				// byte order = 0x00;

				if(AcousticEchoCanceler.isAvailable()) AcousticEchoCanceler.create(audio_recorder.getAudioSessionId()); // 음향 소음 제거
				// manager.setMicrophoneMute(true);
				audio_recorder.startRecording();
				
				try {
					InetAddress addr = InetAddress.getByName(total);
					sock_rec = new DatagramSocket();
					
					while (true) {
						bytes_read = audio_recorder.read(buf, 0, minsize);
						DatagramPacket pack = new DatagramPacket(buf, minsize, addr, AUDIO_PORT); // 재실행시 오류발생 -> 원인파악 필요
						sock_rec.send(pack);
						Thread.sleep(SAMPLE_INTERVAL, 0);
					}
					// Log.d("sended audio_rec byte", "send "  + Integer.toHexString(0xff & buf[minsize - 1]) + ", bytes_count : " + bytes_count);
				} catch (InterruptedException ie) {
					sock_rec.close();
					audio_recorder.release();
					Log.e("error", "InterruptedException");
				} catch (SocketException se) {
					Log.e("error", "SocketException");
				} catch (IOException ie) {
					Log.e("error", "IOException");
				}
			} // end run
		});
		thrd_rec.start();
	}

	private boolean check ;
	public boolean info_Send() {
		check = false;
		thrd_msg = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress serverAddr = InetAddress.getByName(total); // 수신을 원하는 디바이스 IP 주소
					DatagramSocket socket = new DatagramSocket(); // 보통 보내는 쪽은 socket을 열고 대기할 필요가 없기
																	// 때문에 아래 패킷을 생성할때 인자값으로 넘김

					int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC); // 현재 기기 볼륨 size
					
					byte[] buf = (getIp() + "/" + name + "%"+currentVolume +"%android").getBytes();
					DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, CONNECT_PORT);
					Log.e("connect", "S: Sending : " + new String(buf));
					socket.send(packet);
					Log.e("complete", "S: Send ok");
					socket.close(); // 1회
					check = true;
				} catch (Exception e) {
					Log.e("UDP", "S: Error", e);
				}
			}
		});
		thrd_msg.start();
		return check;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    switch (keyCode) {
	    case KeyEvent.KEYCODE_VOLUME_UP:
	        manager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
	                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
	        SendVoulme(total,manager.getStreamVolume(AudioManager.STREAM_MUSIC)+"");	        
	        return true;
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	    	manager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
	                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
	        SendVoulme(total,manager.getStreamVolume(AudioManager.STREAM_MUSIC)+"");	
	        return true;
	    default:
	        return false;
	    }
	}
	
	public void SendVoulme(final String total, final String volume) {
		final int VOLUME_PORT = 12000;
		Thread thrd_vol = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress serverAddr = InetAddress.getByName(total);
					DatagramSocket socket = new DatagramSocket();
					byte[] buf = (getIp()+"/"+volume).getBytes();
					DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, VOLUME_PORT);
					Log.e("volume", "S: Sending : " + new String(buf) + "");
					socket.send(packet);
					socket.close(); // 1회
				} catch (Exception e) {
					Log.e("UDP", "S: Error", e);
				}
			}
		});
		thrd_vol.start();
	}
	
	private void ReceivePort() {
		thrd_port = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socket = new DatagramSocket(CONNECT_PORT);
                    byte[] buf = new byte[1024];
        			Log.i("start port : ", "start port");
                    while(true)
                    {
                        DatagramPacket pack_port = new DatagramPacket(buf, 1024);
                        socket.receive(pack_port);
                        String msg = new String(pack_port.getData(), 0, pack_port.getLength());
            			Log.i("port : ", msg);
            			if(msg!=null){
            				Port = Integer.parseInt(msg);
            				return ;
            			}
                    } 
				} catch (IOException se) {
                    Log.e("error", "SocketException: " + se.toString());
                }
			}}); 
		thrd_port.start();
	}
	
	private String getIp(){
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		
		@SuppressWarnings("deprecation")
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		return ip;
	}
	
	public void RecvVolume(){
		final int VOLUME_PORT = 12000;
		thrd_vol = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socket_vol = new DatagramSocket(VOLUME_PORT);
                    byte[] buf = new byte[1024];
                    String before = manager.getStreamVolume(AudioManager.STREAM_MUSIC)+"";
                    while(true)
                    {
                        DatagramPacket pack_vol = new DatagramPacket(buf, 1024);
                        socket_vol.receive(pack_vol);
                        String msg = new String(pack_vol.getData(), 0, pack_vol.getLength());
            			translate = !translate;
            				
            			if(msg.equals("-1")){
            				handler.sendEmptyMessage(0);
            				finish(); return;
            			}
            			
            			if(before.equals(msg)) continue;
            			Log.e("volume : meg", msg);
            			
            			manager.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(msg), AudioManager.FLAG_PLAY_SOUND);
            			before = msg;
                    }
				} catch (IOException se) {
                    Log.e("error", "SocketException: " + se.toString());
                }
			}
			
			@SuppressLint("HandlerLeak")
			private Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					Toast.makeText(getApplicationContext(), "스피커와 연결이 종료되었습니다.", Toast.LENGTH_SHORT).show(); 
					super.handleMessage(msg);
				}
			};
		}); 
		thrd_vol.start();
	}
	
	// only media stream
	@SuppressWarnings("unused")
	private void setMuteAll(boolean mute) {

		int[] streams = new int[] { AudioManager.STREAM_ALARM,
				AudioManager.STREAM_DTMF, AudioManager.STREAM_RING,
				AudioManager.STREAM_SYSTEM, AudioManager.STREAM_VOICE_CALL };

		for (int stream : streams)
			manager.setStreamMute(stream, mute);
	}
}
