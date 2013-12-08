package bloodandmithril.server;

import java.io.IOException;

import bloodandmithril.csi.request.Ping;
import bloodandmithril.csi.request.Response;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Test {

	public static void main(String[] args) {
		Client client = new Client();
		client.start();

		try {
			client.connect(5000, "192.168.2.4", 42685);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BloodAndMithrilServer.registerClasses(client.getKryo());
		
		client.addListener(new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof Response) {
					((Response) object).Acknowledge();
				}
			}
		});
		
		while(true) {
			try {
				Thread.sleep(1000);
				client.sendTCP(new Ping());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}