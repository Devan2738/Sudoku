import java.util.ArrayList;

/**
 * This is the Cell object class It contains the constructor, variables, and methods for Cell objects. 
 * Cell objects are each at individual locations, with unique row and column indices within the Sudoku puzzle 
 * These objects populate the two-dimensional the 'grid' array in the Solver.java class 
 * 
 * definition of 'subgrid': a set of 3x3 cell objects in the 'grid' in the Solver.java class, 
 * each 'subgrid' contains the numbers 1 through 9
 * 
 * @author devankarsann
 */

public class Cell {

	// Cell objects are treated differently in Solver.java if their value has not been set
	public boolean valueWasSet = false;

	// Each Cell object initially has nine possible numbers
	// These possible numbers are set to false when a value in a neighboring cell is set
	public boolean[] possibleNums = new boolean[9];

	// The value for each cell is initially zero.
	public int value = 0;

	/**
	 * This is the constructor method for a Cell object. All values in the
	 * boolean array 'possibleNums' are initialized to true.
	 */
	public Cell() {
		for (int i = 0; i < 9; i++)
			possibleNums[i] = true;
	}

	/**
	 * This method sets the value of a Cell object, and would be called in
	 * Solver.java after a solution is found. The possibilities of all numbers
	 * for this cell are set to false because its absolute value is known.
	 * 
	 * @param value, number which is to be assigned to a Cell object
	 */
	public void setCellValue(int value) {
		this.value = value;
		for (int i = 0; i < 9; i++) {
			possibleNums[i] = false;

		}
		valueWasSet = true;
	}

	/**
	 * This method sets the possibility of a Cell object at the specified index
	 * to be false, this method is called within the 'updateGrid' method in
	 * Solver.java
	 * 
	 * @param index, value within a Cell object 'possibleNums' boolean array
	 */
	public void setValueToFalse(int index) {
		possibleNums[index - 1] = false;
	}

	/**
	 * This method finds the value which a cell needs to be to fulfill the
	 * requirements of the numbers 1 through 9 appearing in each row, column,
	 * and subgrid.
	 * 
	 * @param list, ArrayList of non-repeating integers made up of known cell
	 * values in the same row, column, or subgrid as a given cell
	 */
	public void findCellValueFromArrayList(ArrayList<Integer> list) {
		for (int i = 0; i < 9; i++) {
			if (!list.contains(i + 1)) {
				setCellValue(i + 1);
			}
		}
	}
	
	/**
	 * This method returns the true/false possibility of a Cell object being a
	 * specific value (i.e. index + 1).
	 * 
	 * @param index, value within a Cell object 'possibleNums' boolean array
	 * @return boolean value
	 */
	public boolean getPossibleNum(int index) {
		return possibleNums[index];
	}

	/**
	 * This method returns the value of a Cell object.
	 * 
	 * @return integer value of Cell object
	 */
	public int getCellValue() {
		return value;
	}

	/**
	 * This method returns boolean value representing whether or not the value
	 * of a Cell was set (i.e. true if the value is not zero).
	 * 
	 * @return boolean value
	 */
	public boolean getValueWasSet() {
		return valueWasSet;
	}
}
