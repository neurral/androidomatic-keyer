/*
 * Copyright (C) 2011 Ben Collins-Sussman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.templaro.opsiz.aka;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AndroidomaticKeyerActivity extends Activity {
	
	private String TAG = "AndroidomaticKeyer";
	private Thread soundThread;
	private Button playButton;
	private EditText keyerEditText;
	private SeekBar speedBar;
	private TextView speedLabel;
	private SeekBar toneBar;
	private TextView toneLabel;
	private String activeMessage;  // eventually chosen from SQLite list
	private int hertz = 700;  // should be tweakable
	private int speed = 15;  // should be tweakable
	private MorsePlayer player = new MorsePlayer(hertz, speed);
	
	/* Memory slot buttons are hard coded at this point, but down the road,
	 * how about dropping the buttons, and just displaying the text in
	 * a growable listview, with ability to add/delete slots in an array?
	 */
	
	private Button memorySlotButton1;
	private TextView memorySlotTextView1;
	private Button memorySlotButton2;
	private TextView memorySlotTextView2;
	private Button memorySlotButton3;
	private TextView memorySlotTextView3;
	private Button memorySlotButton4;
	private TextView memorySlotTextView4;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        soundThread = new Thread(new Runnable() {
	            @Override
	            public void run() {
	        	   player.playMorse();
	            }
	        });
        
        playButton = (Button)findViewById(R.id.playButton);
        playButton.setOnClickListener(playButtonListener);
        
        memorySlotButton1 = (Button)findViewById(R.id.memorySlotButton1);
        memorySlotButton1.setOnClickListener(memorySlotButtonListener1);
        
        memorySlotButton2 = (Button)findViewById(R.id.memorySlotButton2);
        memorySlotButton2.setOnClickListener(memorySlotButtonListener2);
        
        memorySlotButton3 = (Button)findViewById(R.id.memorySlotButton3);
        memorySlotButton3.setOnClickListener(memorySlotButtonListener3);
        
        memorySlotButton4 = (Button)findViewById(R.id.memorySlotButton4);
        memorySlotButton4.setOnClickListener(memorySlotButtonListener4);
        
        memorySlotTextView1 = (TextView)findViewById(R.id.memorySlotTextView1);
        memorySlotTextView2 = (TextView)findViewById(R.id.memorySlotTextView2);
        memorySlotTextView3 = (TextView)findViewById(R.id.memorySlotTextView3);
        memorySlotTextView4 = (TextView)findViewById(R.id.memorySlotTextView4);
        
        keyerEditText = (EditText)findViewById(R.id.keyerEditText);
        
        speedBar = (SeekBar)findViewById(R.id.speedBar);
        speedLabel = (TextView)findViewById(R.id.speedLabel);
        speedBar.setOnSeekBarChangeListener(speedBarListener);
        speedBar.setMax(45);  // actually WPM speed range is 5-50.
        speedBar.setProgress(10);  // so the starting val here is 15 wpm.
        
        toneBar = (SeekBar)findViewById(R.id.toneBar);
        toneLabel = (TextView)findViewById(R.id.toneLabel);
        toneBar.setOnSeekBarChangeListener(toneBarListener);
        toneBar.setMax(1000);  // tone range is 500-1500
        toneBar.setProgress(200);  // so the starting val here is 700hz.
    }
    
    private OnClickListener playButtonListener = new OnClickListener() {
        public void onClick(View v) {
        	if (soundThread.isAlive()) {
        		stopMessage();
        	} else {
        		startMessage();
        	}
        }
    };
    
    private OnClickListener memorySlotButtonListener1 = new OnClickListener() {
    	public void onClick(View v) {
    		keyerEditText.setText(memorySlotTextView1.getText().toString());
        }
    };
    
    private OnClickListener memorySlotButtonListener2 = new OnClickListener() {
    	public void onClick(View v) {
    		keyerEditText.setText(memorySlotTextView2.getText().toString());
        }
    };
    
    private OnClickListener memorySlotButtonListener3 = new OnClickListener() {
    	public void onClick(View v) {
    		keyerEditText.setText(memorySlotTextView3.getText().toString());
        }
    };
    
    private OnClickListener memorySlotButtonListener4 = new OnClickListener() {
    	public void onClick(View v) {
    		keyerEditText.setText(memorySlotTextView4.getText().toString());
        }
    };
    
    private OnSeekBarChangeListener speedBarListener = new OnSeekBarChangeListener() {
    	private boolean was_playing = false;
    	
    	public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
    		speedLabel.setText(String.format("%d WPM", progress + 5));
    	}
    	
    	public void onStartTrackingTouch(SeekBar seekBar) {
    		if (soundThread.isAlive())
    			was_playing = true;
    		stopMessage();  // user has begun changing WPM; kill any current sound
    	}
    	
    	public void onStopTrackingTouch(SeekBar seekBar) {
    		if (was_playing) {
    			startMessage(); // user finished changing WPM; restart sound if necessary
    			was_playing = false;
    		}
    	}
    };
    
    
    private OnSeekBarChangeListener toneBarListener = new OnSeekBarChangeListener() {
    	private boolean was_playing = false;
    	
    	public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
    		toneLabel.setText(String.format("%d Hz", progress + 500));
    	}
    	
    	public void onStartTrackingTouch(SeekBar seekBar) {
    		if (soundThread.isAlive())
    			was_playing = true;
    		stopMessage();  // user has begun changing tone; kill any current sound
    	}
    	
    	public void onStopTrackingTouch(SeekBar seekBar) {
    		if (was_playing) {
    			startMessage(); // user finished changing tone; restart sound if necessary
    			was_playing = false;
    		}
    	}
    };
    
    
	// Play sound (infinite loop) on separate thread from main UI thread.
    void startMessage() {
    	if (soundThread.isAlive()) {
    		Log.i(TAG, "Trying to stop old thread first...");
    		stopMessage();
    	}
    	Log.i(TAG, "Starting morse thread with new message.");
    	player.setMessage(keyerEditText.getText().toString());
    	player.setSpeed(speedBar.getProgress() + 5);
    	player.setTone(toneBar.getProgress() + 500);
    	soundThread = new Thread(new Runnable() {
	           @Override
	            public void run() {
	        	   player.playMorse();
	            }
	        });
    	soundThread.start();
    	playButton.setText("STOP");
    }
    
    void stopMessage() {
    	if (soundThread.isAlive()) {
    		Log.i(TAG, "Stopping existing morse thread.");
    		soundThread.interrupt();
    		try {
    			soundThread.join();  // wait for thread to die
    		} catch (InterruptedException e) {
    			Log.i(TAG, "Main thread interrupted while waiting for child to die!");
    		}
    	}
    	playButton.setText("START");
    }
}


