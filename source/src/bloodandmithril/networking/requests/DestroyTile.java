package bloodandmithril.networking.requests;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.topography.DeleteTileService;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A {@link Request} to destroy a {@link Tile} from {@link Topography}
 */
@Copyright("Matthew Peck 2014")
public class DestroyTile implements Request {
	private static final long serialVersionUID = -3447787166708764634L;

	public final float worldX, worldY;
	public final boolean foreground;
	private int worldId;

	@Inject	private transient DeleteTileService deleteTileService;

	/**
	 * Constructor
	 */
	public DestroyTile(final float worldX, final float worldY, final boolean foreground, final int worldId) {
		this.worldX = worldX;
		this.worldY = worldY;
		this.foreground = foreground;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		deleteTileService.deleteTile(worldId, worldX, worldY, foreground, false);
		final Response destroyTileResponse = new DestroyTileResponse(worldX, worldY, foreground, worldId);
		final Responses responses = new Response.Responses(false);
		responses.add(destroyTileResponse);
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	public static class DestroyTileResponse implements Response {
		private static final long serialVersionUID = 4100572243042514921L;
		public final float worldX, worldY;
		public final boolean foreground;
		private int worldId;

		@Inject	private transient DeleteTileService deleteTileService;

		public DestroyTileResponse(final float worldX, final float worldY, final boolean foreground, final int worldId) {
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

			deleteTileService.deleteTile(worldId, worldX, worldY, foreground, false);
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