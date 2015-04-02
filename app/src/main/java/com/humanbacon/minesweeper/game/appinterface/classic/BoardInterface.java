package com.humanbacon.minesweeper.game.appinterface.classic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

import com.humanbacon.minesweeper.R;
import com.humanbacon.minesweeper.game.ClassicGame;
import com.humanbacon.minesweeper.game.appinterface.classic.GameBoardActivity.BottomBar;
import com.humanbacon.minesweeper.game.appinterface.classic.GameBoardActivity.TopBar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Vibrator;

import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * This view contains the canvas on which the game board is drawn.
 * @author yulapshun
 *
 */
public class BoardInterface extends View{
	//game
	ClassicGame game;
	
	//top and bottom bars
	private GameBoardActivity.TopBar topBar;
	private BottomBar bottomBar;
	
	//size of game board and no. of mines
	private int mineNo;
	private int width;
	private int height;
	private int noOfCellShown = 8;

	//size of the game view
	private float viewHeight;
	private float viewWidth;	

	//size(in px) of a cell, a cell is a square
	private float cellSize;

	//different gesture detectors
	private GestureDetectorCompat mGestureDetector;
	private ScaleGestureDetector mScaleGestureDetector;

	//paint
	private Rect rect;
	Bitmap flagBitmapOriginal;
	Bitmap mineBitmapOriginal;
	Bitmap qMarkBitmapOriginal;
	Bitmap flagBitmap;
	Bitmap mineBitmap;
	Bitmap qMarkBitmap;		
	private BitmapShader flagShader;
	private BitmapShader mineShader;
	private BitmapShader qMarkShader;
	private RadialGradient unclickCellGradient;
	private RadialGradient clickingCellGradient;
	private Paint mPaint = new Paint();
	private Paint mPaintUnclick = new Paint();
	private Paint mPaintKnown = new Paint();
	private Paint mPaintSurround = new Paint();
	private Paint mPaintClicking = new Paint();
	private Paint mPaintText = new Paint();
	private Paint mPaintFlag = new Paint();
	private Paint mPaintMine = new Paint();
	private Paint mPaintQMark = new Paint();
	

	//scaling and panning
	private Matrix canvasMatrix = new Matrix();
	private float[] canvasMatrixValues = new float[9];
	private float MIN_SCALE_FACTOR;
	private float MAX_SCALE_FACTOR;
	private float DEFAULT_SCALE_FACTOR;
	private float focusX;
	private float focusY;
	private float lastFocusX;
	private float lastFocusY;
	
	private float initLeftPadding;
	private float initTopPadding;
	private float leftPadding;
	private float topPadding;
	
	//array showing which flags are wrong
	boolean[][] wrongFlag;
	
	//first cell is clicked, gen board after this
	boolean isFirstClick;
	
	//control modes
	private static final int SINGLE_TAB = 0;
	private static final int LONG_PRESS = 1;
	private static final int DOUBLE_TAB = 2;
	private static final int WHOLE_BOARD = 0;
	private static final int SUITABLE = 1;
	private static final int MAXIMUM = 2;
	
	//settings
	private boolean enableFlagVibration;
	private boolean enableLoseVibration;
	private int primaryControl;
	private int secondaryControl;
	private int defaultZoomLevel;
	private int assistantLevel;
	
	private boolean clickToPutQuestion;
	
	private boolean showSurround = false;
	private int surroundX;
	private int surroundY;
	private boolean showTouching = false;
	private int touchingX;
	private int touchingY;
	
	private Vibrator vibrator;
	
	private RectF cellRect;	
	
	private File lastGame;
	private Scanner lastGameReader;	
	private boolean gameStarted;
	
	private DisplayMetrics metrics;
	private int density;
	
	private String gameType;
	
	/**
	 * Prevents the board go out of bounds when scaling and scrolling.
	 * @param x
	 * @param y
	 */
	private final void keepBoardInBounds(float x, float y){
		//if board will go outside left bound
		canvasMatrix.getValues(canvasMatrixValues);	    		
		if(canvasMatrixValues[2] - x <= viewWidth - cellSize * width * canvasMatrixValues[0]){
			canvasMatrixValues[2] = (viewWidth - cellSize * width * canvasMatrixValues[4]);
			canvasMatrix.setValues(canvasMatrixValues);
		}
		//if board will go outside right bound
		if(canvasMatrixValues[2] - x >= 0){
			canvasMatrixValues[2] = 0.0f;
			canvasMatrix.setValues(canvasMatrixValues);
		}
		//if board will go outside top bound
		if(canvasMatrixValues[5] - y <= viewHeight - cellSize * height * canvasMatrixValues[0]){
			canvasMatrixValues[5] = (viewHeight - cellSize * height * canvasMatrixValues[4]);
			canvasMatrix.setValues(canvasMatrixValues);
		}
		//if board will go outside bottom bound
		if(canvasMatrixValues[5] - y >= 0){
			canvasMatrixValues[5] = 0.0f;
			canvasMatrix.setValues(canvasMatrixValues);
		}				
		
	}
	
	/**
	 * Converts clicked x-coordinate to canvas x-coordinate
	 * @param event
	 * @return
	 */
	private int getBoardX(MotionEvent event){
		return (int) ((int) (event.getY() / canvasMatrixValues[4] + rect.top) / cellSize);
	}
	
	/**
	 * Converts clicked y-coordinate to canvas x-coordinate
	 * @param event
	 * @return
	 */
	private int getBoardY(MotionEvent event){
		return (int) ((int) (event.getX() / canvasMatrixValues[0] + rect.left) / cellSize);
	}
	
	private void centerBoard(){
		topPadding = Math.max((viewHeight - height * cellSize * canvasMatrixValues[4]) / 2, 0);
		leftPadding = Math.max((viewWidth - width * cellSize * canvasMatrixValues[0]) / 2, 0);
		canvasMatrix.postTranslate(leftPadding, topPadding);
		
		canvasMatrix.getValues(canvasMatrixValues);
	}
	
	private void checkWinLose(){
		Builder myAlertDialog = new AlertDialog.Builder(getContext());
		if(game.getLose()){
			topBar.pauseTimer();
			for(int i = 0; i < height; i++){
				for(int j = 0; j < width; j++){
					if(game.getCellState(i, j) == ClassicGame.FLAG && game.getCellContent(i, j) != ClassicGame.MINE){
						wrongFlag[i][j] = true;									
					}else if(game.getCellState(i, j) != ClassicGame.FLAG && game.getCellContent(i, j) == ClassicGame.MINE){
						game.setKnown(i, j);
						invalidate();
					}
				}
			}
			myAlertDialog.setTitle(R.string.lose_message);
			DialogInterface.OnClickListener newGame = new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					startNewGame();
				}
			};
			DialogInterface.OnClickListener back = new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					((Activity)getContext()).finish();
					((Activity)getContext()).overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);
				}
			};
			if(enableLoseVibration)
				vibrator.vibrate(800);
			SharedPreferences settings = getContext().getSharedPreferences("settings", getContext().MODE_PRIVATE);;
			SharedPreferences.Editor editor = settings.edit();
			if(!game.getWin() && !game.getLose() && getGameStarted()){
				editor.putBoolean("continue_classic", true);
			}else{
				editor.putBoolean("continue_classic", false);			
			}
			if(gameType.equals("beginner")){
				int times = settings.getInt("beginner_lose_times", 0);
				editor.putInt("beginner_lose_times", times + 1);
			}else if(gameType.equals("intermediate")){
				int times = settings.getInt("intermediate_lose_times", 0);
				editor.putInt("intermediate_lose_times", times + 1);
			}else if(gameType.equals("expert")){
				int times = settings.getInt("expert_lose_times", 0);
				editor.putInt("expert_lose_times", times + 1);
			}
			
			editor.commit();
			myAlertDialog.setPositiveButton(R.string.new_game, newGame);
			myAlertDialog.setNegativeButton(R.string.back, back);						
			myAlertDialog.show();			
			
		}else if(game.getWin()){
			topBar.pauseTimer();
			for(int i = 0; i < height; i++){
				for(int j = 0; j < width; j++){
					if(game.getCellState(i, j) != ClassicGame.FLAG && game.getCellContent(i, j) == ClassicGame.MINE){
						game.putFlag(i, j);
						invalidate();
					}
				}
			}
			myAlertDialog.setTitle(R.string.win_message);
			DialogInterface.OnClickListener newGame = new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					startNewGame();
				}
			};
			DialogInterface.OnClickListener back = new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					((Activity)getContext()).finish();
					((Activity)getContext()).overridePendingTransition(R.anim.l_to_r_enter, R.anim.l_to_r_leave);
				}
			};
			SharedPreferences settings = getContext().getSharedPreferences("settings", getContext().MODE_PRIVATE);;
			SharedPreferences.Editor editor = settings.edit();
			if(!game.getWin() && !game.getLose() && getGameStarted()){
				editor.putBoolean("continue_classic", true);
			}else{
				editor.putBoolean("continue_classic", false);			
			}
			if(gameType.equals("beginner")){
				int times = settings.getInt("beginner_win_times", 0);
				int score = settings.getInt("beginner_high_score", -1);
				if(score == -1 || topBar.getTime() < score){
					editor.putInt("beginner_high_score", topBar.getTime());	
				}
				editor.putInt("beginner_win_times", times + 1);
			}else if(gameType.equals("intermediate")){
				int times = settings.getInt("intermediate_win_times", 0);
				int score = settings.getInt("intermediate_high_score", -1);
				if(score == -1 || topBar.getTime() < score){
					editor.putInt("intermediate_high_score", topBar.getTime());
				}
				editor.putInt("intermediate_win_times", times + 1);
			}else if(gameType.equals("expert")){
				int times = settings.getInt("expert_win_times", 0);
				int score = settings.getInt("expert_high_score", -1);
				if(score == -1 || topBar.getTime() < score){
					editor.putInt("expert_high_score", topBar.getTime());
				}
				editor.putInt("expert_win_times", times + 1);				
			}
			editor.commit();
			myAlertDialog.setPositiveButton(R.string.new_game, newGame);
			myAlertDialog.setNegativeButton(R.string.back, back);			
			myAlertDialog.show();
		}
	}
	
	private void clickCell(int x, int y){
		//select which cell
		if(!game.getWin() && !game.getLose()){			
			if(x >= 0 && x < height && y >= 0 && y < width){
				gameStarted = true;
				if(game.getCellState(x, y) == ClassicGame.UNKNOWN || game.getCellState(x, y) == ClassicGame.QUESTION){
					if(isFirstClick){
						ClassicGame oldGame = game;
						game = new ClassicGame(mineNo, width, height, true, x, y);
						game.copyGameState(oldGame);
						isFirstClick = false;
						topBar.startTimer();
						
						SharedPreferences settings = getContext().getSharedPreferences("settings", getContext().MODE_PRIVATE);;
						SharedPreferences.Editor editor = settings.edit();
						if(gameType.equals("beginner")){
							int times = settings.getInt("beginner_times", 0);
							editor.putInt("beginner_times", times + 1);
						}else if(gameType.equals("intermediate")){
							int times = settings.getInt("intermediate_times", 0);
							editor.putInt("intermediate_times", times + 1);
						}else if(gameType.equals("expert")){
							int times = settings.getInt("expert_times", 0);
							editor.putInt("expert_times", times + 1);
						}
						editor.commit();
						
					}													
					game.selectCell(x, y);
					//expand(x, y);
					checkWinLose();	
					invalidate();
				}
			}
		}
		topBar.setRemainingMineNo(game.getRemainingMineNo());
		topBar.invalidate();
	}
	
	private void putFlag(int x, int y){
		//if(!isFirstClick){
			if(!game.getWin() && !game.getLose()){
				if(x >= 0 && x < height && y >= 0 && y < width){
					gameStarted = true;
					if(game.getCellState(x, y) == ClassicGame.UNKNOWN || game.getCellState(x, y) == ClassicGame.QUESTION){
						game.putFlag(x, y);
						if(enableFlagVibration)
							vibrator.vibrate(80);
					}else if(game.getCellState(x, y) == ClassicGame.FLAG)
						game.removeFlag(x, y);
					invalidate();					
				}
			}	
		//}
		topBar.setRemainingMineNo(game.getRemainingMineNo());
		topBar.invalidate();
	}
	
	private void putQuestion(int x, int y){
		//if(!isFirstClick){
			if(!game.getWin() && !game.getLose()){
				gameStarted = true;
				if(x >= 0 && x < height && y >= 0 && y < width){
					if(game.getCellState(x, y) == ClassicGame.UNKNOWN){
						game.putQuestion(x, y);
						invalidate();
					}else if(game.getCellState(x, y) == ClassicGame.QUESTION)
						game.removeQuestion(x, y);
					invalidate();
				}
			}	
		//}
	}

	private void openSurroundingCells(int x, int y){
		int surroundFlags;
		if(!game.getWin() && !game.getLose()){
			if(x >= 0 && x < height && y >= 0 && y < width){
				if(x == 0 && y == 0){
					//top-left
					surroundFlags = 0;
					if(game.getCellState(1, 0) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(0, 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(1, 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(1, 0) != ClassicGame.FLAG)
							game.selectCell(1, 0);
						if(game.getCellState(0, 1) != ClassicGame.FLAG)
							game.selectCell(0, 1);
						if(game.getCellState(1, 1) != ClassicGame.FLAG)
							game.selectCell(1, 1);
					}
				}else if(x == height - 1 && y == 0){
					//bottom-left
					surroundFlags = 0;
					if(game.getCellState(height - 2, 0) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 1, 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 2, 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(height - 2, 0) != ClassicGame.FLAG)
							game.selectCell(height - 2, 0);
						if(game.getCellState(height - 1, 1) != ClassicGame.FLAG)
							game.selectCell(height - 1, 1);
						if(game.getCellState(height - 2, 1) != ClassicGame.FLAG)
							game.selectCell(height - 2, 1);
					}
				}else if(x == 0 && y == width - 1){
					//top-right
					surroundFlags = 0;
					if(game.getCellState(0, width - 2) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(1, width - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(1, width - 2) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(0, width - 2) != ClassicGame.FLAG)
							game.selectCell(0, width - 2);
						if(game.getCellState(1, width - 1) != ClassicGame.FLAG)
							game.selectCell(1, width - 1);
						if(game.getCellState(1, width - 2) != ClassicGame.FLAG)
							game.selectCell(1, width - 2);
					}
				}else if(x == height - 1 && y == width - 1){
					//top-left
					surroundFlags = 0;
					if(game.getCellState(height - 2, width - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 1, width - 2) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 2, width - 2) == ClassicGame.FLAG)
						surroundFlags++;					
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(height - 2, width - 1) != ClassicGame.FLAG)
							game.selectCell(height - 2, width - 1);
						if(game.getCellState(height - 1, width - 2) != ClassicGame.FLAG)
							game.selectCell(height - 1, width - 2);
						if(game.getCellState(height - 2, width - 2) != ClassicGame.FLAG)
							game.selectCell(height - 2, width - 2);
					}
				}else if(x > 0 && x < height - 1 && y == 0){
					//left
					surroundFlags = 0;
					if(game.getCellState(x - 1, 0) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x + 1, 0) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x, 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x - 1, 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x + 1, 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(x - 1, 0) != ClassicGame.FLAG)
							game.selectCell(x - 1, 0);
						if(game.getCellState(x + 1, 0) != ClassicGame.FLAG)
							game.selectCell(x + 1, 0);
						if(game.getCellState(x, 1) != ClassicGame.FLAG)
							game.selectCell(x, 1);
						if(game.getCellState(x - 1, 1) != ClassicGame.FLAG)
							game.selectCell(x - 1, 1);
						if(game.getCellState(x + 1, 1) != ClassicGame.FLAG)
							game.selectCell(x + 1, 1);
					}
				}else if(x > 0 && x < height - 1 && y == width - 1){
					//right
					surroundFlags = 0;
					if(game.getCellState(x - 1, width - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x + 1, width - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x, width - 2) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x - 1, width - 2) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x + 1, width - 2) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(x - 1, width - 1) != ClassicGame.FLAG)
							game.selectCell(x - 1, width - 1);
						if(game.getCellState(x + 1, width - 1) != ClassicGame.FLAG)
							game.selectCell(x + 1, width - 1);
						if(game.getCellState(x, width - 2) != ClassicGame.FLAG)
							game.selectCell(x, width - 2);
						if(game.getCellState(x - 1, width - 2) != ClassicGame.FLAG)
							game.selectCell(x - 1, width - 2);
						if(game.getCellState(x + 1, width - 2) != ClassicGame.FLAG)
							game.selectCell(x + 1, width - 2);
					}
				}else if(x == 0 && y > 0 && y < width - 1){
					//top
					surroundFlags = 0;
					if(game.getCellState(0, y - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(0, y + 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(1, y) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(1, y - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(1, y + 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(0, y - 1) != ClassicGame.FLAG)
							game.selectCell(0, y - 1);
						if(game.getCellState(0, y + 1) != ClassicGame.FLAG)
							game.selectCell(0, y + 1);
						if(game.getCellState(1, y) != ClassicGame.FLAG)
							game.selectCell(1, y);
						if(game.getCellState(1, y - 1) != ClassicGame.FLAG)
							game.selectCell(1, y - 1);
						if(game.getCellState(1, y + 1) != ClassicGame.FLAG)
							game.selectCell(1, y + 1);
					}
				}else if(x == height - 1 && y > 0 && y < width - 1){
					//bottom
					surroundFlags = 0;
					if(game.getCellState(height - 1, y - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 1, y + 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 2, y) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 2, y - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(height - 2, y + 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(height - 1, y - 1) != ClassicGame.FLAG)
							game.selectCell(height - 1, y - 1);
						if(game.getCellState(height - 1, y + 1) != ClassicGame.FLAG)
							game.selectCell(height - 1, y + 1);
						if(game.getCellState(height - 2, y) != ClassicGame.FLAG)
							game.selectCell(height - 2, y);
						if(game.getCellState(height - 2, y - 1) != ClassicGame.FLAG)
							game.selectCell(height - 2, y - 1);
						if(game.getCellState(height - 2, y + 1) != ClassicGame.FLAG)
							game.selectCell(height - 2, y + 1);
					}
				}else{
					surroundFlags = 0;
					if(game.getCellState(x, y - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x, y + 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x - 1, y) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x + 1, y) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x - 1, y - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x - 1, y + 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x + 1, y - 1) == ClassicGame.FLAG)
						surroundFlags++;
					if(game.getCellState(x + 1, y + 1) == ClassicGame.FLAG)
						surroundFlags++;	
					if(game.getCellContent(x, y) == surroundFlags){
						if(game.getCellState(x, y - 1) != ClassicGame.FLAG)
							game.selectCell(x, y - 1);
						if(game.getCellState(x, y + 1) != ClassicGame.FLAG)
							game.selectCell(x, y + 1);
						if(game.getCellState(x - 1, y) != ClassicGame.FLAG)
							game.selectCell(x - 1, y );
						if(game.getCellState(x + 1, y) != ClassicGame.FLAG)
							game.selectCell(x + 1, y);
						if(game.getCellState(x - 1, y - 1) != ClassicGame.FLAG)
							game.selectCell(x - 1, y - 1);
						if(game.getCellState(x - 1, y + 1) != ClassicGame.FLAG)
							game.selectCell(x - 1, y + 1);
						if(game.getCellState(x + 1, y - 1) != ClassicGame.FLAG)
							game.selectCell(x + 1, y - 1);
						if(game.getCellState(x + 1, y + 1) != ClassicGame.FLAG){
							game.selectCell(x + 1, y + 1);
						}
							
					}
				}
				checkWinLose();	
				invalidate();	
			}
		}
	}
	
	private void showSurroundingCells(int x, int y){
		if(!game.getWin() && !game.getLose()){
			if(x >= 0 && x < height && y >= 0 && y < width){
				touchingX = x;
				touchingY = y;
				showTouching = true;
				invalidate();
			}
		}	
	}
	
	private void removeSurroundingCells(){
		showTouching = false;
		invalidate();
	}
	
	private void showTouchingCell(int x, int y){
		if(!game.getWin() && !game.getLose()){
			if(x >= 0 && x < height && y >= 0 && y < width){
				if(game.getCellState(x, y) == ClassicGame.KNOWN){
					surroundX = x;
					surroundY = y;
					showSurround = true;
					invalidate();
				}		
			}
		}	
	}
	
	public int getBoardWidth(){
		return width;
	}
	
	public int getBoardHeight(){
		return height;
	}
	
	public int getMineNo(){
		return mineNo;
	}
	
	public boolean getGameStarted(){
		return gameStarted;
	}
	
	private void removeTouchingCell(){
		showSurround = false;
		invalidate();
	}
	
	public float getMinScaleFactor(){
		return MIN_SCALE_FACTOR;
	}
	
	public float getMaxScaleFactor(){
		return MAX_SCALE_FACTOR;
	}
	
	public float getScaleFactor(){
		canvasMatrix.getValues(canvasMatrixValues);
		return canvasMatrixValues[0];
	}
	
	public Bitmap getFlagBitmap(){
		return flagBitmap;
	}
	
	public Bitmap getMineBitmap(){
		return mineBitmap;
	}
	
	public ClassicGame getGame(){
		return game;
	}
	
	public float getOffsetX(){
		canvasMatrix.getValues(canvasMatrixValues);
		return canvasMatrixValues[2];
	}
	
	public float getOffsetY(){
		canvasMatrix.getValues(canvasMatrixValues);
		return canvasMatrixValues[5];
	}
	
	public float getLeftPadding(){
		return leftPadding;
	}
	
	public float getTopPadding(){
		return topPadding;
	}
	
	public float getViewWidth(){
		return viewWidth;
	}
	
	public float getViewHeight(){
		return viewHeight;
	}
	
	public String getGameType(){
		return gameType;
	}
	
	public void setScaleFactor(float factor){
		focusX = viewWidth / 2;
		focusY = viewHeight / 2;
		
		canvasMatrix.postTranslate(-focusX, -focusY);
		canvasMatrix.postScale(factor / canvasMatrixValues[0], factor / canvasMatrixValues[4]);
		canvasMatrix.postTranslate(focusX, focusY);
		
		canvasMatrix.getValues(canvasMatrixValues);
				
		keepBoardInBounds(0.0f, 0.0f);
		
		lastFocusX = focusX;
		lastFocusY = focusY;	
		centerBoard();
		
		topBar.invalidateScrollBars();
		
		invalidate();
	}
	
	public void setEnableFlagVibration(boolean enable){
		enableFlagVibration = enable;
	}
	
	public void setEnableLoseVibration(boolean enable){
		enableLoseVibration = enable;
	}
	
	/**
	 * Set primary control
	 */
	public void setPrimaryControl(int control){
		primaryControl = control;
	}
	
	/**
	 * Set secondary control
	 */
	public void setSecondaryControl(int control){
		secondaryControl = control;
	}
	
	public void setDefaultZoomLevel(int level){
		defaultZoomLevel = level;
		switch(defaultZoomLevel){
		case WHOLE_BOARD:
			DEFAULT_SCALE_FACTOR = MIN_SCALE_FACTOR;
			break;
		case SUITABLE:
			DEFAULT_SCALE_FACTOR = Math.max(MIN_SCALE_FACTOR, Math.min(1.0f, MAX_SCALE_FACTOR));
			break;
		case MAXIMUM:
			DEFAULT_SCALE_FACTOR = MAX_SCALE_FACTOR;
			break;
		}
	}
	
	public void setAssistantLevel(int level){
		assistantLevel = level;
	}
	
	public void setGameType(String type){
		gameType = type;
	}
	
	/**
	 * Reverse primary and secondary control
	 */
	public void toggleControl(){
		int tmp = primaryControl;
		primaryControl = secondaryControl;
		secondaryControl = tmp;
	}		
	
	/**
	 * Click to set question or not
	 */
	public void toggleQuestion(){
		clickToPutQuestion = !clickToPutQuestion;
	}
	/**
	 * Initialize saved game
	 * @param context
	 * @param lastGame
	 * @param topBar
	 * @param bottomBar
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public BoardInterface(Context context, TopBar topBar, BottomBar bottomBar){		
		super(context);					
		metrics = getResources().getDisplayMetrics();
		density = (int) ((int) metrics.density + 0.5f);
		cellSize = 36 * density;
		lastGame = new File(getContext().getFilesDir(), "last_classic_game");				
		this.topBar = topBar;
		this.bottomBar = bottomBar;
		try{
			lastGameReader = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(lastGame))));			
		}catch(FileNotFoundException e) {
			// TODO Auto-generated catch block			
			e.printStackTrace();
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		gameType = lastGameReader.next();		
		mineNo = lastGameReader.nextInt();
		width = lastGameReader.nextInt();
		height = lastGameReader.nextInt();
		int cellState[][] = new int[height][width];
		int cellContent[][] = new int[height][width];
		int remainingMines = lastGameReader.nextInt();
		int time = lastGameReader.nextInt();
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				cellState[i][j] = lastGameReader.nextInt();
				cellContent[i][j] = lastGameReader.nextInt();
				
			}
		}
		topBar.setTimer(time);
		
		
		//setBackgroundColor(0xfff4ce9c);		
		
		mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
		mGestureDetector = new GestureDetectorCompat(context,new GestureListener());
		wrongFlag = new boolean[height][width];
		if(android.os.Build.VERSION.SDK_INT >= 19){
			mScaleGestureDetector.setQuickScaleEnabled(false);
		}				
		
		canvasMatrix = new Matrix();
		focusX = 0.0f;
		focusY = 0.0f;
		lastFocusX = 0.0f;
		lastFocusY = 0.0f;
		MIN_SCALE_FACTOR = 1.0f;
		MAX_SCALE_FACTOR = 5.0f;
		leftPadding = 0.0f;
		topPadding = 0.0f;
		clickToPutQuestion = false;
		vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		//startNewGame();
		game = new ClassicGame(mineNo, width, height, remainingMines, cellState, cellContent, false);
		for(boolean[] row: wrongFlag)
			Arrays.fill(row, false);
		
		isFirstClick = false;
		
		topBar.setRemainingMineNo(game.getRemainingMineNo());
		keepBoardInBounds(0.0f, 0.0f);
		gameStarted = true;
	}
	
	//initialize the game
	/**
	 * This constructor initialize required variables, 
	 * it is called one time only when the view is created.
	 * 
	 * @param context
	 * @param mineNo
	 * @param width
	 * @param height
	 * @param topBar
	 * @param bottomBar
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public BoardInterface(Context context, int mineNo, int width, int height, GameBoardActivity.TopBar topBar, GameBoardActivity.BottomBar bottomBar) {
		super(context);	
		metrics = getResources().getDisplayMetrics();
		density = (int) ((int) metrics.density + 0.5f);
		cellSize = 36 * density;
		this.mineNo = mineNo;
		this.width = width;
		this.height = height;			
		this.topBar = topBar;
		this.bottomBar = bottomBar;
		//flagBitmapOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.flag);
		//mineBitmapOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.mine);
		//qMarkBitmapOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.q_mark);

		//setBackgroundColor(0xfff4ce9c);		
				
		mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
		mGestureDetector = new GestureDetectorCompat(context,new GestureListener());
		wrongFlag = new boolean[height][width];
		if(android.os.Build.VERSION.SDK_INT >= 19){
			mScaleGestureDetector.setQuickScaleEnabled(false);
		}		
		
		canvasMatrix = new Matrix();
		focusX = 0.0f;
		focusY = 0.0f;
		lastFocusX = 0.0f;
		lastFocusY = 0.0f;
		MIN_SCALE_FACTOR = 1.0f;
		MAX_SCALE_FACTOR = 5.0f;
		leftPadding = 0.0f;
		topPadding = 0.0f;
		clickToPutQuestion = false;
		vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		//startNewGame();
		game = new ClassicGame(mineNo, width, height, true);
		for(boolean[] row: wrongFlag)
			Arrays.fill(row, false);
		
		isFirstClick = true;
		topBar.setRemainingMineNo(game.getRemainingMineNo());
		topBar.stopTimer();
		keepBoardInBounds(0.0f, 0.0f);
		gameStarted = false;				
		
	}
	
	//initialize a new game
	/**
	 * This method reset variable to their original values, 
	 * and transform the canvas to its original state, 
	 * it should be called when a new game is started.
	 */
	public void startNewGame(){		
		game = new ClassicGame(mineNo, width, height, true);
		for(boolean[] row: wrongFlag)
			Arrays.fill(row, false);
		
		isFirstClick = true;
		topBar.setRemainingMineNo(game.getRemainingMineNo());
		topBar.stopTimer();
		
		canvasMatrix.setScale(DEFAULT_SCALE_FACTOR, DEFAULT_SCALE_FACTOR);
		
		keepBoardInBounds(0.0f, 0.0f);
				
		bottomBar.setZoomBarProgress(1);
		
		//centerBoard
		canvasMatrix.postTranslate(initLeftPadding, initTopPadding);		
		gameStarted = false;
		invalidate();
	}
	
	//initialize different variables that require size of the view
	//this method will also be called when orientation is changed
	/**
	 * This method is called when the view is created,
	 * it initialize the height and width of the view.
	 * It is also called when user changes orientation,
	 * then the new height and width of the view are calculated.
	 * It also transforms the canvas to the correct state.
	 */
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh){		
		viewWidth = w;
		viewHeight = h;

		//set MIN_SCALE_FACTOR and MAX_SCALE_FAC_TOR
		//float old = MIN_SCALE_FACTOR;
		MIN_SCALE_FACTOR = Math.min(viewHeight / (height * cellSize), viewWidth / (width * cellSize));		
		MAX_SCALE_FACTOR = Math.min(viewHeight / (3 * cellSize), viewWidth / (3 * cellSize));
		switch(defaultZoomLevel){
		case WHOLE_BOARD:
			DEFAULT_SCALE_FACTOR = MIN_SCALE_FACTOR;
			break;
		case SUITABLE:
			DEFAULT_SCALE_FACTOR = Math.max(MIN_SCALE_FACTOR, Math.min(1.0f, MAX_SCALE_FACTOR));
			break;
		case MAXIMUM:
			DEFAULT_SCALE_FACTOR = MAX_SCALE_FACTOR;
			break;
		}
		
		
		//different gradients for cell shaders
		cellRect = new RectF(0, 0, cellSize, cellSize);
		unclickCellGradient  = new RadialGradient(cellSize / 2, cellSize / 2, cellSize * 1.2f, Color.parseColor("#fff4f6f5"), Color.parseColor("#ff92a0a3"), Shader.TileMode.MIRROR);
		clickingCellGradient  = new RadialGradient(cellSize / 2, cellSize / 2, cellSize * 1.2f, Color.parseColor("#88f4f6f5"), Color.parseColor("#8892a0a3"), Shader.TileMode.MIRROR);
		flagBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flag), (int) cellSize, (int) cellSize, true);
		flagShader = new BitmapShader(flagBitmap, TileMode.REPEAT, TileMode.REPEAT);
		if(gameType.equals("beginner")){
			mineBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.beginner_mine), (int) cellSize, (int) cellSize, true);					
		}else if(gameType.equals("intermediate")){
			mineBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.intermediate_mine), (int) cellSize, (int) cellSize, true);					
		}else{
			mineBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.expert_mine), (int) cellSize, (int) cellSize, true);					
		}
		mineShader = new BitmapShader(mineBitmap, TileMode.REPEAT, TileMode.REPEAT);
		qMarkBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.q_mark), (int) cellSize, (int) cellSize, true);		
		qMarkShader = new BitmapShader(qMarkBitmap, TileMode.REPEAT, TileMode.REPEAT);

		//some paints require cellSize to initialize so put in here
		mPaint.setStrokeWidth(5);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(cellSize);
		mPaint.setTextAlign(Paint.Align.CENTER);
		
		mPaintUnclick.setAntiAlias(true);
		mPaintUnclick.setStyle(Paint.Style.FILL);
		mPaintUnclick.setShader(unclickCellGradient);				
		
		mPaintKnown.setAntiAlias(true);
		mPaintKnown.setStyle(Paint.Style.FILL);
		mPaintKnown.setColor(Color.parseColor("#55aaaaaa"));
		
		mPaintClicking.setAntiAlias(true);
		mPaintClicking.setStyle(Paint.Style.FILL);
		mPaintClicking.setShader(clickingCellGradient);
		
		mPaintSurround.setAntiAlias(true);
		mPaintSurround.setStyle(Paint.Style.STROKE);		
		mPaintSurround.setStrokeWidth(5);
		mPaintSurround.setColor(Color.RED);

		mPaintText.setAntiAlias(true);
		mPaintText.setStyle(Paint.Style.FILL);
		mPaintText.setTextSize(cellSize / 1.25f);
		mPaintText.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "ka1.ttf"));
		mPaintText.setTextAlign(Paint.Align.CENTER);
		
		mPaintFlag.setAntiAlias(true);
		mPaintFlag.setStyle(Paint.Style.FILL);
		mPaintFlag.setShader(flagShader);
		//mPaintFlag.setColor(Color.BLUE);
		
		mPaintMine.setAntiAlias(true);
		mPaintMine.setStyle(Paint.Style.FILL);
		mPaintMine.setShader(mineShader);
		//mPaintMine.setColor(Color.BLUE);
		
		mPaintQMark.setAntiAlias(true);
		mPaintQMark.setStyle(Paint.Style.FILL);
		mPaintQMark.setShader(qMarkShader);
		
		canvasMatrix.setScale(DEFAULT_SCALE_FACTOR, DEFAULT_SCALE_FACTOR);
		
		//center the board
		canvasMatrix.getValues(canvasMatrixValues);
		initLeftPadding = Math.max((viewWidth - width * cellSize * DEFAULT_SCALE_FACTOR) / 2, 0);
		initTopPadding = Math.max((viewHeight - height * cellSize * DEFAULT_SCALE_FACTOR) / 2, 0);
		leftPadding = initLeftPadding;
		topPadding = initTopPadding;

		canvasMatrix.setValues(canvasMatrixValues);
		keepBoardInBounds(0, 0);
		canvasMatrix.postTranslate(leftPadding, topPadding);
	}

	
	/**
	 * Gets the correct scaleFactor and focus.
	 * @author yulapshun
	 *
	 */
	//handle scaling
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			lastFocusX = detector.getFocusX();
			lastFocusY = detector.getFocusY();
			return true;
		}		

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			//how much to scale and center for scaling
			//canvas is translated by scaling
			//fouscusShift translate canvas to proper position for a correct focus
			//Matrix transformationMatrix = new Matrix();
			float currentScaleFactor = detector.getScaleFactor();
			canvasMatrix.getValues(canvasMatrixValues);
			//prevent too large and too small scaling			
			if(canvasMatrixValues[0] * currentScaleFactor > MAX_SCALE_FACTOR || canvasMatrixValues[0] * currentScaleFactor < MIN_SCALE_FACTOR){
				canvasMatrixValues[0] = Math.max(MIN_SCALE_FACTOR, Math.min(canvasMatrixValues[0], MAX_SCALE_FACTOR));
				canvasMatrixValues[4] = canvasMatrixValues[0];
				currentScaleFactor = 1.0f;
				canvasMatrix.setValues(canvasMatrixValues);
			}
			focusX = detector.getFocusX();
			focusY = detector.getFocusY();
			canvasMatrix.postTranslate(-focusX, -focusY);
			canvasMatrix.postScale(currentScaleFactor, currentScaleFactor);
			float focusShiftX = focusX - lastFocusX;
			float focusShiftY = focusY - lastFocusY;
			canvasMatrix.postTranslate(focusX + focusShiftX, focusY + focusShiftY);
			keepBoardInBounds(0, 0);
			centerBoard();
			lastFocusX = focusX;
			lastFocusY = focusY;		    	       

			topBar.invalidateScrollBars();
			
			//repaint
			invalidate();
			canvasMatrix.getValues(canvasMatrixValues);						
			
			bottomBar.setZoomBarProgress((int) ((canvasMatrixValues[0] - MIN_SCALE_FACTOR) * 1000 / (MAX_SCALE_FACTOR - MIN_SCALE_FACTOR)));
			
			return true;
		}
	}	

	/**
	 * Gets correct clicked coordinates and scroll offset.
	 * @author yulapshun
	 *
	 */
	//handle panning and clicking
	private class GestureListener extends GestureDetector.SimpleOnGestureListener{

		@Override
		public boolean onSingleTapUp(MotionEvent event){
			if(clickToPutQuestion){
				putQuestion(getBoardX(event), getBoardY(event));
			}else{
				if(primaryControl != DOUBLE_TAB && secondaryControl != DOUBLE_TAB){
					if(primaryControl == SINGLE_TAB)
						clickCell(getBoardX(event), getBoardY(event));
					else if(secondaryControl == SINGLE_TAB)
						putFlag(getBoardX(event), getBoardY(event));			
				}
			}
			return true;
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent event){
			if((primaryControl == DOUBLE_TAB || secondaryControl == DOUBLE_TAB) && !clickToPutQuestion){
				if(primaryControl == SINGLE_TAB)
					clickCell(getBoardX(event), getBoardY(event));
				else if(secondaryControl == SINGLE_TAB)
					putFlag(getBoardX(event), getBoardY(event));			
			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent event){
			if(primaryControl == LONG_PRESS && !clickToPutQuestion)
				clickCell(getBoardX(event), getBoardY(event));
			else if(secondaryControl == LONG_PRESS && !clickToPutQuestion)
				putFlag(getBoardX(event), getBoardY(event));
			if(assistantLevel == 2){
				if(game.getCellState(getBoardX(event), getBoardY(event)) == ClassicGame.KNOWN)
					openSurroundingCells(getBoardX(event), getBoardY(event));
			}
		}
		
		@Override
        public boolean onDoubleTap(MotionEvent event) {
			if(primaryControl == DOUBLE_TAB && !clickToPutQuestion)
				clickCell(getBoardX(event), getBoardY(event));
			else if(secondaryControl == DOUBLE_TAB && !clickToPutQuestion)
				putFlag(getBoardX(event), getBoardY(event));
            return true;
        }

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){	    	
			if(!mScaleGestureDetector.isInProgress()){	
				canvasMatrix.postTranslate(-distanceX, -distanceY);
				
				keepBoardInBounds(distanceX, distanceY);

				topPadding = Math.max((viewHeight - height * cellSize * canvasMatrixValues[4]) / 2, 0);
				leftPadding = Math.max((viewWidth - width * cellSize * canvasMatrixValues[0]) / 2, 0);
				canvasMatrix.postTranslate(leftPadding, topPadding);
				
				topBar.invalidateScrollBars();
				
				//repaint
				invalidate();
			}
			return true;
		}

	}

	/**
	 * Sets various gesture listener.
	 */
	//set different gesture detectors
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Let the ScaleGestureDetector inspect all events.
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
				if(assistantLevel > 0){
					showSurroundingCells(getBoardX(event), getBoardY(event));
					showTouchingCell(getBoardX(event), getBoardY(event));	
				}
			break;
		case MotionEvent.ACTION_UP:
				if(assistantLevel > 0){
					removeSurroundingCells();
					removeTouchingCell();
				}
			break;
		}
		mScaleGestureDetector.onTouchEvent(event);
		mGestureDetector.onTouchEvent(event);		
		return true;
	}	

	/**
	 * Use stacks to simulate recursion to prevent stack overflow.
	 * @param x
	 * @param y
	 */
	public void expand(int x, int y){
		Stack<Integer> xStack = new Stack<Integer>();
		Stack<Integer> yStack = new Stack<Integer>();
		xStack.push(x);
		yStack.push(y);
		while(!xStack.empty()){
			x = xStack.pop();
			y = yStack.pop();
			if((game.getCellState(x,y) == ClassicGame.KNOWN || game.getCellState(x,y) == ClassicGame.QUESTION)/*&& !game.getCellExpanded(x, y)*/){
				//game.setCellExpanded(x, y);
				//top-left
				if(x == 0 && y == 0){
					xStack.push(1);
					yStack.push(0);
					
					xStack.push(0);
					yStack.push(1);
					
					xStack.push(1);
					yStack.push(1);
					
					//expand(1, 0);
					//expand(0, 1);
					//expand(1, 1);
				}

				//bottom-left
				else if(x == height - 1 && y == 0){
					
					xStack.push(x - 1);
					yStack.push(0);
					
					xStack.push(x);
					yStack.push(1);
					
					xStack.push(x - 1);
					yStack.push(1);
					
					//expand(x - 1, 0);
					//expand(x, 1);
					//expand(x - 1, 1);
				}

				//top-right
				else if(x == 0 && y == width - 1){
					
					xStack.push(0);
					yStack.push(y - 1);
					
					xStack.push(1);
					yStack.push(y);
					
					xStack.push(1);
					yStack.push(y - 1);
					
					//expand(0, y - 1);
					//expand(1, y);
					//expand(1, y - 1);
				}

				//bottom-right
				else if(x == height - 1 && y == width - 1){
					
					xStack.push(x - 1);
					yStack.push(y);
					
					xStack.push(x);
					yStack.push(y - 1);
					
					xStack.push(x - 1);
					yStack.push(y - 1);
					
					//expand(x - 1, y);
					//expand(x, y - 1);
					//expand(x - 1, y - 1);
				}

				//left
				else if(y == 0){
					
					xStack.push(x - 1);
					yStack.push(0);
					
					xStack.push(x + 1);
					yStack.push(0);
					
					xStack.push(x);
					yStack.push(1);
					
					xStack.push(x - 1);
					yStack.push(1);
					
					xStack.push(x + 1);
					yStack.push(1);
					
					//expand(x - 1, 0);
					//expand(x + 1, 0);
					//expand(x, 1);
					//expand(x - 1, 1);
					//expand(x + 1, 1);
				}

				//right
				else if(y == width - 1){
					
					xStack.push(x - 1);
					yStack.push(y);
					
					xStack.push(x + 1);
					yStack.push(y);
					
					xStack.push(x);
					yStack.push(y - 1);
					
					xStack.push(x - 1);
					yStack.push(y - 1);
					
					xStack.push(x + 1);
					yStack.push(y - 1);
					
					//expand(x - 1, y);
					//expand(x + 1, y);
					//expand(x, y - 1);
					//expand(x - 1, y - 1);
					//expand(x + 1, y - 1);
				}

				//top
				else if(x == 0){
					
					xStack.push(0);
					yStack.push(y - 1);
					
					xStack.push(0);
					yStack.push(y + 1);
					
					xStack.push(1);
					yStack.push(y);
					
					xStack.push(1);
					yStack.push(y - 1);
					
					xStack.push(1);
					yStack.push(y + 1);
					
					//expand(0, y - 1);
					//expand(0, y + 1);
					//expand(1, y);
					//expand(1, y - 1);
					//expand(1, y + 1);
				}

				//bottom
				else if(x == height - 1){
					
					xStack.push(x);
					yStack.push(y - 1);
					
					xStack.push(x);
					yStack.push(y + 1);
					
					xStack.push(x - 1);
					yStack.push(y);
					
					xStack.push(x - 1);
					yStack.push(y - 1);
					
					xStack.push(x - 1);
					yStack.push(y + 1);
					
					//expand(x, y - 1);
					//expand(x, y + 1);
					//expand(x - 1, y);
					//expand(x - 1, y - 1);
					//expand(x - 1, y + 1);
				}

				//all the others
				else{
					
					xStack.push(x);
					yStack.push(y - 1);
					
					xStack.push(x);
					yStack.push(y + 1);
					
					xStack.push(x - 1);
					yStack.push(y);
					
					xStack.push(x + 1);
					yStack.push(y);
					
					xStack.push(x - 1);
					yStack.push(y - 1);
					
					xStack.push(x - 1);
					yStack.push(y + 1);
					
					xStack.push(x + 1);
					yStack.push(y - 1);
					
					xStack.push(x + 1);
					yStack.push(y + 1);
					
					//expand(x, y - 1);
					//expand(x, y + 1);
					//expand(x - 1, y);
					//expand(x + 1, y);
					//expand(x - 1, y - 1);
					//expand(x - 1, y + 1);
					//expand(x + 1, y - 1);
					//expand(x + 1, y + 1);
				}
			}
		}
	}
	
	/**
	 * Recursively called to show the cells that need to be shown.
	 * Will cause stack overflow in cumstom mode
	 * @param x
	 * @param y
	 */
	//reveal cells
	public void expandRecursive(int x, int y){
		if((game.getCellState(x,y) == ClassicGame.KNOWN || game.getCellState(x,y) == ClassicGame.QUESTION)/* && !game.getCellExpanded(x, y)*/){
			if(game.getCellContent(x, y) == ClassicGame.MINE){

			}else if (game.getCellContent(x, y) == 0){

			}else{

			}

			//game.setCellExpanded(x, y);
			//top-left
			if(x == 0 && y == 0){
				expand(1, 0);
				expand(0, 1);
				expand(1, 1);
			}

			//bottom-left
			else if(x == height - 1 && y == 0){
				expand(x - 1, 0);
				expand(x, 1);
				expand(x - 1, 1);
			}

			//top-right
			else if(x == 0 && y == width - 1){
				expand(0, y - 1);
				expand(1, y);
				expand(1, y - 1);
			}

			//bottom-right
			else if(x == height - 1 && y == width - 1){
				expand(x - 1, y);
				expand(x, y - 1);
				expand(x - 1, y - 1);
			}

			//left
			else if(y == 0){
				expand(x - 1, 0);
				expand(x + 1, 0);
				expand(x, 1);
				expand(x - 1, 1);
				expand(x + 1, 1);
			}

			//right
			else if(y == width - 1){
				expand(x - 1, y);
				expand(x + 1, y);
				expand(x, y - 1);
				expand(x - 1, y - 1);
				expand(x + 1, y - 1);
			}

			//top
			else if(x == 0){
				expand(0, y - 1);
				expand(0, y + 1);
				expand(1, y);
				expand(1, y - 1);
				expand(1, y + 1);
			}

			//bottom
			else if(x == height - 1){
				expand(x, y - 1);
				expand(x, y + 1);
				expand(x - 1, y);
				expand(x - 1, y - 1);
				expand(x - 1, y + 1);
			}

			//all the others
			else{
				expand(x, y - 1);
				expand(x, y + 1);
				expand(x - 1, y);
				expand(x + 1, y);
				expand(x - 1, y - 1);
				expand(x - 1, y + 1);
				expand(x + 1, y - 1);
				expand(x + 1, y + 1);
			}
		}
	}		
	
	/**
	 * Draw the canvas.
	 */
	//draw
	Matrix shaderMatrix;
	public void onDraw(Canvas canvas){		
		super.onDraw(canvas);				
		canvas.save();		
		//don't touch above, required for scaling and panning----------------------------------------------------------------------------------------								

		//require for transformation		
		canvas.concat(canvasMatrix);
		rect = canvas.getClipBounds();						
		
		//actual drawing
		canvas.save();
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){	
				int state = game.getCellState(i,j);
				if(state == ClassicGame.UNKNOWN){
					if(!showTouching){
						canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintUnclick);
					}else if(showTouching && (touchingX != i || touchingY != j)){
						canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintUnclick);
					}
				}else if(state == ClassicGame.KNOWN){
					int content = game.getCellContent(i, j);
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintKnown);
					if(content == 0){
						
					}else if(content == ClassicGame.MINE){
						canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintMine);
					}else{    					
						switch(content){
						case 1:
							mPaintText.setColor(Color.BLUE);
							break;
						case 2:
							mPaintText.setColor(Color.GREEN);
							break;
						case 3:
							mPaintText.setColor(Color.RED);
							break;
						case 4:
							mPaintText.setColor(Color.parseColor("#0000A0"));							
							break;
						case 5:
							mPaintText.setColor(Color.parseColor("#A52A2A"));
							break;
						case 6:
							mPaintText.setColor(Color.CYAN);
							break;
						case 7:
							mPaintText.setColor(Color.BLACK);
							break;
						case 8:
							mPaintText.setColor(Color.GRAY);
							break;
						}
						canvas.drawText(String.valueOf(content), cellSize / 2, cellSize / 2 - ((mPaint.descent() + mPaint.ascent()) / 2), mPaintText);
					}
				}else if(state == ClassicGame.FLAG){
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintKnown);
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintFlag);
					if(wrongFlag[i][j]){
						mPaintText.setColor(Color.RED);
						mPaintText.setTypeface(Typeface.SANS_SERIF);
						canvas.drawText("X",  cellSize / 2, cellSize / 2 - ((mPaint.descent() + mPaint.ascent()) / 2), mPaintText);
						mPaintText.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "ka1.ttf"));
						topBar.incRemainingMineNo();
						topBar.invalidate();
					}
				}else if(state == ClassicGame.QUESTION){
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintKnown);
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintQMark);
					//mPaintText.setColor(Color.BLACK);
					//canvas.drawText("?", cellSize / 2, cellSize / 2 - ((mPaint.descent() + mPaint.ascent()) / 2), mPaintText);	    					
				}
				canvas.translate(cellSize, 0);
			}
			canvas.translate(-width * cellSize, cellSize);
		}
		canvas.restore();				
		
		//show surrounding cells
		if(showSurround){
			//mPaintSurround.setColor(Color.RED);
			canvas.drawRect(surroundY * cellSize - cellSize, surroundX * cellSize - cellSize, surroundY * cellSize + cellSize * 2, surroundX * cellSize + cellSize * 2, mPaintSurround);
			//show surrounding cells as touching
			if(assistantLevel == 2){
				canvas.save();
				canvas.translate(touchingY * cellSize - cellSize, touchingX * cellSize - cellSize);
				if(touchingX - 1 >= 0 && touchingX - 1 < height && touchingY - 1 >= 0 && touchingY - 1 < width && game.getCellState(touchingX - 1, touchingY - 1) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
				canvas.translate(0, cellSize);
				if(touchingX >= 0 && touchingX < height && touchingY - 1 >= 0 && touchingY -1 < width && game.getCellState(touchingX, touchingY - 1) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
				canvas.translate(0, cellSize);
				if(touchingX + 1 >= 0 && touchingX + 1 < height && touchingY - 1 >= 0 && touchingY - 1 < width && game.getCellState(touchingX + 1, touchingY - 1) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
				canvas.translate(cellSize, 0);
				if(touchingX + 1 >= 0 && touchingX + 1 < height && touchingY >= 0 && touchingY < width && game.getCellState(touchingX + 1, touchingY) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
				canvas.translate(cellSize, 0);
				if(touchingX + 1 >= 0 && touchingX + 1 < height && touchingY + 1 >= 0 && touchingY + 1 < width && game.getCellState(touchingX + 1, touchingY + 1) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);				
				canvas.translate(0, -cellSize);
				if(touchingX >= 0 && touchingX < height && touchingY + 1 >= 0 && touchingY + 1 < width && game.getCellState(touchingX, touchingY + 1) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
				canvas.translate(0, -cellSize);
				if(touchingX - 1 >= 0 && touchingX - 1 < height && touchingY + 1 >= 0 && touchingY + 1 < width && game.getCellState(touchingX - 1, touchingY + 1) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
				canvas.translate(-cellSize, 0);
				if(touchingX - 1 >= 0 && touchingX - 1 < height && touchingY >= 0 && touchingY < width && game.getCellState(touchingX - 1, touchingY) == ClassicGame.UNKNOWN)
					canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
				canvas.restore();
			}
		}
		
		//show touching cell
		mPaint.setStyle(Paint.Style.FILL);    	
		if(showTouching && game.getCellState(touchingX, touchingY) == ClassicGame.UNKNOWN){
			canvas.save();
			canvas.translate(touchingY * cellSize, touchingX * cellSize);
			canvas.drawRoundRect(cellRect, cellSize / 10, cellSize / 10, mPaintClicking);
			canvas.restore();
			//show surrounding cells as touching						
		}								

		//don't touch below, required for scaling and panning----------------------------------------------------------------------------------------    	
		canvas.restore();		
	}

}
