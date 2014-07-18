package bloodandmithril.generation.component;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Dummy {@link Component} used as a placeholder when a component can not be
 * generated.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DummyComponent extends Component {
	private static final long serialVersionUID = -695818207182117732L;

	/** Singleton instance */
	private static DummyComponent instance = new DummyComponent();

	/**
	 * @return the singleton DummyComponent instance
	 */
	public static DummyComponent getInstance() {
		return instance;
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		throw new UnsupportedOperationException("Attempting to get tile from a DummyComponent, something has gone horribly wrong!");
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		throw new UnsupportedOperationException("Attempting to get tile from a DummyComponent, something has gone horribly wrong!");
	}


	@Override
	protected void generateInterfaces() {
		// Do nothing
	}


	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		return getInstance();
	}


	/**
	 * Constructor - Do not use.
	 */
	private DummyComponent() {
		super(null, -1);
	}
}