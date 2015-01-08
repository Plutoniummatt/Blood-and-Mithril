package bloodandmithril.character;

import java.util.List;

import bloodandmithril.util.Util;

import com.google.common.collect.Lists;

public class Speech {

	private static final List<String> randomIdleSpeech = Lists.newArrayList();
	private static final List<String> affirmativeSpeech = Lists.newArrayList();
	private static final List<String> cantDoItSpeech = Lists.newArrayList();

	static {
		randomIdleSpeech.add("Well, isn't this nice...");
		randomIdleSpeech.add("I have horse proof bacon");
		randomIdleSpeech.add("What the hell is apple juice?...");
		randomIdleSpeech.add("Run away from the Kog'Maw");
		randomIdleSpeech.add("No you");
		randomIdleSpeech.add("Sometimes I wish I was a fish");
		randomIdleSpeech.add("Metal gear is already active!?");
		randomIdleSpeech.add("Do you think love can bloom even on a battlefield?");
		randomIdleSpeech.add("I need scissors! 61!");
		randomIdleSpeech.add("Some days, you feed on a tree frog");
		randomIdleSpeech.add("Even if a pawn becomes a queen, it is still just a playing piece");
		randomIdleSpeech.add("Death is tragic... But life is miserable");
		randomIdleSpeech.add("Sometimes when I close my eyes...I can't see");
		randomIdleSpeech.add("I would say something...But I don't want to spoil the silence");
		randomIdleSpeech.add("Who's that chick with the helmet?");
		randomIdleSpeech.add("When will this game be released?");
		randomIdleSpeech.add("Maybe one day you'll change your mind, but it'll be too late");
		randomIdleSpeech.add("Don't look at me in that tone of voice");
		randomIdleSpeech.add("My friend threw a rock at the ground and missed");
		randomIdleSpeech.add("Wanna buy a waterproof towel?");
		randomIdleSpeech.add("The road to success is always under construction");
		randomIdleSpeech.add("Peace sells, but who's buyin'?");
		randomIdleSpeech.add("It's not important to win, It's important to make the other guy lose");
		randomIdleSpeech.add("The greatest pleasure in life is doing what people say you cannot do");

		affirmativeSpeech.add("OK");
		affirmativeSpeech.add("As you wish...");
		affirmativeSpeech.add("Sure");
		affirmativeSpeech.add("Your wish is my command");
		affirmativeSpeech.add("On it!");
		affirmativeSpeech.add("Right");
		affirmativeSpeech.add("Yes");
		affirmativeSpeech.add("Understood");

		cantDoItSpeech.add("No can do");
		cantDoItSpeech.add("That is not possible");
		cantDoItSpeech.add("Can't do it...");
		cantDoItSpeech.add("Sorry I can't");
		cantDoItSpeech.add("I don't think so");
		cantDoItSpeech.add("Don't be silly");
	}

	public static String getRandomIdleSpeech() {
		return randomIdleSpeech.get(Util.getRandom().nextInt(randomIdleSpeech.size()));
	}

	public static String getRandomCantDoItSpeech() {
		return cantDoItSpeech.get(Util.getRandom().nextInt(cantDoItSpeech.size()));
	}

	public static String getRandomAffirmativeSpeech() {
		return affirmativeSpeech.get(Util.getRandom().nextInt(affirmativeSpeech.size()));
	}
}