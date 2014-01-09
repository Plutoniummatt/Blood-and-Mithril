package bloodandmithril.csi.requests;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A {@link Request} to destroy a {@link Tile} from {@link Topography}
 */
public class DestroyTile implements Request {

	public final float worldX, worldY;
	public final boolean foreground;

	/**
	 * Constructor
	 */
	public DestroyTile(float worldX, float worldY, boolean foreground) {
		this.worldX = worldX;
		this.worldY = worldY;
		this.foreground = foreground;
	}


	@Override
	public Response respond() {
		Topography.deleteTile(worldX, worldY, foreground);
		return new DestroyTileResponse(worldX, worldY, foreground);
	}


	@Override
	public boolean tcp() {
		return true;
	}


	public class DestroyTileResponse implements Response {

		public final float worldX, worldY;
		public final boolean foreground;

		public DestroyTileResponse(float worldX, float worldY, boolean foreground) {
			this.worldX = worldX;
			this.worldY = worldY;
			this.foreground = foreground;
		}

		@Override
		public void acknowledge() {
			Topography.deleteTile(worldX, worldY, foreground);
		}
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}
}