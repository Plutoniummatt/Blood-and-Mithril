package bloodandmithril.character.ai;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;

@Singleton
@Copyright("Matthew Peck 2016")
public class DummyAITaskExecutor implements AITaskExecutor {


	@Override
	public void execute(final AITask aiTask, final float delta) {
		throw new RuntimeException();
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		throw new RuntimeException();
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		throw new RuntimeException();
	}
}