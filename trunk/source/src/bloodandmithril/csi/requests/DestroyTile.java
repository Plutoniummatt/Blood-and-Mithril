package bloodandmithril.csi.requests;

import java.util.List;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

import com.google.common.collect.Lists;

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
	public List<Response> respond() {
		Topography.deleteTile(worldX, worldY, foreground);
		Response destroyTileResponse = new DestroyTileResponse(worldX, worldY, foreground);
		return Lists.newArrayList(destroyTileResponse);
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