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

	// Used to write solution steps to a text file.
	static FileWriter writer;

	// This variable represents a two-dimensional array of Cell objects.
	static Cell[][] grid;

	// This ArrayList is used when finding the union of cell values neighboring a given Cell.
	static ArrayList<Integer> cellNotNums;
	
	// This variable represents the number of set values in the Sudoku puzzle.
	static int numSet = 0;

	// Row and column indices of 'pair' Cell objects used in solution technique three.
	static int discoveredPairRow;
	static int discoveredPairColumn;
	static int discoveredPairRow2;
	static int discoveredPairColumn2;

	// The number of errors in the grid is calculated before and after solution techniques are used.
	static int totalErrors = 0;

	static boolean cellObjectsAreStillAPair;

	/**
	 * This is the main method for my Sudoku solving program. A file and scanner
	 * object read input from the specified text file entered as an argument. 
	 * Three different solution techniques are used to solve Sudoku puzzles.
	 * The solution steps are written to 'SolutionSteps.txt'.
	 * 
	 * DISCLAIMER: This program currently solves the puzzles in the first four sample input files.
	 * 'SampleInput5.txt' is included as an example of a Sudoku puzzle which cannot currently be solved.
	 * More solution techniques will be integrated in the future to be able to solve advanced puzzles.
	 *
	 * @param args, the filename/filepath of a plain text file formatted by specifications in attached README
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		boolean found = true;

		// A new file object is created.
		File file = new File("SolutionSteps.txt");
		if (file.createNewFile()) {
		} else {
		}
		
		writer = new FileWriter(file);

		File sample = new File(args[0]);
		Scanner scan = new Scanner(sample);

		// These values are expected to be 9, but will read in other values when the program is able to solve larger puzzles.
		int inputNumRows = scan.nextInt();
		int inputNumColumns = scan.nextInt();

		// The 'numLooped' variable was used to prevent an infinite loop from occurring.
		// This could happen in the event of the program not being able to find all cell values.
		int numLooped = 0;

		// The two-dimensional 'grid' array of Cell objects is instantiated based on the dimensions read from the text file.
		grid = new Cell[inputNumRows][inputNumColumns];

		// These two for-loops read input from the given text file.
		for (int i = 0; i < inputNumRows; i++) {
			for (int j = 0; j < inputNumColumns; j++) {
				// Before setting the value of a Cell object, it needs to be instantiated
				grid[i][j] = new Cell();
				int currentInt = scan.nextInt();
				// The default value of a Cell object is zero.
				// Its value should only be set when the input is not zero for that cell
				if (currentInt != 0) {
					grid[i][j].setCellValue(currentInt);
					numSet++;
				}
			}
		}
		// The 'scan' object is no longer needed by the program.
		scan.close();

		// Information about the original grid is printed to the console.
		System.out.println("\n original grid\n");
		System.out.println(printGrid());
		System.out.println(checkGridForErrors());

		// Information about the original grid is written to the text file.
		writer.write("\n original grid\n\n");
		writer.write(printGrid());
		writer.write("\n" + checkGridForErrors());

		// This program will only attempt to solve the entered puzzle if there are no initial errors.
		if (totalErrors == 0) {
			// These for-loops takes the set values of Cell objects and updates the 'possibleNums' boolean array of neighboring Cells.
			for (int i = 0; i < inputNumRows; i++) {
				for (int j = 0; j < inputNumColumns; j++) {
					// Cells will only update the grid if their value was set.
					if (grid[i][j].getValueWasSet() == true) {
						updateGrid(i, j);
					}
				}
			}
			int tempNumSet = numSet;

			// The maximum number of times the solution techniques would need to loop through the 'grid' array of Cells is 64.
			// This was determined by subtracting the minimum number of values needed to solve a puzzle (17) from the total number of Cells (81).
			while (numLooped >= 0 && numLooped <= 64) {

				// Updated information will be written to SolutionSteps.txt if a solution technique was recently used.
				if (found == true) {
					if (tempNumSet == numSet) {
						writer.write("\n ..updated known values and possible values shown below..");
						writer.write(printGiantGrid());

					}
					if (tempNumSet != numSet) {
						writer.write("\n\n ..looping through solution techniques again..");
						writer.write("\n ..updated known and possible values shown below..");
						writer.write(printGiantGrid());
					}
				}
				found = false;

				// start of first solution technique
				// this is the preferred solution method because it is faster at finding cell values
				for (int i = 0; i < grid.length; i++) {
					for (int j = 0; j < grid[0].length; j++) {
						// solution techniques are only used for Cell objects with no set value
						if (grid[i][j].getValueWasSet() == false) {
							// the cellNotNums ArrayList is re-instantiated each time a Cell is analyzed
							cellNotNums = new ArrayList<Integer>();
							// this method uses the union of Cell values to add integers to an ArrayList 
							populateArrayList(i, j);
							// if the size of the ArrayList is 8, the Cell can be definitively found
							if (cellNotNums.size() == 8) {
								grid[i][j].findCellValueFromArrayList(cellNotNums);
								writer.write("\n solution technique one used: grid(" + i + "," + j + ") = " + grid[i][j].getCellValue());
								numSet++;
								found = true;
								updateGrid(i, j);
							}
						}
					}
				}
				// end of first solution technique

				// start of the second solution technique
				// It is only used if the first solution technique was not successful in setting a Cell's value.
				// Each subgrid is checked to see if there is only one possible location for a value.
				if (found == false) {
					for (int i = 0; i < inputNumRows; i++) {
						for (int j = 0; j < inputNumColumns; j++) {
							if (grid[i][j].getValueWasSet() == false) {
								// 'k' represents index values in the 'possibleNums' boolean array of each Cell
								for (int k = 0; k < 9; k++) {
									
									// 'numNotK' will be incremented when the value of 'k' in the possibleNums' boolean array of a Cell is false
									int numNotK = 0;

									// 'anchorRow' and 'anchorColumn' represent the index values of the top-left Cell of each subgrid
									int anchorRow = getAnchor(i);
									int anchorColumn = getAnchor(j);
									
									for (int row = anchorRow; row < (anchorRow + 3); row++) {
										for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
											if (grid[row][col].getPossibleNum(k) == false) {
												numNotK++;
											}
										}
									}
									
									// Cells are set to a value if the other eight Cells in its subgrid cannot be that value
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

				// start of the third solution technique
				// This solution technique is only used if the first and second solution techniques were not successful.
				// It checks to see if there are a pair of possible values in a row, column, or subgrid.
				// This means the possible values in those subgrids can be updated.
				if (found == false) {
					for (int i = 0; i < inputNumRows; i++) {
						for (int j = 0; j < inputNumColumns; j++) {
							if (grid[i][j].getValueWasSet() == false) {
								if (grid[i][j].getPossibleNumsArrayList().size() == 2) {
									if (checkRowForPair(i, j) == true) {
										// writer.write("\n pair found in row " + i);
										// update subgrid 1 with value 1
										updateGrid(i, j, grid[i][j].getPossibleNumsArrayList().get(0));
										// update subgrid 1 with value 2
										updateGrid(i, j, grid[i][j].getPossibleNumsArrayList().get(1));
										// update subgrid 2 with value 1
										updateGrid(discoveredPairRow, discoveredPairColumn,
												grid[i][j].getPossibleNumsArrayList().get(0));
										// update subgrid 2 with value 2
										updateGrid(discoveredPairRow, discoveredPairColumn,
												grid[i][j].getPossibleNumsArrayList().get(1));
									} else if (checkColumnForPair(i, j) == true) {
										// writer.write("\n pair found in row " + i);
										// update subgrid 1 with value 1
//										   updateGrid(i, j, grid[i][j].getPossibleNumsArrayList().get(0));
										// update subgrid 1 with value 2
//										   updateGrid(i, j, grid[i][j].getPossibleNumsArrayList().get(1));
										// update subgrid 2 with value 1
//										   updateGrid(discoveredPairRow, discoveredPairColumn, grid[i][j].getPossibleNumsArrayList().get(0));
										// update subgrid 2 with value 2
//										   updateGrid(discoveredPairRow, discoveredPairColumn, grid[i][j].getPossibleNumsArrayList().get(1));
									} else if (checkSubgridForPair(i, j) == true) {
										// update subgrid 1 with value 2
										updateGrid(discoveredPairRow, discoveredPairColumn, discoveredPairRow2,	discoveredPairColumn2, grid[i][j].getPossibleNumsArrayList().get(0));
										// update subgrid 1 with value 2
										updateGrid(discoveredPairRow, discoveredPairColumn, discoveredPairRow2, discoveredPairColumn2, grid[i][j].getPossibleNumsArrayList().get(1));
									}
								}
							}
						}
					}
				}
				// end of the third solution technique

				// start of fourth solution technique
				if (found == false) {
					// add fouth solution technique here
				}
				// end of the fourth solution technique

				numLooped++;
			}

			// Information about the completed grid is printed to the console and the text file containing solution steps.
			// The information presented depends on the amount of values set.
			
			if (numSet == 81) {
				System.out.println(" completed grid\n");
				System.out.println(printGrid());
				System.out.println(checkGridForErrors());
				writer.write("\n completed grid\n\n");
				writer.write(printGrid());
				writer.write("\n" + checkGridForErrors());

			} else {
				System.out.println("\n incomplete grid");
				System.out.println(printGiantGrid());
				System.out.println(" there are " + (81 - numSet) + " unknown values");
				System.out.println(checkGridForErrors());
				writer.write("\n\n incomplete grid\n\n");
				writer.write(printGrid());
				writer.write("\n\n there are " + (81 - numSet) + " unknown values");
				writer.write("\n" + checkGridForErrors());
			}
		} else {
			System.out.println("\nan error was detected in your input\nplease check your entered values\nafter changing the input, run the program again");
			writer.write("\nan error was detected in your input\nplease check your entered values\nafter changing the input, run the program again");
		}
		writer.close();
	}

	/**
	 * This method returns a String containing all known and possible values of the puzzle.
	 * Specific formatting is used for increased readability.
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

					if (temp.valueWasSet == true)
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
	 * This method return a boolean value depending on whether a Cell has a pair in its column.
	 * The pair would need to have a possibleNumsArrayList of size two containing the same integers.
	 * 
	 * @param row of Cell with two possible values
	 * @param column of Cell with two possible values
	 * 
	 * @return boolean
	 */
	private static boolean checkColumnForPair(int row, int col) {

		cellObjectsAreStillAPair = false;
		int originalCellRowAnchor = getAnchor(row);

		for (int i = 0; i < grid.length; i++) {
			int possiblePairRowAnchor = getAnchor(i);
			if (Math.abs(originalCellRowAnchor - possiblePairRowAnchor) >= 3) {
				if (grid[i][col].getPossibleNumsArrayList().size() == 2) {
					if (grid[i][col].getPossibleNumsArrayList().contains((Object) grid[row][col].getPossibleNumsArrayList().get(0))
							&& grid[i][col].getPossibleNumsArrayList().contains((Object) grid[row][col].getPossibleNumsArrayList().get(1))) 
					{
						cellObjectsAreStillAPair = true;
						discoveredPairRow = i;
						discoveredPairColumn = col;
					}
				}
			}
		}
		return cellObjectsAreStillAPair;
	}

	/**
	 * This method return a boolean value depending on whether a Cell has a pair in its row.
	 * The pair would need to have a possibleNumsArrayList of size two containing the same integers.
	 * 
	 * @param row of Cell with two possible values
	 * @param column of Cell with two possible values
	 * 
	 * @return boolean
	 */
	private static boolean checkRowForPair(int row, int col) throws IOException {

		cellObjectsAreStillAPair = false;
		int originalCellColAnchor = getAnchor(col);

		for (int j = 0; j < grid.length; j++) {
			int possiblePairColAnchor = getAnchor(j);
			if (Math.abs(originalCellColAnchor - possiblePairColAnchor) >= 3) {
				if (grid[row][j].getPossibleNumsArrayList().size() == 2) {
					if (grid[row][j].getPossibleNumsArrayList().contains((Object) grid[row][col].getPossibleNumsArrayList().get(0))
							&& grid[row][j].getPossibleNumsArrayList().contains((Object) grid[row][col].getPossibleNumsArrayList().get(1))) 
					{
						discoveredPairRow = row;
						discoveredPairColumn = j;
						cellObjectsAreStillAPair = true;
					}
				}
			}
		}
		return cellObjectsAreStillAPair;
	}

	/**
	 * This method return a boolean value depending on whether a Cell has a pair in its subgrid.
	 * The pair would need to have a possibleNumsArrayList of size two containing the same integers.
	 * 
	 * @param row of Cell with two possible values
	 * @param column of Cell with two possible values
	 * 
	 * @return boolean
	 */
	private static boolean checkSubgridForPair(int ii, int jj) throws IOException {

		cellObjectsAreStillAPair = false;

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		for (int row = anchorRow; row < anchorRow + 3; row++) {
			for (int col = anchorColumn; col < anchorColumn + 3; col++) {
				if (row != ii || col != jj) {
					if (grid[row][col].getPossibleNumsArrayList().size() == 2) {
						if (grid[row][col].getPossibleNumsArrayList().contains((Object) grid[ii][jj].getPossibleNumsArrayList().get(0))
								&& grid[row][col].getPossibleNumsArrayList().contains((Object) grid[ii][jj].getPossibleNumsArrayList().get(1))) 
						{
							cellObjectsAreStillAPair = true;
							discoveredPairRow = ii;
							discoveredPairColumn = jj;
							discoveredPairRow2 = row;
							discoveredPairColumn2 = col;
						}
					}
				}
			}
		}
		return cellObjectsAreStillAPair;

	}

	/**
	 * This method returns the row or column index of the top-left Cell in any given subgrid.
	 * 
	 * @param num, integer value representing row or column of a Cell
	 * 
	 * @return integer 
	 */
	private static int getAnchor(int num) {
		while (num % 3 != 0)
			num--;
		return num;
	}

	/**
	 * This method finds the union of known values in a Cell's row, column and subgrid. 
	 * The 'cellNotNums' ArrayList is declared globally and reset for each Cell object.
	 * No values need to be returned.
	 * 
	 * @param ii, row index of the given Cell
	 * @param jj, column index of the given Cell
	 */
	private static void populateArrayList(int ii, int jj) {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		// finding union of 'cellNotNums' and set values of integers from the same subgrid as the given Cell object
		for (int row = anchorRow; row < (anchorRow + 3); row++) {
			for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
				if (!cellNotNums.contains((Object) grid[row][col].getCellValue()) && grid[row][col].valueWasSet == true)
					cellNotNums.add(grid[row][col].getCellValue());
			}
		}

		// finding union of 'cellNotNums' and set values of integers from the same column as the given Cell object
		for (int i = 0; i < grid.length; i++) {
			if (!cellNotNums.contains((Object) grid[i][jj].getCellValue()) && grid[i][jj].valueWasSet == true) {
				cellNotNums.add(grid[i][jj].getCellValue());
			}
		}

		// finding union of 'cellNotNums' and set values of integers from the same row as the given Cell object
		for (int j = 0; j < grid.length; j++) {
			if (!cellNotNums.contains((Object) grid[ii][j].getCellValue()) && grid[ii][j].valueWasSet == true) {
				cellNotNums.add(grid[ii][j].getCellValue());
			}
		}
	}

	/**
	 * This method updates the possible values of other Cell objects only in the
	 * same subgrid
	 * 
	 * @param ii, row index of the given Cell
	 * @param jj, column index of the given Cell
	 * @param i, row index of second Cell in subgrid
	 * @param j, column index of second Cell in subgrid
	 * @param value, value which Cell in the same subgrid cannot be
	 * @throws IOException
	 */
	private static void updateGrid(int ii, int jj, int i, int j, int value) throws IOException {

		// System.out.println("value used in new updateGrid method: " + value);

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		// updating true/false values for other cells in the same subgrid
		for (int row = anchorRow; row < (anchorRow + 3); row++) {
			for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
				if ((row != ii || col != jj) && (row != i || col != j)) {
					if (grid[row][col].valueWasSet == false && grid[row][col].getPossibleNum(value - 1) == true)
						grid[row][col].setValueToFalse(value);
				}
			}
		}
	}

	/**
	 * This method updates the possible values of other Cell objects only in the
	 * same subgrid
	 * 
	 * @param ii, row index of the given Cell
	 * @param jj, column index of the given Cell
	 * @param value, value which Cell in the same subgrid cannot be
	 * @throws IOException
	 */
	private static void updateGrid(int ii, int jj, int value) throws IOException {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		for (int row = anchorRow; row < (anchorRow + 3); row++) {
			for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
				if (row != ii || col != jj) {
					if (grid[row][col].valueWasSet == false && grid[row][col].getPossibleNum(value - 1) == true)
						grid[row][col].setValueToFalse(value);
				}
			}
		}
	}

	/**
	 * This method updates the possible values of the neighboring Cells of a given Cell.
	 * 
	 * @param ii, row index of the given Cell
	 * @param jj, column index of the given Cell
	 * @throws IOException
	 */
	private static void updateGrid(int ii, int jj) throws IOException {

		int anchorRow = getAnchor(ii);
		int anchorColumn = getAnchor(jj);

		int cellValue = grid[ii][jj].getCellValue();

		// updating true/false values for other Cells in the same subgrid
		for (int row = anchorRow; row < (anchorRow + 3); row++) {
			for (int col = anchorColumn; col < (anchorColumn + 3); col++) {
				if (row != ii || col != jj) {
					if (grid[row][col].valueWasSet == false)
						grid[row][col].setValueToFalse(cellValue);
				}
			}
		}

		// updating true/false values for other Cells in the same column
		for (int i = 0; i < grid.length; i++) {
			if (i != ii) {
				if (grid[i][jj].valueWasSet == false) {
					grid[i][jj].setValueToFalse(cellValue);
				}
			}
		}

		// updating true/false values for other Cells in the same row
		for (int j = 0; j < grid.length; j++) {
			if (j != jj) {
				if (grid[ii][j].valueWasSet == false) {
					grid[ii][j].setValueToFalse(cellValue);
				}
			}
		}
	}

	/**
	 * This method prints information to the console about the values in the 'grid' array.
	 * 
	 * @return String
	 */
	private static String printGrid() {
		String str = "";
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (j == 0) {
					str += " ";
				}
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
		return str;
	}

	/**
	 * This methods checks for the amount of different kinds of errors.
	 * This information is returned as a String.
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

		totalErrors = numSubGridErrors + numColumnErrors + numRowErrors;

		String error1 = ("\n errors within subgrids are: " + numSubGridErrors);
		String error2 = ("\n errors within columns are: " + numColumnErrors);
		String error3 = ("\n errors within rows are: " + numRowErrors + "\n");
		return error1 + error2 + error3;
	}

	/**
	 * This method returns the current number of set values in the 'grid'
	 * 
	 * @return String, information about 'numSet' variable
	 */
	private static String getNumSet() {
		return "\nnumber cell values set: " + numSet;
	}
}