package com.humanbacon.minesweeper.game.appinterface;

import com.humanbacon.minesweeper.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//this.overridePendingTransition(R.anim.r_to_l_enter, R.anim.r_to_l_leave);
	}
	
	//create a new activity to create a new game
	public void createNewGame(View view){
		Intent intent = new Intent(this, CreateNewGameActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_to_left, R.anim.slide_to_left);
	}
	
	//goto the setting page
	public void gotoSettings(View view){		
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_to_left, R.anim.slide_to_left);
	}
	
	public void showStat(View view){
		Intent intent = new Intent(this, StatActivity.class);		
		startActivity(intent);
		overridePendingTransition(R.anim.slide_to_left, R.anim.slide_to_left);
	}
	
	//close the app
	public void exit(View view){
		System.exit(0);
	}

}
