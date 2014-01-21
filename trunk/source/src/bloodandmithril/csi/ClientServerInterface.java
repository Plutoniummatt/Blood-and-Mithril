package bloodandmithril.csi;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.implementations.BoarAI;
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
import bloodandmithril.character.conditions.Poison;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.csi.requests.CSIMineTile;
import bloodandmithril.csi.requests.CSITradeWith;
import bloodandmithril.csi.requests.CSITradeWith.CSITradeWithResponse;
import bloodandmithril.csi.requests.ChangeNickName;
import bloodandmithril.csi.requests.ChangeNickName.ChangeNickNameResponse;
import bloodandmithril.csi.requests.ClientConnected;
import bloodandmithril.csi.requests.ConsumeItem;
import bloodandmithril.csi.requests.DestroyTile;
import bloodandmithril.csi.requests.DestroyTile.DestroyTileResponse;
import bloodandmithril.csi.requests.EquipOrUnequipItem;
import bloodandmithril.csi.requests.GenerateChunk;
import bloodandmithril.csi.requests.GenerateChunk.GenerateChunkResponse;
import bloodandmithril.csi.requests.IndividualSelection;
import bloodandmithril.csi.requests.MoveIndividual;
import bloodandmithril.csi.requests.OpenTradeWindow;
import bloodandmithril.csi.requests.Ping;
import bloodandmithril.csi.requests.Ping.Pong;
import bloodandmithril.csi.requests.RequestClientList;
import bloodandmithril.csi.requests.RequestClientList.RequestClientListResponse;
import bloodandmithril.csi.requests.SendChatMessage;
import bloodandmithril.csi.requests.SendChatMessage.Message;
import bloodandmithril.csi.requests.SendChatMessage.SendChatMessageResponse;
import bloodandmithril.csi.requests.SetAIIdle;
import bloodandmithril.csi.requests.SynchronizeIndividual;
import bloodandmithril.csi.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.csi.requests.SynchronizePropRequest;
import bloodandmithril.csi.requests.SynchronizePropRequest.SynchronizePropResponse;
import bloodandmithril.csi.requests.SynchronizeWorldState;
import bloodandmithril.csi.requests.SynchronizeWorldState.SynchronizeWorldStateResponse;
import bloodandmithril.csi.requests.TransferItems;
import bloodandmithril.csi.requests.TransferItems.RefreshWindows;
import bloodandmithril.csi.requests.TransferItems.RefreshWindowsResponse;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.equipment.OneHandedWeapon;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.mineral.YellowSand;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.persistence.world.ChunkLoaderImpl;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Chest.ChestContainer;
import bloodandmithril.prop.building.PineChest;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.WorldState;
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

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.common.collect.Maps;

/**
 * The CSI
 *
 * @author Matt
 */
public class ClientServerInterface {

	private static boolean isClient, isServer;

	public static Client client;

	public static Server server;

	public static Thread syncThread;

	public static String clientName;

	public static ExecutorService serverThread;

	public static HashMap<Integer, String> connectedPlayers = Maps.newHashMap();

	/**
	 * Sets up the client and attempt to connect to the server
	 * @throws IOException
	 */
	public static void setupAndConnect(String ip) throws IOException {
		clientName = Util.randomOneOf("Vindi", "Bhaal", "Mach", "Snake", "Mega", "Maller", "Protey", "Legion", "Tengu", "Loki");

		client = new Client(65536, 65536);
		registerClasses(client.getKryo());
		client.start();
		client.connect(5000, ip, 42685, 42686);
		client.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());
		client.getUpdateThread().setUncaughtExceptionHandler(
			new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable throwable) {
					Logger.networkDebug(throwable.getMessage(), LogLevel.WARN);
					throwable.printStackTrace();
				}
			}
		);

		client.addListener(new Listener() {

			@Override
			public void received(Connection connection, final Object object) {
				if (!(object instanceof Responses)) {
					return;
				}

				Responses resp = (Responses) object;

				if (resp.executeInSingleThread()) {
					for (final Response response : resp.responses) {
						if (response.forClient() != -1 && response.forClient() != client.getID()) {
							continue;
						}
						response.acknowledge();
					}
					return;
				}

				for (final Response response : resp.responses) {
					if (response.forClient() != -1 && response.forClient() != client.getID()) {
						continue;
					}
					if (response instanceof GenerateChunkResponse) {
						ChunkLoaderImpl.loaderTasks.add(
							new Task() {
								@Override
								public void execute() {
									response.acknowledge();
								}
							}
						);
					} else if (response instanceof SynchronizeIndividualResponse) {
						BloodAndMithrilClient.newCachedThreadPool.execute(
							new Runnable() {
								@Override
								public void run() {
									response.acknowledge();
								}
							}
						);
					} else if (response instanceof DestroyTileResponse) {
						Topography.addTask(new Task() {
							@Override
							public void execute() {
								response.acknowledge();
							}
						});
					} else if (response instanceof MoveIndividual || response instanceof IndividualSelection) {
						AIProcessor.aiThreadTasks.add(
							new Task() {
								@Override
								public void execute() {
									response.acknowledge();
								}
							}
						);
					} else {
						BloodAndMithrilClient.newCachedThreadPool.execute(
							new Runnable() {
								@Override
								public void run() {
									try {
										response.acknowledge();
									} catch (Throwable t) {
										throw new RuntimeException(t);
									}
								}
							}
						);
					}
				}
			}
		});

		client.sendTCP(new ClientConnected(client.getID(), clientName));
		client.sendTCP(new SynchronizeWorldState());
		SendRequest.sendRequestConnectedPlayerNamesRequest();
	}


	private static synchronized void sendNotification(final int connectionId, final boolean tcp, final boolean executeInSingleThread, final Response... responses) {
		serverThread.execute(
			new Runnable() {
				@Override
				public void run() {
					for (Connection connection : server.getConnections()) {
						if (connectionId == -1) {
							Responses resp = new Responses(executeInSingleThread, new LinkedList<Response>());
							for (Response response : responses) {
								resp.responses.add(response);
							}
							if (tcp) {
								connection.sendTCP(resp);
							} else {
								connection.sendUDP(resp);
							}

							continue;
						}

						if (connectionId == connection.getID()) {
							Responses resp = new Responses(executeInSingleThread, new LinkedList<Response>());
							for (Response response : responses) {
								resp.responses.add(response);
							}
							if (tcp) {
								connection.sendTCP(resp);
							} else {
								connection.sendUDP(resp);
							}
						}
					}
				}
			}
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
		kryo.setReferences(true);

		kryo.register(Commands.class);
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
		kryo.register(TransferItems.class);
		kryo.register(TransferItems.TransferItemsResponse.class);
		kryo.register(TransferItems.RefreshWindows.class);
		kryo.register(OpenTradeWindow.class);
		kryo.register(OpenTradeWindow.OpenTradeWindowResponse.class);
		kryo.register(TransferItems.TradeEntity.class);
		kryo.register(CSITradeWith.class);
		kryo.register(CSITradeWithResponse.class);
		kryo.register(ListingMenuItem.class);
		kryo.register(List.class);
		kryo.register(ArrayList.class);
		kryo.register(LinkedList.class);
		kryo.register(ArrayDeque.class);
		kryo.register(Responses.class);
		kryo.register(ChangeNickName.class);
		kryo.register(ChangeNickNameResponse.class);
		kryo.register(ConcurrentSkipListMap.class);
		kryo.register(CSIMineTile.class);
		kryo.register(RefreshWindowsResponse.class);
		kryo.register(YellowSand.class);
		kryo.register(RequestClientList.class);
		kryo.register(RequestClientListResponse.class);
		kryo.register(ClientConnected.class);
		kryo.register(SendChatMessage.class);
		kryo.register(SendChatMessageResponse.class);
		kryo.register(Message.class);
		kryo.register(SetAIIdle.class);
		kryo.register(SynchronizeWorldState.class);
		kryo.register(SynchronizeWorldStateResponse.class);
		kryo.register(EquipOrUnequipItem.class);
		kryo.register(ConsumeItem.class);
		kryo.register(Poison.class);
		kryo.register(Boar.class);
		kryo.register(BoarAI.class);
		kryo.register(SynchronizePropRequest.class);
		kryo.register(SynchronizePropResponse.class);
		kryo.register(PineChest.class);
		kryo.register(ChestContainer.class);
	}


	/**
	 * Send a {@link Request} to the {@link Server}
	 *
	 * @author Matt
	 */
	public static class SendRequest {
		public static synchronized void sendGenerateChunkRequest(int x, int y) {
			client.sendTCP(new GenerateChunk(x, y));
			Logger.networkDebug("Sending chunk generation request", LogLevel.DEBUG);
		}

		public static synchronized void sendConsumeItemRequest(Consumable consumable, int individualId) {
			client.sendTCP(new ConsumeItem(consumable, individualId));
			Logger.networkDebug("Sending item consumption request", LogLevel.DEBUG);
		}

		public static synchronized void sendEquipOrUnequipItemRequest(boolean equip, Equipable equipable, int individualId) {
			client.sendTCP(new EquipOrUnequipItem(equip, equipable, individualId));
			Logger.networkDebug("Sending equip/unequip request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestConnectedPlayerNamesRequest() {
			client.sendTCP(new RequestClientList());
			Logger.networkDebug("Sending client name list request", LogLevel.DEBUG);
		}

		public static synchronized void sendSynchronizeIndividualRequest(int id) {
			client.sendUDP(new SynchronizeIndividual(id));
			Logger.networkDebug("Sending sync individual request for " + id, LogLevel.TRACE);
		}

		public static synchronized void sendSynchronizeIndividualRequest() {
			client.sendUDP(new SynchronizeIndividual());
			Logger.networkDebug("Sending individual sync request for all", LogLevel.TRACE);
		}

		public static synchronized void sendDestroyTileRequest(float worldX, float worldY, boolean foreground) {
			client.sendTCP(new DestroyTile(worldX, worldY, foreground));
			Logger.networkDebug("Sending destroy tile request", LogLevel.DEBUG);
		}

		public static synchronized void sendPingRequest() {
			client.sendUDP(new Ping());
			Logger.networkDebug("Sending ping request", LogLevel.TRACE);
		}

		public static synchronized void sendClearAITaskRequest(int individualId) {
			client.sendUDP(new SetAIIdle(individualId));
			Logger.networkDebug("Sending request to clear AI task", LogLevel.TRACE);
		}

		public static synchronized void sendTradeWithIndividualRequest(Individual proposer, Individual proposee) {
			client.sendTCP(new CSITradeWith(proposer.id.id, TradeEntity.INDIVIDUAL, proposee.id.id, client.getID()));
			Logger.networkDebug("Sending trade with individual request", LogLevel.DEBUG);
		}

		public static synchronized void sendTradeWithPropRequest(Individual proposer, int propId) {
			client.sendTCP(new CSITradeWith(proposer.id.id, TradeEntity.PROP, propId, client.getID()));
			Logger.networkDebug("Sending trade with prop request", LogLevel.DEBUG);
		}

		public static synchronized void sendIndividualSelectionRequest(int id, boolean select) {
			client.sendTCP(new IndividualSelection(id, select));
			Logger.networkDebug("Sending individual selection request", LogLevel.DEBUG);
		}

		public static synchronized void sendMoveIndividualRequest(int id, Vector2 destinationCoordinates, boolean forceMove) {
			client.sendTCP(new MoveIndividual(id, destinationCoordinates, forceMove));
			Logger.networkDebug("Sending move individual request", LogLevel.DEBUG);
		}

		public static synchronized void sendChangeNickNameRequest(int id, String toChangeTo) {
			client.sendTCP(new ChangeNickName(id, toChangeTo));
			Logger.networkDebug("Sending change individual nickname request", LogLevel.DEBUG);
		}

		public static synchronized void sendMineTileRequest(int individualId, Vector2 location) {
			client.sendTCP(new CSIMineTile(individualId, location));
			Logger.networkDebug("Sending mine tile request", LogLevel.DEBUG);
		}

		public static synchronized void sendRefreshItemWindowsRequest() {
			client.sendTCP(new RefreshWindows());
			Logger.networkDebug("Sending item window refresh request", LogLevel.DEBUG);
		}

		public static synchronized void sendOpenTradeWindowRequest(int proposerId, TradeEntity proposee, int proposeeId) {
			client.sendTCP(new OpenTradeWindow(proposerId, proposee, proposeeId));
			Logger.networkDebug("Sending open trade window request", LogLevel.DEBUG);
		}

		public static synchronized void sendTransferItemsRequest(
				HashMap<Item, Integer> proposerItemsToTransfer, int proposerId,
				HashMap<Item, Integer> proposeeItemsToTransfer, TradeEntity proposeeEntityType, int proposeeId) {

			client.sendTCP(
				new bloodandmithril.csi.requests.TransferItems(
					proposerItemsToTransfer, proposerId,
					proposeeItemsToTransfer,
					proposeeEntityType, proposeeId,
					client.getID()
				)
			);
		}
	}


	/**
	 * Send a notification to a {@link Client}
	 *
	 * @author Matt
	 */
	public static class SendNotification {
		public static synchronized void notifyChatMessage(String message) {
			client.sendTCP(
				new SendChatMessage(
					new Message(
						clientName,
						message
					)
				)
			);
			Logger.networkDebug("Sending chat message", LogLevel.DEBUG);
		}


		public static synchronized void notifyTileMined(int connectionId, Vector2 location, boolean foreGround) {
			sendNotification(
				connectionId,
				true,
				false,
				new DestroyTileResponse(location.x, location.y, foreGround)
			);
		}


		public static synchronized void notifySyncProp(Prop prop) {
			sendNotification(
				-1,
				false,
				false,
				new SynchronizePropRequest.SynchronizePropResponse(prop)
			);
		}


		public static synchronized void notifyRefreshItemWindows() {
			sendNotification(
				-1,
				true,
				false,
				new TransferItems.RefreshWindowsResponse()
			);
		}


		public static synchronized void notifyTradeWindowOpen(int proposerId, TradeEntity proposee, int proposeeId, int connectionId) {
			sendNotification(
				connectionId,
				true,
				false,
				new OpenTradeWindow.OpenTradeWindowResponse(
					proposerId,
					proposee,
					proposeeId
				)
			);
		}


		public static synchronized void notifyGiveItem(int individualId, Item item, int quantity) {
			GameWorld.individuals.get(individualId).giveItem(item, quantity);
			sendNotification(
				-1,
				true,
				true,
				new SynchronizeIndividualResponse(GameWorld.individuals.get(individualId), System.currentTimeMillis()),
				new TransferItems.RefreshWindowsResponse()
			);
		}


		public static synchronized void notifySyncWorldState() {
			sendNotification(
				-1,
				false,
				false,
				new SynchronizeWorldStateResponse(WorldState.currentEpoch)
			);
		}


		public static synchronized void notifyIndividualSync(int id) {
			sendNotification(
				-1,
				false,
				false,
				new SynchronizeIndividualResponse(GameWorld.individuals.get(id), System.currentTimeMillis())
			);
		}
	}
}