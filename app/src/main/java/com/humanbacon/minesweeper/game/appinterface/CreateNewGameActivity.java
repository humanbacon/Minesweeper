package com.humanbacon.minesweeper.game.appinterface;

import com.humanbacon.minesweeper.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CreateNewGameActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_game);
		this.overridePendingTransition(R.anim.r_to_l_enter, R.anim.r_to_l_leave);
	}

	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    this.overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);
	}
	
	//create a new activity to create a new classic game
	public void createNewClassicGame(View view){
		Intent intent = new Intent(this, NewClassicGameActivity.class);
		startActivity(intent);
	}

}
