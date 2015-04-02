package com.humanbacon.minesweeper.game.appinterface.classic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.humanbacon.minesweeper.R;
import com.humanbacon.minesweeper.game.appinterface.HelpActivity;
import com.humanbacon.minesweeper.game.appinterface.SettingsActivity;
import com.humanbacon.minesweeper.game.appinterface.StatActivity;
import com.humanbacon.VerticalSeekBar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GameBoardActivity extends Activity {

	public static final int PORTRAIT = 1;
	public static final int LANDSCAPE = 2;
	private static final int EN = 0;
	private static final int zh_TW = 1;
	private int orientation;

	private DisplayMetrics metrics;
	private SharedPreferences settings;

	boolean pause = true;
	boolean enableQuestion;
	boolean enableFlagVibration;
	boolean enableLoseVibration;
	private int primaryControl;
	private int secondaryControl;
	private int defaultZoomLevel;
	private int assistantLevel;
	private int language;

	private LinearLayout.LayoutParams params;
	private LinearLayout ll;
	private BoardInterface boardView;
	private TopBar topBar;
	private BottomBar bottomBar;
	private LinearLayout boardWithScrollBars;
	private LinearLayout boardWithVerticalScrollBar;
	protected HorizontalScrollBar horizontalScrollBar;
	protected VerticalScrollBar verticalScrollBar;
	private Button resumeButton;	

	private float barPadding;

	private boolean continueLastGame = false;
	private int mineNo = 10;
	private int width = 8;
	private int height = 8;
	int remainingMineNo;
	int elapsedTime;


	private float minScaleFactor;
	private float maxScaleFactor;

	private boolean changeBack;

	private File lastGame;
	private FileOutputStream lastGameWriter;
	
	int density;
	
	String gameType;

	//initialize settings
	private void initSettings(){
		settings = getSharedPreferences("settings", MODE_PRIVATE);
		enableQuestion = settings.getBoolean("enable_question", false);
		enableFlagVibration = settings.getBoolean("enable_flag_vibration", true);
		enableLoseVibration = settings.getBoolean("enable_lose_vibration", false);
		primaryControl = settings.getInt("primary_control", 0);
		secondaryControl = settings.getInt("secondary_control", 1);
		defaultZoomLevel = settings.getInt("default_zoom_level", 0);
		assistantLevel = settings.getInt("assistant_level", 1);
		language = settings.getInt("language", -1);
	}
	
	private void onChangeOrientation(){	
		if(orientation == PORTRAIT){
			ll.setOrientation(LinearLayout.VERTICAL);

			topBar.setOrientation(LinearLayout.HORIZONTAL);
			topBar.setGravity(Gravity.CENTER_VERTICAL);			
			topBar.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (2 * barPadding)));
									
			bottomBar.setOrientation(LinearLayout.HORIZONTAL);
			bottomBar.setGravity(Gravity.CENTER_VERTICAL);
			bottomBar.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) barPadding));
			bottomBar.switchZoomBar();


			boardWithScrollBars.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
			resumeButton.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

		}else if(orientation == LANDSCAPE){			
			ll.setOrientation(LinearLayout.HORIZONTAL);
			topBar.setOrientation(LinearLayout.VERTICAL);
			topBar.setGravity(Gravity.CENTER_HORIZONTAL);
			topBar.setLayoutParams(new LayoutParams((int) (2 * barPadding), LinearLayout.LayoutParams.MATCH_PARENT));

			bottomBar.setOrientation(LinearLayout.VERTICAL);
			bottomBar.setGravity(Gravity.CENTER_HORIZONTAL);
			bottomBar.setLayoutParams(new LayoutParams((int) barPadding, LinearLayout.LayoutParams.MATCH_PARENT));
			bottomBar.switchZoomBar();

			boardWithScrollBars.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
			resumeButton.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
		}
		
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) boardView.getLayoutParams();
		params.setMargins(10 * density, 10 * density, 0, 0);
		boardView.requestLayout();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		orientation = getResources().getConfiguration().orientation;
		onChangeOrientation();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//different initialization
		initSettings();				
		metrics = getResources().getDisplayMetrics();
	    android.content.res.Configuration conf = getResources().getConfiguration();
		switch(language){
		case EN:
			conf.locale = new Locale("en");
		    getResources().updateConfiguration(conf, metrics);	
			break;
		case zh_TW:
			conf.locale = new Locale("zh", "TW");
		    getResources().updateConfiguration(conf, metrics);
			break;
		default:
			
			break;
		}
		density = (int) ((int) metrics.density + 0.5f);
		orientation = getResources().getConfiguration().orientation;
		barPadding = 36 * density;
		continueLastGame = getIntent().getExtras().getBoolean("continue");		
		if(continueLastGame){
			try{
				gameType = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(new File(getFilesDir(), "last_classic_game"))))).next();
			}catch(FileNotFoundException e) {
				// TODO Auto-generated catch block			
				e.printStackTrace();
			}catch(IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();			
			}
					
		}else{
			mineNo = getIntent().getExtras().getInt("mine_no");
			width = getIntent().getExtras().getInt("width");
			height = getIntent().getExtras().getInt("height");
			gameType = getIntent().getExtras().getString("game_type");			
		}
		ll = new LinearLayout(this);		
		topBar = new TopBar(this);		
		bottomBar = new BottomBar(this);		
		resumeButton = new Button(this);
		boardWithScrollBars = new TableLayout(this);
		if(continueLastGame){			
			boardView = new BoardInterface(this, topBar, bottomBar);
			
		}else{			
			boardView = new BoardInterface(this, mineNo, width, height, topBar, bottomBar);		
		}
		boardWithScrollBars = new LinearLayout(this);
		boardWithVerticalScrollBar = new LinearLayout(this);
		horizontalScrollBar = new HorizontalScrollBar(this);
		verticalScrollBar = new VerticalScrollBar(this);
		
		//set different parameters
		ll.setBackgroundResource(R.drawable.board_background);
		
		boardView.setPrimaryControl(primaryControl);		
		boardView.setSecondaryControl(secondaryControl);	
		boardView.setDefaultZoomLevel(defaultZoomLevel);
		boardView.setAssistantLevel(assistantLevel);
		boardView.setEnableFlagVibration(enableFlagVibration);
		boardView.setEnableLoseVibration(enableLoseVibration);			
		if(!continueLastGame)
			boardView.setGameType(gameType);
		resumeButton.setText(R.string.resume_button_text);
		resumeButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				if(pause)
					togglePause();
			}
		});
		
		boardWithVerticalScrollBar.setOrientation(LinearLayout.HORIZONTAL);
		verticalScrollBar.setLayoutParams(new LayoutParams(10 * density, LinearLayout.LayoutParams.MATCH_PARENT));
		boardView.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));		
		boardWithVerticalScrollBar.addView(boardView, 0);
		boardWithVerticalScrollBar.addView(verticalScrollBar, 1);
		
		boardWithScrollBars.setOrientation(LinearLayout.VERTICAL);
		horizontalScrollBar.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 10 * density));		
		boardWithVerticalScrollBar.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
		boardWithScrollBars.addView(boardWithVerticalScrollBar, 0);
		boardWithScrollBars.addView(horizontalScrollBar, 1);
		
		ll.addView(topBar, 0);
		ll.addView(boardWithScrollBars, 1);
		ll.addView(bottomBar, 2);								
						
		setContentView(ll);
		//called to initialize the initial display according to screen orientation
		onChangeOrientation();
		
		this.overridePendingTransition(R.anim.r_to_l_enter, R.anim.r_to_l_leave);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);;
		SharedPreferences.Editor editor = settings.edit();		
		BufferedWriter hehe;
		try {
			if(continueLastGame){
				gameType = boardView.getGameType();
			}
			lastGame = new File(getFilesDir(), "last_classic_game");
			if(!lastGame.exists()){
				lastGame.createNewFile();
			}			
			lastGameWriter = openFileOutput("last_classic_game", Context.MODE_PRIVATE);
			lastGameWriter.write((gameType + " ").getBytes());
			lastGameWriter.write((String.valueOf(boardView.getMineNo()) + " ").getBytes());
			lastGameWriter.write((String.valueOf(boardView.getBoardWidth()) + " ").getBytes());
			lastGameWriter.write((String.valueOf(boardView.getBoardHeight()) + " ").getBytes());
			lastGameWriter.write((String.valueOf(remainingMineNo) + " ").getBytes());
			lastGameWriter.write((String.valueOf(elapsedTime) + " ").getBytes());			
			for(int i = 0; i < boardView.getBoardHeight(); i++){
				for(int j = 0; j < boardView.getBoardWidth(); j++){
					lastGameWriter.write((String.valueOf(boardView.getGame().getCellState(i, j)) + " ").getBytes());
					lastGameWriter.write((String.valueOf(boardView.getGame().getCellContent(i, j)) + " ").getBytes());
				}
			}	    	 	
			lastGameWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!boardView.getGame().getWin() && !boardView.getGame().getLose() && boardView.getGameStarted()){
			editor.putBoolean("continue_classic", true);
		}else{
			editor.putBoolean("continue_classic", false);			
		}
		editor.commit();
		//findViewById(R.id.new_classic_game_container).invalidate();
		this.overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(!pause)
			togglePause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initSettings();
		if(boardView != null){
			boardView.setPrimaryControl(primaryControl);		
			boardView.setSecondaryControl(secondaryControl);	
			boardView.setDefaultZoomLevel(defaultZoomLevel);
			boardView.setAssistantLevel(assistantLevel);
			boardView.setEnableFlagVibration(enableFlagVibration);
			boardView.setEnableLoseVibration(enableLoseVibration);	
		}
		if(bottomBar != null){
			bottomBar.setQuestionVisible(enableQuestion);
		}
		
	}

	public void togglePause(){
		int boardWithScrollBarsIndex = ll.indexOfChild(boardWithScrollBars);
		int pauseButtonIndex = ll.indexOfChild(resumeButton);
		if(pause){
			if(pauseButtonIndex != -1){
				ll.removeView(resumeButton);
				ll.addView(boardWithScrollBars, 1);
				topBar.resumeTimer();
			}
		}else{
			if(boardWithScrollBarsIndex != -1){
				ll.removeView(boardWithScrollBars);
				ll.addView(resumeButton, 1);
				topBar.pauseTimer();
			}    		
		}
		pause = !pause;
	}

	public class TopBar extends LinearLayout{
		LinearLayout remainingMines;
		ImageButton startNewGame;
		LinearLayout timerView;
		private ImageView mineDigits[];
		private ImageView timeDigits[];		
		Timer timer;
		Typeface topBarFont;
		TimerTask timerTask;
		boolean timerStarted;
		int viewWidth;
		int viewHeight;
		Bitmap faceBitmap;
		Bitmap digits[];

		public TopBar(Context context) {					
			super(context);						
			remainingMines =  new LinearLayout(this.getContext());
			startNewGame = new ImageButton(this.getContext());
			timerView = new LinearLayout(this.getContext());
			digits = new Bitmap[11];
			mineDigits = new ImageView[4];
			timeDigits = new ImageView[4];
			if(gameType.equals("beginner")){
				faceBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.beginner_face), (int) (barPadding * 2), (int) (barPadding * 2), true);					
			}else if(gameType.equals("intermediate")){
				faceBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.intermediate_face), (int) (barPadding * 2), (int) (barPadding * 2), true);					
			}else{
				faceBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expert_face), (int) (barPadding * 2), (int) (barPadding * 2), true);					
			}
			remainingMines.setOrientation(LinearLayout.HORIZONTAL);
			timerView.setOrientation(LinearLayout.HORIZONTAL);
			for(int i = 0; i < 4; i++){
				mineDigits[i] = new ImageView(getContext());
				mineDigits[i].setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.25f));					
				remainingMines.addView(mineDigits[i], i);				
				timeDigits[i] = new ImageView(getContext());
				timeDigits[i].setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.25f));					
				timerView.addView(timeDigits[i], i);
			}
			
			
			
			timer = new Timer();
			topBarFont = Typeface.createFromAsset(getAssets(), "Clubland.ttf");
			elapsedTime = 0;
			timerStarted = false;
			remainingMineNo = mineNo;

			this.setGravity(Gravity.CENTER);
			this.setBackgroundColor(0x55ffffff);
			startNewGame.setBackgroundColor(Color.TRANSPARENT);
			startNewGame.setImageBitmap(faceBitmap);
			startNewGame.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					boardView.startNewGame();
				}
			});
			startNewGame.setLayoutParams(new LayoutParams((int) (barPadding * 2), (int) (barPadding * 2)));
			
			if(orientation == PORTRAIT){
				remainingMines.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.33f));				
				timerView.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.33f));
			}else if(orientation == LANDSCAPE){
				remainingMines.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.33f));
				timerView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.33f));
			}

			this.addView(remainingMines, 0);
			this.addView(startNewGame, 1);
			this.addView(timerView, 2);					
		}	

		protected void onSizeChanged (int w, int h, int oldw, int oldh){
			if(orientation == PORTRAIT){				
				digits[0] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_0)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[1] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_1)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[2] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_2)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[3] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_3)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[4] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_4)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[5] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_5)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[6] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_6)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[7] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_7)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[8] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_8)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
				digits[9] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_9)), (int) ((w - barPadding * 2 * density) / 4), (int) ((w - barPadding * 2 * density) / 4 * 29 / 23), true);
			}else if(orientation == LANDSCAPE){
				digits[0] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_0)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[1] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_1)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[2] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_2)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[3] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_3)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[4] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_4)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[5] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_5)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[6] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_6)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[7] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_7)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[8] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_8)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
				digits[9] = Bitmap.createScaledBitmap(Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.digit_9)), (int) (barPadding * density / 4), (int) (barPadding * density / 4 * 29 / 23), true);
			}
			setRemainingMineNo(remainingMineNo);
			setTime(elapsedTime / 100);
		}
		
		public void invalidateScrollBars(){
			verticalScrollBar.invalidate();
			horizontalScrollBar.invalidate();
		}
		
		public void setRemainingMineNo(int mineNo){
			remainingMineNo = mineNo;
			if(mineNo <= 0){
				ImageView digit0 = (ImageView) remainingMines.getChildAt(3);				
				ImageView digit1 = (ImageView) remainingMines.getChildAt(2);				
				ImageView digit2 = (ImageView) remainingMines.getChildAt(1);				
				ImageView digit3 = (ImageView) remainingMines.getChildAt(0);
				digit0.setImageBitmap(digits[0]);
				digit1.setImageBitmap(digits[0]);
				digit2.setImageBitmap(digits[0]);
				digit3.setImageBitmap(digits[0]);
			}else if(mineNo < 9999){
				ImageView digit0 = (ImageView) remainingMines.getChildAt(3);				
				ImageView digit1 = (ImageView) remainingMines.getChildAt(2);				
				ImageView digit2 = (ImageView) remainingMines.getChildAt(1);				
				ImageView digit3 = (ImageView) remainingMines.getChildAt(0);
				digit0.setImageBitmap(digits[mineNo % 1000 % 100 % 10]);
				digit1.setImageBitmap(digits[mineNo % 1000 % 100 / 10]);
				digit2.setImageBitmap(digits[mineNo % 1000 / 100]);
				digit3.setImageBitmap(digits[mineNo / 1000]);
			}else{
				ImageView digit0 = (ImageView) remainingMines.getChildAt(3);				
				ImageView digit1 = (ImageView) remainingMines.getChildAt(2);				
				ImageView digit2 = (ImageView) remainingMines.getChildAt(1);				
				ImageView digit3 = (ImageView) remainingMines.getChildAt(0);
				digit0.setImageBitmap(digits[9]);
				digit1.setImageBitmap(digits[9]);
				digit2.setImageBitmap(digits[9]);
				digit3.setImageBitmap(digits[9]);
			}
		}
		
		public void setTime(int time){
			if(time <= 0){
				ImageView digit0 = (ImageView) timerView.getChildAt(3);				
				ImageView digit1 = (ImageView) timerView.getChildAt(2);				
				ImageView digit2 = (ImageView) timerView.getChildAt(1);				
				ImageView digit3 = (ImageView) timerView.getChildAt(0);
				digit0.setImageBitmap(digits[0]);
				digit1.setImageBitmap(digits[0]);
				digit2.setImageBitmap(digits[0]);
				digit3.setImageBitmap(digits[0]);
			}else if(time < 9999){
				ImageView digit0 = (ImageView) timerView.getChildAt(3);				
				ImageView digit1 = (ImageView) timerView.getChildAt(2);				
				ImageView digit2 = (ImageView) timerView.getChildAt(1);				
				ImageView digit3 = (ImageView) timerView.getChildAt(0);
				digit0.setImageBitmap(digits[time % 1000 % 100 % 10]);
				digit1.setImageBitmap(digits[time % 1000 % 100 / 10]);
				digit2.setImageBitmap(digits[time % 1000 / 100]);
				digit3.setImageBitmap(digits[time / 1000]);
			}else{
				ImageView digit0 = (ImageView) timerView.getChildAt(3);				
				ImageView digit1 = (ImageView) timerView.getChildAt(2);				
				ImageView digit2 = (ImageView) timerView.getChildAt(1);				
				ImageView digit3 = (ImageView) timerView.getChildAt(0);
				digit0.setImageBitmap(digits[9]);
				digit1.setImageBitmap(digits[9]);
				digit2.setImageBitmap(digits[9]);
				digit3.setImageBitmap(digits[9]);
			}
		}

		public void incRemainingMineNo(){
			remainingMineNo++;
		}

		public void startTimer(){    		
			timer = new Timer();
			timerTask = new TimerTask(){
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							setTime(elapsedTime / 100);
							elapsedTime++;
						}
					});
				}
			};
			timer.scheduleAtFixedRate(timerTask, 0, 10); 
			timerStarted = true;
			pause = false;
		}

		public void resumeTimer(){
			if(timerStarted){
				timer = new Timer();
				timerTask = new TimerTask(){
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							public void run() {
								setTime(elapsedTime / 100);
								elapsedTime++;
							}
						});
					}
				};
				timer.scheduleAtFixedRate(timerTask, 0, 10);
			}    		    		
		}

		public void pauseTimer(){
			timer.cancel();    		
		}

		public void stopTimer(){
			timer.cancel();
			elapsedTime = 0;
			setTime(elapsedTime / 100);
			timerStarted = false;
		}

		public void setTimer(int time){
			timer = new Timer();
			elapsedTime = time;
			timerTask = new TimerTask(){
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {							
							setTime(elapsedTime / 100);
							elapsedTime++;
						}
					});
				}
			};
			timer.scheduleAtFixedRate(timerTask, 0, 10); 
			timerStarted = true;
			pause = false;
		}

		public int getTime(){
			return elapsedTime;
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
			if(orientation == PORTRAIT){
				remainingMines.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.33f));
				timerView.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.33f));
			}else if(orientation == LANDSCAPE){
				remainingMines.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.33f));
				timerView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.33f));
			}
		}
	}

	public class BottomBar extends LinearLayout{
		ImageButton toggleControl;
		ImageButton question;
		ImageView zoomOut;
		SeekBar zoomBar;
		VerticalSeekBar verticalZoomBar;
		ImageView zoomIn;
		ImageButton menu;
		Bitmap flagBitmap;
		Bitmap mineBitmap;
		Bitmap qMarkFalseBitmap;
		Bitmap qMarkTrueBitmap;
		boolean reverseControl;
		boolean putQuestion;

		public BottomBar(Context context) {
			super(context);			
			params = new LayoutParams((int) barPadding, (int) barPadding); 
			toggleControl = new ImageButton(this.getContext());
			question = new ImageButton(this.getContext());
			zoomOut = new ImageView(this.getContext());
			zoomBar = new SeekBar(this.getContext());
			verticalZoomBar = new VerticalSeekBar(this.getContext());
			zoomIn = new ImageView(this.getContext());
			menu = new ImageButton(this.getContext());
			flagBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flag), (int) barPadding - 10, (int) barPadding - 10, true);					
			if(gameType.equals("beginner")){
				mineBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.beginner_mine), (int) barPadding - 10, (int) barPadding - 10, true);					
			}else if(gameType.equals("intermediate")){
				mineBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.intermediate_mine), (int) barPadding - 10, (int) barPadding - 10, true);					
			}else{
				mineBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expert_mine), (int) barPadding - 10, (int) barPadding - 10, true);					
			}
			qMarkFalseBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.q_mark_button_false), (int) barPadding - 10, (int) barPadding - 10, true);
			qMarkTrueBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.q_mark_button_true), (int) barPadding - 10, (int) barPadding - 10, true);			
			reverseControl = false;
			putQuestion = false;

			this.setBackgroundColor(0x55ffffff);
			toggleControl.setBackgroundColor(Color.TRANSPARENT);
			toggleControl.setImageBitmap(mineBitmap);
			toggleControl.setLayoutParams(params);												
			toggleControl.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					boardView.toggleControl();
					if(reverseControl){
						toggleControl.setImageBitmap(mineBitmap);						
					}else{
						toggleControl.setImageBitmap(flagBitmap);
					}
					reverseControl = !reverseControl;
				}
			});
			question.setBackgroundColor(Color.TRANSPARENT);
			question.setImageBitmap(qMarkFalseBitmap);
			question.setLayoutParams(params);
			question.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					boardView.toggleQuestion();
					if(putQuestion){
						question.setImageBitmap(qMarkFalseBitmap);
					}else{
						question.setImageBitmap(qMarkTrueBitmap);
					}
					putQuestion = !putQuestion;
				}				
			});			
			zoomOut.setImageResource(R.drawable.zoom_out_icon);
			zoomOut.setLayoutParams(params);
			zoomIn.setImageResource(R.drawable.zoom_in_icon);
			zoomIn.setLayoutParams(params);							        
			zoomBar.setMax(1000);
			zoomBar.setProgress(1);
			zoomBar.setVisibility(VISIBLE);
			zoomBar.setLayoutParams(new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
			zoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					changeBack = false;
				}				
				@Override				
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if(!changeBack){						
						minScaleFactor = boardView.getMinScaleFactor();
						maxScaleFactor = boardView.getMaxScaleFactor();
						boardView.setScaleFactor((maxScaleFactor - minScaleFactor) * progress / 1000 + minScaleFactor);
					}
				}
			});

			verticalZoomBar.setMax(1000);	        
			verticalZoomBar.setProgress(1);
			verticalZoomBar.setVisibility(VISIBLE);
			verticalZoomBar.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1.0f));
			verticalZoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					changeBack = false;
				}
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if(!changeBack){						
						minScaleFactor = boardView.getMinScaleFactor();
						maxScaleFactor = boardView.getMaxScaleFactor();
						boardView.setScaleFactor((maxScaleFactor - minScaleFactor) * progress / 1000 + minScaleFactor);
					}
				}
			});	        	        

			menu.setLayoutParams(params);
			menu.setBackgroundColor(Color.TRANSPARENT);
			menu.setImageResource(R.drawable.menu_icon);

			//menu for android > 2.3
			if(android.os.Build.VERSION.SDK_INT >= 11){
				menu.setOnClickListener(new OnClickListener(){
					@TargetApi(Build.VERSION_CODES.HONEYCOMB)
					@Override
					public void onClick(View view) {
						if(!pause)
							togglePause();
						PopupMenu popupMenu = new PopupMenu(getContext(), view);
						popupMenu.getMenuInflater().inflate(R.menu.classic_game_menu, popupMenu.getMenu());

						popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

							@TargetApi(Build.VERSION_CODES.HONEYCOMB)
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								// TODO Auto-generated method stub
								switch(item.getItemId()){
								/*case R.id.classic_game_menu_back:
									SharedPreferences settings = getSharedPreferences("settings", getContext().MODE_PRIVATE);;
									SharedPreferences.Editor editor = settings.edit();
									if(!boardView.getGame().getWin() && !boardView.getGame().getLose() && boardView.getGameStarted()){
										editor.putBoolean("continue_classic", true);
									}else{
										editor.putBoolean("continue_classic", false);			
									}
									editor.commit();
									finish();
									overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);*/
								case R.id.classic_game_menu_setting:
									Intent intent = new Intent(getContext(), SettingsActivity.class);		
									startActivity(intent);
									overridePendingTransition(R.anim.slide_to_left, R.anim.slide_to_left);									
									break;
								case R.id.classic_game_menu_help:
									Intent intent2 = new Intent(getContext(), HelpActivity.class);		
									startActivity(intent2);
									overridePendingTransition(R.anim.slide_to_left, R.anim.slide_to_left);
									break;
								}
								
								return false;
							}
						});
						popupMenu.show();						
					}
				});
			}
			this.addView(toggleControl, 0);
			this.addView(question, 1);
			this.addView(zoomOut, 2);
			this.addView(zoomBar, 3);	
			this.addView(zoomIn, 4);
			this.addView(menu, 5);
			if(!enableQuestion){
				question.setVisibility(GONE);
			}
		}

		public void setQuestionVisible(boolean visible){
			if(visible)
				question.setVisibility(VISIBLE);
			else{
				question.setVisibility(GONE);
			}
		}
		
		public void switchZoomBar(){
			if(orientation == PORTRAIT){
				this.removeView(this.getChildAt(3));
				zoomBar.setProgress(0);
				zoomOut.setImageResource(R.drawable.zoom_out_icon);
				zoomIn.setImageResource(R.drawable.zoom_in_icon);
				this.addView(zoomBar, 3);
			}else if(orientation == LANDSCAPE){
				this.removeView(this.getChildAt(3));
				verticalZoomBar.setProgressAndThumb(0);
				zoomIn.setImageResource(R.drawable.zoom_out_icon);
				zoomOut.setImageResource(R.drawable.zoom_in_icon);
				this.addView(verticalZoomBar, 3);
			}
		}

		public void setZoomBarProgress(int progress){
			changeBack = true;
			zoomBar.setProgress(progress);
			verticalZoomBar.setProgressAndThumb(progress);
		}

	}
	
	
	public class HorizontalScrollBar extends View{
		int viewWidth;
		int viewHeight;
		Paint mPaint = new Paint();
		public HorizontalScrollBar(Context context) {			
			super(context);
			this.setBackgroundColor(0x55ffffff);			
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(Color.parseColor("#3B474A"));
		}
		
		protected void onSizeChanged (int w, int h, int oldw, int oldh){
			viewWidth = w;
			viewHeight = h;
			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
			params.setMargins(10 * density, 0, 10 * density, 0);
			this.requestLayout();
		}		
		public void onDraw(Canvas canvas){		
			canvas.save();			
			float x = boardView.getOffsetX();
			float scale = boardView.getScaleFactor();
			float width = boardView.getBoardWidth();
			float horizontalScale = (viewWidth / (barPadding * width * scale + 2 * boardView.getLeftPadding()));
			float hihi = -(x - boardView.getLeftPadding()) * horizontalScale;	
			canvas.drawRect(hihi, viewHeight * 3 / 8, hihi + viewWidth * horizontalScale, viewHeight * 5 /8, mPaint);
			canvas.restore();
		}
		
	}
	
	public class VerticalScrollBar extends View{
		int viewWidth;
		int viewHeight;
		Paint mPaint = new Paint();
		public VerticalScrollBar(Context context) {
			super(context);
			this.setBackgroundColor(0x55ffffff);			
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setColor(Color.parseColor("#3B474A"));
		}
		
		protected void onSizeChanged (int w, int h, int oldw, int oldh){
			viewWidth = w;
			viewHeight = h;
			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
			params.setMargins(0, 10 * density, 0, 0);
			this.requestLayout();
		}
		
		public void onDraw(Canvas canvas){		
			canvas.save();
			float y = boardView.getOffsetY();
			float scale = boardView.getScaleFactor();
			float height = boardView.getBoardHeight();
			float verticalScale = (viewHeight / (barPadding * height * scale + 2 * boardView.getTopPadding()));
			float hihi = -(y - boardView.getTopPadding()) * verticalScale;			
			canvas.drawRect(viewWidth * 3 / 8, hihi, viewWidth * 5 / 8, hihi + viewHeight * verticalScale, mPaint);
			canvas.restore();
		}
		
	}

}
