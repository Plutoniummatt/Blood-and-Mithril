package bloodandmithril.server;

import java.io.IOException;

import org.objenesis.strategy.StdInstantiatorStrategy;

import bloodandmithril.csi.request.Ping;
import bloodandmithril.csi.request.Request;
import bloodandmithril.csi.request.Ping.Pong;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;

import com.esotericsoftware.kryo.Kryo;
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

		registerClasses(server.getKryo());
		server.getKryo().setInstantiatorStrategy(new StdInstantiatorStrategy());

		server.addListener(new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof Request) {
					try {
						Thread.sleep(152);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					connection.sendTCP(((Request) object).respond());
				}
			}
		});
	}

	
	/**
	 * Registers all request classes 
	 */
	public static void registerClasses(Kryo kryo) {
		kryo.register(Request.class);
		kryo.register(Ping.class);
		kryo.register(Pong.class);
	}
}