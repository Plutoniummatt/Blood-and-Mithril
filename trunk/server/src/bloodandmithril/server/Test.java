package bloodandmithril.server;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class Test {

	public static void main(String[] args) {
		Client client = new Client();
		client.start();

		try {
			client.connect(5000, "86.9.40.20", 55558);
		} catch (IOException e) {
			e.printStackTrace();
		}

		client.sendTCP("You fucking gay!");

		client.addListener(new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				System.out.println(object);
			}
		});
	}
}