package bloodandmithril.character.ai.routine.daily;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;
import static bloodandmithril.world.Epoch.getTimeString;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;

import bloodandmithril.character.ai.Routine.RoutinePanel;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;

/**
 * Used to set the time the {@link DailyRoutine} takes place
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class DailyRoutineInfoPanel extends RoutinePanel {

	@Inject private UserInterface userInterface;

	private Button changeTimeButton, changeToleranceButton;

	DailyRoutineInfoPanel(final Component parent, final DailyRoutine routine) {
		super(parent, routine);
		this.changeTimeButton = new Button(
			"Change time",
			Fonts.defaultFont,
			0,
			0,
			110,
			16,
			() -> {
				userInterface.addLayeredComponentUnique(
					new TextInputWindow(
						"changeTime",
						250,
						100,
						"Change time",
						250,
						100,
						args -> {
							try {
								final String[] split = ((String) args[0]).split(":");
								routine.routineTime = Float.parseFloat(split[0]) + Float.parseFloat(split[1])/60f;
							} catch (final Exception e) {
								userInterface.addClientMessage("Error", "Enter time in HH:mm format");
							}
						},
						"Confirm",
						true,
						routine.routineTime == null ? "00:00" : Epoch.getTimeString(routine.routineTime)
					)
				);
			},
			Color.GREEN,
			Color.WHITE,
			Color.GRAY,
			UIRef.M
		);
		this.changeToleranceButton = new Button(
			"Change duration",
			Fonts.defaultFont,
			0,
			0,
			150,
			16,
			() -> {
				userInterface.addLayeredComponentUnique(
					new TextInputWindow(
						"changeDuration",
						250,
						100,
						"Change duration",
						250,
						100,
						args -> {
							try {
								final String[] split = ((String) args[0]).split(":");
								routine.toleranceTime = Float.parseFloat(split[0]) + Float.parseFloat(split[1])/60f;
							} catch (final Exception e) {
								userInterface.addClientMessage("Error", "Enter time in HH:mm format");
							}
						},
						"Confirm",
						true,
						Epoch.getTimeString(routine.toleranceTime)
					)
				);
			},
			Color.GREEN,
			Color.WHITE,
			Color.GRAY,
			UIRef.M
		);
	}


	@Override
	public final boolean leftClick(final List<ContextMenu> copy, final Deque<Component> windowsCopy) {
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
							return Wiring.injector().getInstance(providerClass).getDailyRoutineContextMenu(routine.getHost(), (DailyRoutine) routine);
						}
					)
				);
			}

			parent.setActive(false);
			copy.add(menu);
		}

		return super.leftClick(copy, windowsCopy) || changeTimeButton.click() || changeToleranceButton.click();
	}


	@Override
	public final void render(final Graphics graphics) {
		super.render(graphics);
		defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));
		final DailyRoutine dailyRoutine = (DailyRoutine) routine;

		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			dailyRoutine.routineTime == null ? "Not configured" : "Routine occurs daily between " + getTimeString(dailyRoutine.routineTime) + " and " + getTimeString(dailyRoutine.routineTime + dailyRoutine.toleranceTime),
			x + 10,
			y - 27,
			width - 5
		);

		if (dailyRoutine.lastExecutedEpoch == null) {
			Epoch epoch = Domain.getWorld(routine.getHost().getWorldId()).getEpoch();
			if (dailyRoutine.routineTime != null && epoch.getTime() >= dailyRoutine.routineTime) {
				epoch = epoch.copy();
				epoch.incrementDay();
			}
			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				"Next scheduled occurrence: " + (dailyRoutine.routineTime == null ? "N/A" : getTimeString(dailyRoutine.routineTime) + " on " + epoch.getDateString()),
				x + 10,
				y - 47,
				width - 5
			);
		} else {
			final Epoch copy = dailyRoutine.lastExecutedEpoch.copy();
			copy.incrementDay();
			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				"Next scheduled occurrence: " + (dailyRoutine.routineTime == null ? "N/A" : getTimeString(dailyRoutine.routineTime) + " on " + copy.getDateString()),
				x + 10,
				y - 47,
				width - 5
			);
		}

		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			"Task:",
			x + 10,
			y - 97,
			width - 5
		);

		defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.WHITE, parent.getAlpha()) : Colors.modulateAlpha(Color.WHITE, 0.6f * parent.getAlpha()));

		if (routine.getTaskGenerator() != null) {
			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				routine.getTaskGenerator().getDailyRoutineDetailedDescription(),
				x + 10,
				y - 117,
				width - 5
			);
		}

		changeToleranceButton.render(x + 84, y - height + 70, parent.isActive(), parent.getAlpha(), graphics);
		changeTimeButton.render(x + 64, y - height + 90, parent.isActive(), parent.getAlpha(), graphics);
	}
}