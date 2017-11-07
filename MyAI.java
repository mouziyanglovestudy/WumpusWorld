import java.util.Deque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;


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
    private Set<Integer> wumpusCells;
    private int currentCell;
    private int direction;
    private Deque<Integer> stack;
    private int goal; // the cell ai want to go
    private boolean shouldClimb;
    
	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
	    this.allCells = new Hashtable<Integer, Cell>();
	    this.breezeCells = new HashSet<Integer>();
	    this.stenchCells = new HashSet<Integer>();
	    this.wumpusCells = new HashSet<Integer>();
	    this.stack = new LinkedList<Integer>();
	    
	    for (int i = 0; i < 10; i++) {
	        for (int j = 0; j < 10; j++) {
	            this.allCells.put(i*10+j, new Cell(i, j));
	        }
	    }

	    this.currentCell = 0*10+0;
	    this.direction = 1;
	    this.shouldClimb = false;
	    
	    allCells.get(currentCell).visit();
	    allCells.get(currentCell).changeMightPit(false);
	    allCells.get(currentCell).changeMightWumpus(false);
	    
	    this.goal = 0;
	    
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
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

	    
	    allCells.get(currentCell).visit();
	    checkBump(bump);
		checkBreeze(breeze);
		checkStench(stench);
		if (glitter) {
		    goal = 0;
		    shouldClimb = true;
		    return Action.GRAB;
		}
		addToStack();
		if (goal == currentCell) {
		    if (stack.isEmpty()) {
		        goal = 0;
		        shouldClimb = true;
		    } else {
                goal = stack.pop();
            }
		    if (shouldClimb && currentCell == 0) {
		        return Action.CLIMB;
		    } 
		}
		
	    System.out.println("goal : " + goal);
	    System.out.println("stack size: " + stack.size());
	    System.out.println("current : " + currentCell + "Visited? : " + allCells.get(currentCell).isVisited());
	    stack.stream().forEach(i -> System.out.println(i));
	    System.out.println("Possible wumpus:");
	    wumpusCells.stream().forEach(i -> System.out.println(i));
	        
		return moveToGoal(goal);
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
	 * Get the nearby cell coordinate of the given cell
	 */
	private Set<Integer> getNearbyCellCoords(int cell) {
	    Set<Integer> nearbyCells = new HashSet<Integer>();
	    for (int i : FOUR_DIRECTIONS) {
	        if (cell % 10 == 0 && i == -1) {
	            continue;
	        }
	        nearbyCells.add(cell + i);
	    }
	    return nearbyCells;
	}
	
	/*
	 * Add all nearby cells with mightPit
	 */
	private void checkBreeze(boolean isBreeze) {
	    if (!isBreeze) {
	        for (int cellCoords : getNearbyCellCoords(currentCell)) {
	            Cell cell = allCells.get(cellCoords);
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
	        for (int cellCoords : getNearbyCellCoords(currentCell)) {
	            Cell cell = allCells.get(cellCoords);
	            if (cell != null) {
	                cell.changeMightWumpus(false);
	            }
	        }
	    }
	    else {
	        stenchCells.add(currentCell);
	        for (int cellCoords: getNearbyCellCoords(currentCell)) {
                Cell cell = allCells.get(cellCoords);
                if (cell != null && !cell.mightWumpus()) {
                    wumpusCells.add(cellCoords);
	            }
	        }
	    }
	}
	
	private void addToStack() {
	    for (int i: new int[]{getBehindDirection(direction), getLeftDirection(direction), 
	            getRightDirection(direction), direction}) {
	        Cell cell = allCells.get(currentCell + i);
	        if (cell != null && !(currentCell % 10 == 0 && i == -1)) {
	            if (!cell.isVisited() && !cell.mightPit() && !cell.mightWumpus()) {
	                if (stack.contains(cell.getIntCoord())) {
	                    stack.remove(cell.getIntCoord());
	                }
	                if (goal != cell.getIntCoord()) {
	                    stack.push(cell.getIntCoord());
	                }
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
	    for (int cellCoords : getNearbyCellCoords(currentCell)) {
	        Cell cell = allCells.get(cellCoords);
	        if (cell != null && !cell.mightPit() && !cell.mightWumpus()) {
	            tempList.add(cell);
	        }
	    }

	    Cell cellChosen = tempList.get(0);
	    for (Cell cell: tempList) {
	        if (cellChosen.getDistance(goal) > cell.getDistance(goal)) {
	            cellChosen = cell; // get the cell closest to the goal with heuristics
	        }
	    }

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
	        direction = getRightDirection(direction);
	        return Action.TURN_RIGHT;
	    }
	}
	
	/*
	 * React when bump
	 */
	private void checkBump(boolean isBump) {
	    if (!isBump) {
	        return;
	    }
	    Set<Integer> removing = new HashSet<Integer>();
	    if (direction == 1) {
	        currentCell -= direction;
	        int xcoord = currentCell % 10;
	        for (int cell: allCells.keySet()) {
	            if (cell % 10 > xcoord) {
	                removing.add(cell);
	            }
	        }
	    } else if (direction == 10) {
	        currentCell -= direction;
	        int ycoord = currentCell / 10;
	        for (int cell: allCells.keySet()) {
	            if (cell / 10 > ycoord) {
	                removing.add(cell);
	            }
	        }
	    } else {
	        System.out.println("error: direction");
	    }
	    for (int cell: removing) {
	        allCells.remove(cell);
	    }
	    goal = currentCell;
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
    
    public boolean mightPit() {
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