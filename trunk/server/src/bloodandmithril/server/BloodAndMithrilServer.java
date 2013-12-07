package bloodandmithril.server;

import java.io.IOException;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

/**
 * Entry point class for the remote game server
 *
 * @author Matt
 */
public class BloodAndMithrilServer {

	public static void main(String[] args) {
		Server server = new Server();
		server.start();

		try {
			server.bind(42685);
		} catch (IOException e) {
			Logger.networkDebug(e.getMessage(), LogLevel.WARN);
		}

		server.getKryo().register(Color.class);
		server.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());

		server.addListener(new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				System.out.println(object.getClass().getSimpleName());
			}
		});
	}
}