package bloodandmithril.networking.requests;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import bloodandmithril.character.AddIndividualService;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

/**
 * Synchronizes an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SynchronizeIndividual implements Request {

	/** ID of the individual to sync */
	private final int id;

	/**
	 * Constructor
	 */
	public SynchronizeIndividual(final int id) {
		this.id = id;
	}

	/**
	 * Synchronize all individuals
	 */
	public SynchronizeIndividual() {
		this.id = -1;
	}


	@Override
	public Responses respond() {
		final Responses responses = new Response.Responses(false);

		Response response;
		if (id == -1) {
			response = new SynchronizeIndividualResponse(Sets.newHashSet(Domain.getIndividualIds()));
			responses.add(response);
			return responses;
		}
		response = new SynchronizeIndividualResponse(id, System.currentTimeMillis());
		responses.add(response);
		return responses;
	}


	/**
	 * Response class of {@link SynchronizeIndividual}
	 *
	 * @author Matt
	 */
	public static class SynchronizeIndividualResponse implements Response {

		private Individual individual;

		private final Set<Integer> individuals;

		private final long timeStamp;

		private Integer individualId;

		@Inject
		private transient GameClientStateTracker gameClientStateTracker;
		@Inject
		private transient AddIndividualService addIndividualService;

		/**
		 * Synchronize single individual
		 */
		public SynchronizeIndividualResponse(final int individualId, final long timeStamp) {
			this.individualId = individualId;
			this.timeStamp = timeStamp;
			this.individuals = null;
		}

		/**
		 * Synchronize all individuals
		 */
		public SynchronizeIndividualResponse(final Set<Integer> individuals) {
			this.individuals = individuals;
			this.individualId = null;
			this.timeStamp = -1;
		}

		@Override
		public void acknowledge() {
			if (this.individualId != null) {
				syncSingleIndividual();
			}

			if (this.individuals != null) {
				for (final Integer id : individuals) {
					ClientServerInterface.SendRequest.sendSynchronizeIndividualRequest(id);
				}
			}
		}

		@Override
		public int forClient() {
			return -1;
		}

		private void syncSingleIndividual() {
			final Individual got = Domain.getIndividual(individual.getId().getId());
			if (got == null) {
				addIndividualService.addIndividual(individual, individual.getWorldId());
			} else {
				if (timeStamp < got.getTimeStamp()) {
					// Received snapshot is older than the most recently updated snapshot
					return;
				}
				got.copyFrom(individual);

				if (!got.getSelectedByClient().contains(ClientServerInterface.client.getID())) {
					gameClientStateTracker.removeSelectedIndividualIf(id -> {
						return id == got.getId().getId();
					});
				}
			}
			Logger.networkDebug("Received data for individual: " + individual.getId().getSimpleName(), LogLevel.TRACE);
		}

		@Override
		public void prepare() {
			if (this.individualId != null) {
				this.individual = Domain.getIndividual(individualId).copy();

				// Do not synch stimuli
				this.individual.getAI().clearStimuli();

				// Handle AITasks with Paths explicitly, these guys cause ConcurrentModificationExceptions and nasty NPE's even with
				// ConcurrentLinkedDeque's
				final AITask current = this.individual.getAI().getCurrentTask();

				synchronized (current) {
					if (current instanceof GoToLocation) {
						((GoToLocation) current).setPath(((GoToLocation) current).getPath().copy());
					} else if (current instanceof CompositeAITask) {
						final AITask currentTask = ((CompositeAITask) current).getCurrentTask();
						if (currentTask instanceof GoToLocation) {
							((GoToLocation) currentTask).setPath(((GoToLocation) currentTask).getPath().copy());
						}
					}
				}
			}
		}
	}


	@Override
	public boolean tcp() {
		return false;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}