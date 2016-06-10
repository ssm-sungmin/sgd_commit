package com.example.example1;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;

public class EqualizerActivity extends Activity {

    int BUFFER_SIZE = 7056;
    
    private ArrayList<SeekBar> seekBars;
    private short numberFrequencyBands;
    private short lowerEqualizerBandLevel;        // 최소 dB
    private short upperEqualizerBandLevel;        // 최대 dB

    ButtonRectangle btn_start, btn_stop, btn_equalizer1, btn_equalizer2, btn_equalizer3, btn_equalizer4;
    AudioTrack myAT = null;
    Thread play_thread = null;
    FileInputStream fin;
    DataInputStream dis;
    Boolean playing = false;
    byte[] byteData = null;

    ArrayList<Integer> ab = new ArrayList<Integer>();
    public static String total;
    Equalizer[] mEqualizer;            // 이퀄라이저
    
    DBManager db;
    String str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equalizer);

        Intent intent = new Intent(this.getIntent());
		ab = intent.getIntegerArrayListExtra("ab");
		total = intent.getStringExtra("total");
		
		TextView num1 = (TextView) findViewById(R.id.num1);
		TextView num2 = (TextView) findViewById(R.id.num2);
		TextView num3 = (TextView) findViewById(R.id.num3);
		TextView num4 = (TextView) findViewById(R.id.num4);
		num1.setText(total.charAt(0) + "");
		num2.setText(total.charAt(1) + "");
		num3.setText(total.charAt(2) + "");
		num4.setText(total.charAt(3) + "");

        btn_equalizer1 = (ButtonRectangle) findViewById(R.id.btn_equalizer1);
        btn_equalizer2 = (ButtonRectangle) findViewById(R.id.btn_equalizer2);
        btn_equalizer3 = (ButtonRectangle) findViewById(R.id.btn_equalizer3);
        btn_equalizer4 = (ButtonRectangle) findViewById(R.id.btn_equalizer4);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // myAT = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE, AudioTrack.MODE_STREAM);

        // ### AudioTrack 세션마다 이퀄라이저 적용 가능
        /*mEqualizer = new Equalizer(0, myAT.getAudioSessionId());
        mEqualizer.setEnabled(true); */
        
        if(ab.size()==0){
        	Toast.makeText(getApplicationContext(), "연결되어있는 장치가 없습니다.", Toast.LENGTH_SHORT).show();
        	return ;
        }
        
        mEqualizer = new Equalizer[ab.size()];
        
        for(int i = 0 ; i < ab.size(); i++){
        	Log.d("equlizer", ab.get(i) + " / " +ab.size());
        	mEqualizer[i] = new Equalizer(0, ab.get(i));
        	mEqualizer[i].setEnabled(true);
        }

        btn_equalizer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝
                seekBars.get(0).setProgress(1500);      // 60 Hz
                seekBars.get(1).setProgress(1500);      // 230 Hz
                seekBars.get(2).setProgress(1700);      // 910 Hz
                seekBars.get(3).setProgress(1800);      // 3.6 KHz
                seekBars.get(4).setProgress(1100);      // 14 KHz

                for(int i= 0 ;i<ab.size();i++){
                	mEqualizer[i].setBandLevel((short) 0, (short) 0);
                	mEqualizer[i].setBandLevel((short) 1, (short) 0);
                	mEqualizer[i].setBandLevel((short) 2, (short) 200);
                	mEqualizer[i].setBandLevel((short) 3, (short) 300);
                	mEqualizer[i].setBandLevel((short) 4, (short) -400);
                }
            }
        });

        btn_equalizer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클래식
                seekBars.get(0).setProgress(1500);      // 60 Hz
                seekBars.get(1).setProgress(1500);      // 230 Hz
                seekBars.get(2).setProgress(1000);      // 910 Hz
                seekBars.get(3).setProgress(1500);      // 3.6 KHz
                seekBars.get(4).setProgress(2300);      // 14 KHz

                for(int i= 0 ;i<ab.size();i++){
                	mEqualizer[i].setBandLevel((short) 0, (short) 0);
                	mEqualizer[i].setBandLevel((short) 1, (short) 0);
                	mEqualizer[i].setBandLevel((short) 2, (short) -500);
                	mEqualizer[i].setBandLevel((short) 3, (short) 0);
                	mEqualizer[i].setBandLevel((short) 4, (short) 800);
                }
            }
        });

        btn_equalizer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 재즈
                seekBars.get(0).setProgress(1500);      // 60 Hz
                seekBars.get(1).setProgress(1500);      // 230 Hz
                seekBars.get(2).setProgress(1000);      // 910 Hz
                seekBars.get(3).setProgress(1300);      // 3.6 KHz
                seekBars.get(4).setProgress(1400);      // 14 KHz

                for(int i= 0 ;i<ab.size();i++){
                	mEqualizer[i].setBandLevel((short) 0, (short) 0);
                	mEqualizer[i].setBandLevel((short) 1, (short) 0);
                	mEqualizer[i].setBandLevel((short) 2, (short) -500);
                	mEqualizer[i].setBandLevel((short) 3, (short) -200);
                	mEqualizer[i].setBandLevel((short) 4, (short) -100);
                }
            }
        });

        btn_equalizer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 락
                seekBars.get(0).setProgress(1500);      // 60 Hz
                seekBars.get(1).setProgress(1500);      // 230 Hz
                seekBars.get(2).setProgress(600);      // 910 Hz
                seekBars.get(3).setProgress(1300);      // 3.6 KHz
                seekBars.get(4).setProgress(1800);      // 14 KHz

                for(int i= 0 ;i<ab.size();i++){
                	mEqualizer[i].setBandLevel((short) 0, (short) 0);
                	mEqualizer[i].setBandLevel((short) 1, (short) 0);
                	mEqualizer[i].setBandLevel((short) 2, (short) -900);
                	mEqualizer[i].setBandLevel((short) 3, (short) -200);
                	mEqualizer[i].setBandLevel((short) 4, (short) 300);
                }
            }
        });

        setupEqualizerUI();

        // equlizer session 유지 -> sqlite
        db = new DBManager(getApplicationContext(), "number_list", null, 1);
        
        // db.delete("DELETE FROM Equlizer_list"); // table drop
        // 있으면 insert 안되도록 설정
        str = db.PrintData();
        if(str.equals(""))
        	db.insert("insert into number_list values(null, 1500,1500,1500,1500,1500,0,0,0,0,0)");
        else
        	cookies_sync_start();
    }
    
    @Override
    protected void onDestroy() { 
    	super.onDestroy(); 
    	if(ab.size()!=0)
    		cookies_sync_stop();
    };
    
	private void cookies_sync_start() {
		Log.i("str", str);
		
		int start = str.indexOf(",");
		int number1 = Integer.parseInt(str.substring(str.indexOf("number1:") + 8 , start)); // 인덱스 초과오류
		start = str.indexOf(",", start + 1);
		int number2 = Integer.parseInt(str.substring(str.indexOf("number2:") + 8 , start)); 
		start = str.indexOf(",", start + 1);
		int number3 = Integer.parseInt(str.substring(str.indexOf("number3:") + 8 , start)); 
		start = str.indexOf(",", start + 1);
		int number4 = Integer.parseInt(str.substring(str.indexOf("number4:") + 8 ,start));
		start = str.indexOf(",", start + 1);
		int number5 = Integer.parseInt(str.substring(str.indexOf("number5:") + 8 ,start));
		start = str.indexOf(",", start + 1);
		int number6 = Integer.parseInt(str.substring(str.indexOf("number6:") + 8 ,start));
		start = str.indexOf(",", start + 1);
		int number7 = Integer.parseInt(str.substring(str.indexOf("number7:") + 8 ,start)); 
		start = str.indexOf(",", start + 1);
		int number8 = Integer.parseInt(str.substring(str.indexOf("number8:") + 8 ,start)); 
		start = str.indexOf(",", start + 1);
		int number9 = Integer.parseInt(str.substring(str.indexOf("number9:") + 8 ,start)); 
		start = str.indexOf(",", start + 1);
		int number10 = Integer.parseInt(str.substring(str.indexOf("number10:") + 9 ,start));

		seekBars.get(0).setProgress(number1);      // 60 Hz
        seekBars.get(1).setProgress(number2);      // 230 Hz
        seekBars.get(2).setProgress(number3);      // 910 Hz
        seekBars.get(3).setProgress(number4);      // 3.6 KHz
        seekBars.get(4).setProgress(number5);      // 14 KHz

        for(int i= 0 ;i<ab.size();i++){
        	mEqualizer[i].setBandLevel((short) 0, (short) number6);
        	mEqualizer[i].setBandLevel((short) 1, (short) number7);
        	mEqualizer[i].setBandLevel((short) 2, (short) number8);
        	mEqualizer[i].setBandLevel((short) 3, (short) number9);
        	mEqualizer[i].setBandLevel((short) 4, (short) number10);
        }
	}

    private void cookies_sync_stop(){
        db.update("update number_list set number1 = " + seekBars.get(0).getProgress() + 
        		", number2 = " + seekBars.get(1).getProgress() + 
        		", number3 = " + seekBars.get(2).getProgress() + 
        		", number4 = " + seekBars.get(3).getProgress() + 
        		", number5 = " + seekBars.get(4).getProgress() + 
        		", number6 = " + mEqualizer[0].getBandLevel((short) 0) + 
        		", number7 = " + mEqualizer[0].getBandLevel((short) 1) + 
        		", number8 = " + mEqualizer[0].getBandLevel((short) 2) + 
        		", number9 = " + mEqualizer[0].getBandLevel((short) 3) + 
        		", number10 = " + mEqualizer[0].getBandLevel((short) 4) + 
        		" where _id = '1';");
    }

    @SuppressWarnings("unused")
    @SuppressLint({ "RtlHardcoded", "NewApi" })
	private void setupEqualizerUI() {

		int textSize = 13;
		int seekBarHeight = 200;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Log.d("FILEPATH", "displayMetrics.density : " + displayMetrics.density);
        Log.d("FILEPATH", "deviceWidth : " + deviceWidth + ", deviceHeight : " + deviceHeight);

        boolean displayRotationCheck = true;

//      LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.LinearLayoutEqual);
      LinearLayout mLinearLayout = (LinearLayout) new LinearLayout(this);
      mLinearLayout.setOrientation(LinearLayout.VERTICAL);
      mLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT));
//      RelativeLayout mLinearLayout = (RelativeLayout) new RelativeLayout(this);
      RelativeLayout relativeLayoutWrap = (RelativeLayout) new RelativeLayout(this);
      RelativeLayout.LayoutParams params;

      RelativeLayout relativeLayoutEqual = (RelativeLayout) findViewById(R.id.RelativeLayoutEqual);

      seekBars = new ArrayList<SeekBar>();

      /*
          화면 해상도에 따른 이퀄라이저 표시 방법 설정
       */
      if (deviceWidth < 1200) {
          displayRotationCheck = false;
          textSize = 10;
          seekBarHeight = 200;
          
          int widthSize = deviceWidth / 100;
          widthSize = widthSize * 100;
          
          params = new RelativeLayout.LayoutParams(widthSize, widthSize);
      } else {
          params = new RelativeLayout.LayoutParams(1200, 1200);
      }
      params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      relativeLayoutWrap.setLayoutParams(params);

      LinearLayout equalLinearLayout = (LinearLayout) new LinearLayout(this);
      equalLinearLayout.setOrientation(LinearLayout.VERTICAL);
      equalLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
      ));

        LinearLayout mLinearLayoutHeader = (LinearLayout) findViewById(R.id.LinearLayoutHeader);
        TextView equalizerHeading = new TextView(this);
        equalizerHeading.setText("이퀄라이저");
        equalizerHeading.setTextSize(20);
        equalizerHeading.setTextColor(Color.WHITE);
        equalizerHeading.setGravity(Gravity.LEFT);
        mLinearLayoutHeader.addView(equalizerHeading);

        for(int i=0;i<ab.size();i++){
        	numberFrequencyBands = (short) mEqualizer[i].getNumberOfBands();
        	lowerEqualizerBandLevel = mEqualizer[i].getBandLevelRange()[0];        // 최소 dB
        	upperEqualizerBandLevel = mEqualizer[i].getBandLevelRange()[1];        // 최대 dB
        }

        for (short i = 0; i < numberFrequencyBands; i++) {
//        for (short i = 0; i < 1; i++) {
            final short equalizerBandIndex = i;
            float equalizerCenterFreq = 0;
            final String equalizerCenterFreqHz;

            for(int index=0;index<ab.size();index++){
            	equalizerCenterFreq = mEqualizer[index].getCenterFreq(equalizerBandIndex);
            }

            /*
                이퀄라이저 Hz 텍스트뷰 셋팅
                #######################################################################
             */
            if (equalizerCenterFreq >= 1000000000) {
                equalizerCenterFreqHz = " mHz";
                equalizerCenterFreq = equalizerCenterFreq / 1000000000;
            }
            else if (equalizerCenterFreq >= 1000000) {
                equalizerCenterFreqHz = " kHz";
                equalizerCenterFreq = equalizerCenterFreq / 1000000;
            } else {
                equalizerCenterFreqHz = " Hz";
                equalizerCenterFreq = equalizerCenterFreq / 1000;
            }

            DecimalFormat df = new DecimalFormat("#.#");
            String frequencyHeader = df.format(equalizerCenterFreq) + equalizerCenterFreqHz;

            TextView frequencyHeaderTextView = new TextView(this);
            frequencyHeaderTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            frequencyHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextView.setText(frequencyHeader);
            frequencyHeaderTextView.setTextColor(Color.WHITE);

            /*
                최소 데시벨 텍스트뷰 셋팅
                #######################################################################
             */
            TextView lowerEqualizerBandLevelTextview = new TextView(this);
            lowerEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(250, 200));
            lowerEqualizerBandLevelTextview.setGravity(Gravity.CENTER);
            lowerEqualizerBandLevelTextview.setText((lowerEqualizerBandLevel / 100) + " dB\n\n" + frequencyHeader);
            lowerEqualizerBandLevelTextview.setTextColor(Color.WHITE);
            //  #######################################################################

            /*
                최대 데시벨 텍스트뷰 셋팅
                #######################################################################
             */
            TextView upperEqualizerBandLevelTextview = new TextView(this);
            upperEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(250, 80));
            upperEqualizerBandLevelTextview.setGravity(Gravity.CENTER);
            upperEqualizerBandLevelTextview.setText((upperEqualizerBandLevel / 100) + " dB");
            upperEqualizerBandLevelTextview.setTextColor(Color.WHITE);
            //  #######################################################################

            /*
                seekBar 셋팅
                #######################################################################
             */
            LinearLayout seekBarRowLayout = new LinearLayout(this);
            seekBarRowLayout.setOrientation(LinearLayout.HORIZONTAL);

            // seekBar 레이아웃
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;

            SeekBar seekBar = new SeekBar(this);
            seekBar.setId(i);

            seekBar.setLayoutParams(layoutParams);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
            seekBar.setProgress(mEqualizer[0].getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);

            seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.seekbar_progressbar));
            seekBar.setThumb(getResources().getDrawable(R.drawable.thumbler_small));
            seekBar.setThumbOffset(seekBar.getWidth()/4);
           
            if (Build.VERSION.SDK_INT >= 21) // 21 vesion 이후
            	seekBar.setSplitTrack(false);

            seekBars.add(seekBar);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.d("SeekBar", "BandIndex = " + equalizerBandIndex + ", Value = " + (progress + lowerEqualizerBandLevel));
                    for(int index = 0 ; index < ab.size(); index++)
                    	mEqualizer[index].setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            
            //  #######################################################################


            if (displayRotationCheck) {
                lowerEqualizerBandLevelTextview.setRotation(90);
                upperEqualizerBandLevelTextview.setRotation(90);
                seekBarRowLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        250));
            } else {
                mLinearLayout.addView(frequencyHeaderTextView);
                lowerEqualizerBandLevelTextview.setText((lowerEqualizerBandLevel / 100) + " dB");
                lowerEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                upperEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                seekBarRowLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        100));
            }

            seekBarRowLayout.addView(lowerEqualizerBandLevelTextview);
            seekBarRowLayout.addView(seekBar);
            seekBarRowLayout.addView(upperEqualizerBandLevelTextview);

            seekBarRowLayout.setGravity(Gravity.CENTER_VERTICAL);

            mLinearLayout.addView(seekBarRowLayout);
        }
        if (displayRotationCheck) {
            mLinearLayout.setRotation(270);
        }
        relativeLayoutWrap.addView(mLinearLayout);
        relativeLayoutEqual.addView(relativeLayoutWrap);
    }
}