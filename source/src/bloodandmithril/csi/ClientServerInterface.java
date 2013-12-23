package bloodandmithril.csi;

import java.io.IOException;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.csi.GenerateChunk.GenerateChunkResponse;
import bloodandmithril.csi.Ping.Pong;
import bloodandmithril.csi.SynchronizeIndividual.IndividualSyncRequest;
import bloodandmithril.csi.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.persistence.world.ChunkLoaderImpl;
import bloodandmithril.util.Task;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.DebugTile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;
import bloodandmithril.world.topography.tile.tiles.BrickTile;
import bloodandmithril.world.topography.tile.tiles.GlassTile;
import bloodandmithril.world.topography.tile.tiles.SeditmentaryTile;
import bloodandmithril.world.topography.tile.tiles.SoilTile;
import bloodandmithril.world.topography.tile.tiles.StoneTile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.glass.ClearGlassTile;
import bloodandmithril.world.topography.tile.tiles.sedimentary.YellowSandTile;
import bloodandmithril.world.topography.tile.tiles.soil.DryDirtTile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;
import bloodandmithril.world.topography.tile.tiles.stone.GraniteTile;
import bloodandmithril.world.topography.tile.tiles.stone.SandStoneTile;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

/**
 * The CSI
 *
 * @author Matt
 */
public class ClientServerInterface {

	/** The client */
	private static Client client;

	/**
	 * Sets up the client and attempt to connect to the server
	 */
	public static void setupAndConnect() {
		client = new Client(65536, 65536);
		client.start();

		try {
			client.connect(5000, "192.168.2.6", 42685);
		} catch (IOException e) {
			e.printStackTrace();
		}

		registerClasses(client.getKryo());
		client.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());

		client.addListener(new Listener() {
			@Override
			public void received(Connection connection, final Object object) {
				ChunkLoaderImpl.loaderTasks.add(
					new Task() {
						@Override
						public void execute() {
							if (object instanceof Response) {
								Response response = (Response) object;
								response.acknowledge();
							}
						}
					}
				);
			}
		});
	}


	public static void sendGenerateChunkRequest(int x, int y) {
		client.sendTCP(new GenerateChunk(x, y));
	}


	/**
	 * Registers all request classes
	 */
	public static void registerClasses(Kryo kryo) {
		kryo.register(Request.class);
		kryo.register(Ping.class);
		kryo.register(Pong.class);
		kryo.register(IndividualSyncRequest.class);
		kryo.register(IndividualState.class);
		kryo.register(SynchronizeIndividual.class);
		kryo.register(SynchronizeIndividualResponse.class);
		kryo.register(Vector2.class);
		kryo.register(ChunkData.class);
		kryo.register(GenerateChunk.class);
		kryo.register(GenerateChunkResponse.class);
		kryo.register(Tile.class);
		kryo.register(Tile.Orientation.class);
		kryo.register(Tile[].class);
		kryo.register(Tile[][].class);
		kryo.register(BrickTile.class);
		kryo.register(YellowBrickTile.class);
		kryo.register(YellowBrickPlatform.class);
		kryo.register(DebugTile.class);
		kryo.register(EmptyTile.class);
		kryo.register(GlassTile.class);
		kryo.register(ClearGlassTile.class);
		kryo.register(SeditmentaryTile.class);
		kryo.register(YellowSandTile.class);
		kryo.register(SoilTile.class);
		kryo.register(DryDirtTile.class);
		kryo.register(StandardSoilTile.class);
		kryo.register(StoneTile.class);
		kryo.register(GraniteTile.class);
		kryo.register(SandStoneTile.class);
	}
}