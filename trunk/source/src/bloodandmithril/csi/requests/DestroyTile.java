package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
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
	public Responses respond() {
		Topography.deleteTile(worldX, worldY, foreground);
		Response destroyTileResponse = new DestroyTileResponse(worldX, worldY, foreground);
		Responses responses = new Response.Responses(false, new LinkedList<Response>());
		responses.responses.add(destroyTileResponse);
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	public static class DestroyTileResponse implements Response {

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
		
		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}
}