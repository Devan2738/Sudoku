import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the driver class of my Sudoku solving program. This class contains
 * the main method and additional methods which are necessary for finding and
 * setting values of Cells.
 * 
 * @author devankarsann
 *
 */

public class Solver {

	// these variables are static so the helper methods have access to them
	
	static FileWriter writer;
	static Cell[][] grid;
	static ArrayList<Integer> cellNotNums;
	static ArrayList<Cell[][]> gridStates = new ArrayList<Cell[][]>();
	static ArrayList<int[]> guessStates = new ArrayList<int[]>();
	static int previousState = -1;
	static int numSet = 0;
	static int totalErrors = 0;
	static int numLooped = 0;
	static int dimension; 
	
	/**
	 * This is the main method for my Sudoku solving program. A file and scanner
	 * object read input from the specified text file whose file name is entered as 
	 * an argument. Sudoku puzzle entered can be solved in 5 seconds or less.
	 * 
	 * @param args,
	 *            the filename/filepath of a plain text file formatted by
	 *            specifications in attached README
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		boolean found = true;

		File file = new File("solutionSteps");
		
		writer = new FileWriter(file);

		File sample = new File(args[0]);
		Scanner scan = new Scanner(sample);

		int inputNumRows = scan.nextInt();
		int inputNumColumns = scan.nextInt();
		dimension = inputNumRows;
		
		grid = new Cell[inputNumRows][inputNumColumns];

		for (int i = 0; i < inputNumRows; i++) {
			for (int j = 0; j < inputNumColumns; j++) {
				grid[i][j] = new Cell(inputNumRows);
				int currentInt = scan.nextInt();
				if (currentInt != 0) {
					grid[i][j].setCellValue(currentInt);
					numSet++;
				}
			}
		}
		scan.close();

		// general information about the grid is printed to the console
		System.out.println("\n original grid\n");
		System.out.println(printGrid());
		System.out.println("\n there are " + (dimension*dimension - numSet) + " unknown values");
		System.out.println(checkGridForErrors());
		System.out.println(" calculating...");

		// general information about the grid is printed to solutionSteps text file
		writer.write("\n original grid\n\n");
		writer.write(printGrid());
		writer.write("\n\n there are " + (dimension*dimension - numSet) + " unknown values");
		writer.write("\n" + checkGridForErrors());

		// the solving algorithm starts if there are no input errors in the grid
		if (totalErrors == 0) {
			for (int i = 0; i < inputNumRows; i++) {
				for (int j = 0; j < inputNumColumns; j++) {
					if (grid[i][j].getValueWasSet() == true) {
						updateGrid(i, j);
					}
				}
			}
			
			while (numSet != dimension*dimension) {

				System.out.println(printGrid());
				System.out.println("\n there are " + (dimension*dimension - numSet) + " unknown values");
				System.out.println(checkGridForErrors());
				
				writer.write("\n there are " + numSet + " set values in the grid\n");

				if (numSet == dimension*dimension)
					break;

				found = false;

				// part 1 start
				// the condition for this if-statement checks if any guesses have been made
				// if so, the grid is checked for errors and empty cell domains
				// those two scenarios decsribe the result of making a bad guess
				if (gridStates.size() > 0) {
					for (int i = 0; i < grid.length; i++) {
						for (int j = 0; j < grid[0].length; j++) {
							if (grid[i][j].getValueWasSet() == false) {
								if (grid[i][j].getNumPossible() == 0 || numberOfErrors() > 0) {
									writer.write("\n fourth solution helper method is being used");
									writer.write("\n cell where no values are possible: row " + i + ", col " + j + "\n");
									writer.write("\n grid state BEFORE load (gridStates ArrayList size: " + gridStates.size() + ")\n" + printGrid());
									grid = gridStates.get(gridStates.size() - 1);
									int[] lastGuess = guessStates.get(guessStates.size() - 1);
									grid[lastGuess[0]][lastGuess[1]].setValueToFalse(lastGuess[2]);
									numSet = lastGuess[3];
									gridStates.remove(gridStates.size() - 1);
									writer.write("\n bad guess update... row: " + lastGuess[0] + " col: " + lastGuess[1] + " is not " + lastGuess[2]);
									writer.write("\n grid state AFTER load + bad guess update)\ngridStates ArrayList size: " + gridStates.size() + "\n" + printGrid());
									guessStates.remove(guessStates.size() - 1);
								}
							}
						}
					}
				}
				// part 1 end
				
				// part 2 start
				// if there is only one possible value for a cell it's that value
				if (numSet < dimension*dimension) {
					for (int i = 0; i < grid.length; i++) {
						for (int j = 0; j < grid[0].length; j++) {
							if (grid[i][j].getValueWasSet() == false && found == false) {
								if (grid[i][j].getPossibleNumsArrayList().size() == 1) {
									writer.write("\n\n cell in row: " + i + ", col: " + j + " (value = "	+ grid[i][j].getCellValue() + ") has one possible value: " + grid[i][j].getPossibleNumsArrayList().toString() + "\n");
									writer.write("\n grid before solution zero update: " + "\n numSet: " + numSet + "\n" + printGrid());
									grid[i][j].setCellValue(grid[i][j].getPossibleNumsArrayList().get(0));
									updateGrid(i, j);
									numSet++;
									writer.write("\n grid after solution zero update: " + "\n numSet: " + numSet + "\n" + printGrid());
									found = true;
								}
							} 
						}
					}
				}
				// part 2 end
				
				// part 3 start
				// this solution technique checks if only one cell can be a value in a sub-grid
				if (numSet < dimension*dimension) {
					for (int i = 0; i < inputNumRows; i++) {
						for (int j = 0; j < inputNumColumns; j++) {
							if (grid[i][j].getValueWasSet() == false) {
								int numNotK = 0;
								int k;
								for (k = 0; k < dimension; k++) {
									int anchorRow = getAnchor(i);
									int anchorColumn = getAnchor(j);
									for (int row = anchorRow; row < (anchorRow + 4) && row < dimension; row++) {
										for (int col = anchorColumn; col < (anchorColumn + 4) && col < dimension; col++) {
											if (grid[row][col].getPossibleNum(k) == false) {
												numNotK++;
											}
										}
									}
								}
								if (numNotK == dimension - 1 && grid[i][j].getPossibleNum(k) == true) {
									writer.write("\nsecond solution technique will be used\ncell in row: " + i + ", col: " + j + " equals " + (k + 1) + "\n" + printGrid());
									grid[i][j].setCellValue(k + 1);
									writer.write("\nsecond solution technique was used\n" + printGrid());
									updateGrid(i, j);
									numSet++;
									found = true;
									break;
								}
							}
						}
					}
				}
				// part 3 end
				
				// part 4 start
				if (numSet < dimension*dimension) {
					for (int i = 0; i < grid.length; i++) {
						for (int j = 0; j < grid[0].length; j++) {
							if (grid[i][j].getValueWasSet() == false) {
								cellNotNums = new ArrayList<Integer>();
								// this ArrayList consists of the known neighboring values of a cell
								populateArrayList(i, j);
								// if there are 8 unknown values neighboring a cell, we can find the unknown value
								if (cellNotNums.size() == dimension-1) {
									writer.write("\n first solution technique used");
									writer.write("\n cell value found at row " + i + ", col: " + j);
									grid[i][j].findCellValueFromArrayList(cellNotNums);
									writer.write("\n solution technique one used: grid(" + i + "," + j + ") = "	+ grid[i][j].getCellValue());
									numSet++;
									found = true;
									updateGrid(i, j);
								}
							}
						}
					}
				}
				// part 4 end
				
				// part 5 here
				
				// part 6 start
				// if other solution techniques weren't successful, we check to see if a guess should be made
				if (found == false && numSet < dimension*dimension) {
					boolean proceed = true;
					writer.write("\n fourth solution technique checking\n");
					// if a cell in the grid has 1 or 0 possible values, a guess should not be made
					for (int i = 0; i < grid.length; i++) {
						for (int j = 0; j < grid[0].length; j++) {
							if (grid[i][j].getValueWasSet() == false) {
								if (grid[i][j].getPossibleNumsArrayList().size() == 1) {
									writer.write("\n cell in row: " + i + ", col: " + j + " (value = " + grid[i][j].getCellValue() + ") has one possible value: " + grid[i][j].getPossibleNumsArrayList().toString() + "\n");
									proceed = false;
								}
								if (grid[i][j].getPossibleNumsArrayList().size() == 0) {
									writer.write("\n cell in row: " + i + ", col: " + j + " (value = " + grid[i][j].getCellValue() + ") has no values! ");
									proceed = false;
								}
							}
						}

					}
					if (proceed == true) {
						writer.write("\n fourth solution technique used\n");
						setCellGuess();
					}
				}
				// part 6 end
				
				numLooped++;
			}
			System.out.println("numLooped: " + numLooped);
			// in a 9x9 Sudoku value there are 81 values... if 81 values are known , the puzzle in complete
			if (numSet == dimension*dimension) {
				System.out.println("\n completed grid\n");
				System.out.println(printGrid());
				System.out.println(checkGridForErrors());
				writer.write("\n\n completed grid\n\n");
				writer.write(printGrid());
				writer.write("\n" + checkGridForErrors());

			} else {
				System.out.println(" umm... try again?\n");
				writer.write(" umm... try again?\n");
			}
			
		} 
		
		// if the user does enter errors, a short message is displayed
		else {
			System.out.println("\nan error was detected in your input\nplease check your entered values\nafter changing the input, run the program again");
			writer.write("\nan error was detected in your input\nplease check your entered values\nafter changing the input, run the program again");
		}
		writer.close();
	}

	/**
	 * This method returns a String containing all known and possible values of the
	 * puzzle. Specific formatting is used for increased readability.
	 * 
	 * @return String
	 */
	private static String printGiantGrid() {

		String str = "";
		str += "\n  _ _ _ _ _ _ _ _ _   _ _ _ _ _ _ _ _ _   _ _ _ _ _ _ _ _ _\n";
		for (int i = 0; i < 9 * 3; i++) {
			for (int j = 0; j < 9 * 3; j += 3) {
				Cell temp = grid[i / 3][j / 3];

				if (j == 0)
					str += " |";
				for (int k = 0; k < 3; k++) {

					if (temp.valueWasSet() == true)
						str += temp.getCellValue();
					else if (temp.getPossibleNum((k + ((i % 3) * 3))) == true)
						str += (k + 1 + ((i % 3) * 3));
					else
						str += " ";
					if ((j == 6 || j == 15) && k == 2)
						str += "| |";
					else if ((i == 8 || i == 17) && k != 2) {
						str += "_";
					} else if (k == 2) {
						str += "|";
					} else if (i % 3 == 2) {
						str += "_";
					} else
						str += " ";
				}
				if (j == (24))
					str += ("\n");
				if (j == 24 && (i == 8 || i == 17))
					str += "  _ _ _ _ _ _ _ _ _   _ _ _ _ _ _ _ _ _   _ _ _ _ _ _ _ _ _\n";
			}
		}
		return str;
	}

	/**
	 * This method returns the row or column index of the top-left Cell in any given
	 * subgrid.
	 * 
	 * @param num, integer value representing row or column of a Cell
	 * 
	 * @return integer
	 */
	private static int getAnchor(int num) {
		while (num % Math.sqrt(dimension) != 0)
			num--;
		return num;
	}

	/**
	 * This method finds the union of known values in a Cell's row, column and
	 * subgrid. The 'cellNotNums' ArrayList is declared globally and reset for each
	 * Cell object. No values need to be returned.
	 * 
	 * @param ii, row index of the given Cell
	 * @param jj, column index of the given Cell
	 */
	private static void populateArrayList(int ii, int jj) {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		for (int row = anchorRow; row < (anchorRow + Math.sqrt(dimension)); row++) {
			for (int col = anchorColumn; col < (anchorColumn + Math.sqrt(dimension)); col++) {
				if (!cellNotNums.contains((Object) grid[row][col].getCellValue()) && grid[row][col].valueWasSet() == true)
					cellNotNums.add(grid[row][col].getCellValue());
			}
		}

		for (int i = 0; i < grid.length; i++) {
			if (!cellNotNums.contains((Object) grid[i][jj].getCellValue()) && grid[i][jj].valueWasSet() == true) {
				cellNotNums.add(grid[i][jj].getCellValue());
			}
		}

		for (int j = 0; j < grid.length; j++) {
			if (!cellNotNums.contains((Object) grid[ii][j].getCellValue()) && grid[ii][j].valueWasSet() == true) {
				cellNotNums.add(grid[ii][j].getCellValue());
			}
		}
	}

	/**
	 * This method updates the possible values of the neighboring Cells of a given
	 * Cell.
	 * 
	 * @param ii,
	 *            row index of the given Cell
	 * @param jj,
	 *            column index of the given Cell
	 * @throws IOException
	 */
	private static void updateGrid(int ii, int jj) throws IOException {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		int cellValue = grid[ii][jj].getCellValue();

		for (int row = anchorRow; row < (anchorRow + Math.sqrt(dimension)); row++) {
			for (int col = anchorColumn; col < (anchorColumn + Math.sqrt(dimension)); col++) {
				if (row != ii || col != jj) {
					if (grid[row][col].valueWasSet() == false) {
						grid[row][col].setValueToFalse(cellValue);
					}
				}
			}
		}

		for (int i = 0; i < grid.length; i++) {
			if (i != ii) {
				grid[i][jj].setValueToFalse(cellValue);
			}
		}

		for (int j = 0; j < grid.length; j++) {
			if (j != jj) {
				if (grid[ii][j].valueWasSet() == false) {
					grid[ii][j].setValueToFalse(cellValue);
				}
			}
		}
	}

	/**
	 * This method updates the possible values of the neighboring Cells of a given
	 * Cell.
	 * 
	 * @param ii, row index of the given Cell
	 * @param jj, column index of the given Cell
	 * @throws IOException
	 */
	private static void updateGrid(Cell[][] gridForGridStates, int ii, int jj) throws IOException {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		int cellValue = gridForGridStates[ii][jj].getCellValue();

		for (int row = anchorRow; row < (anchorRow + Math.sqrt(dimension)); row++) {
			for (int col = anchorColumn; col < (anchorColumn + Math.sqrt(dimension)); col++) {
				if (row != ii || col != jj) {
					if (gridForGridStates[row][col].valueWasSet() == false) {
						gridForGridStates[row][col].setValueToFalse(cellValue);
					}
				}
			}
		}

		for (int i = 0; i < gridForGridStates.length; i++) {
			if (i != ii) {
				if (gridForGridStates[i][jj].valueWasSet() == false) {
					gridForGridStates[i][jj].setValueToFalse(cellValue);
				}
			}
		}

		for (int j = 0; j < gridForGridStates.length; j++) {
			if (j != jj) {
				if (gridForGridStates[ii][j].valueWasSet() == false) {
					gridForGridStates[ii][j].setValueToFalse(cellValue);
				}
			}
		}
	}

	/**
	 * This method prints information to the console about the values in the 'grid'
	 * array.
	 * 
	 * @return String
	 */
	private static String printGrid() {
		String str = "";
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				int cell = grid[i][j].getCellValue();
				if (j == 0) {
					str += " ";
				}
				if ((j + 1) % Math.sqrt(dimension) == 0 && j != dimension - 1) {
					if (cell > 9)
						str += cell + "|";
					else {
						if ((i + 1) % Math.sqrt(dimension) == 0 && i != dimension - 1)
							str += "_" + cell + "|";
						else
							str += " " + cell + "|";
					}
				} else if ((i + 1) % Math.sqrt(dimension) == 0 && i != dimension - 1) {
					if (cell > 9) {
						str += cell;
						if (j != dimension - 1)
							str += "_";
					}
					else {
						str += "_" + cell;
						if (j != dimension - 1)
							str += "_";
					}
				} else {
					if (cell > 9)
						str += cell + " ";
					else
						str += " " + cell + " ";
				}
			}
			if (i != dimension - 1)
				str += ("\n");
		}
		return str;
	}

	/**
	 * This methods checks for the amount of different kinds of errors. This
	 * information is returned as a String.
	 * 
	 * @return String
	 */
	private static String checkGridForErrors() {

		int numSubGridErrors = 0;
		int numColumnErrors = 0;
		int numRowErrors = 0;

		for (int ii = 0; ii < grid.length; ii++) {
			for (int jj = 0; jj < grid[0].length; jj++) {

				int cellValue = grid[ii][jj].getCellValue();
				int anchorRow = getAnchor(ii);
				int anchorColumn = getAnchor(jj);

				if (cellValue != 0) {

					for (int row = anchorRow; row < (anchorRow + Math.sqrt(dimension)); row++) {
						for (int col = anchorColumn; col < (anchorColumn + Math.sqrt(dimension)); col++) {
							if (cellValue == grid[row][col].getCellValue() && (row != ii || col != jj)) {
								numSubGridErrors++;
							}
						}
					}

					for (int j = 0; j < grid.length; j++) {
						if (cellValue == grid[ii][j].getCellValue() && (j != jj)) {
							numRowErrors++;
						}
					}

					for (int i = 0; i < grid.length; i++) {
						if (cellValue == grid[i][jj].getCellValue() && (i != ii)) {
							numColumnErrors++;
						}
					}
				}
			}
		}

		totalErrors = numSubGridErrors + numColumnErrors + numRowErrors;

		String error1 = ("\n errors within subgrids are: " + numSubGridErrors);
		String error2 = ("\n errors within columns are: " + numColumnErrors);
		String error3 = ("\n errors within rows are: " + numRowErrors + "\n");
		return error1 + error2 + error3;
	}

	private static int numberOfErrors() {

		int numSubGridErrors = 0;
		int numColumnErrors = 0;
		int numRowErrors = 0;

		for (int ii = 0; ii < grid.length; ii++) {
			for (int jj = 0; jj < grid[0].length; jj++) {

				int cellValue = grid[ii][jj].getCellValue();
				int anchorRow = getAnchor(ii);
				int anchorColumn = getAnchor(jj);

				if (cellValue != 0) {

					for (int row = anchorRow; row < (anchorRow + Math.sqrt(dimension)); row++) {
						for (int col = anchorColumn; col < (anchorColumn + Math.sqrt(dimension)); col++) {
							if (cellValue == grid[row][col].getCellValue() && (row != ii || col != jj)) {
								numSubGridErrors++;
							}
						}
					}

					for (int j = 0; j < grid.length; j++) {
						if (cellValue == grid[ii][j].getCellValue() && (j != jj)) {
							numRowErrors++;
						}
					}

					for (int i = 0; i < grid.length; i++) {
						if (cellValue == grid[i][jj].getCellValue() && (i != ii)) {
							numColumnErrors++;
						}
					}
				}
			}
		}

		totalErrors = numSubGridErrors + numColumnErrors + numRowErrors;
		return totalErrors;
	}

	/**
	 * This method returns a Cell object which has the first occurrence of the
	 * lowest possible numbers in the grid
	 * 
	 * @return Cell object
	 * @throws IOException
	 */
	private static void setCellGuess() throws IOException {
		Cell[][] tempGrid = new Cell[dimension][dimension];
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				tempGrid[i][j] = new Cell(dimension);
				if (grid[i][j].valueWasSet() == true) {
					tempGrid[i][j].setCellValue(grid[i][j].getCellValue());
					for (int ii = 0; ii < tempGrid[i][j].getPossibleNumsArrayList().size(); ii++) {
						if (tempGrid[i][j].getPossibleNumsArrayList().get(ii) != tempGrid[i][j].getCellValue()) {
							tempGrid[i][j].setValueToFalse(tempGrid[i][j].getPossibleNumsArrayList().get(ii));
						}
					}
				}
			}
		}

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				updateGrid(tempGrid, i, j);
			}
		}
		writer.write("\n grid state about to be SAVED, gridStates size: " + gridStates.size() + "\n");

		gridStates.add(tempGrid);
		writer.write("\n grid state was SAVED, gridStates size: " + gridStates.size() + "\n");
		writer.write("\n grid before guess\n" + printGrid());
		Cell tempCell = new Cell(dimension);
		int row = 0;
		int col = 0;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].getValueWasSet() == false) {
					if (grid[i][j].getPossibleNumsArrayList().size() > 1) {
						if (grid[i][j].getNumPossible() <= tempCell.getNumPossible()) {
							tempCell = grid[i][j];
							//System.out.println("grid[" + i + "][" + j + "] possible nums: " + tempCell.getPossibleNumsArrayList() + "\n");
							row = i;
							col = j;
						}
					}
				}
			}
		}
		//System.out.println("cell at row: " + row + ", col: " + col + " was chosen, it has " + grid[row][col].getNumPossible() + " possible values: " + grid[row][col].getPossibleNumsArrayList() + "\n");
		//System.exit(0);
		int guess = grid[row][col].getPossibleNumsArrayList().get(0);
		writer.write("\n cellForGuessing is: row " + row + ", col " + col + ", possible nums array list = " + grid[row][col].getPossibleNumsArrayList().toString() + ", guess = " + guess + "\n");
		grid[row][col].setCellValue(guess);
		for (int i = 0; i < grid[row][col].getPossibleNumsArrayList().size(); i++) {
			if (grid[row][col].getPossibleNumsArrayList().get(i) != guess) {
				grid[row][col].setValueToFalse(grid[row][col].getPossibleNumsArrayList().get(i));
			}
		}

		int[] tempGuess = new int[4];
		tempGuess[0] = row;
		tempGuess[1] = col;
		tempGuess[2] = guess;
		tempGuess[3] = numSet;
		guessStates.add(tempGuess);
		numSet++;
		previousState += 1;
		updateGrid(row, col);
		writer.write("\n updated grid after guess\n" + printGrid());
		//System.exit(0);
	}
}