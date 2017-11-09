import java.util.Deque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


// ======================================================================
// FILE:        MyAI.java
//
// AUTHOR:      Abdullah Younis
//
// DESCRIPTION: This file contains your agent class, which you will
//              implement. You are responsible for implementing the
//              'getAction' function and any helper methods you feel you
//              need.
//
// NOTES:       - If you are having trouble understanding how the shell
//                works, look at the other parts of the code, as well as
//                the documentation.
//
//              - You are only allowed to make changes to this portion of
//                the code. Any changes to other portions of the code will
//                be lost when the tournament runs your code.
// ======================================================================

public class MyAI extends Agent
{
    private static final int[] FOUR_DIRECTIONS = new int[] {-10, -1, 1, 10};
    private Hashtable<Integer, Cell> allCells;
    private Set<Integer> breezeCells, stenchCells;
    private int currentCell;
    private int direction;
    private boolean hasGold;
    private Deque<Integer> stack;
    private int goal; // the cell ai want to go; -1 when ai is exploring
    
	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
	    this.allCells = new Hashtable<Integer, Cell>();
	    this.breezeCells = new HashSet<Integer>();
	    this.stenchCells = new HashSet<Integer>();
	    this.stack = new LinkedList<Integer>();
	    
	    for (int i = 0; i < 10; i++) {
	        for (int j = 0; j < 10; j++) {
	            this.allCells.put(i*10+j, new Cell(i, j));
	        }
	    }

	    this.currentCell = 0*10+0;
	    this.direction = 1;
	    this.hasGold = false;
	    
	    allCells.get(currentCell).visit();
	    allCells.get(currentCell).changeMightPit(false);
	    allCells.get(currentCell).changeMightWumpus(false);
	    
	    this.goal = -1;
	    
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}
	
	public Action getAction
	(
		boolean stench,
		boolean breeze,
		boolean glitter,
		boolean bump,
		boolean scream
	)
	{
	    if (goal == -1) {
	        if (glitter) {
	            hasGold = true;
	            goal = 0;
	            return Action.GRAB;
	        }
	        if (breeze || stench) {
	            if (currentCell == 0) {
	                return Action.CLIMB;
	            }
	            goal = 0;
	            return moveToGoal(goal);
	        }
	        if (currentCell > 1) {
	            goal = 0;
                return moveToGoal(goal);
	        }
	        else {
	            currentCell += direction;
	            allCells.get(currentCell).visit();
	            return Action.FORWARD;
	        }
	    } else {
	         return moveToGoal(goal);
	    }
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

		/* TODO: if (!hasGold) {
		    checkBreeze(breeze);
		    checkStench(stench);
		    addToStack();
		}*/
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}
	
	// ======================================================================
	// YOUR CODE BEGINS
	// ======================================================================
	
	/*
	 * Method below shows relationships between 4 directions
	 */
	private int getLeftDirection(int direction) {
	    if (direction == 1) {
	        return 10;
	    } else if (direction == 10) {
	        return -1;
	    } else if (direction == -1) {
	        return -10;
	    } else if (direction == -10) {
	        return 1;
	    } else {
	        return 0;
	    }
	}
	
	private int getRightDirection(int direction) {
	    if (direction == 1) {
	         return -10;
	     } else if (direction == 10) {
	         return 1;
	     } else if (direction == -1) {
	         return 10;
	     } else if (direction == -10) {
	         return -1;
	     } else {
	         return 0;
	     }
	}
	   
	private int getBehindDirection(int direction) {
	    return 0 - direction;
	}
	
	/*
	 * Add all nearby cells with mightPit
	 */
	private void checkBreeze(boolean isBreeze) {
	    if (!isBreeze) {
	        for (int i: FOUR_DIRECTIONS) {
	            Cell cell = allCells.get(currentCell + i);
	            if (cell != null) {
	                cell.changeMightPit(false);
	            }
	        }
	    }
	    else {
	        breezeCells.add(currentCell);
	    }
	}
	
	/*
	 * Add all nearby cells with mightWumpus
	 */
	private void checkStench(boolean isStench) {
	    if (!isStench) {
	        for (int i: FOUR_DIRECTIONS) {
	            Cell cell = allCells.get(currentCell + i);
	            if (cell != null) {
	                cell.changeMightWumpus(false);
	            }
	        }
	    }
	    else {
	        stenchCells.add(currentCell);
	    }
	}
	
	private void addToStack() {
	    for (int i: new int[]{getBehindDirection(direction), }) {
	        Cell cell = allCells.get(currentCell + i);
	        if (cell != null) {
	            if (!cell.isVisited() && !cell.mightPit() && !cell.mightWumpus()) {
	                stack.push(cell.getIntCoord());
	            }
	        }
	    }
	}
	
	// move towards goal with visited cells
	// TODO: improve later
	private Action moveToGoal(int goal) {
	    if (currentCell == goal) {
	        return Action.CLIMB;
	    }
	    List<Cell> tempList = new LinkedList<Cell>();
	    for (int i: FOUR_DIRECTIONS) {
	        Cell cell = allCells.get(currentCell + i);
	        if (cell != null && cell.isVisited()) {
	            tempList.add(cell);
	        }
	    }

	    /*tempList.sort((a, b) -> a.getDistance(goal) - b.getDistance(goal));
	    for (Cell i: tempList) {
	        System.out.print("###" + i.getIntCoord());
	    }
	    Cell cellChosen = tempList.get(0);
	    */
	    Cell cellChosen = tempList.get(0);
	    for (Cell cell: tempList) {
	        if (cellChosen.getDistance(goal) > cell.getDistance(goal)) {
	            cellChosen = cell;
	        }
	    }
	    //System.out.println("\ncurrent " + currentCell);
	    //System.out.println("chosen " + cellChosen.getIntCoord());
	    // choose the cell with smallest heurist distance
	    // TODO: Add dead-end condition
	    int requiredDirection = cellChosen.getIntCoord() - currentCell;
	    if (direction == requiredDirection) {
	        currentCell = cellChosen.getIntCoord();
	        return Action.FORWARD;
	    } else if (requiredDirection == getLeftDirection(direction) 
	            || requiredDirection == getBehindDirection(direction)) {
	        direction = getLeftDirection(direction);
	        return Action.TURN_LEFT;
	    } else {
	        return Action.TURN_RIGHT;
	    }
	}
	// ======================================================================
	// YOUR CODE ENDS
	// ======================================================================
}

class Cell {
    
    private int x, y; // x, y coordinate
    private boolean isVisited;
    private boolean mightPit, isPit;
    private boolean mightWumpus, isWumpus;
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.isVisited = false;
        this.mightPit = true;
        this.mightWumpus = true;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getIntCoord() {
        return 10 * x + y;
    }
    
    public int getDistance(int goal) {
        int goalX = goal / 10;
        int goalY = goal % 10;
        return Math.abs(goalX - x) + Math.abs(goalY - y);
    }
    
    public boolean isVisited() {
        return this.isVisited;
    }
    
    public boolean mightPit( ) {
        return this.mightPit;
    }
    
    public boolean isPit() {
        return this.isPit;
    }
    
    public boolean mightWumpus() {
        return this.mightWumpus;
    }
    
    public boolean isWumpus() {
        return this.isWumpus;
    }
    
    public void visit() {
        this.isVisited = true;
    }
    
    public void changeIsPit(boolean f) {
        this.isPit = f;
    }
    
    public void changeMightPit(boolean f) {
        this.mightPit = f;
        
    }
    
    public void changeIsWumpus(boolean f) {
        this.isWumpus = f;
    }
    
    public void changeMightWumpus(boolean f) {
        this.mightWumpus = f;
    }
}