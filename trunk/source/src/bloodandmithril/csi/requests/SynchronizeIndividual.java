package bloodandmithril.csi.requests;

import java.util.LinkedList;
import java.util.Set;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.GameWorld;

import com.google.common.collect.Sets;

/**
 * Synchronizes an {@link Individual}
 *
 * @author Matt
 */
public class SynchronizeIndividual implements Request {

	/** ID of the individual to sync */
	private final int id;

	/**
	 * Constructor
	 */
	public SynchronizeIndividual(int id) {
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
		Responses responses = new Response.Responses(false, new LinkedList<Response>());

		Response response;
		if (id == -1) {
			response = new SynchronizeIndividualResponse(Sets.newHashSet(GameWorld.individuals.keySet()));
			responses.responses.add(response);
			return responses;
		}
		response = new SynchronizeIndividualResponse(GameWorld.individuals.get(id), System.currentTimeMillis());
		responses.responses.add(response);
		return responses;
	}


	/**
	 * Response class of {@link SynchronizeIndividual}
	 *
	 * @author Matt
	 */
	public static class SynchronizeIndividualResponse implements Response {

		private final Individual individual;

		private final Set<Integer> individuals;

		private final long timeStamp;

		/**
		 * Synchronize single individual
		 */
		public SynchronizeIndividualResponse(Individual individual, long timeStamp) {
			this.individual = individual.copy();
			
			// Handle AITasks with Paths explicitly, these guys cause ConcurrentModificationExceptions and nasty NPE's even with
			// ConcurrentLinkedDeque's
			AITask current = this.individual.ai.getCurrentTask();
			if (current instanceof GoToLocation) {
				((GoToLocation) current).setPath(((GoToLocation) current).getPath().copy());
			} else if (current instanceof CompositeAITask) {
				AITask currentTask = ((CompositeAITask) current).getCurrentTask();
				if (currentTask instanceof GoToLocation) {
					((GoToLocation) currentTask).setPath(((GoToLocation) currentTask).getPath().copy());
				}
			}
			
			this.timeStamp = timeStamp;

			this.individuals = null;
		}

		/**
		 * Synchronize all individuals
		 */
		public SynchronizeIndividualResponse(Set<Integer> individuals) {
			this.individuals = individuals;
			this.individual = null;
			this.timeStamp = -1;
		}

		@Override
		public void acknowledge() {
			if (this.individual != null) {
				syncSingleIndividual();
			}

			if (this.individuals != null) {
				for (Integer id : individuals) {
					ClientServerInterface.SendRequest.sendSynchronizeIndividualRequest(id);
				}
			}
		}

		@Override
		public int forClient() {
			return -1;
		}

		private void syncSingleIndividual() {
			Individual got = GameWorld.individuals.get(individual.id.id);
			if (got == null) {
				GameWorld.individuals.put(individual.id.id, individual);
			} else {
				if (timeStamp < got.getTimeStamp()) {
					// Received snapshot is older than the most recently updated snapshot
					return;
				}
				got.copyFrom(individual);
			}
			Logger.networkDebug("Received data for individual: " + individual.id.getSimpleName(), LogLevel.TRACE);
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