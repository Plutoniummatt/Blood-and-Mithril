package bloodandmithril.generation.component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A Pre-Fabricated {@link Component} that does not require generation.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class PrefabricatedComponent extends Component {
	private static final long serialVersionUID = 1029361927219136813L;

	private final ComponentBlueprint blueprint;

	protected final boolean inverted;

	public static Pixmap prefabPixmap;

	/**
	 * Constructor
	 */
	protected PrefabricatedComponent(ComponentBlueprint blueprint, Boundaries boundaries, int structureKey, boolean inverted) {
		super(boundaries, structureKey);
		this.blueprint = blueprint;
		this.inverted = inverted;
	}


	/** Load textures */
	public static void setup() {
		Texture texture = new Texture(Gdx.files.internal("data/image/prefab.png"));
		texture.getTextureData().prepare();

		prefabPixmap = texture.getTextureData().consumePixmap();
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		if (inverted) {
			return blueprint.getForegroundTile(
				boundaries.right - worldTileX,
				worldTileY - boundaries.bottom
			);
		} else {
			return blueprint.getForegroundTile(
				worldTileX - boundaries.left,
				worldTileY - boundaries.bottom
			);
		}
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if (inverted) {
			return blueprint.getBackgroundTile(
				boundaries.right - worldTileX,
				worldTileY - boundaries.bottom
			);
		} else {
			return blueprint.getBackgroundTile(
				worldTileX - boundaries.left,
				worldTileY - boundaries.bottom
			);
		}
	}
}