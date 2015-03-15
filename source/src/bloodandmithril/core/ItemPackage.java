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
import bloodandmithril.item.items.container.GlassBottle;
import bloodandmithril.item.items.equipment.misc.FlintAndFiresteel;
import bloodandmithril.item.items.equipment.misc.Lantern;
import bloodandmithril.item.items.equipment.misc.Torch;
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.equipment.weapon.dagger.CombatKnife;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Machette;
import bloodandmithril.item.items.equipment.weapon.ranged.LongBow;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.FireArrow;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.GlowStickArrow;
import bloodandmithril.item.items.food.animal.ChickenLeg;
import bloodandmithril.item.items.food.plant.Carrot;
import bloodandmithril.item.items.food.plant.DeathCap;
import bloodandmithril.item.items.furniture.MedievalWallTorch;
import bloodandmithril.item.items.furniture.SmallWoodenCrate;
import bloodandmithril.item.items.furniture.WoodenChest;
import bloodandmithril.item.items.material.Brick;
import bloodandmithril.item.items.material.Glass;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.items.material.Log;
import bloodandmithril.item.items.material.Plank;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.items.material.Stick;
import bloodandmithril.item.items.mineral.earth.Ashes;
import bloodandmithril.item.items.mineral.earth.Dirt;
import bloodandmithril.item.items.mineral.earth.Sand;
import bloodandmithril.item.items.misc.Currency;
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
		Container prop = (Container) new SmallWoodenCrate(StandardWood.class).getProp();
		prop.giveItem(new FlintAndFiresteel(), 1);
		prop.giveItem(Plank.plank(StandardWood.class), 20);
		prop.giveItem(Stick.stick(StandardWood.class), 20);
		prop.giveItem(new Carrot(), 25);
		prop.giveItem(new ChickenLeg(false), 5);
		prop.giveItem(new BushKnife(), 2);

		HashMap<Class<? extends Liquid>, Float> newHashMap = Maps.newHashMap();
		newHashMap.put(Water.class, 2f);
		prop.giveItem(new GlassBottle(newHashMap), 5);

		availableItemPackages.add(
			new ItemPackage(
				prop,
				"Starter package",
				true
			)
		);
		
		if (BloodAndMithrilClient.devMode) {
			Container chest = (Container) new WoodenChest(StandardWood.class).getProp();
			chest.giveItem(new FlintAndFiresteel(), 10);
			chest.giveItem(Plank.plank(StandardWood.class), 200);
			chest.giveItem(Stick.stick(StandardWood.class), 200);
			chest.giveItem(new BushKnife(), 10);
			chest.giveItem(new CombatKnife(), 10);
			chest.giveItem(new Broadsword(), 10);
			chest.giveItem(new Machette(), 10);
			chest.giveItem(new LongBow<StandardWood>(StandardWood.class), 10);
			chest.giveItem(new GlowStickArrow.GlowStickArrowItem<Steel>(Steel.class), 500);
			chest.giveItem(new FireArrow.FireArrowItem<Steel>(Steel.class), 500);
			chest.giveItem(new Carrot(), 500);
			chest.giveItem(new DeathCap(false), 500);
			chest.giveItem(new DeathCap(true), 500);
			chest.giveItem(new ChickenLeg(false), 500);
			chest.giveItem(new ChickenLeg(true), 500);
			chest.giveItem(Rock.rock(Coal.class), 500);
			chest.giveItem(Rock.rock(SandStone.class), 500);
			chest.giveItem(new Ashes(), 500);
			chest.giveItem(new Sand(), 500);
			chest.giveItem(new Dirt(), 500);
			chest.giveItem(Log.log(StandardWood.class), 500);
			chest.giveItem(Plank.plank(StandardWood.class), 500);
			chest.giveItem(new Torch(), 10);
			HashMap<Class<? extends Liquid>, Float> map = Maps.newHashMap();
			map.put(Water.class, 2f);
			HashMap<Class<? extends Liquid>, Float> map2 = Maps.newHashMap();
			map2.put(Oil.class, 2f);
			chest.giveItem(new GlassBottle(map), 5);
			chest.giveItem(new GlassBottle(map2), 5);
			chest.giveItem(Brick.brick(SandStone.class), 100);
			chest.giveItem(new Glass(), 100);
			chest.giveItem(Ingot.ingot(Iron.class), 100);
			chest.giveItem(Ingot.ingot(Steel.class), 100);
			chest.giveItem(Stick.stick(StandardWood.class), 100);
			chest.giveItem(new Currency(), 1000);
			chest.giveItem(new Lantern(100f), 10);
			chest.giveItem(new MedievalWallTorch(), 1000);
			chest.giveItem(new Carrot.CarrotSeed(), 1000);
			
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