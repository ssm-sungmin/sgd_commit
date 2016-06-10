package com.example.example1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewAdapter extends BaseAdapter {
	Context mContext = null;
	AudioManager manager = null;
	ArrayList<ListData> mListData = new ArrayList<ListData>();

	public ListViewAdapter(Context mContext, AudioManager manager) {
		super();
		this.mContext = mContext;
		this.manager = manager;
	}

	@Override
	public int getCount() {
		return mListData.size();
	}

	@Override
	public Object getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addItem(String mTitle, AudioTrack visul_track){ // , AudioTrack visul_track
	    ListData addInfo = null;
	    addInfo = new ListData();
	    addInfo.mTitle = mTitle.substring(0, mTitle.indexOf("%"));
	    addInfo.ip = mTitle.substring(0, mTitle.indexOf("/"));
	    
	    int start = mTitle.indexOf("%") + 1;
	    addInfo.volume = Integer.parseInt(mTitle.substring(start,mTitle.indexOf("%",start))); // length 주석o
	    addInfo.device = mTitle.substring(mTitle.indexOf("%",start) + 1 ,mTitle.length());
	    addInfo.track = visul_track;

	    mListData.add(addInfo);
	}
	 
	public void remove(int position){
	    mListData.get(position).track.stop();
	    mListData.remove(position);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
	    if (convertView == null) {
	        holder = new ViewHolder();
			
	        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = inflater.inflate(R.layout.listview_item, null);

			holder.layout = (LinearLayout)convertView.findViewById(R.id.layout);	        
			holder.mText = (TextView) convertView.findViewById(R.id.mText);
		    holder.vol = (SeekBar)convertView.findViewById(R.id.seekBar1);
		    holder.icon = (ImageView)convertView.findViewById(R.id.imageView1);
		    holder.swc = (Switch)convertView.findViewById(R.id.switch1);
		    // holder.mVisualizerView = (VisualizerView)convertView.findViewById(R.id.visualizerView);
	 
	        convertView.setTag(holder);
	    }else{
	        holder = (ViewHolder) convertView.getTag();
	    }

	    ListData mData = mListData.get(position);
	 
	    holder.mText.setText(mData.mTitle);

		/*if(position%2==0) holder.layout.setBackgroundColor(Color.WHITE);
		else holder.layout.setBackgroundColor(Color.LTGRAY);*/
		
	    holder.vol.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
	    holder.vol.setProgress(mData.volume);
	    holder.vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
	    	public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				if(mListData.size() <= position) return;  // position indexoutbound 사전예방
        		
        		if(mListData.get(position).device.equals("pc"))
        			mListData.get(position).track.setStereoVolume((float)progress/15, (float)progress/15);
        		
	        	// 변화할때 겹치는 문제  ---- !!! // 해당 싱크만 get 할것 && 해당 volume 수정
	        	if(fromUser || mListData.get(position).trans){
	        		if(fromUser)
		        		mListData.get(position).volume = progress;
	        		
	        		if(!mListData.get(position).lock){
	        			Log.i("", mListData.get(position).ip+"//"+progress+"//"+mListData.get(position).volume);
	        			SendVoulme(mListData.get(position).ip+"", mListData.get(position).volume+"");
	        		}
	        	}
        		mListData.get(position).trans = false;
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
	    
	    // Log.d("volume / device",mData.volume + " / " + mData.device); // 문자열 확인
	    if(mData.device.equals("pc")){
	    	holder.icon.setImageResource(R.drawable.pc_icon);
	    	mData.track.setStereoVolume((float)mData.volume/15, (float)mData.volume/15);

	    	holder.swc.setVisibility(View.VISIBLE); // 버튼 추가
	    	holder.swc.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mListData.get(position).lock = isChecked;
					if(isChecked){
						Toast.makeText(mContext, "Master 볼륨 조절이 잠겼습니다.", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(mContext, "Master 볼륨 조절이 열렸습니다.", Toast.LENGTH_SHORT).show();
					}
				}
			});
	    }
	    else{
	    	holder.icon.setImageResource(R.drawable.phone_icon);
	    	holder.swc.setVisibility(View.GONE); // 버튼 제거
	    }

	   /* holder.mVisualizerView.link(mData.track); // null 처리
		// holder.addBarGraphRenderers();
		// Add the LineDrawer to the view
		holder.addLineRenderer();*/
	    
	    return convertView;
	}
	
	public void seekchanged(int position,int volume){
		Log.i("position /// volume",position+"////"+volume);
		mListData.get(position).volume = volume;
	}
	
	public void Disconnect(){
		for(ListData list : mListData) {
			SendVoulme(list.ip,"-1");
			list.track.stop();
		}
		mListData.clear();
	}
	
	public void SendVoulme(final String total, final String volume) {
		final int VOLUME_PORT = 12000;
		Thread thrd_vol = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress serverAddr = InetAddress.getByName(total);
					DatagramSocket socket = new DatagramSocket();
					byte[] buf = (volume).getBytes();
					DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, VOLUME_PORT);
					Log.e("volume____", "S: " + new String(buf) + "");
					socket.send(packet);
					socket.close(); // 1회
				} catch (Exception e) {
					Log.e("UDP", "S: Error", e);
				}
			}
		});
		thrd_vol.start();
	}
	
	public class ViewHolder {
		public LinearLayout layout;
	    public TextView mText;
	    public SeekBar vol;
	    public ImageView icon;
	    public Switch swc;
	    // public VisualizerView mVisualizerView;
	    
		/*// Creates a bar
	    public void addBarGraphRenderers()
	    {
	        Paint paint = new Paint();
	        paint.setStrokeWidth(50f);
	        paint.setAntiAlias(true);
	        paint.setColor(Color.rgb(255,0,0));
	        BarDrawer barGraphRendererBottom = new BarDrawer(16, paint, false);
	        mVisualizerView.addRenderer(barGraphRendererBottom);
	    }
	    
	 // Creates a Line
	    private void addLineRenderer()
	    {
	        Paint linePaint = new Paint();
	        linePaint.setStrokeWidth(1f);
	        linePaint.setAntiAlias(true);
	        linePaint.setColor(Color.rgb(232,85,5));

	        Paint lineFlashPaint = new Paint();
	        lineFlashPaint.setStrokeWidth(5f);
	        lineFlashPaint.setAntiAlias(true);
	        lineFlashPaint.setColor(Color.rgb(255,0,0));
	        LineDrawer lineDrawer = new LineDrawer(linePaint, lineFlashPaint, false);
	        mVisualizerView.addRenderer(lineDrawer);
	    }*/
	}

	public class ListData {
		/** 리스트 정보를 담고 있을 객체 생성 **/
		// 아이콘 public Drawable mIcon;
		// 제목
		public String mTitle;
		// 볼륨
		public String ip;
		public int volume;
		public String device;
		public boolean trans = false;
		public AudioTrack track;
		
		long backKeyPressedTime = 0;
		boolean click = true;
		boolean lock = false;
	}
}