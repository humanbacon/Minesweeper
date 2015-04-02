package com.humanbacon.minesweeper.game.appinterface;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Locale;

import com.humanbacon.minesweeper.R;
import com.humanbacon.minesweeper.game.appinterface.SettingsActivity.CheckBoxListener;
import com.humanbacon.minesweeper.game.appinterface.SettingsActivity.SpinnerListener;
import com.humanbacon.minesweeper.game.appinterface.classic.GameBoardActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity2 extends Activity {

	ViewAnimator animator;
	ViewAnimator statAnimator;
	int prevChild;
	private static final int SINGLE_TAB = 0;
	private static  final int LONG_PRESS = 1;
	private static final int DOUBLE_TAB = 2;
	private static final int WHOLE_BOARD = 0;
	private static final int SUITABLE = 1;
	private static final int MAXIMUM = 2;
	private static final int en = 0;
	private static final int zh_TW = 1;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private CheckBox checkBox;
	private CheckBoxListener checkBoxListener;
	private Spinner spinner;
	private ArrayAdapter<CharSequence> spinnerAdapter;
	private SpinnerListener spinnerListener;

	boolean enableQuestion;
	boolean enableFlagVibration;
	int primaryControl;
	int secondaryControl;
	int defaultZoomLevel;
	int assistantLevel;
	int language;

	ToastThread toastThread = new ToastThread();
	
	class ToastThread extends Thread {
		 
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            try {
            	Toast toast = Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.loading), Toast.LENGTH_SHORT);
        		toast.show();
            } catch (Exception e) {
                
            }
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		initSettings();		
		DisplayMetrics dm = getResources().getDisplayMetrics();
	    android.content.res.Configuration conf = getResources().getConfiguration();
		switch(language){
		case en:
			conf.locale = new Locale("en");
		    getResources().updateConfiguration(conf, dm);	
			break;
		case zh_TW:
			conf.locale = new Locale("zh", "TW");
		    getResources().updateConfiguration(conf, dm);
			break;
		}				
		animator = (ViewAnimator)findViewById(R.id.hihi);	
		statAnimator = (ViewAnimator)findViewById(R.id.stat_animator);
		
	}

	@Override
	protected void onResume() {
		super.onResume();	    
		SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
		if(!settings.getBoolean("continue_classic", false))
			findViewById(R.id.continue_classic).setVisibility(View.GONE);
		else
			findViewById(R.id.continue_classic).setVisibility(View.VISIBLE);
		//findViewById(R.id.new_classic_game_container).invalidate();
		//findViewById(R.id.continue_classic).invalidate();
		// Normal case behavior follows
	}

	@Override
	public void onBackPressed() {
		if(animator.getDisplayedChild() == 0){
			super.onBackPressed();
		}else{
			animator.setInAnimation(this, R.anim.l_to_r_enter);
			animator.setOutAnimation(this, R.anim.l_to_r_leave);
			animator.setDisplayedChild(prevChild);
			//animator.showPrevious();			
		}		
	}
	private long mLastClickTime = 0;
	//create a new activity to create a new game
	public void createNewGame(View view){
		if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
			return;
		}
		mLastClickTime = SystemClock.elapsedRealtime();
		if(animator.getDisplayedChild() != prevChild)
			prevChild = animator.getDisplayedChild();
		animator.setInAnimation(this, R.anim.r_to_l_enter);
		animator.setOutAnimation(this, R.anim.r_to_l_leave);
		animator.showNext();
	}

	//goto the setting page
	public void gotoSettings(View view){	
		if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
			return;
		}
		mLastClickTime = SystemClock.elapsedRealtime();
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("B0556E71DA948701AF335C175A39735B")
        .build();
	    adView.loadAd(adRequest);
	    
	    if(animator.getDisplayedChild() != prevChild)
	    	prevChild = animator.getDisplayedChild();			
		animator.setInAnimation(this, R.anim.r_to_l_enter);			
		animator.setOutAnimation(this, R.anim.r_to_l_leave);
		animator.setDisplayedChild(2);			
	}

	public void showStat(View view){		
		if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
			return;
		}
		mLastClickTime = SystemClock.elapsedRealtime();
		AdView adView = (AdView) findViewById(R.id.adView2);
		AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("B0556E71DA948701AF335C175A39735B")
        .build();
	    adView.loadAd(adRequest);
	    if(animator.getDisplayedChild() != prevChild)
	    	prevChild = animator.getDisplayedChild();	
		findViewById(R.id.beginner_stat).setBackgroundResource(R.drawable.highlight_button_selector);
		findViewById(R.id.intermediate_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
		findViewById(R.id.expert_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
		TextView statTextView = (TextView) findViewById(R.id.beginner_stat_content);
		String statText = getResources().getString(R.string.stat_1) + settings.getInt("beginner_times", 0) + getResources().getString(R.string.stat_2) + settings.getInt("beginner_win_times", 0) + getResources().getString(R.string.stat_3);
		int highScore = settings.getInt("beginner_high_score", -1);
		if(highScore != -1){
			statText += getResources().getString(R.string.stat_4) + highScore / 100.0f + getResources().getString(R.string.stat_5);
		}
		statTextView.setText(statText);
		animator.setInAnimation(this, R.anim.r_to_l_enter);			
		animator.setOutAnimation(this, R.anim.r_to_l_leave);
		animator.setDisplayedChild(3);	
		statAnimator.setDisplayedChild(0);
	}
	
	public void switchStat(View view){
		TextView statTextView;
		String statText;
		int highScore;
		switch(view.getId()){
		case R.id.beginner_stat:
			view.setBackgroundResource(R.drawable.highlight_button_selector);
			findViewById(R.id.intermediate_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
			findViewById(R.id.expert_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
			statTextView = (TextView) findViewById(R.id.beginner_stat_content);
			statText = getResources().getString(R.string.stat_1) + settings.getInt("beginner_times", 0) + getResources().getString(R.string.stat_2) + settings.getInt("beginner_win_times", 0) + getResources().getString(R.string.stat_3);
			highScore = settings.getInt("beginner_high_score", -1);
			if(highScore != -1){
				statText += getResources().getString(R.string.stat_4) + highScore / 100.0f + getResources().getString(R.string.stat_5);
			}
			statTextView.setText(statText);
			statAnimator.setDisplayedChild(0);
			break;
		case R.id.intermediate_stat:			
			view.setBackgroundResource(R.drawable.highlight_button_selector);
			findViewById(R.id.beginner_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
			findViewById(R.id.expert_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
			statTextView = (TextView) findViewById(R.id.intermediate_stat_content);
			statText = getResources().getString(R.string.stat_1) + settings.getInt("intermediate_times", 0) + getResources().getString(R.string.stat_2) + settings.getInt("intermediate_win_times", 0) + getResources().getString(R.string.stat_3);
			highScore = settings.getInt("intermediate_high_score", -1);
			if(highScore != -1){
				statText += getResources().getString(R.string.stat_4) + highScore / 100.0f + getResources().getString(R.string.stat_5);
			}
			statTextView.setText(statText);
			statAnimator.setDisplayedChild(1);
			break;
		case R.id.expert_stat:
			view.setBackgroundResource(R.drawable.highlight_button_selector);
			findViewById(R.id.intermediate_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
			findViewById(R.id.beginner_stat).setBackgroundResource(R.drawable.theme2_btn_default_normal_holo_light);
			statTextView = (TextView) findViewById(R.id.expert_stat_content);
			statText = getResources().getString(R.string.stat_1) + settings.getInt("expert_times", 0) + getResources().getString(R.string.stat_2) + settings.getInt("expert_win_times", 0) + getResources().getString(R.string.stat_3);
			highScore = settings.getInt("expert_high_score", -1);
			if(highScore != -1){
				statText += getResources().getString(R.string.stat_4) + highScore / 100.0f + getResources().getString(R.string.stat_5);
			}
			statTextView.setText(statText);
			statAnimator.setDisplayedChild(2);
			break;
		}
	}

	//close the app
	public void exit(View view){
		System.exit(0);
	}
	
	public void createClassicGameBoard(View view){
		toastThread.run();
		Intent intent = new Intent(this, GameBoardActivity.class);
		switch(view.getId()){
		case R.id.beginner:
			intent.putExtra("continue", false);
			intent.putExtra("mine_no", 10);
			intent.putExtra("width", 8);
			intent.putExtra("height", 8);
			intent.putExtra("game_type", "beginner");
			break;
		case R.id.intermediate:
			intent.putExtra("continue", false);
			intent.putExtra("mine_no", 40);
			intent.putExtra("width", 16);
			intent.putExtra("height", 16);
			intent.putExtra("game_type", "intermediate");
			break;
		case R.id.expert:
			intent.putExtra("continue", false);
			intent.putExtra("mine_no", 99);
			intent.putExtra("width", 30);
			intent.putExtra("height", 16);
			intent.putExtra("game_type", "expert");
			break;
		}
		startActivity(intent);
	}

	int width = 30;
	int height = 16;
	int mineNo = 99;
	int maxMineNo = 240;
	public void createCustomClassicGameBoard(View view){		


		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		View v = inflater.inflate(R.layout.new_custom_classic_game_dialog, null);
		final SeekBar widthBar = (SeekBar)v.findViewById(R.id.custom_width);
		final SeekBar heightBar = (SeekBar)v.findViewById(R.id.custom_height);
		final SeekBar mineBar = (SeekBar)v.findViewById(R.id.custom_mine_no);
		final TextView customWidthView = (TextView)v.findViewById(R.id.custom_width_text);
		final TextView customHeightView = (TextView)v.findViewById(R.id.custom_height_text);
		final TextView customMineNoView = (TextView)v.findViewById(R.id.custom_mine_no_text);
		final TextView maxMineNoView = (TextView)v.findViewById(R.id.max_mine_no);
		DialogInterface.OnClickListener confirm = new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){				
				width = widthBar.getProgress();
				height = heightBar.getProgress();
				mineNo = mineBar.getProgress();
				hihi();	
			}
		};
		DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		};	
		builder.setView(v)
		.setPositiveButton(R.string.confirm, confirm)
		.setNegativeButton(R.string.cancel, cancel)
		.setTitle(R.string.new_custom_game);
		widthBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(progress <= 4){
					progress = 5;
					widthBar.setProgress(progress);
				}
				width = progress;
				maxMineNo = width * height / 2;
				customWidthView.setText(String.valueOf(width));
				maxMineNoView.setText(String.valueOf(maxMineNo));
				mineBar.setMax(maxMineNo);
			}
		});
		heightBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(progress <= 4){
					progress = 5;
					heightBar.setProgress(progress);
				}
				height = progress;
				maxMineNo = width * height / 2;
				customHeightView.setText(String.valueOf(height));
				maxMineNoView.setText(String.valueOf(maxMineNo));
				mineBar.setMax(maxMineNo);
			}
		});
		mineBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(progress <= 0){
					progress = 1;
					mineBar.setProgress(progress);
				}
				mineNo = progress;
				customMineNoView.setText(String.valueOf(mineNo));
			}
		});				

		builder.create();
		builder.show();						
	}

	public void hihi(){
		toastThread.run();
		Intent intent = new Intent(this, GameBoardActivity.class);
		intent.putExtra("continue", false);
		intent.putExtra("mine_no", mineNo);
		intent.putExtra("width", width);
		intent.putExtra("height", height);
		intent.putExtra("game_type", "custom");
		startActivity(intent);
	}

	public void continueClassicGame(View view){
		toastThread.run();
		Intent intent = new Intent(this, GameBoardActivity.class);
		intent.putExtra("continue", true);
		intent.putExtra("mine_no", -1);
		intent.putExtra("width", -1);
		intent.putExtra("height", -1);
		intent.putExtra("game_type", "continue");
		startActivity(intent);
	}

	private void initSettings(){

		settings = getSharedPreferences("settings", MODE_PRIVATE);
		editor = settings.edit();

		checkBoxListener = new CheckBoxListener();					
		spinnerListener = new SpinnerListener();

		//check box
		checkBox = (CheckBox) findViewById(R.id.enable_question);		
		checkBox.setOnCheckedChangeListener(checkBoxListener);
		enableQuestion = settings.getBoolean("enable_question", false);
		if(enableQuestion){
			checkBox.toggle();
		}

		checkBox = (CheckBox) findViewById(R.id.enable_flag_vibration);		
		checkBox.setOnCheckedChangeListener(checkBoxListener);
		enableFlagVibration = settings.getBoolean("enable_flag_vibration", true);
		if(enableFlagVibration){
			checkBox.toggle();
		}

		//spinner
		spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.pref_control_array, android.R.layout.simple_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		

		spinner = (Spinner) findViewById(R.id.primary_control);		
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(spinnerListener);
		primaryControl = settings.getInt("primary_control", SINGLE_TAB);
		spinner.setSelection(primaryControl);

		spinner = (Spinner) findViewById(R.id.secondary_control);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(spinnerListener);
		secondaryControl = settings.getInt("secondary_control", LONG_PRESS);
		spinner.setSelection(secondaryControl);

		spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.pref_zoom_level_array, android.R.layout.simple_spinner_item);
		spinner = (Spinner) findViewById(R.id.default_zoom_level);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(spinnerListener);
		defaultZoomLevel = settings.getInt("default_zoom_level", WHOLE_BOARD);
		spinner.setSelection(defaultZoomLevel);

		spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.pref_assistant_level_array, android.R.layout.simple_spinner_item);
		spinner = (Spinner) findViewById(R.id.assistant_level);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(spinnerListener);
		assistantLevel = settings.getInt("assistant_level", 1);
		spinner.setSelection(assistantLevel);
				
		spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.pref_language_array, android.R.layout.simple_spinner_item);
		spinner = (Spinner) findViewById(R.id.language);
		spinner.setAdapter(spinnerAdapter);		
		spinner.setOnItemSelectedListener(spinnerListener);
		language = settings.getInt("language", -1);		
		if(getResources().getConfiguration().locale.toString().equals("zh_HK") || getResources().getConfiguration().locale.toString().equals("zh_TW")){
			spinner.setSelection(zh_TW);
		}else{
			spinner.setSelection(en);
		}
	}

	class CheckBoxListener implements CompoundButton.OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch(buttonView.getId()){
			case R.id.enable_question:
				editor.putBoolean("enable_question", buttonView.isChecked());				
				break;
			case R.id.enable_flag_vibration:
				editor.putBoolean("enable_flag_vibration", buttonView.isChecked());
			}			
			editor.commit();
		}
	}

	class SpinnerListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
			int oldPrimary = primaryControl;
			int oldSecondary = secondaryControl;
			switch(parent.getId()){
			case R.id.primary_control:
				editor.putInt("primary_control", position);
				primaryControl = position;
				//prevent primary and second controls being the same			
				if(primaryControl == secondaryControl){
					primaryControl = oldPrimary;
					Spinner spinner = (Spinner) findViewById(R.id.primary_control);
					spinner.setSelection(primaryControl);
					editor.putInt("primary_control", primaryControl);				
				}
				break;
			case R.id.secondary_control:
				editor.putInt("secondary_control", position);
				secondaryControl = position;
				//prevent primary and second controls being the same			
				if(primaryControl == secondaryControl){
					secondaryControl = oldSecondary;
					Spinner spinner = (Spinner) findViewById(R.id.secondary_control);
					spinner.setSelection(secondaryControl);
					editor.putInt("secondary_control", secondaryControl);				
				}
				break;
			case R.id.default_zoom_level:
				editor.putInt("default_zoom_level", position);
				defaultZoomLevel = position;
				break;
			case R.id.assistant_level:
				editor.putInt("assistant_level", position);
				assistantLevel = position; 
			case R.id.language:				
				editor.putInt("language", position);
				language = position;
				DisplayMetrics dm = getResources().getDisplayMetrics();
			    android.content.res.Configuration conf = getResources().getConfiguration();
			    switch(language){
				case en:
					conf.locale = new Locale("en");
				    getResources().updateConfiguration(conf, dm);	
					break;
				case zh_TW:
					conf.locale = new Locale("zh", "TW");
				    getResources().updateConfiguration(conf, dm);
					break;
				default:
					
					break;
				}		
			}
			editor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent){
			// TODO Auto-generated method stub

		}
	}


}
