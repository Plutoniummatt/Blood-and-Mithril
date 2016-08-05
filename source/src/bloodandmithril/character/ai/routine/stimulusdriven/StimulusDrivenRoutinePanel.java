package bloodandmithril.character.ai.routine.stimulusdriven;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.util.Fonts.defaultFont;

import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.character.ai.Routine.RoutinePanel;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.RoutineTasks;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine.SuspiciousSoundAITriggerFunction;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;

/**
 * Used to set various things about {@link StimulusDrivenRoutine}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class StimulusDrivenRoutinePanel extends RoutinePanel {

	private Button changeStimulusButton;

	StimulusDrivenRoutinePanel(final Component parent, final StimulusDrivenRoutine routine) {
		super(parent, routine);
		this.changeStimulusButton = new Button(
			"Change stimulus",
			Fonts.defaultFont,
			0,
			0,
			150,
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
		if (changeTaskButton.click()) {
			final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

			for (final Class<? extends RoutineTask> routineTaskClass : RoutineTasks.getTaskClasses()) {
				menu.addMenuItem(
					new MenuItem(
						routineTaskClass.getAnnotation(Name.class).name(),
						() -> {},
						Color.ORANGE,
						Color.GREEN,
						Color.GRAY,
						() -> {
							final Class<? extends RoutineTaskContextMenuProvider> providerClass = routineTaskClass.getAnnotation(RoutineContextMenusProvidedBy.class).value();
							return Wiring.injector().getInstance(providerClass).getStimulusDrivenRoutineContextMenu(routine.getHost(), (StimulusDrivenRoutine) routine);
						}
					)
				);
			}

			parent.setActive(false);
			copy.add(menu);
		}

		if (changeStimulusButton.click()) {
			final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

			menu.addMenuItem(
				new MenuItem(
					"Sound heard",
					() -> {
						((StimulusDrivenRoutine) routine).triggerFunction = new SuspiciousSoundAITriggerFunction(SuspicionLevel.INVESTIGATE);
					},
					Color.ORANGE,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);

			parent.setActive(false);
			copy.add(menu);
		}

		return super.leftClick(copy, windowsCopy) || changeStimulusButton.click();
	}

	@Override
	public final void leftClickReleased() {
	}

	@Override
	public final void render(final Graphics graphics) {
		super.render(graphics);
		defaultFont.setColor(parent.isActive() ? Colors.modulateAlpha(Color.ORANGE, parent.getAlpha()) : Colors.modulateAlpha(Color.ORANGE, 0.6f * parent.getAlpha()));

		defaultFont.drawWrapped(
			graphics.getSpriteBatch(),
			((StimulusDrivenRoutine) routine).triggerFunction == null ? "Not configured" : ((StimulusDrivenRoutine) routine).triggerFunction.getDetailedDescription(),
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

		if (((StimulusDrivenRoutine) routine).getTaskGenerator() != null) {
			defaultFont.drawWrapped(
				graphics.getSpriteBatch(),
				((StimulusDrivenRoutine) routine).getTaskGenerator().getStimulusDrivenRoutineDetailedDescription(),
				x + 10,
				y - 117,
				width - 5
			);
		}

		changeStimulusButton.render(x + 84, y - height + 70, parent.isActive(), parent.getAlpha(), graphics);
	}
}