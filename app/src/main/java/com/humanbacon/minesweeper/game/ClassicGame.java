package com.humanbacon.minesweeper.game;

import java.io.File;
import java.util.Random;
import java.util.Stack;

import android.util.Log;

public class ClassicGame {
	//state
	public static final int UNKNOWN = 0;
	public static final int KNOWN = 1;
	public static final int FLAG = 2;
	public static final int QUESTION = 3;
	
	//content
	public static final int MINE = 9;
	
	private int mineNo;
	private int width;
	private int height;
	private Cell board[][];
	private int remainingMineNo;
	private int unknownCount;
	private int knownCount;
	private int flagCount;
	private int questionCount;
	private boolean lose;
	private boolean win;
	private boolean firstClickIsZero;

	//constructor for resume saved game
	public ClassicGame(int mineNo, int width, int height, int remainingMines, int cellState[][], int cellContent[][], boolean empty){
		this.mineNo = mineNo;
		this.width = width;
		this.height = height;
		this.remainingMineNo = remainingMines;
		unknownCount = width * height;
		board = new Cell[height][width];
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				board[i][j] = new Cell(cellState[i][j], cellContent[i][j]);
				if(cellState[i][j] == KNOWN){
					unknownCount--;
					knownCount++;
				}
				if(cellState[i][j] == FLAG){
					flagCount++;
				}
				if(cellState[i][j] == QUESTION){
					questionCount = 0;
				}
			}
		}
		lose = false;
		win = false;
	}
	
	//constructor for empty gameBoard
	public ClassicGame(int mineNo, int width, int height, boolean empty){
		this.mineNo = mineNo;
		this.width = width;
		this.height = height;
		remainingMineNo = mineNo;
		board = new Cell[height][width];
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				board[i][j] = new Cell();
			}
		}
		unknownCount = width * height;
		knownCount = 0;
		flagCount = 0;
		questionCount = 0;
		lose = false;
		win = false;
	}
	
	//constructor for the first clicked cell is not always zero
	public ClassicGame(int mineNo, int width, int height){
		this(mineNo, width, height, false, -1, -1);
	}
		
	//constructor for the first clicked cell is always zero
	public ClassicGame(int mineNo, int width, int height, boolean firstClickIsZero, int x, int y){
		this.firstClickIsZero = firstClickIsZero;
		this.mineNo = mineNo;
		this.width = width;
		this.height = height;
		remainingMineNo = mineNo;
		board = new Cell[height][width];
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				board[i][j] = new Cell();
			}
		}
		unknownCount = width * height;
		knownCount = 0;
		flagCount = 0;
		questionCount = 0;
		lose = false;
		win = false;
		genBoard(x, y);
	}
	
	//generate board
	private void genBoard(int x, int y){
		//generate mines
		int remainingMine = mineNo;
		Random generator = new Random();
		int i,j;
		//make sure the first clicked cell is always zero
		int mineException[][] = {{x - 1, y - 1}, {x - 1, y}, {x - 1, y + 1}, {x, y - 1},
								{x, y + 1}, {x + 1, y - 1}, {x + 1, y}, {x + 1, y + 1}};
		
		while(remainingMine != 0){			
			do{
				i = generator.nextInt(height);
				j = generator.nextInt(width);
				for(int a = 0; a < 8; a++){
					int e[] = mineException[a];
					if((i == e[0] && j == e[1])){						
						i = generator.nextInt(height);
						j = generator.nextInt(width);
						a = -1;
					}
				}
			}while(board[i][j].content == ClassicGame.MINE || (i == x && j == y));
			board[i][j].content = ClassicGame.MINE;
			remainingMine--;
		}
		
		//top-left
		if(board[0][0].content != ClassicGame.MINE){
			if(board[1][0].content == ClassicGame.MINE)
				board[0][0].content++;
			if(board[0][1].content == ClassicGame.MINE)
				board[0][0].content++;
			if(board[1][1].content == ClassicGame.MINE)
				board[0][0].content++;
		}		
		//bottom-left
		if(board[height - 1][0].content != ClassicGame.MINE){
			if(board[height - 2][0].content == ClassicGame.MINE)
				board[height - 1][0].content++;
			if(board[height - 1][1].content == ClassicGame.MINE)
				board[height - 1][0].content++;
			if(board[height - 2][1].content == ClassicGame.MINE)
				board[height - 1][0].content++;
		}		
		//top-right
		if(board[0][width - 1].content != ClassicGame.MINE){
			if(board[0][width - 2].content == ClassicGame.MINE)
				board[0][width - 1].content++;
			if(board[1][width - 1].content == ClassicGame.MINE)
				board[0][width - 1].content++;
			if(board[1][width - 2].content == ClassicGame.MINE)
				board[0][width - 1].content++;
		}

		//bottom-right
		if(board[height - 1][width - 1].content != ClassicGame.MINE){
			if(board[height - 2][width - 1].content == ClassicGame.MINE)
				board[height - 1][width - 1].content++;
			if(board[height - 1][width - 2].content == ClassicGame.MINE)
				board[height - 1][width - 1].content++;
			if(board[height - 2][width - 2].content == ClassicGame.MINE)
				board[height - 1][width - 1].content++;
		}
		
		//left and right
		for(i = 1; i < height - 1; i++){
			//left
			if(board[i][0].content != ClassicGame.MINE){
				if(board[i - 1][0].content == ClassicGame.MINE)
					board[i][0].content++;
				if(board[i + 1][0].content == ClassicGame.MINE)
					board[i][0].content++;
				if(board[i][1].content == ClassicGame.MINE)
					board[i][0].content++;
				if(board[i - 1][1].content == ClassicGame.MINE)
					board[i][0].content++;
				if(board[i + 1][1].content == ClassicGame.MINE)
					board[i][0].content++;
			}			
			//right
			if(board[i][width - 1].content != ClassicGame.MINE){
				if(board[i - 1][width - 1].content == ClassicGame.MINE)
					board[i][width - 1].content++;
				if(board[i + 1][width - 1].content == ClassicGame.MINE)
					board[i][width - 1].content++;
				if(board[i][width - 2].content == ClassicGame.MINE)
					board[i][width - 1].content++;
				if(board[i - 1][width - 2].content == ClassicGame.MINE)
					board[i][width - 1].content++;
				if(board[i + 1][width - 2].content == ClassicGame.MINE)
					board[i][width - 1].content++;
			}
		}		
		//top and bottom
		for(j = 1; j < width - 1; j++){
			//top
			if(board[0][j].content != ClassicGame.MINE){
				if(board[0][j - 1].content == ClassicGame.MINE)
					board[0][j].content++;
				if(board[0][j + 1].content == ClassicGame.MINE)
					board[0][j].content++;
				if(board[1][j].content == ClassicGame.MINE)
					board[0][j].content++;
				if(board[1][j - 1].content == ClassicGame.MINE)
					board[0][j].content++;
				if(board[1][j + 1].content == ClassicGame.MINE)
					board[0][j].content++;
			}
			//bottom
			if(board[height - 1][j].content != ClassicGame.MINE){
				if(board[height - 1][j - 1].content == ClassicGame.MINE)
					board[height - 1][j].content++;
				if(board[height - 1][j + 1].content == ClassicGame.MINE)
					board[height - 1][j].content++;
				if(board[height - 2][j].content == ClassicGame.MINE)
					board[height - 1][j].content++;
				if(board[height - 2][j - 1].content == ClassicGame.MINE)
					board[height - 1][j].content++;
				if(board[height - 2][j + 1].content == ClassicGame.MINE)
					board[height - 1][j].content++;
			}
		}
		
		//all the others
		for(i = 1; i < height - 1; i++){
			for(j = 1; j < width - 1; j++){
				if(board[i][j].content != ClassicGame.MINE){
					if(board[i][j - 1].content == ClassicGame.MINE)
						board[i][j].content++;
					if(board[i][j + 1].content == ClassicGame.MINE)
						board[i][j].content++;
					if(board[i - 1][j].content == ClassicGame.MINE)
						board[i][j].content++;
					if(board[i + 1][j].content == ClassicGame.MINE)
						board[i][j].content++;
					if(board[i - 1][j - 1].content == ClassicGame.MINE)
						board[i][j].content++;
					if(board[i - 1][j + 1].content == ClassicGame.MINE)
						board[i][j].content++;
					if(board[i + 1][j - 1].content == ClassicGame.MINE)
						board[i][j].content++;
					if(board[i + 1][j + 1].content == ClassicGame.MINE)
						board[i][j].content++;
				}				
			}
		}		
	}

	
	public void selectCell(int x, int y){
		Stack<Integer> xStack = new Stack<Integer>();
		Stack<Integer> yStack = new Stack<Integer>();
		xStack.push(x);
		yStack.push(y);
		while(!xStack.empty()){			
			x = xStack.pop();
			y = yStack.pop();			
			if(board[x][y].state == ClassicGame.UNKNOWN || board[x][y].state == ClassicGame.QUESTION){			
				board[x][y].state = ClassicGame.KNOWN;
				unknownCount--;
				knownCount++;
				if(board[x][y].content == ClassicGame.MINE){
					lose = true;				
					return;
				}
				if(unknownCount == mineNo){
					win = true;
					remainingMineNo = 0;
					return;
				}
				if(board[x][y].content == 0){
					//top-left
					if(x == 0 && y == 0){
						if(board[1][0].content != ClassicGame.MINE){
							xStack.push(1);
							yStack.push(0);
							//continue;
						}
						//selectCell(1, 0);
						if(board[0][1].content != ClassicGame.MINE){
							xStack.push(0);
							yStack.push(1);
							//continue;
						}
						//selectCell(0, 1);
						if(board[1][1].content != ClassicGame.MINE){
							xStack.push(1);
							yStack.push(1);
							//continue;
						}
						//selectCell(1, 1);
					}

					//bottom-left
					else if(x == height - 1 && y == 0){
						if(board[x - 1][0].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(0);
							//continue;
						}
							//selectCell(x - 1, 0);
						if(board[x][1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(1);
							//continue;
						}
							//selectCell(x, 1);
						if(board[x - 1][1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(1);
							//continue;
						}
							//selectCell(x - 1, 1);
					}

					//top-right
					else if(x == 0 && y == width - 1){
						if(board[0][y - 1].content != ClassicGame.MINE){
							xStack.push(0);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(0, y - 1);
						if(board[1][y].content != ClassicGame.MINE){
							xStack.push(1);
							yStack.push(y);
							//continue;
						}
							//selectCell(1, y);
						if(board[1][y - 1].content != ClassicGame.MINE){
							xStack.push(1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(1, y - 1);
					}

					//bottom-right
					else if(x == height - 1 && y == width - 1){
						if(board[x - 1][y].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y);
							//continue;
						}
							//selectCell(x - 1, y);
						if(board[x][y - 1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x, y - 1);
						if(board[x - 1][y - 1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x - 1, y - 1);
					}

					//left
					else if(y == 0){
						if(board[x - 1][0].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(0);
							//continue;
						}
							//selectCell(x - 1, 0);
						if(board[x + 1][0].content != ClassicGame.MINE){
							xStack.push(x + 1);
							yStack.push(0);
							//continue;
						}
							//selectCell(x + 1, 0);
						if(board[x][1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(1);
							//continue;
						}
							//selectCell(x, 1);
						if(board[x - 1][1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(1);
							//continue;
						}
							//selectCell(x - 1, 1);
						if(board[x + 1][1].content != ClassicGame.MINE){
							xStack.push(x + 1);
							yStack.push(1);
							//continue;
						}
							//selectCell(x + 1, 1);
					}

					//right
					else if(y == width - 1){
						if(board[x - 1][y].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y);
							//continue;
						}
							//selectCell(x - 1, y);
						if(board[x + 1][y].content != ClassicGame.MINE){
							xStack.push(x + 1);
							yStack.push(y);
							//continue;
						}
							//selectCell(x + 1, y);
						if(board[x][y - 1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x, y - 1);
						if(board[x - 1][y - 1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x - 1, y - 1);
						if(board[x + 1][y - 1].content != ClassicGame.MINE){
							xStack.push(x + 1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x + 1, y - 1);
					}

					//top
					else if(x == 0){
						if(board[0][y - 1].content != ClassicGame.MINE){
							xStack.push(0);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(0, y - 1);
						if(board[0][y + 1].content != ClassicGame.MINE){
							xStack.push(0);
							yStack.push(y + 1);
							//continue;
						}
							//selectCell(0, y + 1);
						if(board[1][y].content != ClassicGame.MINE){
							xStack.push(1);
							yStack.push(y);
							//continue;
						}
							//selectCell(1, y);
						if(board[1][y - 1].content != ClassicGame.MINE){
							xStack.push(1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(1, y - 1);
						if(board[1][y + 1].content != ClassicGame.MINE){
							xStack.push(1);
							yStack.push(y + 1);
							//continue;
						}
							//selectCell(1, y + 1);
					}

					//bottom
					else if(x == height - 1){
						if(board[x][y - 1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x, y - 1);
						if(board[x][y + 1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(y + 1);
							//continue;
						}
							//selectCell(x, y + 1);
						if(board[x - 1][y].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y);
							//continue;
						}
							//selectCell(x - 1, y);
						if(board[x - 1][y - 1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x - 1, y - 1);
						if(board[x - 1][y + 1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y + 1);
							//continue;
						}
							//selectCell(x - 1, y + 1);
					}

					//all the others
					else{
						if(board[x][y - 1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x, y - 1);
						if(board[x][y + 1].content != ClassicGame.MINE){
							xStack.push(x);
							yStack.push(y + 1);
							//continue;
						}
							//selectCell(x, y + 1);
						if(board[x - 1][y].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y);
							//continue;
						}
							//selectCell(x - 1, y);
						if(board[x + 1][y].content != ClassicGame.MINE){
							xStack.push(x + 1);
							yStack.push(y);
							//continue;
						}
							//selectCell(x + 1, y);
						if(board[x - 1][y - 1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x - 1, y - 1);
						if(board[x - 1][y + 1].content != ClassicGame.MINE){
							xStack.push(x - 1);
							yStack.push(y + 1);
							//continue;
						}
							//selectCell(x - 1, y + 1);
						if(board[x + 1][y - 1].content != ClassicGame.MINE){
							xStack.push(x + 1);
							yStack.push(y - 1);
							//continue;
						}
							//selectCell(x + 1, y - 1);
						if(board[x + 1][y + 1].content != ClassicGame.MINE){
							xStack.push(x + 1);
							yStack.push(y + 1);
							//continue;
						}
							//selectCell(x + 1, y + 1);
					}
				}
			}
		}
	}

	public void selectCellRecursive(int x, int y){
		if(board[x][y].state == ClassicGame.UNKNOWN || board[x][y].state == ClassicGame.QUESTION){			
			board[x][y].state = ClassicGame.KNOWN;
			unknownCount--;
			knownCount++;
			if(board[x][y].content == ClassicGame.MINE){
				lose = true;				
				return;
			}
			if(unknownCount == mineNo){
				win = true;
				remainingMineNo = 0;
				return;
			}
			if(board[x][y].content == 0){
				//top-left
				if(x == 0 && y == 0){
					if(board[1][0].content != ClassicGame.MINE)
						selectCell(1, 0);
					if(board[0][1].content != ClassicGame.MINE)
						selectCell(0, 1);
					if(board[1][1].content != ClassicGame.MINE)
						selectCell(1, 1);
				}
				
				//bottom-left
				else if(x == height - 1 && y == 0){
					if(board[x - 1][0].content != ClassicGame.MINE)
						selectCell(x - 1, 0);
					if(board[x][1].content != ClassicGame.MINE)
						selectCell(x, 1);
					if(board[x - 1][1].content != ClassicGame.MINE)
						selectCell(x - 1, 1);
				}
				
				//top-right
				else if(x == 0 && y == width - 1){
					if(board[0][y - 1].content != ClassicGame.MINE)
						selectCell(0, y - 1);
					if(board[1][y].content != ClassicGame.MINE)
						selectCell(1, y);
					if(board[1][y - 1].content != ClassicGame.MINE)
						selectCell(1, y - 1);
				}
							
				//bottom-right
				else if(x == height - 1 && y == width - 1){
					if(board[x - 1][y].content != ClassicGame.MINE)
						selectCell(x - 1, y);
					if(board[x][y - 1].content != ClassicGame.MINE)
						selectCell(x, y - 1);
					if(board[x - 1][y - 1].content != ClassicGame.MINE)
						selectCell(x - 1, y - 1);
				}
				
				//left
				else if(y == 0){
					if(board[x - 1][0].content != ClassicGame.MINE)
						selectCell(x - 1, 0);
					if(board[x + 1][0].content != ClassicGame.MINE)
						selectCell(x + 1, 0);
					if(board[x][1].content != ClassicGame.MINE)
						selectCell(x, 1);
					if(board[x - 1][1].content != ClassicGame.MINE)
						selectCell(x - 1, 1);
					if(board[x + 1][1].content != ClassicGame.MINE)
						selectCell(x + 1, 1);
				}
				
				//right
				else if(y == width - 1){
					if(board[x - 1][y].content != ClassicGame.MINE)
						selectCell(x - 1, y);
					if(board[x + 1][y].content != ClassicGame.MINE)
						selectCell(x + 1, y);
					if(board[x][y - 1].content != ClassicGame.MINE)
						selectCell(x, y - 1);
					if(board[x - 1][y - 1].content != ClassicGame.MINE)
						selectCell(x - 1, y - 1);
					if(board[x + 1][y - 1].content != ClassicGame.MINE)
						selectCell(x + 1, y - 1);
				}

				//top
				else if(x == 0){
					if(board[0][y - 1].content != ClassicGame.MINE)
						selectCell(0, y - 1);
					if(board[0][y + 1].content != ClassicGame.MINE)
						selectCell(0, y + 1);
					if(board[1][y].content != ClassicGame.MINE)
						selectCell(1, y);
					if(board[1][y - 1].content != ClassicGame.MINE)
						selectCell(1, y - 1);
					if(board[1][y + 1].content != ClassicGame.MINE)
						selectCell(1, y + 1);
				}

				//bottom
				else if(x == height - 1){
					if(board[x][y - 1].content != ClassicGame.MINE)
						selectCell(x, y - 1);
					if(board[x][y + 1].content != ClassicGame.MINE)
						selectCell(x, y + 1);
					if(board[x - 1][y].content != ClassicGame.MINE)
						selectCell(x - 1, y);
					if(board[x - 1][y - 1].content != ClassicGame.MINE)
						selectCell(x - 1, y - 1);
					if(board[x - 1][y + 1].content != ClassicGame.MINE)
						selectCell(x - 1, y + 1);
				}
				
				//all the others
				else{
					if(board[x][y - 1].content != ClassicGame.MINE)
						selectCell(x, y - 1);
					if(board[x][y + 1].content != ClassicGame.MINE)
						selectCell(x, y + 1);
					if(board[x - 1][y].content != ClassicGame.MINE)
						selectCell(x - 1, y);
					if(board[x + 1][y].content != ClassicGame.MINE)
						selectCell(x + 1, y);
					if(board[x - 1][y - 1].content != ClassicGame.MINE)
						selectCell(x - 1, y - 1);
					if(board[x - 1][y + 1].content != ClassicGame.MINE)
						selectCell(x - 1, y + 1);
					if(board[x + 1][y - 1].content != ClassicGame.MINE)
						selectCell(x + 1, y - 1);
					if(board[x + 1][y + 1].content != ClassicGame.MINE)
						selectCell(x + 1, y + 1);
				}
			}
		}
	}
	
	public void copyGameState(ClassicGame game){
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				this.board[i][j].state = game.board[i][j].state;
			}
		}
	}
	
	public void putFlag(int x, int y){
		board[x][y].state = FLAG;
		flagCount++;
		remainingMineNo--;
	}
	
	public void removeFlag(int x, int y){
		board[x][y].state = UNKNOWN;
		flagCount--;
		remainingMineNo++;
	}

	public void putQuestion(int x, int y){
		board[x][y].state = QUESTION;
	}
	
	public void removeQuestion(int x, int y){
		board[x][y].state = UNKNOWN;
	}
	
	public int getCellState(int x, int y){
		return board[x][y].state;
	}
	
	public int getCellContent(int x, int y){
		return board[x][y].content;
	}
	
	public boolean getWin(){
		return win;
	}
	
	public boolean getLose(){
		return lose;
	}

	public void setKnown(int x, int y){
		board[x][y].state = KNOWN;
	}

	public int getRemainingMineNo(){
		return remainingMineNo;
	}
	
	private class Cell{
		int state;
		int content;
		public Cell(){
			state = 0;
			content = 0;
		}
		public Cell(int state, int content){
			this.state = state;
			this.content = content;
		}		
	}

}
