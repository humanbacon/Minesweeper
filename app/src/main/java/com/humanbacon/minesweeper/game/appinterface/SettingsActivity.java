package com.humanbacon.minesweeper.game.appinterface;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Locale;
import java.util.Scanner;

import com.humanbacon.minesweeper.R;
import com.humanbacon.minesweeper.game.appinterface.MainActivity2.CheckBoxListener;
import com.humanbacon.minesweeper.game.appinterface.MainActivity2.SpinnerListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class SettingsActivity extends Activity {
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		setContentView(R.layout.activity_settings);
		this.overridePendingTransition(R.anim.r_to_l_enter, R.anim.r_to_l_leave);
		try {
			Scanner lastGameReader = new Scanner(new BufferedReader(new FileReader(new File(getFilesDir(), "last_classic_game"))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		initSettings();
		AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("B0556E71DA948701AF335C175A39735B")
        .build();
	    adView.loadAd(adRequest);
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
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    this.overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);		
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
