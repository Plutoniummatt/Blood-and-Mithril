package bloodandmithril.networking.requests;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.math.Vector2;

/**
 * A {@link Request} to destroy a {@link Tile} from {@link Topography}
 */
@Copyright("Matthew Peck 2014")
public class DestroyTile implements Request {

	public final float worldX, worldY;
	public final boolean foreground;
	private int worldId;

	/**
	 * Constructor
	 */
	public DestroyTile(float worldX, float worldY, boolean foreground, int worldId) {
		this.worldX = worldX;
		this.worldY = worldY;
		this.foreground = foreground;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		Domain.getWorld(worldId).getTopography().deleteTile(worldX, worldY, foreground, false);
		Response destroyTileResponse = new DestroyTileResponse(worldX, worldY, foreground, worldId);
		Responses responses = new Response.Responses(false);
		responses.add(destroyTileResponse);
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	public static class DestroyTileResponse implements Response {

		public final float worldX, worldY;
		public final boolean foreground;
		private int worldId;

		public DestroyTileResponse(float worldX, float worldY, boolean foreground, int worldId) {
			this.worldX = worldX;
			this.worldY = worldY;
			this.foreground = foreground;
			this.worldId = worldId;
		}

		@Override
		public void acknowledge() {
			SoundService.play(
				SoundService.pickAxe,
				new Vector2(worldX, worldY),
				false,
				null
			);
			Domain.getWorld(worldId).getTopography().deleteTile(worldX, worldY, foreground, false);
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