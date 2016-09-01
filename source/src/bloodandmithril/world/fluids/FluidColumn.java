package bloodandmithril.world.fluids;

import bloodandmithril.core.Copyright;

/**
 * A vertical fluid column
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class FluidColumn {

	/**
	 * World tile coordinates of the lowest tile of this fluid column
	 */
	private int tileX;
	private int tileY;

	/**
	 * Volume of the column, where the volume of one tile = TILE_SIZE * TILE_SIZE
	 */
	private int volume;

	/**
	 * Height of this column, in tiles
	 */
	private int height;

	/**
	 * unique ID of this {@link FluidColumn}
	 */
	private Integer id;


	/**
	 * Constructor
	 *
	 * @param tileX
	 * @param tileY
	 * @param volume
	 */
	public FluidColumn(final int tileX, final int tileY, final int volume) {
		this.tileX = tileX;
		this.tileY = tileY;
		this.volume = volume;
	}


	/**
	 * @see #tileX
	 */
	public int getX() {
		return tileX;
	}


	/**
	 * @see #tileX
	 */
	public int getY() {
		return tileY;
	}


	/**
	 * @see #volume
	 */
	public int getVolume() {
		return volume;
	}


	/**
	 * @see #volume
	 */
	public void setVolume(final int volume) {
		this.volume = volume;
	}


	/**
	 * @see #id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @see #id
	 */
	public void setId(final int id) {
		this.id = id;
	}


	/**
	 * @see #height
	 */
	public int getHeight() {
		return height;
	}


	/**
	 * @see #height
	 */
	public void setHeight(final int height) {
		this.height = height;
	}
}