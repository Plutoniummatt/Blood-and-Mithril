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
import bloodandmithril.item.items.equipment.weapon.dagger.BushKnife;
import bloodandmithril.item.items.food.animal.ChickenLeg;
import bloodandmithril.item.items.food.plant.Carrot;
import bloodandmithril.item.items.furniture.WoodenCrate;
import bloodandmithril.item.items.material.Plank;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.item.liquid.Water;
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
		Container prop = (Container) new WoodenCrate(StandardWood.class).getProp();
		prop.giveItem(new FlintAndFiresteel(), 1);
		prop.giveItem(Plank.plank(StandardWood.class), 20);
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