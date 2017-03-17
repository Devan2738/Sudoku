import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the driver class of my Sudoku solving program. This class contains
 * the main method and helper methods which are necessary for finding and
 * setting values of Cell objects
 * 
 * definition of 'subgrid': a set of 3x3 cell objects in the 'grid' in the
 * Solver.java class, each 'subgrid' contains the numbers 1 through 9
 * 
 * @author devankarsann
 *
 */
public class Solver {

	// These variables are declared globally for increased visibility, so
	// methods outside
	// of the main method have access to them.

	// 'grid' is a two-dimensional array of Cell objects
	static Cell[][] grid;

	// 'cellNotNums' is an ArrayList of Integer objects
	// This ArrayList is used when finding the union of cell values in the same
	// row, column, or subgrid as a given Cell object
	static ArrayList<Integer> cellNotNums;

	// the main method will continue using the two included solving techniques
	// until all 81 values are found
	// when a value is set, 'numSet' is incremented by one
	static int numSet = 0;

	/**
	 * This is the main method for my Sudoku solving program. A file and scanner
	 * object are made read input from the specified text file. Two different
	 * solution techniques are used to solve Sudoku puzzles.
	 * 
	 * DISCLAIMER: This program currently solves easy to medium Sudoku puzzles.
	 * More solution techniques will be designed and programmed in the in order
	 * to be able to solve difficult to advanced puzzles.
	 * 
	 * @param args, no arguments are currently needed to run this program
	 * @throws FileNotFoundException, accounts for possibility of invalid file name
	 */
	public static void main(String[] args) throws FileNotFoundException {

		// a file and scanner object are made based on the name of the text file entered
		File sample = new File(args[0]);
		Scanner scan = new Scanner(sample);

		// The number of rows and columns in the Sudoku puzzle are read from the first line of the text file
		// 'inputNumRows' and 'inputNumCols' should always be 9 for traditional Sudoku puzzles (dimension of 9x9)
		int inputNumRows = scan.nextInt();
		int inputNumColumns = scan.nextInt();

		// the 'numLooped' variable was used during debugging to limit the
		// amount of times the solution techniques were used
		// in the event of an infinite loop occurring due to neither solution
		// technique finding cell values
		int numLooped = 0;

		// the two-dimensional 'grid' array of Cell objects is instantiated
		// based on the dimensions read from the text file
		grid = new Cell[inputNumRows][inputNumColumns];

		// these two for-loops read input from the given text file
		for (int i = 0; i < inputNumRows; i++) {
			for (int j = 0; j < inputNumColumns; j++) {
				// before setting the value of a Cell object, it needs to be
				// instantiated
				grid[i][j] = new Cell();
				int currentInt = scan.nextInt();
				// the default value of a Cell object is zero
				// its value should only be set when the input is not zero for
				// that cell
				if (currentInt != 0) {
					grid[i][j].setCellValue(currentInt);
					numSet++;
				}
			}
		}

		scan.close();

		// information about the original grid is printed to the console
		System.out.println("original grid");
		printGrid();
		//checkGridForErrors();

		// these for-loops takes the set values of Cell objects and updates the
		// 'possibleNums' boolean array of neighboring cells
		for (int i = 0; i < inputNumRows; i++) {
			for (int j = 0; j < inputNumColumns; j++) {
				// if the value of a Cell object was not set, that Cell object
				// will not update the grid
				if (grid[i][j].getValueWasSet() == true) {
					updateGrid(i, j);
				}
			}
		}

		// while (numLooped >= 0 && numLooped <= 1){
		while (numSet >= 0 && numSet < 81) {

			boolean found = false;

			// start of first solution technique

			// this is the preferred solution method because it is faster at
			// finding cell values
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					// solution techniques are only used for Cell objects which
					// have not been set
					if (grid[i][j].getValueWasSet() == false) {
						// the cellNotNums ArrayList is re-instantiated for each
						// Cell object in the grid
						cellNotNums = new ArrayList<Integer>();
						populateArrayList(i, j);
						// if the union of the known values (1 to 9) in a Cell
						// object's row, column, and subgrid are 8, there is one
						// missing number from 1 to 9
						if (cellNotNums.size() == 8) {
							grid[i][j].findCellValueFromArrayList(cellNotNums);
							numSet++;
							found = true;
							// if a value is set, we will update the grid
							updateGrid(i, j);
						}
					}
				}
			}
			// end of first solution technique

			// start of the second solution technique

			// this solution technique is slower than the first technique,
			// it is not used unless the first solution technique was not
			// successful in setting a Cell object's value
			// each subgrid is checked to see if there is only one possible
			// location in that subgrid for a number from 1 to 9
			if (found == false) {
				for (int i = 0; i < inputNumRows; i++) {
					for (int j = 0; j < inputNumColumns; j++) {
						// solution techniques are only used for Cell objects
						// which have not been set
						if (grid[i][j].getValueWasSet() == false) {
							// 'k' represents index values in the 'possibleNums'
							// boolean array of each Cell object
							for (int k = 0; k < 9; k++) {

								// this variable is incremented when the value
								// of 'k' in the possibleNums' boolean array a
								// Cell object is false
								int numNotK = 0;

								// 'anchorRow' and 'anchorColumn' represent the
								// index values of the top-left Cell of each
								// subgrid
								int anchorRow = getAnchor(i);
								int anchorColumn = getAnchor(j);

								// for all rows in subgrid
								for (int row = anchorRow; row < (anchorRow + 3); row++) {
									// for all columns in subgrid
									for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
										if (grid[row][col].getPossibleNum(k) == false) {
											numNotK++;
										}
									}
								}

								// Cell objects are set to a value if the other
								// eight Cell objects in its subgrid cannot be
								// that value
								if (numNotK == 8 && grid[i][j].getPossibleNum(k) == true) {
									grid[i][j].setCellValue(k + 1);
									updateGrid(i, j);
									numSet++;
									found = true;
									break;
								}
							}
						}
					}
				}
			}
			// end of the second solution technique

			// numLooped++;
		}

		// information about the completed grid is printed to the console
		System.out.println("\ncompleted grid");
		printGrid();
		checkGridForErrors();
	}

	/**
	 * This method returns the row or column index of the top-left Cell object
	 * in any given subgrid.
	 * 
	 * @param num, integer value representing row or column of a Cell object
	 * @return integer value
	 */
	// get the i or j value of the top left cell of each sub grid
	private static int getAnchor(int num) {
		while (num % 3 != 0)
			num--;
		return num;
	}

	/**
	 * This method finds the union of known values in a Cell object's row,
	 * column and subgrid. The 'cellNotNums' ArrayList is declared globally and
	 * reset for each Cell object, so nothing needs to be returned.
	 * 
	 * @param ii, row index of the given Cell object
	 * @param jj, column index of the given Cell object
	 */
	private static void populateArrayList(int ii, int jj) {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		// finding union of 'cellNotNums' and set values of integers from the
		// same subgrid as the given Cell object
		for (int row = anchorRow; row < (anchorRow + 3); row++) {
			for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
				if (!cellNotNums.contains((Object) grid[row][col].getCellValue()) && grid[row][col].valueWasSet == true)
					cellNotNums.add(grid[row][col].getCellValue());
			}
		}

		// finding union of 'cellNotNums' and set values of integers from the
		// same column as the given Cell object
		for (int i = 0; i < grid.length; i++) {
			if (!cellNotNums.contains((Object) grid[i][jj].getCellValue()) && grid[i][jj].valueWasSet == true) {
				cellNotNums.add(grid[i][jj].getCellValue());
			}
		}

		// finding union of 'cellNotNums' and set values of integers from the
		// same row as the given Cell object
		for (int j = 0; j < grid.length; j++) {
			if (!cellNotNums.contains((Object) grid[ii][j].getCellValue()) && grid[ii][j].valueWasSet == true) {
				cellNotNums.add(grid[ii][j].getCellValue());
			}
		}
	}

	/**
	 * This method updates the possible values of other Cell objects in the same
	 * row, column, or subgrid as the Cell object represented by the indices
	 * entered.
	 * 
	 * @param ii, row index of the given Cell object
	 * @param jj, column index of the given Cell object
	 */
	private static void updateGrid(int ii, int jj) {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		int cellValue = grid[ii][jj].getCellValue();

		// updating true/false values for other cells in the same subgrid
		for (int row = anchorRow; row < (anchorRow + 3); row++) {
			for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
				if (row != ii || col != jj) {
					grid[row][col].setValueToFalse(cellValue);
				}
			}
		}

		// updating true/false values for other Cell objects in the same column
		for (int i = 0; i < grid.length; i++) {
			// if (i == 5 & jj == 7)
			// System.out.println("(5,7) cell warning");
			if (i != ii) {
				grid[i][jj].setValueToFalse(cellValue);
			}
		}

		// updating true/false values for other Cell objects in the same row
		for (int j = 0; j < grid.length; j++) {
			if (j != jj) {
				grid[ii][j].setValueToFalse(cellValue);
			}
		}
	}

	/**
	 * This method prints information to the console about the 'grid' array and
	 * the values in the grid.
	 */
	private static void printGrid() {
		String str = "";
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (j == 2 || j == 5) {
					str += grid[i][j].getCellValue() + "|";
				} else if ((i == 2 || i == 5) && j != 8) {
					str += grid[i][j].getCellValue() + "_";
				} else {
					str += grid[i][j].getCellValue() + " ";
				}
			}
			if (i != 8)
				str += ("\n");
		}
		System.out.println(getNumSet() + "\n" + str);
	}

	/**
	 * This methods checks for the amount of different kinds of errors and
	 * prints this information to the console.
	 */
	private static void checkGridForErrors() {

		int numSubGridErrors = 0;
		int numColumnErrors = 0;
		int numRowErrors = 0;

		for (int ii = 0; ii < grid.length; ii++) {
			for (int jj = 0; jj < grid[0].length; jj++) {

				int cellValue = grid[ii][jj].getCellValue();
				int anchorRow = getAnchor(ii);
				int anchorColumn = getAnchor(jj);

				// if the value of a cell was not set, it would not have any
				// errors
				if (cellValue != 0) {

					// checking for errors withing the same subgrids
					for (int row = anchorRow; row < (anchorRow + 3); row++) {
						for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
							if (cellValue == grid[row][col].getCellValue() && (row != ii || col != jj)) {
								numSubGridErrors++;
							}
						}
					}

					// checking for errors within the same rows
					for (int j = 0; j < grid.length; j++) {
						if (cellValue == grid[ii][j].getCellValue() && (j != jj)) {
							numRowErrors++;
						}
					}

					// checking for errors within the same columns
					for (int i = 0; i < grid.length; i++) {
						if (cellValue == grid[i][jj].getCellValue() && (i != ii)) {
							numColumnErrors++;
						}
					}
				}
			}
		}

		System.out.println("\nerrors within subgrids are: " + numSubGridErrors);
		System.out.println("errors within columns are: " + numColumnErrors);
		System.out.println("errors within rows are: " + numRowErrors);
	}

	/**
	 * This method returns the current number of set values in the 'grid'
	 * 
	 * @return String, information about 'numSet' variable
	 */
	private static String getNumSet() {
		return "number cell values set: " + numSet + "\n";
	}
}
