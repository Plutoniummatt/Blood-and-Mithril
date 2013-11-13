package bloodandmithril.generation.settings;

public class GlobalGenerationSettings {

	// General generation settings
	public final static int maxStructureWidth = 5; // Chunks
	public final static int defaultSurfaceHeight = 10; // Tiles
	
	// Surface settings
	public final static int maxSurfaceHeight = 20; //Chunks

	// Layers settings
	public final static int minLayerHeight = 20; // Tiles
	public final static int maxLayerHeight = 40; // Tiles
	public static final int minLayerStretch = 10; // Tiles
	public final static int maxLayerStretch = 30; // Tiles
	
	// Plains settings
	public final static int plainsMinWidth = 10; //Chunks
	public final static int plainsMaxWidth = 50; // Chunks
	public final static int plainsMinHeight = 5; // Tiles
	public final static int plainsMaxHeight = 15; // Tiles
	
	// Hills settings
	public final static int hillsMinWidth = 20; //Chunks
	public final static int hillsMaxWidth = 50; // Chunks
	public final static int hillsMinHeight = 20; // Tiles
	public final static int hillsMaxHeight = 80; // Tiles
	
	// Desert settings
	public final static int desertMinWidth = 50; //Chunks
	public final static int desertMaxWidth = 150; // Chunks
	public final static int desertMaxSurfaceHeightVariation = 30; // Tiles
	public final static int desertMaxTransitionDepth = -100; // Tiles
	public final static int desertMaxSandstoneDepth = -200; // Tiles
	public final static int desertTransitionWidth = 400; // Tiles
	
	// Underground settings
	// Caves settings
	public final static int cavesMaxWidth = 5; //Chunks, max is twice this really
	public final static int cavesMaxHeight = 5; //Chunks, max is twice this really
}
