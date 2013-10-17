package spritestar.world.generation.substructures.components.connections;

import java.util.List;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.Line;
import spritestar.world.generation.Component;
import spritestar.world.generation.StructureMap;
import spritestar.world.generation.substructures.components.Connection;
import spritestar.world.topography.tile.Tile;

import com.badlogic.gdx.math.Vector2;

/**
 * The empty space and floor of some stairs.
 *
 * @author Sam
 */
public class Stairs extends Connection {
	private static final long serialVersionUID = -5986392956679541045L;
	
	/** Stair slope is calculated with y = slopeGradient * x + slopeConstant*/
	private float slopeConstant;
	private float slopeGradient;
	
	/**	The direction these stairs prefer, true if positive gradient, false for negative. */
	public boolean upRight;

	/** The height from the floor to the ceiling directly above. */
	private final int corridorHeight;
	
	private final int corridorHeightChange;

	/** Tiles used in this {@link Component} */
	private Class<? extends Tile> corridorFloorTile;
	private Class<? extends Tile> platformFloorTile;

	
	/**
	 * Constructor
	 */
	public Stairs(
			List<Connection> connectionsToGenerateFrom,
			Boundaries boundaries,
			Line connection,
			Line floor,
			boolean upRight,
			boolean generateConnection,
			Class<? extends Tile> corridorFloorTile,
			Class<? extends Tile> platformFloorTile,
			int corridorHeight,
			int corridorHeightChange
			) {
		
		super(connectionsToGenerateFrom, boundaries, connection, floor, generateConnection);
		this.upRight = upRight;
		this.corridorHeight = corridorHeight;
		this.corridorFloorTile = corridorFloorTile;
		this.platformFloorTile = platformFloorTile;
		this.corridorHeightChange = corridorHeightChange;
	}


	@Override
	protected Tile getForegroundTileFromComponent(int worldTileX, int worldTileY) {
		if (worldTileY - worldTileX * slopeGradient < slopeConstant || worldTileY - worldTileX * slopeGradient > slopeConstant + corridorHeight) {
			return null;
		} else {
			if (worldTileY - worldTileX * slopeGradient == slopeConstant) {
				if (StructureMap.getStructure(structureKey).getForegroundTile(worldTileX + 1, worldTileY) instanceof Tile.EmptyTile) {
					try {
						return platformFloorTile.newInstance();
					} catch (Exception e) {
						throw new RuntimeException();
					}
				} else {
					try {
						return corridorFloorTile.newInstance();
					} catch (Exception e) {
						throw new RuntimeException();
					}
				}
			}
			return new Tile.EmptyTile();
		}
	}


	@Override
	protected Tile getBackgroundTileFromComponent(int worldTileX, int worldTileY) {
		return null;
	}


	@Override
	protected void generateConnection() {
		connectionLine.organiseLine();
		floorLine.organiseLine();
		Line stairFloorLine = createStairFloorLine();
		Vector2 intersection = floorLine.getIntersection(stairFloorLine);
		if (floorLine.isWithinBoxContainingLine(intersection)) {
			slopeGradient = stairFloorLine.getGradient();
		} else {
			upRight = !upRight;
			stairFloorLine = createStairFloorLine();
			intersection = floorLine.getIntersection(stairFloorLine);
			if (floorLine.isWithinBoxContainingLine(intersection)) {
				slopeGradient = stairFloorLine.getGradient();
			} else {
				stairFloorLine = getBestStairFloorLine();
				slopeGradient = stairFloorLine.getGradient();
				intersection = floorLine.getIntersection(stairFloorLine);
			}
		}
		slopeConstant = getConstant();
		correctBoundaries(intersection);
	}
	
	
	/**
	 * Sets the boundaries based on the newly calculated stair slope.
	 */
	private void correctBoundaries(Vector2 intersection) {
		boundaries.top = (int)Math.max(Math.max(connectionLine.y1, connectionLine.y2), intersection.y + corridorHeight - 1 - (upRight ? corridorHeightChange : 0)); // -1 is for the floor 
		boundaries.bottom = (int)Math.min(Math.min(connectionLine.y1, connectionLine.y2), intersection.y);
		boundaries.left = (int)Math.min(Math.min(connectionLine.x1, connectionLine.x2), intersection.x);
		boundaries.right = (int)Math.max(Math.max(connectionLine.x1, connectionLine.x2), intersection.x - 1 - (upRight ? 0 : corridorHeightChange)); // -1 is for the floor
	}
	
	
	/**
	 * return - The most valid line from a connectionLine point to a floorLine point. Assuming gradients of 1 or -1 aren't possible.
	 */
	private Line getBestStairFloorLine() {
		Line line1 = new Line(connectionLine.x1, connectionLine.y1, floorLine.x1, floorLine.y1);
		Line line2 = new Line(connectionLine.x1, connectionLine.y1, floorLine.x2, floorLine.y2);
		Line line3 = new Line(connectionLine.x2, connectionLine.y2, floorLine.x1, floorLine.y1);
		Line line4 = new Line(connectionLine.x2, connectionLine.y2, floorLine.x2, floorLine.y2);
		Line comparison1;
		Line comparison2;
		if (line1.getGradient() > 1 || line1.getGradient() < -1) {
			comparison1 = Math.abs(line1.getGradient()) > Math.abs(line2.getGradient()) ? line2 : line1;
		} else {
			comparison1 = Math.abs(line1.getGradient()) > Math.abs(line2.getGradient()) ? line1 : line2;
		}
		if (line3.getGradient() > 1 || line3.getGradient() < -1) {
			comparison2 = Math.abs(line3.getGradient()) > Math.abs(line4.getGradient()) ? line4 : line3;
		} else {
			comparison2 = Math.abs(line3.getGradient()) > Math.abs(line4.getGradient()) ? line3 : line4;
		}
		float gradient1 = comparison1.getGradient() > 1 ? comparison1.getGradient() : 1 / comparison1.getGradient();
		float gradient2 = comparison2.getGradient() > 1 ? comparison2.getGradient() : 1 / comparison2.getGradient();
		
		if (gradient1 < gradient2) {
			return comparison1;
		} else {
			return comparison2;
		}
		
	}


	/**
	 * Gets the constant used in the equation for the stair floor line.
	 * The gradient must be set first.
	 */
	private float getConstant() {
		if (connectionLine.x1 == connectionLine.x2) {
			return connectionLine.y1 - (slopeGradient * connectionLine.x1);
		} else {
			if (upRight) {
				return connectionLine.y2 - (slopeGradient * connectionLine.x2);
			} else {
				return connectionLine.y1 - (slopeGradient * connectionLine.x1);
			}
		}
	}
	
	
	/**
	 * Creates enough of the stair floor line to use it for it's direction.
	 */
	private Line createStairFloorLine() { 
		if (connectionLine.x1 == connectionLine.x2) {
			if (upRight) {
				return new Line(
					connectionLine.x1,
					connectionLine.y1,
					connectionLine.x1 + 1,
					connectionLine.y1 + 1
				);
			} else {
				return new Line(
					connectionLine.x1,
					connectionLine.y1,
					connectionLine.x1 + 1,
					connectionLine.y1 - 1
				);
			}
		} else {
			if (upRight) {
				return new Line(
					connectionLine.x2,
					connectionLine.y2,
					connectionLine.x2 + 1,
					connectionLine.y2 + 1
				);
			} else {
				return new Line(
					connectionLine.x1,
					connectionLine.y1,
					connectionLine.x1 + 1,
					connectionLine.y1 - 1
				);
			}
		}
	}


	@Override
	public Line getFloorLine(Line connectionLine) {
		// TODO Auto-generated method stub
		return null;
	}
}