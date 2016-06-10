package com.example.example1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.example1.ListViewAdapter.ListData;

public class Speaker extends Activity {
	
	private ListView mListView = null;
	private ListViewAdapter mAdapter = null;
	private Handler handler = new Handler();
	DB_delete db_delete = new DB_delete();

	public static boolean all_exit; // static 선언
	public static String total; // static 선언
	ArrayList<Integer> ab = new ArrayList<Integer>();
	Thread thrd_dev, thrd_vol;
	DatagramSocket sock_meg, socket_vol;
	SeekBar all;
	AudioManager mgr;
	BroadcastReceiver myvol_broad;
	AudioTrack visul_track;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speaker);

		Intent intent = new Intent(this.getIntent());
		total = intent.getStringExtra("total");

		TextView num1 = (TextView) findViewById(R.id.num1);
		TextView num2 = (TextView) findViewById(R.id.num2);
		TextView num3 = (TextView) findViewById(R.id.num3);
		TextView num4 = (TextView) findViewById(R.id.num4);
		num1.setText(total.charAt(0) + "");
		num2.setText(total.charAt(1) + "");
		num3.setText(total.charAt(2) + "");
		num4.setText(total.charAt(3) + "");
		
		mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		all = (SeekBar)findViewById(R.id.seekBar);
		initBar(all,AudioManager.STREAM_MUSIC);
		
		mListView = (ListView)findViewById(R.id.mList);
		mAdapter = new ListViewAdapter(this, mgr);
		mListView.setAdapter(mAdapter);
		
		all_exit = false;
		RecvDevice();
		RecvVolume();
		new NotiManager().notibar_create(this, 1, "Speaker", "스피커가 재생중 입니다. 인증 번호:" + total, total);
		
		Button equ = (Button)findViewById(R.id.equ);
		equ.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Speaker.this,EqualizerActivity.class);
				intent.putIntegerArrayListExtra("ab", ab);
				intent.putExtra("total", total); // 연결정보 전달
				startActivity(intent);
			}
		});

		myvol_broad= new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				all.setProgress(mgr.getStreamVolume(AudioManager.STREAM_MUSIC));
				Log.d("Music Stream", "has changed / "+ intent.getAction());
			}
		};
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
	    registerReceiver(myvol_broad, filter);
	};
	@Override
	public void onBackPressed() { super.onBackPressed(); Log.e("back","back"); finish(); };
	@Override
	protected void onDestroy() {
		super.onDestroy();
		all_exit = true;
		sock_meg.close(); // socket bind solution
		socket_vol.close();
		mAdapter.Disconnect();
		db_delete.execute();
		new NotiManager().notibar_remove(getApplicationContext()); 
		unregisterReceiver(myvol_broad); // broadcast quit
	};

	static final int CONNECT_PORT = 11000;
	static final int SAMPLE_RATE = 44100;

	public void RecvDevice(){
		thrd_dev = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int AUDIO_PORT = 2048;
                	sock_meg = new DatagramSocket(CONNECT_PORT);
                    byte[] buf = new byte[1024];
                    while(!all_exit)
                    {
                        DatagramPacket pack_msg = new DatagramPacket(buf, 1024);
                        sock_meg.receive(pack_msg);
                        String msg = new String(pack_msg.getData(), 0, pack_msg.getLength());
            			
            			int pos = overlap(1, msg.substring(0,msg.indexOf("/")));
            			Log.d("msg / position", msg +"/"+ pos);
            			if(pos!=-1){
            				for(int i = 0 ; i < 3;i++) // 3회 for safety - 연결거부
            					SendPort(msg.toString().substring(0,msg.toString().indexOf("/")) ,"-1");
            				
            				mAdapter.remove(pos);
            				ab.remove(pos);
            				ui_update();
            				continue;
            			}
            			
            			SendPort(msg.toString().substring(0,msg.toString().indexOf("/")) ,AUDIO_PORT+"");
            			RecvAudio(AUDIO_PORT);
            			AUDIO_PORT+=5;
            			
            			while(visul_track==null);
            			// create adapter and set to listview.
            			mAdapter.addItem(msg.toString(),visul_track);
            			visul_track = null;
            			
            			// mAdapter.addItem(msg.toString());
            			mListView.setOnItemClickListener(new OnItemClickListener() {
            				@Override
            				public void onItemClick(AdapterView<?> parent, View v, int position, long id){
            					ListData mData = mAdapter.mListData.get(position);
            					if (System.currentTimeMillis() > mData.backKeyPressedTime + 2000){ // 1초
            						mData.click = true; mData.backKeyPressedTime = System.currentTimeMillis();
            					}
            					mData.click = !mData.click;
            					if(mData.click){
            						mAdapter.SendVoulme(mData.ip ,"-1");
            						mAdapter.remove(position);
            						ab.remove(position);
            						ui_update();
            					}else
                					Toast.makeText(getApplicationContext(), "더블 클릭시 연결종료 됩니다.", Toast.LENGTH_SHORT).show();
            					}
            				});
            			ui_update();
                    }
				} catch (IOException se) {
                    Log.e("error", "SocketException: " + se.toString());
                }
			}});
		
		thrd_dev.start();
	}

	private void ui_update(){
		new Thread(new Runnable() {
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						mAdapter.notifyDataSetChanged();
					}
				});
			}
		}).start();
	}
	private int overlap(int k, String msg) {
		for(int i = 0; i < mAdapter.mListData.size();i++){
			String str = "";
			switch(k){
			case 0: str = mAdapter.mListData.get(i).mTitle.toString(); break;
			case 1: str = mAdapter.mListData.get(i).ip.toString(); break;
			}
			if(str.equals(msg)) return i;
		}
		return -1;
	}
	
	private void SendPort(final String ip, final String AUDIO_PORT) {
		Thread thrd_port = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress serverAddr = InetAddress.getByName(ip);
					DatagramSocket socket = new DatagramSocket();
					byte[] buf = (AUDIO_PORT).getBytes();
					DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, CONNECT_PORT);
					Log.d("Port send", ip + "/" + AUDIO_PORT);
					socket.send(packet);
					socket.close(); // 1회
				} catch (Exception e) {
					Log.e("UDP", "S: Error", e);
				}
			}
		});
		thrd_port.start();
	}
	
	public void RecvAudio(final int AUDIO_PORT) {
		Thread thrd = new Thread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Log.e("start audio recv", "start recv thread, thread id:" + Thread.currentThread().getId() + "PORT" + AUDIO_PORT);

				// minimum buffer size. need to be careful. might cause problems. try setting manually if any problems faced
				int minBufSize = 7056; //AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
				
				AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
						AudioFormat.ENCODING_PCM_16BIT, minBufSize, AudioTrack.MODE_STREAM);
				ab.add(track.getAudioSessionId()); // list 추가
				
				visul_track = track;
                track.play();
                try {
                	DatagramSocket sock = new DatagramSocket(AUDIO_PORT);
                    byte[] buf = new byte[minBufSize];
                    while(!all_exit)
                    {
                        DatagramPacket pack = new DatagramPacket(buf, minBufSize);
                        sock.receive(pack);
                        track.write(pack.getData(), 0, pack.getLength());
                    }
                    // Log.d("minbufsize / recived audio length", minBufSize + " buf header" + Integer.toHexString(0xff & buf[minBufSize - 1]) + " recv pack: " + pack.getLength());
                    sock.close();
                }
                catch (SocketException se)
                {
                    Log.e("error", "SocketException: " + se.toString());
                }
                catch (IOException ie)
                {
                    Log.e("error", "IOException" + ie.toString());
                }
            } // end run
        });
        thrd.start();
    }
	
	private void initBar(SeekBar bar, final int stream) {
	    bar.setMax(mgr.getStreamMaxVolume(stream));
	    bar.setProgress(mgr.getStreamVolume(stream));
	    bar.setThumbOffset(bar.getWidth()/2);

	    bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	        @Override
	    	public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
	    		mgr.setStreamVolume(stream, progress, AudioManager.FLAG_PLAY_SOUND);
	    	}
	        @Override
	    	public void onStartTrackingTouch(SeekBar bar) {
	    		// no-op
	    	}
	        @Override
	    	public void onStopTrackingTouch(SeekBar bar) {
	    		// no-op
	    	}
	    });
	}
	
	// connected device volume always 최신화
	public void RecvVolume(){
		final int VOLUME_PORT = 12000;
		thrd_vol = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socket_vol = new DatagramSocket(VOLUME_PORT);
                    byte[] buf = new byte[1024];
                    // int before = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                    while(!all_exit)
                    {
                        DatagramPacket pack_vol = new DatagramPacket(buf, 1024);
                        socket_vol.receive(pack_vol);
                        String msg = new String(pack_vol.getData(), 0, pack_vol.getLength());
            			int position = overlap(1, msg.substring(0, msg.indexOf("/")));
            			Log.e("recvvolume", msg.substring(0, msg.indexOf("/"))+"/"+position);
            			int change_vol = Integer.parseInt(msg.substring(msg.indexOf("/")+1));

//            			Log.e("change_vol", change_vol + " // " + before);
//            			if(!msg.isEmpty() && Math.abs(before-change_vol)!=1) continue;
//            			before = change_vol;
            			
            			if(position!=-1){
            				mAdapter.seekchanged(position, change_vol);
            				mAdapter.mListData.get(position).trans = true;
                			ui_update(); // 리스트 전체를 업데이트 한다. --> volume 조절
            			}
                    }
				} catch (IOException se) {
                    Log.e("error", "SocketException: " + se.toString());
                }
			}
		}); 
		thrd_vol.start();
	}
	
	class DB_delete extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpPost request = new HttpPost(
						 "http://ss.issro.net/Destroy.php?r_number=" + total);
				HttpClient client = new DefaultHttpClient();
				client.execute(request);
				Log.i("db_delete",total+" delete complete");
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
