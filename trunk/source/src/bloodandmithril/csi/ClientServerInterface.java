package bloodandmithril.csi;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
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
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.Attack.Strike;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.Harvest;
import bloodandmithril.character.ai.task.Harvest.HarvestItem;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.ai.task.MineTile.Mine;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.ai.task.TradeWith.Trade;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.character.ai.task.Wait;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.character.conditions.Hunger;
import bloodandmithril.character.conditions.Poison;
import bloodandmithril.character.conditions.Thirst;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Boar;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.character.skill.Skills;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.csi.requests.CSIMineTile;
import bloodandmithril.csi.requests.CSITradeWith;
import bloodandmithril.csi.requests.CSITradeWith.CSITradeWithResponse;
import bloodandmithril.csi.requests.ChangeFactionControlPassword;
import bloodandmithril.csi.requests.ChangeFactionControlPassword.RefreshFactionWindow;
import bloodandmithril.csi.requests.ChangeNickName;
import bloodandmithril.csi.requests.ChangeNickName.ChangeNickNameResponse;
import bloodandmithril.csi.requests.ClientConnected;
import bloodandmithril.csi.requests.ConsumeItem;
import bloodandmithril.csi.requests.DestroyPropNotification;
import bloodandmithril.csi.requests.DestroyTile;
import bloodandmithril.csi.requests.FurnaceSmelt;
import bloodandmithril.csi.requests.IgniteFurnaceRequest;
import bloodandmithril.csi.requests.DestroyTile.DestroyTileResponse;
import bloodandmithril.csi.requests.DrinkLiquid;
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
import bloodandmithril.csi.requests.SendAttackRequest;
import bloodandmithril.csi.requests.SendChatMessage;
import bloodandmithril.csi.requests.SendChatMessage.Message;
import bloodandmithril.csi.requests.SendChatMessage.SendChatMessageResponse;
import bloodandmithril.csi.requests.SendHarvestRequest;
import bloodandmithril.csi.requests.SetAIIdle;
import bloodandmithril.csi.requests.AddLightRequest;
import bloodandmithril.csi.requests.AddLightRequest.RemoveLightNotification;
import bloodandmithril.csi.requests.AddLightRequest.SyncLightResponse;
import bloodandmithril.csi.requests.SynchronizeFaction;
import bloodandmithril.csi.requests.SynchronizeFaction.SynchronizeFactionResponse;
import bloodandmithril.csi.requests.SynchronizeIndividual;
import bloodandmithril.csi.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.csi.requests.SynchronizePropRequest;
import bloodandmithril.csi.requests.SynchronizePropRequest.SynchronizePropResponse;
import bloodandmithril.csi.requests.SynchronizeWorldState;
import bloodandmithril.csi.requests.SynchronizeWorldState.SynchronizeWorldStateResponse;
import bloodandmithril.csi.requests.ToggleWalkRun;
import bloodandmithril.csi.requests.TransferItems;
import bloodandmithril.csi.requests.TransferItems.RefreshWindows;
import bloodandmithril.csi.requests.TransferItems.RefreshWindowsResponse;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.graphics.Light;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;
import bloodandmithril.item.Item;
import bloodandmithril.item.equipment.Broadsword;
import bloodandmithril.item.equipment.ButterflySword;
import bloodandmithril.item.equipment.OneHandedWeapon;
import bloodandmithril.item.equipment.Weapon;
import bloodandmithril.item.material.animal.ChickenLeg;
import bloodandmithril.item.material.brick.YellowBrick;
import bloodandmithril.item.material.container.Bottle;
import bloodandmithril.item.material.container.GlassBottle;
import bloodandmithril.item.material.fuel.Coal;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.item.material.liquid.Liquid.Empty;
import bloodandmithril.item.material.liquid.Liquid.Water;
import bloodandmithril.item.material.mineral.Ashes;
import bloodandmithril.item.material.mineral.YellowSand;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.item.material.plant.CarrotSeed;
import bloodandmithril.item.material.plant.CookedCarrot;
import bloodandmithril.item.material.plant.DeathCap;
import bloodandmithril.item.material.plant.Felberries;
import bloodandmithril.item.material.plant.Seed;
import bloodandmithril.item.misc.Currency;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.ConstructionWithContainer.ConstructionContainer;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.prop.building.PineChest;
import bloodandmithril.prop.plant.FelberryBush;
import bloodandmithril.prop.plant.Plant;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
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
import bloodandmithril.world.topography.tile.tiles.glass.InterlacedWindowTile;
import bloodandmithril.world.topography.tile.tiles.sedimentary.YellowSandTile;
import bloodandmithril.world.topography.tile.tiles.soil.DryDirtTile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;
import bloodandmithril.world.topography.tile.tiles.stone.GraniteTile;
import bloodandmithril.world.topography.tile.tiles.stone.SandStoneTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.common.collect.Lists;
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
		clientName = InetAddress.getLocalHost().getHostName();

		client = new Client(65536, 65536);
		registerClasses(client.getKryo());
		client.start();
		client.connect(5000, ip, 42685, 42686);
		client.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());
		client.getUpdateThread().setUncaughtExceptionHandler(
			(thread, throwable) -> {
				Logger.networkDebug(throwable.getMessage(), LogLevel.WARN);
				throwable.printStackTrace();
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
					for (final Response response : resp.getResponses()) {
						if (response.forClient() != -1 && response.forClient() != client.getID()) {
							continue;
						}
						response.acknowledge();
					}
					return;
				}

				for (final Response response : resp.getResponses()) {
					if (response.forClient() != -1 && response.forClient() != client.getID()) {
						continue;
					}
					if (response instanceof GenerateChunkResponse) {
						ChunkLoader.loaderTasks.add(
							() -> {
								response.acknowledge();
							}
						);
					} else if (response instanceof SynchronizeIndividualResponse) {
						BloodAndMithrilClient.newCachedThreadPool.execute(
							() -> {
								response.acknowledge();
							}
						);
					} else if (response instanceof DestroyTileResponse) {
						Topography.addTask(() -> {
							response.acknowledge();
						});
					} else if (response instanceof MoveIndividual || response instanceof IndividualSelection) {
						AIProcessor.aiThreadTasks.add(
							() -> {
								response.acknowledge();
							}
						);
					} else {
						BloodAndMithrilClient.newCachedThreadPool.execute(
							() -> {
								try {
									response.acknowledge();
								} catch (Throwable t) {
									throw new RuntimeException(t);
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


	public static synchronized void sendNotification(final int connectionId, final boolean tcp, final boolean executeInSingleThread, final Response... responses) {
		serverThread.execute(
			() -> {
				for (Connection connection : server.getConnections()) {
					if (connectionId == -1) {
						Responses resp = new Responses(executeInSingleThread);
						for (Response response : responses) {
							response.prepare();
							resp.add(response);
						}
						if (tcp) {
							connection.sendTCP(resp);
						} else {
							connection.sendUDP(resp);
						}

						continue;
					}

					if (connectionId == connection.getID()) {
						Responses resp = new Responses(executeInSingleThread);
						for (Response response : responses) {
							response.prepare();
							resp.add(response);
						}
						if (tcp) {
							connection.sendTCP(resp);
						} else {
							connection.sendUDP(resp);
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

		kryo.register(Ashes.class);
		kryo.register(InterlacedWindowTile.class);
		kryo.register(AddLightRequest.class);
		kryo.register(AITask.class);
		kryo.register(ArrayDeque.class);
		kryo.register(ArrayList.class);
		kryo.register(ArtificialIntelligence.AIMode.class);
		kryo.register(ArtificialIntelligence.class);
		kryo.register(AStarPathFinder.class);
		kryo.register(AStarPathFinder.Node.class);
		kryo.register(Attack.class);
		kryo.register(Bleeding.class);
		kryo.register(bloodandmithril.prop.plant.Carrot.class);
		kryo.register(Boar.class);
		kryo.register(BoarAI.class);
		kryo.register(Bottle.class);
		kryo.register(Box.class);
		kryo.register(BrickTile.class);
		kryo.register(Broadsword.class);
		kryo.register(ButterflySword.class);
		kryo.register(Carrot.class);
		kryo.register(CarrotSeed.class);
		kryo.register(CookedCarrot.class);
		kryo.register(ChangeFactionControlPassword.class);
		kryo.register(ChangeNickName.class);
		kryo.register(ChangeNickNameResponse.class);
		kryo.register(ChickenLeg.class);
		kryo.register(ChunkData.class);
		kryo.register(Class.class);
		kryo.register(ClearGlassTile.class);
		kryo.register(ClientConnected.class);
		kryo.register(Coal.class);
		kryo.register(Color.class);
		kryo.register(Commands.class);
		kryo.register(CompositeAITask.class);
		kryo.register(ConcurrentHashMap.class);
		kryo.register(ConcurrentLinkedDeque.class);
		kryo.register(ConcurrentSkipListMap.class);
		kryo.register(ConstructionContainer.class);
		kryo.register(ConsumeItem.class);
		kryo.register(CSIMineTile.class);
		kryo.register(CSITradeWith.class);
		kryo.register(CSITradeWithResponse.class);
		kryo.register(Currency.class);
		kryo.register(DeathCap.class);
		kryo.register(DebugTile.class);
		kryo.register(Depth.class);
		kryo.register(DestroyPropNotification.class);
		kryo.register(DestroyTile.class);
		kryo.register(DestroyTileResponse.class);
		kryo.register(DrinkLiquid.class);
		kryo.register(DryDirtTile.class);
		kryo.register(DualKeyHashMap.class);
		kryo.register(Elf.class);
		kryo.register(ElfAI.class);
		kryo.register(Empty.class);
		kryo.register(EmptyTile.class);
		kryo.register(Epoch.class);
		kryo.register(Equipable.class);
		kryo.register(EquipmentSlot.class);
		kryo.register(EquipOrUnequipItem.class);
		kryo.register(Faction.class);
		kryo.register(Felberries.class);
		kryo.register(FelberryBush.class);
		kryo.register(Furnace.class);
		kryo.register(Domain.getIndividuals().keySet().getClass());
		kryo.register(GenerateChunk.class);
		kryo.register(GenerateChunkResponse.class);
		kryo.register(GlassBottle.class);
		kryo.register(GlassTile.class);
		kryo.register(GoToLocation.class);
		kryo.register(GoToMovingLocation.class);
		kryo.register(GraniteTile.class);
		kryo.register(Harvest.class);
		kryo.register(Harvestable.class);
		kryo.register(HarvestItem.class);
		kryo.register(HashMap.class);
		kryo.register(HashSet.class);
		kryo.register(Hunger.class);
		kryo.register(Idle.class);
		kryo.register(IgniteFurnaceRequest.class);
		kryo.register(Individual.class);
		kryo.register(IndividualIdentifier.class);
		kryo.register(IndividualSelection.class);
		kryo.register(IndividualSelection.SelectIndividualResponse.class);
		kryo.register(IndividualState.class);
		kryo.register(Light.class);
		kryo.register(LinkedList.class);
		kryo.register(Liquid.class);
		kryo.register(List.class);
		kryo.register(ListingMenuItem.class);
		kryo.register(Message.class);
		kryo.register(Mine.class);
		kryo.register(MineTile.class);
		kryo.register(MoveIndividual.class);
		kryo.register(MoveIndividual.MoveIndividualResponse.class);
		kryo.register(OneHandedWeapon.class);
		kryo.register(OpenTradeWindow.class);
		kryo.register(OpenTradeWindow.OpenTradeWindowResponse.class);
		kryo.register(Path.class);
		kryo.register(PineChest.class);
		kryo.register(Ping.class);
		kryo.register(Plant.class);
		kryo.register(Poison.class);
		kryo.register(Pong.class);
		kryo.register(RefreshFactionWindow.class);
		kryo.register(RefreshWindowsResponse.class);
		kryo.register(RemoveLightNotification.class);
		kryo.register(Request.class);
		kryo.register(RequestClientList.class);
		kryo.register(RequestClientListResponse.class);
		kryo.register(Responses.class);
		kryo.register(SandStoneTile.class);
		kryo.register(SeditmentaryTile.class);
		kryo.register(Seed.class);
		kryo.register(SendAttackRequest.class);
		kryo.register(SendChatMessage.class);
		kryo.register(SendChatMessageResponse.class);
		kryo.register(SendHarvestRequest.class);
		kryo.register(SetAIIdle.class);
		kryo.register(Skills.class);
		kryo.register(SoilTile.class);
		kryo.register(StandardSoilTile.class);
		kryo.register(StoneTile.class);
		kryo.register(Strike.class);
		kryo.register(SynchronizeFaction.class);
		kryo.register(SynchronizeFactionResponse.class);
		kryo.register(SynchronizeIndividual.class);
		kryo.register(SynchronizeIndividualResponse.class);
		kryo.register(SynchronizePropRequest.class);
		kryo.register(SynchronizePropResponse.class);
		kryo.register(SynchronizeWorldState.class);
		kryo.register(SynchronizeWorldStateResponse.class);
		kryo.register(SyncLightResponse.class);
		kryo.register(Thirst.class);
		kryo.register(Tile.class);
		kryo.register(Tile.Orientation.class);
		kryo.register(Tile[].class);
		kryo.register(Tile[][].class);
		kryo.register(ToggleWalkRun.class);
		kryo.register(Trade.class);
		kryo.register(TradeWith.class);
		kryo.register(Trading.class);
		kryo.register(TransferItems.class);
		kryo.register(TransferItems.RefreshWindows.class);
		kryo.register(TransferItems.TradeEntity.class);
		kryo.register(TransferItems.TransferItemsResponse.class);
		kryo.register(TreeMap.class);
		kryo.register(Vector2.class);
		kryo.register(Wait.class);
		kryo.register(Water.class);
		kryo.register(WayPoint.class);
		kryo.register(Weapon.class);
		kryo.register(YellowBrick.class);
		kryo.register(YellowBrickPlatform.class);
		kryo.register(YellowBrickTile.class);
		kryo.register(YellowSand.class);
		kryo.register(YellowSandTile.class);
	}


	/**
	 * Send a {@link Request} to the {@link Server}
	 *
	 * @author Matt
	 */
	public static class SendRequest {
		public static synchronized void sendGenerateChunkRequest(int x, int y, int worldId) {
			client.sendTCP(new GenerateChunk(x, y, worldId));
			Logger.networkDebug("Sending chunk generation request", LogLevel.DEBUG);
		}
		
		public static synchronized void sendAddLightRequest(Light light) {
			client.sendTCP(new AddLightRequest(light.size, light.x, light.y, light.color, light.intensity, light.spanBegin, light.spanEnd));
			Logger.networkDebug("Sending add light request", LogLevel.DEBUG);
		}

		public static synchronized void sendHarvestRequest(int individualId, int propId) {
			client.sendTCP(new SendHarvestRequest(individualId, propId));
			Logger.networkDebug("Sending harvest request", LogLevel.DEBUG);
		}

		public static synchronized void sendConsumeItemRequest(Consumable consumable, int individualId) {
			client.sendTCP(new ConsumeItem(consumable, individualId));
			Logger.networkDebug("Sending item consumption request", LogLevel.DEBUG);
		}

		public static synchronized void sendEquipOrUnequipItemRequest(boolean equip, Equipable equipable, int individualId) {
			client.sendTCP(new EquipOrUnequipItem(equip, equipable, individualId));
			Logger.networkDebug("Sending equip/unequip request", LogLevel.DEBUG);
		}

		public static synchronized void sendRunWalkRequest(int individualId, boolean walk) {
			client.sendTCP(new ToggleWalkRun(individualId, walk));
			Logger.networkDebug("Sending run/walk request", LogLevel.DEBUG);
		}

		public static synchronized void sendDrinkLiquidRequest(int individualId, Bottle bottleToDrinkFrom, float amount) {
			client.sendTCP(new DrinkLiquid(individualId, bottleToDrinkFrom, amount));
			Logger.networkDebug("Sending drink liquid request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestConnectedPlayerNamesRequest() {
			client.sendTCP(new RequestClientList());
			Logger.networkDebug("Sending client name list request", LogLevel.DEBUG);
		}
		
		public static synchronized void sendIgniteFurnaceRequest(int furnaceId) {
			client.sendTCP(new IgniteFurnaceRequest(furnaceId));
			Logger.networkDebug("Sending ignite furnace request", LogLevel.DEBUG);
		}
		
		public static synchronized void sendFurnaceSmeltRequest(int furnaceId) {
			client.sendTCP(new FurnaceSmelt(furnaceId));
			Logger.networkDebug("Sending furnace smelt request", LogLevel.DEBUG);
		}

		public static synchronized void sendSynchronizeIndividualRequest(int id) {
			client.sendUDP(new SynchronizeIndividual(id));
			Logger.networkDebug("Sending sync individual request for " + id, LogLevel.TRACE);
		}

		public static synchronized void sendSynchronizeIndividualRequest() {
			client.sendUDP(new SynchronizeIndividual());
			Logger.networkDebug("Sending individual sync request for all", LogLevel.TRACE);
		}

		public static synchronized void sendDestroyTileRequest(float worldX, float worldY, boolean foreground, int worldId) {
			client.sendTCP(new DestroyTile(worldX, worldY, foreground, worldId));
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

		public static synchronized void sendSynchronizeFactionsRequest() {
			client.sendTCP(new SynchronizeFaction());
			Logger.networkDebug("Sending synchronize faction request", LogLevel.DEBUG);
		}

		public static synchronized void sendTradeWithIndividualRequest(Individual proposer, Individual proposee) {
			client.sendTCP(new CSITradeWith(proposer.getId().getId(), TradeEntity.INDIVIDUAL, proposee.getId().getId(), client.getID()));
			Logger.networkDebug("Sending trade with individual request", LogLevel.DEBUG);
		}

		public static synchronized void sendAttackRequest(Individual attacker, Individual victim) {
			client.sendTCP(new SendAttackRequest(attacker.getId().getId(), victim.getId().getId()));
			Logger.networkDebug("Sending attack individual request", LogLevel.DEBUG);
		}

		public static synchronized void sendTradeWithPropRequest(Individual proposer, int propId) {
			client.sendTCP(new CSITradeWith(proposer.getId().getId(), TradeEntity.PROP, propId, client.getID()));
			Logger.networkDebug("Sending trade with prop request", LogLevel.DEBUG);
		}

		public static synchronized void sendIndividualSelectionRequest(int id, boolean select) {
			client.sendTCP(new IndividualSelection(id, select, client.getID()));
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

		public static synchronized void sendChangeFactionControlPasswordRequest(int factionId, String newPassword) {
			client.sendTCP(new ChangeFactionControlPassword(factionId, newPassword));
			Logger.networkDebug("Sending change faction control password request", LogLevel.DEBUG);
		}

		public static synchronized void sendOpenTradeWindowRequest(int proposerId, TradeEntity proposee, int proposeeId) {
			client.sendTCP(new OpenTradeWindow(proposerId, proposee, proposeeId));
			Logger.networkDebug("Sending open trade window request", LogLevel.DEBUG);
		}
		
		public static synchronized void sendChatMessage(String message) {
			client.sendTCP(
				new SendChatMessage(
					new Message(
						clientName,
						message
					)
				)
			);
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
		public static synchronized void notifyRemoveProp(int propId) {
			sendNotification(
				-1,
				true,
				true,
				new DestroyPropNotification(propId)
			);
		}
		
		
		public static synchronized void notifyRemoveLight(int lightId) {
			sendNotification(
				-1,
				true,
				true,
				new AddLightRequest.RemoveLightNotification(lightId)
			);
		}


		public static synchronized void notifySyncPlayerList() {
			List<String> names = Lists.newArrayList();
			for (Connection connection : ClientServerInterface.server.getConnections()) {
				names.add(ClientServerInterface.connectedPlayers.get(connection.getID()));
			}
			sendNotification(
				-1,
				true,
				false,
				new RequestClientListResponse(names)
			);
		}


		public static synchronized void notifySyncFaction(Faction faction) {
			sendNotification(
				-1,
				false,
				true,
				new SynchronizeFactionResponse(faction)
			);
		}


		public static synchronized void notifyTileMined(int connectionId, Vector2 location, boolean foreGround, int worldId) {
			sendNotification(
				connectionId,
				true,
				false,
				new DestroyTileResponse(location.x, location.y, foreGround, worldId)
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


		public static synchronized void notifyGiveItem(int individualId, Item item) {
			Domain.getIndividuals().get(individualId).giveItem(item);
			sendNotification(
				-1,
				true,
				true,
				new SynchronizeIndividualResponse(individualId, System.currentTimeMillis()),
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
				new SynchronizeIndividualResponse(id, System.currentTimeMillis())
			);
		}


		public static synchronized void notifySyncLight(int id, Light light) {
			sendNotification(
				-1,
				false,
				false,
				new AddLightRequest.SyncLightResponse(id, light.size, light.x, light.y, light.color, light.intensity, light.spanBegin, light.spanEnd)
			);
		}
	}
}