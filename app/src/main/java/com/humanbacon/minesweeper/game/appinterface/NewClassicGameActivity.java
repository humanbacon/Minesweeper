package com.humanbacon.minesweeper.game.appinterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.humanbacon.minesweeper.R;
import com.humanbacon.minesweeper.game.appinterface.classic.GameBoardActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class NewClassicGameActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_classic_game);		
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.overridePendingTransition(R.anim.r_to_l_enter, R.anim.r_to_l_leave);
	}
	
	//protected void onRestart(){
		//startActivity(getIntent());
	//}
	
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
		super.onBackPressed();
		this.overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);
	}


	public void createClassicGameBoard(View view){
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
		DialogInterface.OnClickListener confirm = new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				hihi();				
			}
		};
		DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		};		

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		View v = inflater.inflate(R.layout.new_custom_classic_game_dialog, null);
		SeekBar widthBar = (SeekBar)v.findViewById(R.id.custom_width);
		SeekBar heightBar = (SeekBar)v.findViewById(R.id.custom_height);
		final SeekBar mineBar = (SeekBar)v.findViewById(R.id.custom_mine_no);
		final TextView customWidthView = (TextView)v.findViewById(R.id.custom_width_text);
		final TextView customHeightView = (TextView)v.findViewById(R.id.custom_height_text);
		final TextView customMineNoView = (TextView)v.findViewById(R.id.custom_mine_no_text);
		final TextView maxMineNoView = (TextView)v.findViewById(R.id.max_mine_no);
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
				if(progress < 3){
					progress = 3;
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
				if(progress < 3){
					progress = 3;
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
				}
				mineNo = progress;
				customMineNoView.setText(String.valueOf(mineNo));
			}
		});				
		
		builder.create();
		builder.show();						
	}
	
	public void hihi(){
		Intent intent = new Intent(this, GameBoardActivity.class);
		intent.putExtra("continue", false);
		intent.putExtra("mine_no", mineNo);
		intent.putExtra("width", width);
		intent.putExtra("height", height);
		intent.putExtra("game_type", "custom");
		startActivity(intent);
	}
	
	public void continueClassicGame(View view){
		Intent intent = new Intent(this, GameBoardActivity.class);
		intent.putExtra("continue", true);
		intent.putExtra("mine_no", -1);
		intent.putExtra("width", -1);
		intent.putExtra("height", -1);
		intent.putExtra("game_type", "continue");
		startActivity(intent);
	}

}
