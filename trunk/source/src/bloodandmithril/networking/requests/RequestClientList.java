package bloodandmithril.networking.requests;

import java.util.HashMap;
import java.util.List;

import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.ui.components.window.ChatWindow;
import bloodandmithril.util.Fonts;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryonet.Connection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * {@link Request} for a list of connected clients
 *
 * @author Matt
 */
public class RequestClientList implements Request {


	@Override
	public Responses respond() {
		Responses responses = new Responses(false);

		List<String> names = Lists.newArrayList();
		for (Connection connection : ClientServerInterface.server.getConnections()) {
			names.add(ClientServerInterface.connectedPlayers.get(connection.getID()));
		}

		responses.add(new RequestClientListResponse(names));
		return responses;
	}


	@Override
	public boolean tcp() {
		return false;
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}


	public static class RequestClientListResponse implements Response {

		private final List<String> names;

		/**
		 * Constructor
		 */
		public RequestClientListResponse(List<String> names) {
			this.names = names;
		}

		@Override
		public void acknowledge() {
			for (Component component : UserInterface.layeredComponents) {
				if (component instanceof ChatWindow) {

					List<HashMap<ListingMenuItem<String>, Object>> listings;
					listings = Lists.newArrayList();

					for (String name : names) {
						ListingMenuItem<String> item = new ListingMenuItem<String>(
							name,
							new Button(
								name,
								Fonts.defaultFont,
								0,
								0,
								name.length() * 10,
								16,
								() -> {},
								name.equals(ClientServerInterface.clientName) ? Color.GREEN : Color.ORANGE,
								name.equals(ClientServerInterface.clientName) ? Color.GREEN : Color.ORANGE,
								name.equals(ClientServerInterface.clientName) ? Color.GREEN : Color.ORANGE,
								UIRef.BL
							),
							null
						);

						HashMap<ListingMenuItem<String>, Object> map = Maps.newHashMap();
						map.put(item, 0);
						listings.add(map);
					}
					((ChatWindow) component).participants.refresh(listings);
				}
			}
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}
}