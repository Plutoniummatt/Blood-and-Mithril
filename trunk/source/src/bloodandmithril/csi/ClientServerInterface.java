package bloodandmithril.csi;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.implementations.AStarPathFinder;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.ai.task.MineTile.Mine;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.ai.task.TradeWith.Trade;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.character.ai.task.Wait;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.csi.requests.CSITrade;
import bloodandmithril.csi.requests.CSITrade.TradeEntity;
import bloodandmithril.csi.requests.DestroyTile;
import bloodandmithril.csi.requests.DestroyTile.DestroyTileResponse;
import bloodandmithril.csi.requests.GenerateChunk;
import bloodandmithril.csi.requests.GenerateChunk.GenerateChunkResponse;
import bloodandmithril.csi.requests.IndividualSelection;
import bloodandmithril.csi.requests.MoveIndividual;
import bloodandmithril.csi.requests.Ping;
import bloodandmithril.csi.requests.Ping.Pong;
import bloodandmithril.csi.requests.SynchronizeIndividual;
import bloodandmithril.csi.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.equipment.OneHandedWeapon;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.persistence.world.ChunkLoaderImpl;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.Topography;
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

import com.badlogic.gdx.Gdx;
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

	private static Thread clientThread;

	private static boolean isClient, isServer;

	/**
	 * Sets up the client and attempt to connect to the server
	 * @throws IOException
	 */
	public static void setupAndConnect(String ip) throws IOException {
		client = new Client(65536, 65536);
		registerClasses(client.getKryo());
		client.start();
		client.connect(5000, ip, 42685, 42686);
		client.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());
		client.getUpdateThread().setUncaughtExceptionHandler(
			new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					client.stop();
					client.start();
					try {
						client.reconnect();
					} catch (IOException e) {
						Gdx.app.exit();
					}
					Logger.networkDebug(throwable.getMessage(), LogLevel.TRACE);
				}
			}
		);

		client.addListener(new Listener() {
			@Override
			public void received(Connection connection, final Object object) {
				if (object instanceof GenerateChunkResponse) {
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
				} else if (object instanceof SynchronizeIndividualResponse) {
					BloodAndMithrilClient.newCachedThreadPool.execute(
						new Runnable() {
							@Override
							public void run() {
								Response response = (Response) object;
								response.acknowledge();
							}
						}
					);
				} else if (object instanceof DestroyTileResponse) {
					Topography.addTask(new Task() {
						@Override
						public void execute() {
							Response response = (Response) object;
							response.acknowledge();
						}
					});
				} else if (object instanceof MoveIndividual || object instanceof IndividualSelection) {
					AIProcessor.aiThreadTasks.add(
						new Task() {
							@Override
							public void execute() {
								Response response = (Response) object;
								response.acknowledge();
							}
						}
					);
				} else if (object instanceof Response) {
					BloodAndMithrilClient.newCachedThreadPool.execute(
						new Runnable() {
							@Override
							public void run() {
								Response response = (Response) object;
								response.acknowledge();
							}
						}
					);
				}
			}
		});

		clientThread = new Thread(
			new Runnable() {
				@Override
				public void run() {
					while(client.isConnected()) {
						try {
							Thread.sleep(100);
							sendSynchronizeIndividualRequest();
							ping();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		);

		clientThread.start();
	}

	public static synchronized void sendGenerateChunkRequest(int x, int y) {
		client.sendTCP(new GenerateChunk(x, y));
	}

	public static synchronized void sendSynchronizeIndividualRequest(int id) {
		client.sendUDP(new SynchronizeIndividual(id));
	}

	public static synchronized void sendSynchronizeIndividualRequest() {
		client.sendUDP(new SynchronizeIndividual());
	}

	public static synchronized void sendDestroyTileRequest(float worldX, float worldY, boolean foreground) {
		client.sendTCP(new DestroyTile(worldX, worldY, foreground));
	}

	public static synchronized void ping() {
		client.sendUDP(new Ping());
	}

	public static synchronized void individualSelection(int id, boolean select) {
		client.sendTCP(new IndividualSelection(id, select));
	}

	public static synchronized void moveIndividual(int id, Vector2 destinationCoordinates, boolean forceMove) {
		client.sendTCP(new MoveIndividual(id, destinationCoordinates, forceMove));
	}

	public static synchronized void trade(
			HashMap<ListingMenuItem<Item>, Integer> proposerItemsToTransfer,
			TradeEntity proposerEntityType, int proposerId,
			HashMap<ListingMenuItem<Item>, Integer> proposeeItemsToTransfer,
			TradeEntity proposeeEntityType, int proposeeId
	) {
		client.sendTCP(
			new bloodandmithril.csi.requests.CSITrade(
				proposerItemsToTransfer,
				proposerEntityType, proposerId,
				proposeeItemsToTransfer,
				proposeeEntityType, proposeeId
			)
		);
	}

	public static boolean isServer() {
		return isServer;
	}

	public static boolean isClient() {
		return isClient;
	}

	public static synchronized void setServer(boolean isServer) {
		ClientServerInterface.isServer = isServer;
	}

	public static synchronized void setClient(boolean isClient) {
		ClientServerInterface.isClient = isClient;
	}

	/**
	 * Registers all request classes
	 */
	public static void registerClasses(Kryo kryo) {
		kryo.register(Request.class);
		kryo.register(Ping.class);
		kryo.register(Pong.class);
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
		kryo.register(Ping.class);
		kryo.register(Pong.class);
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
		kryo.register(ConcurrentHashMap.class);
		kryo.register(Elf.class);
		kryo.register(ElfAI.class);
		kryo.register(AITask.class);
		kryo.register(Idle.class);
		kryo.register(GoToLocation.class);
		kryo.register(GoToMovingLocation.class);
		kryo.register(CompositeAITask.class);
		kryo.register(MineTile.class);
		kryo.register(TradeWith.class);
		kryo.register(Mine.class);
		kryo.register(Trading.class);
		kryo.register(Trade.class);
		kryo.register(Wait.class);
		kryo.register(Individual.class);
		kryo.register(IndividualIdentifier.class);
		kryo.register(IndividualState.class);
		kryo.register(Epoch.class);
		kryo.register(ArtificialIntelligence.class);
		kryo.register(ArtificialIntelligence.AIMode.class);
		kryo.register(HashMap.class);
		kryo.register(HashSet.class);
		kryo.register(EquipmentSlot.class);
		kryo.register(OneHandedWeapon.class);
		kryo.register(Equipable.class);
		kryo.register(Broadsword.class);
		kryo.register(ButterflySword.class);
		kryo.register(Box.class);
		kryo.register(ChickenLeg.class);
		kryo.register(Carrot.class);
		kryo.register(Path.class);
		kryo.register(WayPoint.class);
		kryo.register(TreeMap.class);
		kryo.register(AStarPathFinder.class);
		kryo.register(AStarPathFinder.Node.class);
		kryo.register(DualKeyHashMap.class);
		kryo.register(GameWorld.individuals.keySet().getClass());
		kryo.register(DestroyTile.class);
		kryo.register(DestroyTileResponse.class);
		kryo.register(IndividualSelection.class);
		kryo.register(IndividualSelection.SelectIndividualResponse.class);
		kryo.register(MoveIndividual.class);
		kryo.register(MoveIndividual.MoveIndividualResponse.class);
		kryo.register(CSITrade.class);
		kryo.register(CSITrade.CSITradeResponse.class);
	}
}