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
* 
* @author devankarsann
*/

public class Cell {

	private boolean valueWasSet = false;
	private boolean[] possibleNumsBoolean;
	private ArrayList<Integer> possibleNumsArrayList = new ArrayList<Integer>();
	private int value = 0;
	private int dimension;
	/**
	 * This is the constructor method for a Cell object.
	 * values in possibleNumsBoolean are initialized to true.
	 * possible values are added to possibleNumsArrayList.
	 */
	public Cell(int inputNumRows) {
		dimension = inputNumRows;
		possibleNumsBoolean = new boolean[dimension];
		for (int i = 0; i < dimension; i++) {
			possibleNumsBoolean[i] = true;
			possibleNumsArrayList.add(i + 1);
		}
	}

	/**
	 * This method sets the value for a Cell and would be called after a solution is found.
	 * Possible values are set to false because the value is known.
	 * Remaining numbers are removed from possibleNumsArrayList.
	 * 
	 * @param value to be assigned to a Cell
	 */
	public void setCellValue(int value) {
		this.value = value;
		for (int i = 0; i < dimension; i++)
			possibleNumsBoolean[i] = false;
		possibleNumsArrayList.clear();
		setValueWasSet(true);
	}

	/**
	 * This method sets the possibility of a Cell at the specified index to be false.
	 * 
	 * @param index
	 */
	public void setValueToFalse(int index) {
		if (possibleNumsArrayList.contains((Object) index)) 
		{
			possibleNumsBoolean[index - 1] = false;
			int temp = possibleNumsArrayList.indexOf(index);
			possibleNumsArrayList.remove(temp);
		}
	}

	/**
	 * This method returns the number of possible values a Cell object can be
	 * @return int
	 */
	public int getNumPossible() {
		return possibleNumsArrayList.size();
	}
	
	/**
	 * This method finds the value which a Cell needs to be to fulfill the
	 * requirements of the numbers 1 through 9 appearing in each row, column, and subgrid.
	 * 
	 * @param list, ArrayList of non-repeating integers made up of known Cell values 
	 * in the same row, column, or subgrid as a given Cell
	 */
	public void findCellValueFromArrayList(ArrayList<Integer> list) {
		for (int i = 0; i < dimension; i++) {
			if (!list.contains(i + 1)) {
				setCellValue(i + 1);
			}
		}
	}

	/**
	 * This method returns the possibility (true/false) of a Cell being a specific value
	 * 
	 * @param index of value within a Cell's possibleNumsBoolean array
	 * @return boolean
	 */
	public boolean getPossibleNum(int index) {
		return possibleNumsBoolean[index];
	}

	/**
	 * This method returns the value of a Cell.
	 * 
	 * @return
	 */
	public int getCellValue() {
		return value;
	}

	/**
	 * This method returns boolean value representing whether or not the value
	 * of a Cell was set (i.e. true if the value is not zero).
	 * 
	 * @return
	 */
	public boolean getValueWasSet() {
		return valueWasSet();
	}

	/**
	 * This method returns the 'possibleNumsArrayList associated with a Cell
	 * 
	 * @return ArrayList<Integer>
	 */
	public ArrayList<Integer> getPossibleNumsArrayList() {
		return possibleNumsArrayList;
	}

	public boolean valueWasSet() {
		return valueWasSet;
	}

	public void setValueWasSet(boolean valueWasSet) {
		this.valueWasSet = valueWasSet;
	}
}