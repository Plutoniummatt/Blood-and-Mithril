package bloodandmithril.character.ai.task.trade;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.trade.TradeWith.Trade;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.world.Domain;

/**
 * Executes {@link Trade}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TradeExecutor implements AITaskExecutor {
	
	@Inject UserInterface userInterface;

	@Override
	public void execute(AITask aiTask, float delta) {
		Trade task = (Trade) aiTask;
		
		if (task.proposee.isLocked()) {
			return;
		}

		if (task.proposee instanceof Individual) {
			final Individual proposeeCasted = (Individual) task.proposee;

			if (task.proposer.getDistanceFrom(proposeeCasted.getState().position) > 64f) {
				return;
			}

			if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
				ClientServerInterface.SendNotification.notifyTradeWindowOpen(task.proposer.getId().getId(), TradeEntity.INDIVIDUAL, ((Individual) task.proposee).getId().getId(), task.connectionId);
			} else if (ClientServerInterface.isClient()) {
				openTradeWindowWithIndividual(task.proposer, proposeeCasted);
			}

			task.proposer.clearCommands();
			task.proposer.getAI().setCurrentTask(new Trading(task.proposer.getId(), ((Individual) task.proposee).getId().getId(), TradeEntity.INDIVIDUAL));
			proposeeCasted.clearCommands();
			proposeeCasted.getAI().setCurrentTask(new Trading(proposeeCasted.getId(), task.proposer.getId().getId(), TradeEntity.INDIVIDUAL));
		} else if (task.proposee instanceof Prop) {

			if (task.proposer.getDistanceFrom(((Prop)task.proposee).position) > 64f) {
				return;
			}

			if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
				ClientServerInterface.SendNotification.notifyTradeWindowOpen(task.proposer.getId().getId(), TradeEntity.PROP, ((Prop) task.proposee).id, task.connectionId);
			} else if (ClientServerInterface.isClient()) {
				openTradeWindowWithProp(task.proposer, task.proposee);
			}

			task.proposer.clearCommands();
			task.proposer.getAI().setCurrentTask(new Trading(task.proposer.getId(), ((Prop) task.proposee).id, TradeEntity.PROP));
		}		
	}
	

	@Override
	public boolean isComplete(AITask aiTask) {
		Trade task = (Trade) aiTask;
		
		if (task.proposee instanceof Individual) {
			if (!((Individual) task.proposee).isAlive()) {
				return true;
			}
		}

		return task.proposer.getAI().getCurrentTask() instanceof Trading;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
	
	
	public static final Comparator<Item> sortOrder = new Comparator<Item>() {
		@Override
		public int compare(final Item o1, final Item o2) {
			return ComparisonChain.start()
				.compare(o1.getType().getColor().toIntBits(), o2.getType().getColor().toIntBits())
				.compare(o1.getSingular(false).toUpperCase(), o2.getSingular(false).toUpperCase())
				.result();
		}
	};

	
	/**
	 * Opens a {@link TradeWindow} with a {@link Prop}  is also a {@link Container}
	 */
	public void openTradeWindowWithProp(final Individual proposer, final Container container) {
		if (container instanceof Prop) {
			final Prop prop = Domain.getWorld(proposer.getWorldId()).props().getProp(((Prop) container).id);
				userInterface.addLayeredComponentUnique(
				new TradeWindow(
					proposer.getId().getSimpleName() + " interacting with container",
					true,
					proposer,
					(Container) prop,
					sortOrder
				)
			);
		}
	}


	/**
	 * Opens a {@link TradeWindow} with another {@link Individual}
	 */
	public void openTradeWindowWithIndividual(final Individual proposer, final Individual proposeeCasted) {
		userInterface.addLayeredComponentUnique(
			new TradeWindow(
				"Trade between " + proposer.getId().getFirstName() + " and " + proposeeCasted.getId().getFirstName(),
				true,
				proposer,
				proposeeCasted,
				sortOrder
			)
		);
	}
}