import java.util.ArrayList;

/**
 * This is the Cell object class. It contains the constructor, variables, and
 * methods for Cell objects. Cell objects are each at individual locations, with
 * unique row and column indices within the two-dimensional the 'grid' array
 * in the Solver.java class
 * 
 * definition of 'subgrid': a group of 3x3 cell objects, each cell having a 
 * unique value from numbers 1 to 9
 * 
 * definition of 'neighboring cell': Cell objects within the same row, column, or 
 * subgrid as another Cell object
 * @author devankarsann
 */

public class Cell {

	// The solution techniques ignore Cell whose value has already been set.
	public boolean valueWasSet = false;

	// Each Cell object initially has nine possible numbers
	// These possible numbers are set to false when a value in a neighboring cell is set
	public boolean[] possibleNumsBoolean = new boolean[9];

	// this ArrayList will be used for the third solution technique
	// Values will be removed when possible values for a Cell are updated
	// All values will be removed when the value of a Cell is set
	public ArrayList<Integer> possibleNumsArrayList = new ArrayList<Integer>();

	// The value for each Cell is initially zero.
	public int value = 0;

	/**
	 * This is the constructor method for a Cell. All values in the
	 * boolean array 'possibleNumsBoolean' are initialized to true.
	 */
	public Cell() {
		for (int i = 0; i < 9; i++) {
			possibleNumsBoolean[i] = true;
			possibleNumsArrayList.add(i + 1);
		}
	}

	/**
	 * This method sets the value of a Cell, and would be called in Solver.java 
	 * after a solution is found. The possibilities of all numbers for this cell
	 * are set to false because its absolute value is known.
	 * 
	 * @param value, number which is to be assigned to a Cell
	 */
	public void setCellValue(int value) {
		this.value = value;
		for (int i = 0; i < 9; i++)
			possibleNumsBoolean[i] = false;
		for (int i = 0; i < possibleNumsArrayList.size(); i++) {
			possibleNumsArrayList.remove(i);
		}
		valueWasSet = true;
	}

	/**
	 * This method sets the possibility of a Cell at the specified index to be false.
	 * This method is called within the 'updateGrid' method in Solver.java
	 * 
	 * @param index of value in Cell 'possibleNumsBoolean' boolean array
	 */
	public void setValueToFalse(int index) {
		possibleNumsBoolean[index - 1] = false;
		if (possibleNumsArrayList.contains((Object) index)) {
			int temp = possibleNumsArrayList.indexOf(index);
			possibleNumsArrayList.remove(temp);
		}
	}

	/**
	 * This method finds the value which a Cell needs to be, to fulfill the
	 * requirements of the numbers 1 through 9 appearing in each row, column, and subgrid.
	 * 
	 * @param list, ArrayList of non-repeating integers made up of known Cell values 
	 * in the same row, column, or subgrid as a given Cell
	 */
	public void findCellValueFromArrayList(ArrayList<Integer> list) {
		for (int i = 0; i < 9; i++) {
			if (!list.contains(i + 1)) {
				setCellValue(i + 1);
			}
		}
	}

	/**
	 * This method returns the possibility (true/false) of a Cell being a specific value
	 * (i.e. index + 1).
	 * 
	 * @param index of value within a Cell's 'possibleNumsBoolean' boolean array
	 * @return boolean value
	 */
	public boolean getPossibleNum(int index) {
		return possibleNumsBoolean[index];
	}

	/**
	 * This method returns the value of a Cell.
	 * 
	 * @return integer
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

	/**
	 * This method returns the 'possibleNumsArrayList associated with a Cell
	 * 
	 * @return ArrayList<Integer>
	 */
	public ArrayList<Integer> getPossibleNumsArrayList() {
		return possibleNumsArrayList;
	}
}
