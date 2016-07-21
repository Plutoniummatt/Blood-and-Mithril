package bloodandmithril.networking.requests;

import bloodandmithril.networking.Response;

public class RunStaticMethodNotification implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8888652753964554643L;
	private Runnable staticMethod;
	private int client;

	/**
	 * Constructor
	 */
	public RunStaticMethodNotification(Runnable staticMethod, int client) {
		this.staticMethod = staticMethod;
		this.client = client;
	}


	@Override
	public void acknowledge() {
		staticMethod.run();
	}


	@Override
	public int forClient() {
		return client;
	}


	@Override
	public void prepare() {
	}
}