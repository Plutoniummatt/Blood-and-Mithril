package bloodandmithril.core;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.persistence.PersistenceUtil.encode;
import static bloodandmithril.persistence.PersistenceUtil.readFile;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.GlassBottleItem;
import bloodandmithril.item.items.equipment.offhand.Lantern;
import bloodandmithril.item.items.equipment.offhand.Torch;
import bloodandmithril.item.items.equipment.offhand.shield.WoodenBuckler;
import bloodandmithril.item.items.equipment.offhand.shield.WoodenKiteShield;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.items.equipment.weapon.ranged.LongBow;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.FireArrowProjectile;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.GlowStickArrowProjectile;
import bloodandmithril.item.items.food.animal.ChickenLegItem;
import bloodandmithril.item.items.food.plant.CarrotItem;
import bloodandmithril.item.items.food.plant.DeathCapItem;
import bloodandmithril.item.items.furniture.MedievalWallTorchItem;
import bloodandmithril.item.items.furniture.SmallWoodenCrateItem;
import bloodandmithril.item.items.furniture.WoodenChestItem;
import bloodandmithril.item.items.material.ArrowHeadItem;
import bloodandmithril.item.items.material.BrickItem;
import bloodandmithril.item.items.material.GlassItem;
import bloodandmithril.item.items.material.IngotItem;
import bloodandmithril.item.items.material.LogItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.items.mineral.earth.AshesItem;
import bloodandmithril.item.items.mineral.earth.DirtItem;
import bloodandmithril.item.items.mineral.earth.SandItem;
import bloodandmithril.item.items.misc.CurrencyItem;
import bloodandmithril.item.items.misc.FlintAndFiresteelItem;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.liquid.Oil;
import bloodandmithril.item.liquid.Water;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.persistence.PersistenceUtil;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * An {@link ItemPackage} is essentially a {@link Container} that contains {@link Item}s that can be used to deploy into the game world.
 *
 * @author Matt
 */
public class ItemPackage implements Serializable {
	private static final long serialVersionUID = -801321038681883210L;

	public static final ArrayList<ItemPackage> availableItemPackages = Lists.newArrayList();
	private final Container container;
	private final boolean standardPackage;
	private String name;

	static {
		try {
			availableItemPackages.addAll(
				decode(readFile("packages.txt"))
			);
		} catch (FileNotFoundException e) {
			save();
		} catch (Exception e) {
			Logger.generalDebug("Failed to load item packages", LogLevel.OVERRIDE, e);
		}
	}

	/**
	 * Constructor
	 */
	public ItemPackage(Container container, String name, boolean standardPackage) {
		this.container = container;
		this.name = name;
		this.standardPackage = standardPackage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Container getContainer() {
		return container;
	}

	public static List<ItemPackage> getAvailablePackages() {
		filter();
		addDefaults();
		return availableItemPackages;
	}

	private static void addDefaults() {
		Container prop = (Container) new SmallWoodenCrateItem(StandardWood.class).getProp();
		prop.giveItem(new FlintAndFiresteelItem(), 1);
		prop.giveItem(PlankItem.plank(StandardWood.class), 20);
		prop.giveItem(StickItem.stick(StandardWood.class), 20);
		prop.giveItem(new CarrotItem(), 25);
		prop.giveItem(new ChickenLegItem(false), 5);
		prop.giveItem(new BushKnife(), 2);

		HashMap<Class<? extends Liquid>, Float> newHashMap = Maps.newHashMap();
		newHashMap.put(Water.class, 2f);
		prop.giveItem(new GlassBottleItem(newHashMap), 5);

		availableItemPackages.add(
			new ItemPackage(
				prop,
				"Starter package",
				true
			)
		);
		
		if (BloodAndMithrilClient.devMode) {
			Container chest = (Container) new WoodenChestItem(StandardWood.class).getProp();
			chest.giveItem(new WoodenKiteShield(), 10);
			chest.giveItem(new WoodenBuckler(), 10);
			chest.giveItem(new FlintAndFiresteelItem(), 10);
			chest.giveItem(PlankItem.plank(StandardWood.class), 200);
			chest.giveItem(StickItem.stick(StandardWood.class), 200);
			chest.giveItem(new BushKnife(), 10);
			chest.giveItem(new CombatKnife(), 10);
			chest.giveItem(new Broadsword(), 10);
			chest.giveItem(new Machette(), 10);
			chest.giveItem(new LongBow<StandardWood>(StandardWood.class), 10);
			chest.giveItem(new GlowStickArrowProjectile.GlowStickArrowItem<Steel>(Steel.class), 500);
			chest.giveItem(new FireArrowProjectile.FireArrowItem<Steel>(Steel.class), 500);
			chest.giveItem(new CarrotItem(), 500);
			chest.giveItem(new DeathCapItem(false), 500);
			chest.giveItem(new DeathCapItem(true), 500);
			chest.giveItem(new ChickenLegItem(false), 500);
			chest.giveItem(new ChickenLegItem(true), 500);
			chest.giveItem(RockItem.rock(Coal.class), 500);
			chest.giveItem(RockItem.rock(SandStone.class), 500);
			chest.giveItem(new AshesItem(), 500);
			chest.giveItem(new SandItem(), 500);
			chest.giveItem(new DirtItem(), 500);
			chest.giveItem(LogItem.log(StandardWood.class), 500);
			chest.giveItem(PlankItem.plank(StandardWood.class), 500);
			chest.giveItem(new Torch(), 10);
			HashMap<Class<? extends Liquid>, Float> map = Maps.newHashMap();
			map.put(Water.class, 2f);
			HashMap<Class<? extends Liquid>, Float> map2 = Maps.newHashMap();
			map2.put(Oil.class, 2f);
			chest.giveItem(new GlassBottleItem(map), 5);
			chest.giveItem(new GlassBottleItem(map2), 5);
			chest.giveItem(BrickItem.brick(SandStone.class), 100);
			chest.giveItem(new GlassItem(), 100);
			chest.giveItem(IngotItem.ingot(Iron.class), 100);
			chest.giveItem(IngotItem.ingot(Steel.class), 100);
			chest.giveItem(StickItem.stick(StandardWood.class), 100);
			chest.giveItem(new CurrencyItem(), 1000);
			chest.giveItem(new Lantern(100f), 10);
			chest.giveItem(new MedievalWallTorchItem(), 1000);
			chest.giveItem(new CarrotItem.CarrotSeedItem(), 1000);
			chest.giveItem(ArrowHeadItem.arrowHead(Steel.class), 1000);
			
			availableItemPackages.add(
				new ItemPackage(
					chest,
					"Developer package",
					true
				)
			);
		}
	}

	public static void save() {
		filter();
		PersistenceUtil.writeFile("packages.txt", encode(availableItemPackages));
	}

	private static void filter() {
		List<ItemPackage> toKeep = Lists.newArrayList(Collections2.filter(availableItemPackages, itemPackage -> {
			return !itemPackage.isDefault();
		}));

		availableItemPackages.clear();
		availableItemPackages.addAll(toKeep);
	}

	public boolean isDefault() {
		return standardPackage;
	}
}