package bloodandmithril.character.ai.routine.entityvisible;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;

import bloodandmithril.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.Routine.RoutinePanel;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.IndividualEntityVisible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Behaviour;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.item.items.Item.VisibleItem;
import bloodandmithril.prop.Harvestable.VisibleHarvestable;
import bloodandmithril.prop.Lightable.LightableUnlit;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Function;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Wrapper;

/**
 * Used to set the time the {@link EntityVisibleRoutine} takes place
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class EntityVisibleInfoPanel extends RoutinePanel {

	private Button changeVisibleEntityButton;

	EntityVisibleInfoPanel(final Component parent, final EntityVisibleRoutine routine) {
		super(parent, routine);
		this.changeVisibleEntityButton = new Button(
			"Change visible entity",
			Fonts.defaultFont,
			0,
			0,
			210,
			16,
			() -> {
			},
			Color.GREEN,
			Color.WHITE,
			Color.GRAY,
			UIRef.M
		);
	}

	@Override
	public final boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
		final EntityVisibleRoutine entityVisibleRoutine = (EntityVisibleRoutine) routine;

		if (changeTaskButton.click()) {
			final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

			for (final Class<? extends RoutineTask> routineClass : RoutineTasks.getTaskClasses()) {
				menu.addMenuItem(
					new MenuItem(
						routineClass.getAnnotation(Name.class).name(),
						() -> {},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						() -> {
							final Class<? extends RoutineTaskContextMenuProvider> providerClass = routineClass.getAnnotation(RoutineContextMenusProvidedBy.class).value();
							return Wiring.injector().getInstance(providerClass).getEntityVisibleRoutineContextMenu(routine.getHost(), (EntityVisibleRoutine) routine);
						}
					)
				);
			}

			parent.setActive(false);
			copy.add(menu);
		}

		if (changeVisibleEntityButton.click()) {
			parent.setActive(false);
			final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

			final Wrapper<Behaviour> args = new Wrapper<>(null);
			final Function<ContextMenu> deriveIndividualTypeContextMenu = () -> { return deriveIndividualTypeContextMenu(args);};

			menu.addMenuItem(
				new MenuItem(
					"Choose entity type",
					() -> {},
					Color.ORANGE,
					Color.GREEN,
					Color.GRAY,
					() -> { return new ContextMenu(getMouseScreenX(), getMouseScreenY(), false,
						new MenuItem("Individual", () -> {}, Color.ORANGE, Color.GREEN, Color.GRAY,
							() -> { return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
								new MenuItem("Any", () -> {}, Color.MAGENTA, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu),
								new MenuItem("Friendly", () -> {
									args.t = Behaviour.FRIENDLY;
								}, Color.ORANGE, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu),
								new MenuItem("Hostile", () -> {
									args.t = Behaviour.HOSTILE;
								}, Color.ORANGE, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu),
								new MenuItem("Neutral", () -> {
									args.t = Behaviour.NEUTRAL;
								}, Color.ORANGE, Color.GREEN, Color.GRAY, deriveIndividualTypeContextMenu)
							);}
						),
						new MenuItem("Prop", () -> {}, Color.ORANGE, Color.GREEN, Color.GRAY,
							() -> { return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
								derivePropTypeContextMenu()
							);}
						),
						new MenuItem("Item", () -> {}, Color.ORANGE, Color.GREEN, Color.GRAY,
							() -> { return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
								new MenuItem(
									"Any Item",
									() -> {
										entityVisibleRoutine.identificationFunction = new VisibleItem();
										entityVisibleRoutine.setAiTaskGenerator(null); // TODO?
									},
									Color.MAGENTA,
									Color.GREEN,
									Color.GRAY,
									null
								)
							);}
						)
					);}
				)
			);
			copy.add(menu);
		}

		return super.leftClick(copy, windowsCopy) || changeVisibleEntityButton.click();
	}

	private MenuItem[] derivePropTypeContextMenu() {
		final EntityVisibleRoutine entityVisibleRoutine = (EntityVisibleRoutine) routine;
		final List<MenuItem> items = Lists.newArrayList();

		items.add(
			new MenuItem(
				"Lightable prop",
				() -> {
					entityVisibleRoutine.identificationFunction = new LightableUnlit();
					entityVisibleRoutine.setAiTaskGenerator(null);
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		items.add(
			new MenuItem(
				"Harvestable prop",
				() -> {
					entityVisibleRoutine.identificationFunction = new VisibleHarvestable();
					entityVisibleRoutine.setAiTaskGenerator(null);
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		final MenuItem[] array = new MenuItem[items.size()];
		for (int i = 0; i < items.size(); i++) {
			array[i] = items.get(i);
		}
		return array;
	}

	private final ContextMenu deriveIndividualTypeContextMenu(final Wrapper<Behaviour> args) {
		final EntityVisibleRoutine entityVisibleRoutine = (EntityVisibleRoutine) routine;

		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		for (final Entry<Class<? extends Visible>, List<Class<? extends Individual>>> category : Individual.getAllIndividualClasses().entrySet()) {
			final ContextMenu secondaryMenu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

			for (final Class<? extends Individual> clazz : category.getValue()) {
				secondaryMenu.addMenuItem(
					new MenuItem(
						clazz.getAnnotation(Name.class).name(),
						() -> {
							entityVisibleRoutine.identificationFunction = new IndividualEntityVisible(
									entityVisibleRoutine.getHost().getId().getId(),
								clazz,
								args.t,
								true
							);

							entityVisibleRoutine.setAiTaskGenerator(null);
						},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						null
					)
				);
			}

			secondaryMenu.addFirst(
				new MenuItem(
					"Any " + category.getKey().getAnnotation(Name.class).name(),
					() -> {
						entityVisibleRoutine.identificationFunction = new IndividualEntityVisible(
								entityVisibleRoutine.getHost().getId().getId(),
							category.getKey(),
							args.t,
							true
						);

						entityVisibleRoutine.setAiTaskGenerator(null);
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);

			menu.addMenuItem(
				new MenuItem(
					category.getKey().getAnnotation(Name.class).name(),
					() -> {},
					Color.ORANGE,
					Color.GREEN,
					Color.GRAY,
					() -> { return secondaryMenu;}
				)
			);
		}

		menu.addFirst(
			new MenuItem(
				"Any Individual",
				() -> {
					entityVisibleRoutine.identificationFunction = new IndividualEntityVisible(
						entityVisibleRoutine.getHost().getId().getId(),
						Individual.class,
						args.t,
						true
					);

					entityVisibleRoutine.setAiTaskGenerator(null);
				},
				Color.MAGENTA,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		return menu;
	}

	@Override
	public final void render(final Graphics graphics) {
		super.render(graphics);

		final EntityVisibleRoutine entityVisibleRoutine = (EntityVisibleRoutine) routine;
		defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			entityVisibleRoutine.identificationFunction == null ? "Not configured" : entityVisibleRoutine.identificationFunction.getDetailedDescription(entityVisibleRoutine.getHost()),
			x + 10,
			y - 27,
			width - 5
		);

		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			"Task:",
			x + 10,
			y - 97,
			width - 5
		);

		defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.WHITE, parent.getAlpha()) : Colors.modulateAlpha(Color.WHITE, 0.6f * parent.getAlpha()));

		if (entityVisibleRoutine.getTaskGenerator() != null) {
			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				entityVisibleRoutine.getTaskGenerator().getEntityVisibleRoutineDetailedDescription(),
				x + 10,
				y - 117,
				width - 5
			);
		}

		changeVisibleEntityButton.render(x + 114, y - height + 70, parent.isActive(), parent.getAlpha(), graphics);
	}
}
