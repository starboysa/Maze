import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Main {
	public static CellGrid mainGrid;

	public static void main(String[] args) {
		int xAmount = 30;
		int yAmount = 30;
		StdDraw.setCanvasSize(1024, 1024);
		StdDraw.setXscale(0, xAmount);
		StdDraw.setYscale(0, yAmount);
		mainGrid = new CellGrid(xAmount-0.1, yAmount-0.1);
		mainGrid.beginPathcreation();
		mainGrid.draw();
	}
}

class Vector2 {
	public double x;
	public double y;

	public Vector2(double xArg, double yArg) {
		x = xArg;
		y = yArg;
	}

	@Override
	public String toString() {
		return "{"+x+", "+y+"}";
	}
}

class CellGrid {
	public static Random rand = new Random();
	public Vector2 size;
	public List<Cell[]> cells = new ArrayList<Cell[]>();
	public Iterator<Cell[]> cellIterator;

	public CellGrid(double sizeX, double sizeY) {
		size = new Vector2(sizeX, sizeY);
		for(int i=0; i<=sizeX*10; i++) {
			Cell[] tempCA = new Cell[(int) Math.ceil(sizeY*10)+1];
			for(int j=0; j<=sizeY*10; j++) {
				tempCA[j] = new Cell(0.0+(i/10.0), 0.0+(j/10.0), this, new Vector2(i, j));
			}
			cells.add(tempCA);
		}
		cellIterator = cells.iterator();
	}

	public void beginPathcreation() {
		int startX = 0;//rand.nextInt((int) Math.ceil(size.x*10.0));
		int startY = 0;//rand.nextInt((int) Math.ceil(size.y*10.0));
		cells.get(startX)[startY].findPath(Side.parseInt(rand.nextInt(3)), false);
	}

	public void draw() {
		o.print("Starting Renderer");
		int allowedThreadAmount = 7;
		for(int i=1;i<=allowedThreadAmount;i++) {
			(new xRenderThread()).start();
		}
	}
}

class Cell {
	public static final double cellSize = 0.1;
	public static Random rand = new Random();
	public Vector2 position;
	public List<Side> holes = new ArrayList<Side>();
	public CellGrid parent;
	public boolean inPath = false;
	private Vector2 indexPosition;
	private Side prevSide;

	public Cell(double xArg, double yArg, CellGrid parentArg, Vector2 indexPositionArg) {
		position = new Vector2(xArg, yArg);
		parent = parentArg;
		indexPosition = indexPositionArg;
	}

	public void draw() {
		drawCell(position, superCast(holes.toArray()));
	}

	public Side[] superCast(Object[] oA) {
		Side[] returnVal = new Side[oA.length];
		if(oA.length > 0) {
			for(int i=0; i<oA.length; i++) {
				returnVal[i] = (Side) oA[i];
			}
		}

		return returnVal;
	}

	public static void drawCell(double xOffset, double yOffset, Side[] args) {
		if(!contains(Side.SOUTH, args)) {
			StdDraw.line(xOffset+0.0, yOffset+0.0, xOffset+0.1, yOffset+0.0);
		}
		if(!contains(Side.WEST, args)) {
			StdDraw.line(xOffset+0.0, yOffset+0.0, xOffset+0.0, yOffset+0.1);
		}
		if(!contains(Side.NORTH, args)) {
			StdDraw.line(xOffset+0.0, yOffset+0.1, xOffset+0.1, yOffset+0.1);
		}
		if(!contains(Side.EAST, args)) {
			StdDraw.line(xOffset+0.1, yOffset+0.0, xOffset+0.1, yOffset+0.1);
		}
	}

	public static void drawCell(Vector2 vArgs, Side[] args) {
		drawCell(vArgs.x, vArgs.y, args);
	}

	public static boolean contains(Object o, Object[] oA) {
		if(oA.length > 0){
			for(Object oTester : oA) {
				if(oTester == o) {
					return true;
				}
			}
		}
		return false;
	}

	public void findPath(Side in, boolean add) {
		o.print("Pathfinding");
		this.inPath = true;
		prevSide = in;
		if(add) {
			holes.add(in);
		}

		Side randSide = Side.parseInt(rand.nextInt(4));
		Cell nextCell;
		int counter = 1;
		do {
			randSide = Side.parseInt(wrapAdd(randSide.sideInt));
			nextCell = getCellFromSide(randSide);
			if(nextCell != null) {
				if(nextCell.inPath) {
					nextCell = null;
				}
			}
			counter++;
		} while(nextCell == null && counter <= 10);

		if(counter < 10 && nextCell != null) {
			holes.add(randSide);
			nextCell.findPath(randSide.findOpposite(), true);
		} else {
			Cell prevCell = getCellFromSide(in);
			if(prevCell != null) {
				prevCell.findPath(prevCell.prevSide, false);
			}
		}
	}

	public Cell getCellFromSide(Side side) {
		int xPos = (int) (this.indexPosition.x+(side.relativePosition().x*10));
		int yPos = (int) (this.indexPosition.y+(side.relativePosition().y*10));
		if(xPos <= parent.size.x*10 && yPos <= parent.size.y*10 && xPos >= 0 && yPos >= 0) {
			return parent.cells.get(xPos)[yPos];
		}
		return null;
	}

	public int wrapAdd(int num) {
		num++;
		if(num == 4) {
			num = 0;
		}
		return num;
	}
}

enum Side {
	NORTH(0),
	WEST(1),
	SOUTH(2),
	EAST(3),
	BLANK(4);

	public final int sideInt;
	Side(int sideIntArg) {
		this.sideInt = sideIntArg;
	}

	Side findOpposite() {
		//could change to +2
		if(this == NORTH) {
			return SOUTH;
		} else if(this == WEST) {
			return EAST;
		} else if(this == SOUTH) {
			return NORTH;
		} else if(this == EAST) {
			return WEST;
		} else if(this == BLANK) {
			return BLANK;
		} else {
			o.print("ERROR: Side.findOpposite() Side is null, I don't know how this could even happen...");
			return null;
		}
	}

	Vector2 relativePosition() {
		if(this == NORTH) {
			return new Vector2(0, Cell.cellSize);
		} else if(this == WEST) {
			return new Vector2(-Cell.cellSize, 0);
		} else if(this == SOUTH) {
			return new Vector2(0, -Cell.cellSize);
		} else if(this == EAST) {
			return new Vector2(Cell.cellSize, 0);
		} else if(this == BLANK) {
			return new Vector2(0, 0);
		} else {
			o.print("ERROR: Side.relativePosition() Side is null, I don't know how this could even happen...");
			return null;
		}
	}

	static Side parseInt(int parse) {
		if(parse == NORTH.sideInt) {
			return NORTH;
		} else if(parse == WEST.sideInt) {
			return WEST;
		} else if(parse == SOUTH.sideInt) {
			return SOUTH;
		} else if(parse == EAST.sideInt) {
			return EAST;
		} else if(parse == BLANK.sideInt) {
			return BLANK;
		} else {
			o.print("ERROR: Side.parseInt(int parse) parse isn't a valid integer");
			return null;
		}
	}
}

//Cause System.out.println() is way to hard
class o {
	public static void print(Object s) {
		System.out.println(s);
	}
}

class xRenderThread extends Thread {
	@Override
	public void run() {
		while(Main.mainGrid.cellIterator.hasNext()) {
			Cell[] renderObjs = Main.mainGrid.cellIterator.next();
			for(Cell renderObj : renderObjs) {
				renderObj.draw();
			}
		}
	}
}