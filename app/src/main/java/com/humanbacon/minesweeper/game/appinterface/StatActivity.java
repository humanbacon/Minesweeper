package com.humanbacon.minesweeper.game.appinterface;

import com.humanbacon.minesweeper.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(new StatView(this));
		setContentView(R.layout.activity_stats);
		this.overridePendingTransition(R.anim.r_to_l_enter, R.anim.r_to_l_leave);
	}

	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    this.overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);
	}
	
	public class StatView extends LinearLayout{
		SharedPreferences settings = getSharedPreferences("settings", getContext().MODE_PRIVATE);;
		SharedPreferences.Editor editor = settings.edit();
		TextView beginnerTimes;
		TextView beginnerWinTimes;
		TextView beginnerLoseTimes;
		TextView intermediateTimes;
		TextView intermediateWinTimes;
		TextView intermediateLoseTimes;
		TextView expertTimes;
		TextView expertWinTimes;
		TextView expertLoseTimes;
		TextView beginnerHighScore;
		TextView intermediateHighScore;
		TextView expertHighScore;
		
		public StatView(Context context) {			
			super(context);
			this.setOrientation(LinearLayout.VERTICAL);
			beginnerTimes = new TextView(getContext());
			beginnerWinTimes = new TextView(getContext());
			beginnerLoseTimes = new TextView(getContext());
			intermediateTimes = new TextView(getContext());
			intermediateWinTimes = new TextView(getContext());
			intermediateLoseTimes = new TextView(getContext());
			expertTimes = new TextView(getContext());
			expertWinTimes = new TextView(getContext());
			expertLoseTimes = new TextView(getContext());	
			beginnerHighScore = new TextView(getContext());	
			intermediateHighScore = new TextView(getContext());	
			expertHighScore = new TextView(getContext());
			this.addView(beginnerTimes, 0);
			this.addView(beginnerWinTimes, 1);
			this.addView(beginnerLoseTimes, 2);
			this.addView(intermediateTimes, 3);
			this.addView(intermediateWinTimes, 4);
			this.addView(intermediateLoseTimes, 5);
			this.addView(expertTimes, 6);
			this.addView(expertWinTimes, 7);
			this.addView(expertLoseTimes, 8);
			this.addView(beginnerHighScore, 9);
			this.addView(intermediateHighScore, 10);
			this.addView(expertHighScore, 11);
			beginnerTimes.setText("Beginner play: " + settings.getInt("beginner_times", 0));
			beginnerWinTimes.setText("Beginner win: " + settings.getInt("beginner_win_times", 0));
			beginnerLoseTimes.setText("Beginner lose: " + settings.getInt("beginner_lose_times", 0));
			intermediateTimes.setText("Intermediate play: " + settings.getInt("intermediate_times", 0));
			intermediateWinTimes.setText("Intermediate win: " + settings.getInt("intermediate_win_times", 0));
			intermediateLoseTimes.setText("Intermediate lose: " + settings.getInt("intermediate_lose_times", 0));
			expertTimes.setText("Expert play: " + settings.getInt("expert_times", 0));
			expertWinTimes.setText("Expert win: " + settings.getInt("expert_win_times", 0));
			expertLoseTimes.setText("Expert lose: " + settings.getInt("expert_lose_times", 0));
			int hi = settings.getInt("beginner_high_score", -1);
			if(hi == -1){
				beginnerHighScore.setText("Beginner high score: No high score");
			}else{
				beginnerHighScore.setText("Beginner high score: " + (hi / 100.0f) + "s");
			}
			hi = settings.getInt("intermediate_high_score", -1);
			if(hi == -1){
				intermediateHighScore.setText("Intermediate high score: No high score");
			}else{
				intermediateHighScore.setText("Intermediate high score: " + (hi / 100.0f) + "s");
			}
			hi = settings.getInt("expert_high_score", -1);
			if(hi == -1){
				expertHighScore.setText("Expert high score: No high score");
			}else{
				expertHighScore.setText("Expert high score: " + (hi / 100.0f) + "s");
			}
		}		
		
	}
	
}
