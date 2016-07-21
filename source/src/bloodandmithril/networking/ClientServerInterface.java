package bloodandmithril.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.audio.SoundService.SuspiciousSound;
import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AIProcessor.JitGoToLocationFunction;
import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.implementations.ElfAI;
import bloodandmithril.character.ai.implementations.HareAI;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.implementations.AStarPathFinder;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Sniffer;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.SpecificEntityVisible;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.TypeEntityVisible;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine.IndividualAffectedByConditionTriggerFunction;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine.IndividualConditionTriggerFunction;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine.IndividualHealthTriggerFunction;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine.StimulusTriggerFunction;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine.SuspiciousSoundAITriggerFunction;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.Attack.AttackTarget;
import bloodandmithril.character.ai.task.Attack.AttackTaskGenerator;
import bloodandmithril.character.ai.task.Attack.AttackVisibleIndividualTaskGenerator;
import bloodandmithril.character.ai.task.Attack.ReevaluateAttack;
import bloodandmithril.character.ai.task.Attack.WithinAttackRangeOrCantAttack;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.Construct;
import bloodandmithril.character.ai.task.Construct.Constructing;
import bloodandmithril.character.ai.task.Craft;
import bloodandmithril.character.ai.task.Craft.Crafting;
import bloodandmithril.character.ai.task.Follow;
import bloodandmithril.character.ai.task.Follow.FollowTaskGenerator;
import bloodandmithril.character.ai.task.Follow.RepathCondition;
import bloodandmithril.character.ai.task.Follow.WithinNumberOfWaypointsFunction;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.GoToMovingLocation.GoToMovingLocationTaskGenerator;
import bloodandmithril.character.ai.task.Harvest;
import bloodandmithril.character.ai.task.Harvest.HarvestAreaTaskGenerator;
import bloodandmithril.character.ai.task.Harvest.HarvestItem;
import bloodandmithril.character.ai.task.Harvest.HarvestSelectedHarvestablesTaskGenerator;
import bloodandmithril.character.ai.task.Harvest.HarvestVisibleEntityTaskGenerator;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.Jump;
import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.ai.task.LightLightable.GenerateLightAnyVisibleLightables;
import bloodandmithril.character.ai.task.LightLightable.LightFire;
import bloodandmithril.character.ai.task.LightLightable.LightLightablesInAreaTaskGenerator;
import bloodandmithril.character.ai.task.LightLightable.LightSelectedLightablesTaskGenerator;
import bloodandmithril.character.ai.task.LockUnlockContainer;
import bloodandmithril.character.ai.task.LockUnlockContainer.LockUnlock;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.ai.task.MineTile.AttemptMine;
import bloodandmithril.character.ai.task.MineTile.WithinInteractionBox;
import bloodandmithril.character.ai.task.OpenCraftingStation;
import bloodandmithril.character.ai.task.OpenCraftingStation.OpenCraftingStationWindow;
import bloodandmithril.character.ai.task.PlantSeed;
import bloodandmithril.character.ai.task.PlantSeed.PlantSeedTaskGenerator;
import bloodandmithril.character.ai.task.Speak.SpeakTaskGenerator;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.character.ai.task.TakeItem.LootAreaTaskGenerator;
import bloodandmithril.character.ai.task.TakeItem.Take;
import bloodandmithril.character.ai.task.TakeItem.TakeVisibleItemTaskGenerator;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.ai.task.TradeWith.Trade;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.ai.task.Wait;
import bloodandmithril.character.conditions.Bleeding;
import bloodandmithril.character.conditions.Exhaustion;
import bloodandmithril.character.conditions.Hunger;
import bloodandmithril.character.conditions.Poison;
import bloodandmithril.character.conditions.Thirst;
import bloodandmithril.character.faction.Faction;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.character.individuals.Individual.Behaviour;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.character.individuals.IndividualKineticsProcessingData;
import bloodandmithril.character.individuals.IndividualState;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.individuals.characters.Hare;
import bloodandmithril.character.proficiency.Proficiencies;
import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.character.proficiency.proficiencies.Cooking;
import bloodandmithril.character.proficiency.proficiencies.Smithing;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.biome.BiomeDecider;
import bloodandmithril.generation.biome.DefaultBiomeDecider;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.DiminishingColorChangingParticle;
import bloodandmithril.graphics.particles.DiminishingTracerParticle;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService.BloodSplat;
import bloodandmithril.graphics.particles.ParticleService.FlameEmber;
import bloodandmithril.graphics.particles.ParticleService.ParrySpark;
import bloodandmithril.graphics.particles.RandomParticle;
import bloodandmithril.graphics.particles.TracerParticle;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.Item.VisibleItem;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.container.GlassBottleItem;
import bloodandmithril.item.items.container.LiquidContainerItem;
import bloodandmithril.item.items.container.WoodenBucketItem;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.items.equipment.EquipperImpl;
import bloodandmithril.item.items.equipment.EquipperImpl.AlwaysTrueFunction;
import bloodandmithril.item.items.equipment.EquipperImpl.FalseFunction;
import bloodandmithril.item.items.equipment.EquipperImpl.RingFunction;
import bloodandmithril.item.items.equipment.offhand.Lantern;
import bloodandmithril.item.items.equipment.offhand.OffhandEquipment;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.item.items.equipment.weapon.Dagger;
import bloodandmithril.item.items.equipment.weapon.OneHandedAxe;
import bloodandmithril.item.items.equipment.weapon.OneHandedMeleeWeapon;
import bloodandmithril.item.items.equipment.weapon.RangedWeapon;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.items.equipment.weapon.ranged.LongBow;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile.ArrowItem;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.FireArrowProjectile;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.FireArrowProjectile.FireArrowItem;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.GlowStickArrowProjectile;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.GlowStickArrowProjectile.GlowStickArrowItem;
import bloodandmithril.item.items.food.animal.ChickenLegItem;
import bloodandmithril.item.items.food.plant.CarrotItem;
import bloodandmithril.item.items.food.plant.CarrotItem.CarrotSeedItem;
import bloodandmithril.item.items.food.plant.CarrotItem.CarrotSeedProp;
import bloodandmithril.item.items.food.plant.DeathCapItem;
import bloodandmithril.item.items.furniture.MedievalWallTorchItem;
import bloodandmithril.item.items.material.ArrowHeadItem;
import bloodandmithril.item.items.material.BrickItem;
import bloodandmithril.item.items.material.GlassItem;
import bloodandmithril.item.items.material.IngotItem;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.items.material.SlabItem;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.items.mineral.earth.AshesItem;
import bloodandmithril.item.items.mineral.earth.DirtItem;
import bloodandmithril.item.items.mineral.earth.SandItem;
import bloodandmithril.item.items.misc.CurrencyItem;
import bloodandmithril.item.items.misc.FlintAndFiresteelItem;
import bloodandmithril.item.items.misc.key.Key;
import bloodandmithril.item.items.misc.key.SkeletonKeyItem;
import bloodandmithril.item.liquid.Acid;
import bloodandmithril.item.liquid.Blood;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.liquid.Milk;
import bloodandmithril.item.liquid.Oil;
import bloodandmithril.item.liquid.Water;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.Hematite;
import bloodandmithril.item.material.mineral.Mineral;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.functions.IndividualSelected;
import bloodandmithril.networking.requests.AddFloatingTextNotification;
import bloodandmithril.networking.requests.AttackRequest;
import bloodandmithril.networking.requests.CSIMineTile;
import bloodandmithril.networking.requests.CSIOpenCraftingStation;
import bloodandmithril.networking.requests.CSIOpenCraftingStation.NotifyOpenCraftingStationWindow;
import bloodandmithril.networking.requests.CSITradeWith;
import bloodandmithril.networking.requests.ChangeFactionControlPassword;
import bloodandmithril.networking.requests.ChangeIndividualBiography;
import bloodandmithril.networking.requests.ChangeNickName;
import bloodandmithril.networking.requests.ChangeNickName.ChangeNickNameResponse;
import bloodandmithril.networking.requests.ClientConnected;
import bloodandmithril.networking.requests.ConstructionRequest;
import bloodandmithril.networking.requests.ConsumeItem;
import bloodandmithril.networking.requests.DestroyPropNotification;
import bloodandmithril.networking.requests.DestroyTile;
import bloodandmithril.networking.requests.DestroyTile.DestroyTileResponse;
import bloodandmithril.networking.requests.DiscardLiquid;
import bloodandmithril.networking.requests.DrinkLiquid;
import bloodandmithril.networking.requests.EquipOrUnequipItem;
import bloodandmithril.networking.requests.FollowRequest;
import bloodandmithril.networking.requests.GenerateChunk;
import bloodandmithril.networking.requests.GenerateChunk.GenerateChunkResponse;
import bloodandmithril.networking.requests.IndividualSelection;
import bloodandmithril.networking.requests.IndividualSpeakRequest;
import bloodandmithril.networking.requests.LockUnlockContainerRequest;
import bloodandmithril.networking.requests.MessageWindowNotification;
import bloodandmithril.networking.requests.MoveIndividual;
import bloodandmithril.networking.requests.NotifyOpenConstructionWindow;
import bloodandmithril.networking.requests.NotifyTextBubble;
import bloodandmithril.networking.requests.OpenTradeWindow;
import bloodandmithril.networking.requests.Ping;
import bloodandmithril.networking.requests.Ping.Pong;
import bloodandmithril.networking.requests.PlacePropRequest;
import bloodandmithril.networking.requests.PlaySound;
import bloodandmithril.networking.requests.RefreshFactionWindow;
import bloodandmithril.networking.requests.RefreshWindows;
import bloodandmithril.networking.requests.RefreshWindowsResponse;
import bloodandmithril.networking.requests.RequestAttackRanged;
import bloodandmithril.networking.requests.RequestClientList;
import bloodandmithril.networking.requests.RequestClientList.RequestClientListResponse;
import bloodandmithril.networking.requests.RequestConstructDeconstruct;
import bloodandmithril.networking.requests.RequestDiscardItem;
import bloodandmithril.networking.requests.RequestLightCampfire;
import bloodandmithril.networking.requests.RequestPlantSeed;
import bloodandmithril.networking.requests.RequestSetAmmo;
import bloodandmithril.networking.requests.RequestStartCrafting;
import bloodandmithril.networking.requests.RequestSuppressAI;
import bloodandmithril.networking.requests.RequestTakeItem;
import bloodandmithril.networking.requests.RequestTakeItemFromCraftingStation;
import bloodandmithril.networking.requests.RequestThrowItem;
import bloodandmithril.networking.requests.RequestTransferLiquidBetweenContainers;
import bloodandmithril.networking.requests.RunStaticMethodNotification;
import bloodandmithril.networking.requests.SendChatMessage;
import bloodandmithril.networking.requests.SendChatMessage.Message;
import bloodandmithril.networking.requests.SendChatMessage.SendChatMessageResponse;
import bloodandmithril.networking.requests.SendHarvestRequest;
import bloodandmithril.networking.requests.SetAIIdle;
import bloodandmithril.networking.requests.SynchronizeFaction;
import bloodandmithril.networking.requests.SynchronizeFaction.SynchronizeFactionResponse;
import bloodandmithril.networking.requests.SynchronizeIndividual;
import bloodandmithril.networking.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.networking.requests.SynchronizeItems;
import bloodandmithril.networking.requests.SynchronizeParticles;
import bloodandmithril.networking.requests.SynchronizeProjectiles;
import bloodandmithril.networking.requests.SynchronizePropRequest;
import bloodandmithril.networking.requests.SynchronizePropRequest.SynchronizePropResponse;
import bloodandmithril.networking.requests.SynchronizeWorldState;
import bloodandmithril.networking.requests.SynchronizeWorldState.SynchronizeWorldStateResponse;
import bloodandmithril.networking.requests.ToggleWalkRun;
import bloodandmithril.networking.requests.TransferItems;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;
import bloodandmithril.networking.requests.dev.RequestSpawnIndividual;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Harvestable.VisibleHarvestable;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Lightable.LightableUnlit;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.prop.furniture.Furniture;
import bloodandmithril.prop.furniture.Furniture.NonPassableTilesOnly;
import bloodandmithril.prop.furniture.MedievalWallTorchProp.NotEmptyTile;
import bloodandmithril.prop.furniture.RottenWoodenChest;
import bloodandmithril.prop.furniture.SmallWoodenCrateProp;
import bloodandmithril.prop.furniture.WoodenChestProp;
import bloodandmithril.prop.plant.DeadDesertBush;
import bloodandmithril.prop.plant.DryGrass;
import bloodandmithril.prop.plant.GreenGrass;
import bloodandmithril.prop.plant.PlantProp;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.ui.FloatingText;
import bloodandmithril.ui.components.TextBubble.TextBubbleSerializableBean;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.RepeatingCountdown;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.Commands;
import bloodandmithril.util.datastructure.ConcurrentDualKeySkipListMap;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.util.datastructure.SerializableDoubleWrapper;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.WorldProjectiles;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.DebugTile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;
import bloodandmithril.world.topography.tile.tiles.BrickTile;
import bloodandmithril.world.topography.tile.tiles.GlassTile;
import bloodandmithril.world.topography.tile.tiles.SeditmentaryTile;
import bloodandmithril.world.topography.tile.tiles.SoilTile;
import bloodandmithril.world.topography.tile.tiles.StoneTile;
import bloodandmithril.world.topography.tile.tiles.brick.GreyBrickTile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickFloor;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.glass.ClearGlassTile;
import bloodandmithril.world.topography.tile.tiles.glass.InterlacedWindowTile;
import bloodandmithril.world.topography.tile.tiles.sedimentary.SandTile;
import bloodandmithril.world.topography.tile.tiles.soil.DryDirtTile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;
import bloodandmithril.world.topography.tile.tiles.stone.GraniteTile;
import bloodandmithril.world.topography.tile.tiles.stone.SandStoneTile;

@Copyright("Matthew Peck 2014")
public class ClientServerInterface {

	private static boolean isClient, isServer;
	public static Client client;
	public static Server server;
	public static Thread syncThread;
	public static String clientName;
	public static ExecutorService serverThread;
	public static HashMap<Integer, String> connectedPlayers = Maps.newHashMap();
	public static long ping;

	/**
	 * Sets up the client and attempt to connect to the server
	 * @throws IOException
	 */
	public static void setupAndConnect(final String ip) throws IOException {
		clientName = InetAddress.getLocalHost().getHostName();

		client = new Client(65536, 65536);
		registerClasses(client.getKryo());
		client.start();
		client.connect(5000, ip, 42685, 42686);

		((Kryo.DefaultInstantiatorStrategy) client.getKryo().getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
		client.getUpdateThread().setUncaughtExceptionHandler(
			(thread, throwable) -> {
				Logger.networkDebug(throwable.getMessage(), LogLevel.WARN);
				throwable.printStackTrace();
			}
		);

		client.addListener(new ClientListener(client));

		client.sendTCP(new ClientConnected(client.getID(), clientName));
		client.sendTCP(new SynchronizeWorldState());
		SendRequest.sendRequestConnectedPlayerNamesRequest();
	}


	public static synchronized void sendNotification(final int connectionId, final boolean tcp, final boolean executeInSingleThread, final Response... responses) {
		serverThread.execute(
			() -> {
				for (final Connection connection : server.getConnections()) {
					if (connectionId == -1) {
						final Responses resp = new Responses(executeInSingleThread);
						for (final Response response : responses) {
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
						final Responses resp = new Responses(executeInSingleThread);
						for (final Response response : responses) {
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

	public static synchronized void setServer(final boolean isServer) {
		ClientServerInterface.isServer = isServer;
	}

	public static synchronized void setClient(final boolean isClient) {
		ClientServerInterface.isClient = isClient;
	}

	public static int getClientID() {
		return client == null ? 0 : client.getID();
	}


	/**
	 * Registers all request classes
	 */
	public static void registerClasses(final Kryo kryo) {
		kryo.setReferences(true);
		kryo.register(RequestSpawnIndividual.class);

		kryo.register(GreenGrass.class);
		kryo.register(GreyBrickTile.class);
		kryo.register(RepeatingCountdown.class);
		kryo.register(RandomParticle.class);
		kryo.register(SuspicionLevel.class);
		kryo.register(Lightable.class);
		kryo.register(WrapperForTwo.class);
		kryo.register(VisibleItem.class);
		kryo.register(VisibleHarvestable.class);
		kryo.register(TypeEntityVisible.class);
		kryo.register(SpecificEntityVisible.class);
		kryo.register(LightableUnlit.class);
		kryo.register(EntityVisible.class);
		kryo.register(SuspiciousSoundAITriggerFunction.class);
		kryo.register(StimulusTriggerFunction.class);
		kryo.register(IndividualConditionTriggerFunction.class);
		kryo.register(IndividualHealthTriggerFunction.class);
		kryo.register(IndividualAffectedByConditionTriggerFunction.class);
		kryo.register(String[].class);
		kryo.register(TakeVisibleItemTaskGenerator.class);
		kryo.register(SpeakTaskGenerator.class);
		kryo.register(PlantSeedTaskGenerator.class);
		kryo.register(LootAreaTaskGenerator.class);
		kryo.register(LightSelectedLightablesTaskGenerator.class);
		kryo.register(LightLightablesInAreaTaskGenerator.class);
		kryo.register(HarvestVisibleEntityTaskGenerator.class);
		kryo.register(HarvestSelectedHarvestablesTaskGenerator.class);
		kryo.register(HarvestAreaTaskGenerator.class);
		kryo.register(GoToMovingLocationTaskGenerator.class);
		kryo.register(GenerateLightAnyVisibleLightables.class);
		kryo.register(FollowTaskGenerator.class);
		kryo.register(AttackVisibleIndividualTaskGenerator.class);
		kryo.register(AttackTaskGenerator.class);
		kryo.register(TaskGenerator.class);
		kryo.register(StimulusDrivenRoutine.class);
		kryo.register(DailyRoutine.class);
		kryo.register(EntityVisibleRoutine.class);
		kryo.register(IndividualConditionRoutine.class);
		kryo.register(Routine.class);
		kryo.register(Behaviour.class);
		kryo.register(DefaultBiomeDecider.class);
		kryo.register(BiomeDecider.class);
		kryo.register(DryGrass.class);
		kryo.register(Lantern.class);
		kryo.register(RequestSuppressAI.class);
		kryo.register(RequestThrowItem.class);
		kryo.register(RottenWoodenChest.class);
		kryo.register(DeadDesertBush.class);
		kryo.register(SerializableDoubleWrapper.class);
		kryo.register(Sniffer.class);
		kryo.register(SoundStimulus.class);
		kryo.register(SuspiciousSound.class);
		kryo.register(bloodandmithril.character.ai.perception.Listener.class);
		kryo.register(Observer.class);
		kryo.register(Stimulus.class);
		kryo.register(OffhandEquipment.class);
		kryo.register(bloodandmithril.item.items.furniture.SmallWoodenCrateItem.class);
		kryo.register(SmallWoodenCrateProp.class);
		kryo.register(Proficiency.class);
		kryo.register(Proficiencies.class);
		kryo.register(bloodandmithril.character.proficiency.proficiencies.Trading.class);
		kryo.register(Smithing.class);
		kryo.register(Torch.class);
		kryo.register(Cooking.class);
		kryo.register(Carpentry.class);
		kryo.register(LinkedBlockingQueue.class);
		kryo.register(IndividualSpeakRequest.class);
		kryo.register(NonPassableTilesOnly.class);
		kryo.register(DiminishingColorChangingParticle.class);
		kryo.register(NotEmptyTile.class);
		kryo.register(ArrowHeadItem.class);
		kryo.register(StickItem.class);
		kryo.register(NotifyTextBubble.class);
		kryo.register(TextBubbleSerializableBean.class);
		kryo.register(Particle.class);
		kryo.register(TracerParticle.class);
		kryo.register(DiminishingTracerParticle.class);
		kryo.register(SynchronizeParticles.class);
		kryo.register(WorldProjectiles.class);
		kryo.register(Projectile.class);
		kryo.register(SynchronizeProjectiles.class);
		kryo.register(RequestAttackRanged.class);
		kryo.register(RequestSetAmmo.class);
		kryo.register(ReturnIndividualPosition.class);
		kryo.register(SandStone.class);
		kryo.register(LongBow.class);
		kryo.register(bloodandmithril.prop.furniture.MedievalWallTorchProp.class);
		kryo.register(MedievalWallTorchItem.class);
		kryo.register(GlowStickArrowItem.class);
		kryo.register(GlowStickArrowProjectile.class);
		kryo.register(FireArrowItem.class);
		kryo.register(FireArrowProjectile.class);
		kryo.register(ArrowItem.class);
		kryo.register(ArrowProjectile.class);
		kryo.register(JitGoToLocationFunction.class);
		kryo.register(JitGoToLocation.class);
		kryo.register(Jump.class);
		kryo.register(Travel.class);
		kryo.register(MovementMode.class);
		kryo.register(HareAI.class);
		kryo.register(Hare.class);
		kryo.register(FlintAndFiresteelItem.class);
		kryo.register(RequestLightCampfire.class);
		kryo.register(LightFire.class);
		kryo.register(LightLightable.class);
		kryo.register(Campfire.class);
		kryo.register(FlameEmber.class);
		kryo.register(IndividualSelected.class);
		kryo.register(MessageWindowNotification.class);
		kryo.register(PlantSeed.class);
		kryo.register(RequestPlantSeed.class);
		kryo.register(CarrotSeedProp.class);
		kryo.register(CarrotSeedItem.class);
		kryo.register(bloodandmithril.item.items.food.plant.SeedItem.class);
		kryo.register(SeedProp.class);
		kryo.register(AttemptMine.class);
		kryo.register(WithinInteractionBox.class);
		kryo.register(Growable.class);
		kryo.register(Function.class);
		kryo.register(SerializableMappingFunction.class);
		kryo.register(PlacePropRequest.class);
		kryo.register(FollowRequest.class);
		kryo.register(DiscardLiquid.class);
		kryo.register(ParrySpark.class);
		kryo.register(BloodSplat.class);
		kryo.register(RunStaticMethodNotification.class);
		kryo.register(PlaySound.class);
		kryo.register(ReevaluateAttack.class);
		kryo.register(Countdown.class);
		kryo.register(WithinNumberOfWaypointsFunction.class);
		kryo.register(RepathCondition.class);
		kryo.register(Follow.class);
		kryo.register(FloatingText.class);
		kryo.register(AddFloatingTextNotification.class);
		kryo.register(OneHandedAxe.class);
		kryo.register(Machette.class);
		kryo.register(Broadsword.class);
		kryo.register(CombatKnife.class);
		kryo.register(BushKnife.class);
		kryo.register(WithinAttackRangeOrCantAttack.class);
		kryo.register(AttackTarget.class);
		kryo.register(Attack.class);
		kryo.register(AttackRequest.class);
		kryo.register(FalseFunction.class);
		kryo.register(Furniture.class);
		kryo.register(YellowBrickFloor.class);
		kryo.register(AITask.class);
		kryo.register(AStarPathFinder.Node.class);
		kryo.register(AStarPathFinder.class);
		kryo.register(Acid.class);
		kryo.register(Action.class);
		kryo.register(AlwaysTrueFunction.class);
		kryo.register(BlacksmithWorkshop.class);
		kryo.register(ArrayDeque.class);
		kryo.register(ArrayList.class);
		kryo.register(ArtificialIntelligence.AIMode.class);
		kryo.register(ArtificialIntelligence.class);
		kryo.register(AshesItem.class);
		kryo.register(Bleeding.class);
		kryo.register(Blood.class);
		kryo.register(HareAI.class);
		kryo.register(Box.class);
		kryo.register(BrickItem.class);
		kryo.register(BrickTile.class);
		kryo.register(Dagger.class);
		kryo.register(CSIMineTile.class);
		kryo.register(CSIOpenCraftingStation.class);
		kryo.register(CSITradeWith.class);
		kryo.register(CarrotItem.class);
		kryo.register(ChangeFactionControlPassword.class);
		kryo.register(ChangeIndividualBiography.class);
		kryo.register(ChangeNickName.class);
		kryo.register(ChangeNickNameResponse.class);
		kryo.register(ChickenLegItem.class);
		kryo.register(ChunkData.class);
		kryo.register(Class.class);
		kryo.register(ClearGlassTile.class);
		kryo.register(ClientConnected.class);
		kryo.register(Coal.class);
		kryo.register(Color.class);
		kryo.register(Commands.class);
		kryo.register(CompositeAITask.class);
		kryo.register(ConcurrentDualKeySkipListMap.class);
		kryo.register(ConcurrentHashMap.class);
		kryo.register(ConcurrentLinkedDeque.class);
		kryo.register(ConcurrentSkipListMap.class);
		kryo.register(Construct.class);
		kryo.register(Constructing.class);
		kryo.register(ConstructionRequest.class);
		kryo.register(ConsumeItem.class);
		kryo.register(Container.class);
		kryo.register(ContainerImpl.class);
		kryo.register(Craft.class);
		kryo.register(Crafting.class);
		kryo.register(CraftingStation.class);
		kryo.register(Oil.class);
		kryo.register(CurrencyItem.class);
		kryo.register(DeathCapItem.class);
		kryo.register(DebugTile.class);
		kryo.register(Depth.class);
		kryo.register(DestroyPropNotification.class);
		kryo.register(DestroyTile.class);
		kryo.register(DestroyTileResponse.class);
		kryo.register(DirtItem.class);
		kryo.register(Domain.getIndividualIds().getClass());
		kryo.register(DrinkLiquid.class);
		kryo.register(DryDirtTile.class);
		kryo.register(DualKeyHashMap.class);
		kryo.register(Elf.class);
		kryo.register(ElfAI.class);
		kryo.register(EmptyTile.class);
		kryo.register(Epoch.class);
		kryo.register(EquipOrUnequipItem.class);
		kryo.register(Equipable.class);
		kryo.register(EquipmentSlot.class);
		kryo.register(EquipperImpl.class);
		kryo.register(Exhaustion.class);
		kryo.register(Faction.class);
		kryo.register(Furnace.class);
		kryo.register(GenerateChunk.class);
		kryo.register(GenerateChunkResponse.class);
		kryo.register(GlassItem.class);
		kryo.register(GlassBottleItem.class);
		kryo.register(GlassTile.class);
		kryo.register(GoToLocation.class);
		kryo.register(GoToMovingLocation.class);
		kryo.register(GraniteTile.class);
		kryo.register(Harvest.class);
		kryo.register(HarvestItem.class);
		kryo.register(Harvestable.class);
		kryo.register(HashMap.class);
		kryo.register(HashSet.class);
		kryo.register(Hematite.class);
		kryo.register(Hunger.class);
		kryo.register(Idle.class);
		kryo.register(Individual.class);
		kryo.register(IndividualIdentifier.class);
		kryo.register(IndividualKineticsProcessingData.class);
		kryo.register(IndividualSelection.SelectIndividualResponse.class);
		kryo.register(IndividualSelection.class);
		kryo.register(IndividualState.class);
		kryo.register(IngotItem.class);
		kryo.register(InterlacedWindowTile.class);
		kryo.register(Iron.class);
		kryo.register(Key.class);
		kryo.register(LinkedList.class);
		kryo.register(Liquid.class);
		kryo.register(LiquidContainerItem.class);
		kryo.register(List.class);
		kryo.register(ListingMenuItem.class);
		kryo.register(LockUnlock.class);
		kryo.register(LockUnlockContainer.class);
		kryo.register(LockUnlockContainerRequest.class);
		kryo.register(Message.class);
		kryo.register(Metal.class);
		kryo.register(Milk.class);
		kryo.register(MineTile.class);
		kryo.register(Mineral.class);
		kryo.register(MoveIndividual.class);
		kryo.register(NotifyOpenCraftingStationWindow.class);
		kryo.register(OneHandedMeleeWeapon.class);
		kryo.register(OpenCraftingStation.class);
		kryo.register(OpenCraftingStationWindow.class);
		kryo.register(OpenTradeWindow.OpenTradeWindowResponse.class);
		kryo.register(OpenTradeWindow.class);
		kryo.register(Path.class);
		kryo.register(StandardWood.class);
		kryo.register(Ping.class);
		kryo.register(PlantProp.class);
		kryo.register(Poison.class);
		kryo.register(Pong.class);
		kryo.register(RefreshFactionWindow.class);
		kryo.register(RefreshWindows.class);
		kryo.register(RefreshWindowsResponse.class);
		kryo.register(Request.class);
		kryo.register(RequestClientList.class);
		kryo.register(RequestClientListResponse.class);
		kryo.register(RequestDiscardItem.class);
		kryo.register(RequestStartCrafting.class);
		kryo.register(RequestTakeItem.class);
		kryo.register(RequestTakeItemFromCraftingStation.class);
		kryo.register(RequestTransferLiquidBetweenContainers.class);
		kryo.register(Responses.class);
		kryo.register(RingFunction.class);
		kryo.register(RockItem.class);
		kryo.register(SandItem.class);
		kryo.register(SandStoneTile.class);
		kryo.register(SandTile.class);
		kryo.register(SeditmentaryTile.class);
		kryo.register(SendChatMessage.class);
		kryo.register(SendChatMessageResponse.class);
		kryo.register(SendHarvestRequest.class);
		kryo.register(SerializableColor.class);
		kryo.register(SetAIIdle.class);
		kryo.register(SkeletonKeyItem.class);
		kryo.register(Proficiencies.class);
		kryo.register(SlabItem.class);
		kryo.register(SoilTile.class);
		kryo.register(StandardSoilTile.class);
		kryo.register(Steel.class);
		kryo.register(StoneTile.class);
		kryo.register(SynchronizeFaction.class);
		kryo.register(SynchronizeFactionResponse.class);
		kryo.register(SynchronizeIndividual.class);
		kryo.register(SynchronizeIndividualResponse.class);
		kryo.register(SynchronizeItems.class);
		kryo.register(SynchronizePropRequest.class);
		kryo.register(SynchronizePropResponse.class);
		kryo.register(SynchronizeWorldState.class);
		kryo.register(SynchronizeWorldStateResponse.class);
		kryo.register(Take.class);
		kryo.register(TakeItem.class);
		kryo.register(Thirst.class);
		kryo.register(Tile.Orientation.class);
		kryo.register(Tile.class);
		kryo.register(Tile[].class);
		kryo.register(Tile[][].class);
		kryo.register(ToggleWalkRun.class);
		kryo.register(Trade.class);
		kryo.register(TradeWith.class);
		kryo.register(Trading.class);
		kryo.register(TransferItems.TradeEntity.class);
		kryo.register(TransferItems.TransferItemsResponse.class);
		kryo.register(TransferItems.class);
		kryo.register(TreeMap.class);
		kryo.register(Vector2.class);
		kryo.register(Wait.class);
		kryo.register(Water.class);
		kryo.register(WayPoint.class);
		kryo.register(Weapon.class);
		kryo.register(WoodenBucketItem.class);
		kryo.register(WoodenChestProp.class);
		kryo.register(WorkBench.class);
		kryo.register(YellowBrickPlatform.class);
		kryo.register(YellowBrickTile.class);
		kryo.register(bloodandmithril.item.items.furniture.WoodenChestItem.class);
		kryo.register(bloodandmithril.prop.plant.CarrotProp.class);
	}


	/**
	 * Send a {@link Request} to the {@link Server}
	 *
	 * @author Matt
	 */
	public static class SendRequest {
		public static synchronized void sendAISuppressionRequest(final Individual individual, final boolean suppress) {
			client.sendTCP(new RequestSuppressAI(individual, suppress));
			Logger.networkDebug("Sending AI suppression request", LogLevel.DEBUG);
		}

		public static synchronized void sendThrowItemRequest(final Individual individual, final Item item, final Vector2 mouseCoords) {
			client.sendTCP(new RequestThrowItem(individual, item, mouseCoords));
			Logger.networkDebug("Sending throw item request", LogLevel.DEBUG);
		}

		public static synchronized void sendSpawnIndividualRequest(final Individual individual) {
			client.sendTCP(new RequestSpawnIndividual(individual));
			Logger.networkDebug("Sending individual spawn request", LogLevel.DEBUG);
		}

		public static synchronized void sendLightLightableRequest(final Individual host, final Lightable lightable) {
			client.sendTCP(new RequestLightCampfire(host, lightable));
			Logger.networkDebug("Sending light lightable request", LogLevel.DEBUG);
		}


		public static synchronized void sendPlantSeedRequest(final Individual planter, final SeedProp toPlant) {
			client.sendTCP(new RequestPlantSeed(planter, toPlant));
			Logger.networkDebug("Sending seed planting request", LogLevel.DEBUG);
		}


		public static synchronized void sendGenerateChunkRequest(final int x, final int y, final int worldId) {
			client.sendTCP(new GenerateChunk(x, y, worldId));
			Logger.networkDebug("Sending chunk generation request for (" + x + ", " + y + ")", LogLevel.DEBUG);
		}


		public static synchronized void sendFollowRequest(final Individual follower, final Individual followee, final int distance, final SerializableFunction<Boolean> terminationCondition) {
			client.sendTCP(new FollowRequest(follower, followee, distance, terminationCondition));
			Logger.networkDebug("Sending chunk follow request", LogLevel.DEBUG);
		}


		public static synchronized void sendPlacePropRequest(final Individual indi, final PropItem propItem, final float x, final float y, final Prop prop, final int worldId) {
			client.sendTCP(new PlacePropRequest(propItem, indi == null ? null : indi.getId().getId(), prop, x, y, worldId));
			Logger.networkDebug("Sending prop placement request", LogLevel.DEBUG);
		}


		public static synchronized void sendDiscardItemRequest(final Individual individual, final Item item, final int quantity) {
			client.sendTCP(new RequestDiscardItem(individual, item, quantity));
			Logger.networkDebug("Sending discard item request", LogLevel.DEBUG);
		}


		public static synchronized void sendUpdateBiographyRequest(final Individual individual, final String description) {
			client.sendTCP(new ChangeIndividualBiography(individual, description));
			Logger.networkDebug("Sending change individual description request", LogLevel.DEBUG);
		}


		public static synchronized void sendStartCraftingRequest(final Individual individual, final CraftingStation craftingStation, final SerializableDoubleWrapper<Item, Integer> item, final int quantity) {
			client.sendTCP(new RequestStartCrafting(individual, craftingStation, item, quantity));
			Logger.networkDebug("Sending start crafting item request", LogLevel.DEBUG);
		}


		public static synchronized void sendTakeItemFromCraftingStationRequest(final CraftingStation craftingStation, final Individual individual) {
			client.sendTCP(new RequestTakeItemFromCraftingStation(individual, craftingStation));
			Logger.networkDebug("Sending take item from crafting station request", LogLevel.DEBUG);
		}


		public static synchronized void sendOpenCraftingStationRequest(final Individual individual, final CraftingStation craftingStation) {
			client.sendTCP(new CSIOpenCraftingStation(individual.getId().getId(), craftingStation.id, client.getID(), individual.getWorldId()));
			Logger.networkDebug("Sending smith request", LogLevel.DEBUG);
		}

		public static synchronized void sendLockUnlockContainerRequest(final int individualId, final int containerId, final boolean lock) {
			client.sendTCP(new LockUnlockContainerRequest(individualId, containerId, lock));
			Logger.networkDebug("Sending lock/unlock container request", LogLevel.DEBUG);
		}

		public static synchronized void sendHarvestRequest(final int individualId, final int propId) {
			client.sendTCP(new SendHarvestRequest(individualId, propId));
			Logger.networkDebug("Sending harvest request", LogLevel.DEBUG);
		}

		public static synchronized void sendConstructRequest(final int individualId, final int propId, final boolean deconstruct) {
			client.sendTCP(new ConstructionRequest(individualId, propId, deconstruct));
			Logger.networkDebug("Sending construction request", LogLevel.DEBUG);
		}

		public static synchronized void sendConsumeItemRequest(final Consumable consumable, final int individualId) {
			client.sendTCP(new ConsumeItem(consumable, individualId));
			Logger.networkDebug("Sending item consumption request", LogLevel.DEBUG);
		}

		public static synchronized void sendEquipOrUnequipItemRequest(final boolean equip, final Equipable equipable, final int individualId) {
			client.sendTCP(new EquipOrUnequipItem(equip, equipable, individualId));
			Logger.networkDebug("Sending equip/unequip request", LogLevel.DEBUG);
		}

		public static synchronized void sendRunWalkRequest(final int individualId, final boolean walk) {
			client.sendTCP(new ToggleWalkRun(individualId, walk));
			Logger.networkDebug("Sending run/walk request", LogLevel.DEBUG);
		}

		public static synchronized void sendDrinkLiquidRequest(final int individualId, final LiquidContainerItem bottleToDrinkFrom, final float amount) {
			client.sendTCP(new DrinkLiquid(individualId, bottleToDrinkFrom, amount));
			Logger.networkDebug("Sending drink liquid request", LogLevel.DEBUG);
		}

		public static synchronized void sendDiscardLiquidRequest(final int individualId, final LiquidContainerItem bottleToDiscardFrom, final float amount) {
			client.sendTCP(new DiscardLiquid(individualId, bottleToDiscardFrom, amount));
			Logger.networkDebug("Sending discard liquid request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestTransferLiquidBetweenContainers(final Individual individual, final LiquidContainerItem from, final LiquidContainerItem to, final float amount) {
			client.sendTCP(new RequestTransferLiquidBetweenContainers(individual, from, to, amount));
			Logger.networkDebug("Sending transfer liquid between containers request", LogLevel.DEBUG);
		}

		public static synchronized void sendAttackRangedRequest(final Individual individual, final Vector2 direction) {
			client.sendTCP(new RequestAttackRanged(individual.getId().getId(), direction));
			Logger.networkDebug("Sending ranged attack request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestChangeAmmo(final Individual individual, final RangedWeapon weapon, final Item ammo) {
			client.sendTCP(new RequestSetAmmo(individual.getId().getId(), weapon, ammo));
			Logger.networkDebug("Sending change ammo request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestAttack(final Individual attacker, final Individual... victims) {
			client.sendTCP(new AttackRequest(attacker, Sets.newHashSet(victims)));
			Logger.networkDebug("Sending attack request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestConnectedPlayerNamesRequest() {
			client.sendTCP(new RequestClientList());
			Logger.networkDebug("Sending client name list request", LogLevel.DEBUG);
		}

		public static synchronized void sendIndividualSpeakRequest(final Individual individual, final boolean speak) {
			client.sendTCP(new IndividualSpeakRequest(individual, speak));
			Logger.networkDebug("Sending individual speak request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestTakeItem(final Individual individual, final Item item) {
			client.sendTCP(new RequestTakeItem(individual, Lists.newArrayList(item)));
			Logger.networkDebug("Sending take item request", LogLevel.DEBUG);
		}

		public static synchronized void sendRequestTakeItems(final Individual individual, final Collection<Item> items) {
			client.sendTCP(new RequestTakeItem(individual, items));
			Logger.networkDebug("Sending take item request", LogLevel.DEBUG);
		}

		public static synchronized void sendSynchronizeIndividualRequest(final int id) {
			client.sendUDP(new SynchronizeIndividual(id));
			Logger.networkDebug("Sending sync individual request for " + id, LogLevel.TRACE);
		}

		public static synchronized void sendSynchronizeIndividualRequest() {
			client.sendUDP(new SynchronizeIndividual());
			Logger.networkDebug("Sending individual sync request for all", LogLevel.TRACE);
		}

		public static synchronized void sendDestroyTileRequest(final float worldX, final float worldY, final boolean foreground, final int worldId) {
			client.sendTCP(new DestroyTile(worldX, worldY, foreground, worldId));
			Logger.networkDebug("Sending destroy tile request", LogLevel.DEBUG);
		}

		public static synchronized void sendPingRequest() {
			client.sendUDP(new Ping());
			Logger.networkDebug("Sending ping request", LogLevel.TRACE);
		}

		public static synchronized void sendClearAITaskRequest(final int individualId) {
			client.sendUDP(new SetAIIdle(individualId));
			Logger.networkDebug("Sending request to clear AI task", LogLevel.TRACE);
		}

		public static synchronized void sendSynchronizeFactionsRequest() {
			client.sendTCP(new SynchronizeFaction());
			Logger.networkDebug("Sending synchronize faction request", LogLevel.DEBUG);
		}

		public static synchronized void sendTradeWithIndividualRequest(final Individual proposer, final Individual proposee) {
			client.sendTCP(new CSITradeWith(proposer.getId().getId(), TradeEntity.INDIVIDUAL, proposee.getId().getId(), client.getID()));
			Logger.networkDebug("Sending trade with individual request", LogLevel.DEBUG);
		}

		public static synchronized void sendTradeWithPropRequest(final Individual proposer, final int propId) {
			client.sendTCP(new CSITradeWith(proposer.getId().getId(), TradeEntity.PROP, propId, client.getID()));
			Logger.networkDebug("Sending trade with prop request", LogLevel.DEBUG);
		}

		public static synchronized void sendConstructionRequest(final Individual constructor, final int propId) {
			client.sendTCP(new RequestConstructDeconstruct(constructor.getId().getId(), propId, client.getID()));
			Logger.networkDebug("Sending construction/deconstruction request", LogLevel.DEBUG);
		}

		public static synchronized void sendIndividualSelectionRequest(final int id, final boolean select) {
			client.sendTCP(new IndividualSelection(id, select, client.getID()));
			Logger.networkDebug("Sending individual selection request", LogLevel.DEBUG);
		}

		public static synchronized void sendMoveIndividualRequest(final int id, final Vector2 destinationCoordinates, final boolean forceMove, final boolean add, final boolean jump, final Vector2 jumpFrom, final Vector2 jumpTo) {
			client.sendTCP(new MoveIndividual(id, destinationCoordinates, forceMove, add, jump, jumpFrom, jumpTo));
			Logger.networkDebug("Sending move individual request", LogLevel.DEBUG);
		}

		public static synchronized void sendChangeNickNameRequest(final int id, final String toChangeTo) {
			client.sendTCP(new ChangeNickName(id, toChangeTo));
			Logger.networkDebug("Sending change individual nickname request", LogLevel.DEBUG);
		}

		public static synchronized void sendMineTileRequest(final int individualId, final Vector2 location) {
			client.sendTCP(new CSIMineTile(individualId, location));
			Logger.networkDebug("Sending mine tile request", LogLevel.DEBUG);
		}

		public static synchronized void sendRefreshItemWindowsRequest() {
			client.sendTCP(new RefreshWindows());
			Logger.networkDebug("Sending item window refresh request", LogLevel.DEBUG);
		}

		public static synchronized void sendChangeFactionControlPasswordRequest(final int factionId, final String newPassword) {
			client.sendTCP(new ChangeFactionControlPassword(factionId, newPassword));
			Logger.networkDebug("Sending change faction control password request", LogLevel.DEBUG);
		}

		public static synchronized void sendOpenTradeWindowRequest(final int proposerId, final TradeEntity proposee, final int proposeeId) {
			client.sendTCP(new OpenTradeWindow(proposerId, proposee, proposeeId));
			Logger.networkDebug("Sending open trade window request", LogLevel.DEBUG);
		}

		public static synchronized void sendChatMessage(final String message) {
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
				final HashMap<Item, Integer> proposerItemsToTransfer, final int proposerId,
				final HashMap<Item, Integer> proposeeItemsToTransfer, final TradeEntity proposeeEntityType, final int proposeeId) {

			client.sendTCP(
				new bloodandmithril.networking.requests.TransferItems(
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
		public static synchronized void notifyMessage(final int connectionId, final String title, final String message, final SerializableFunction<Boolean> function) {
			sendNotification(
				connectionId,
				true,
				false,
				new MessageWindowNotification(connectionId, title, message, function)
			);
		}


		public static synchronized void notifyTextBubble(final String text, final SerializableFunction<Vector2> position, final long duration, final int xOffset, final int yOffset, final int connectionId) {
			sendNotification(
				connectionId,
				false,
				true,
				new NotifyTextBubble(position, text, duration, xOffset, yOffset)
			);
		}


		public static synchronized void notifyRunStaticMethod(final int connectionId, final Runnable staticMethod) {
			sendNotification(
				connectionId,
				false,
				true,
				new RunStaticMethodNotification(staticMethod, connectionId)
			);
		}


		public static synchronized void notifyPlaySound(final int connectionId, final int soundId, final Vector2 location) {
			sendNotification(
				connectionId,
				false,
				true,
				new PlaySound(soundId, location)
			);
		}


		public static synchronized void notifyRemoveProp(final int propId, final int worldId) {
			sendNotification(
				-1,
				true,
				true,
				new DestroyPropNotification(propId, worldId)
			);
		}


		public static synchronized void notifySyncItems(final int worldId) {
			sendNotification(
				-1,
				true,
				true,
				new SynchronizeItems(worldId)
			);
		}


		public static synchronized void notifySyncProjectiles(final int worldId) {
			sendNotification(
				-1,
				true,
				true,
				new SynchronizeProjectiles(worldId)
			);
		}


		public static synchronized void notifySyncParticles(final int worldId) {
			sendNotification(
				-1,
				true,
				true,
				new SynchronizeParticles(worldId)
			);
		}


		public static synchronized void notifyAddFloatingText(final FloatingText floatingText, final int worldId) {
			sendNotification(
				-1,
				true,
				true,
				new AddFloatingTextNotification(floatingText, worldId)
			);
		}


		public static synchronized void notifyOpenCraftingStationWindow(final int individualId, final int craftingStationId, final int connectionId, final int worldId) {
			sendNotification(
				connectionId,
				true,
				true,
				new CSIOpenCraftingStation.NotifyOpenCraftingStationWindow(individualId, craftingStationId, worldId)
			);
		}


		public static synchronized void notifySyncPlayerList() {
			final List<String> names = Lists.newLinkedList();
			for (final Connection connection : ClientServerInterface.server.getConnections()) {
				names.add(ClientServerInterface.connectedPlayers.get(connection.getID()));
			}
			sendNotification(
				-1,
				true,
				false,
				new RequestClientListResponse(names)
			);
		}


		public static synchronized void notifySyncFaction(final Faction faction) {
			sendNotification(
				-1,
				false,
				true,
				new SynchronizeFactionResponse(faction)
			);
		}


		public static synchronized void notifyTileMined(final int connectionId, final Vector2 location, final boolean foreGround, final int worldId) {
			sendNotification(
				connectionId,
				true,
				false,
				new DestroyTileResponse(location.x, location.y, foreGround, worldId)
			);
		}


		public static synchronized void notifySyncProp(final Prop prop) {
			sendNotification(
				-1,
				false,
				false,
				new SynchronizePropRequest.SynchronizePropResponse(prop)
			);
		}


		public static synchronized void notifyRefreshWindows() {
			sendNotification(
				-1,
				true,
				false,
				new RefreshWindowsResponse()
			);
		}


		public static synchronized void notifyTradeWindowOpen(final int proposerId, final TradeEntity proposee, final int proposeeId, final int connectionId) {
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


		public static synchronized void notifyConstructionWindowOpen(final int individualId, final int construction, final int connectionId) {
			sendNotification(
				connectionId,
				true,
				false,
				new NotifyOpenConstructionWindow(
					individualId,
					construction
				)
			);
		}


		public static synchronized void notifyGiveItem(final int individualId, final Item item, final Vector2 positionIfCantReceive) {
			final Individual individual = Domain.getIndividual(individualId);
			if (individual.canReceive(item)) {
				individual.giveItem(item);
			} else {
				Domain.getWorld(individual.getWorldId()).items().addItem(item, positionIfCantReceive, new Vector2(40f, 0).rotate(Util.getRandom().nextFloat() * 360f));
			}
			sendNotification(
				-1,
				true,
				true,
				new SynchronizeIndividualResponse(individualId, System.currentTimeMillis()),
				new RefreshWindowsResponse()
			);
		}


		public static synchronized void notifySyncWorldState(final int worldId) {
			sendNotification(
				-1,
				false,
				false,
				new SynchronizeWorldStateResponse(Domain.getWorld(worldId))
			);
		}


		public static synchronized void notifyIndividualSync(final int id) {
			sendNotification(
				-1,
				false,
				false,
				new SynchronizeIndividualResponse(id, System.currentTimeMillis())
			);
		}
	}
}