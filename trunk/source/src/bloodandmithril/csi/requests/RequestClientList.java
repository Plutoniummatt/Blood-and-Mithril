package bloodandmithril.csi.requests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.UIRef;
import bloodandmithril.ui.components.Button;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;
import bloodandmithril.ui.components.window.ChatWindow;
import bloodandmithril.ui.components.window.TextInputWindow;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;

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
		Responses responses = new Responses(false, new LinkedList<Response>());

		List<String> names = Lists.newArrayList();
		for (Connection connection : ClientServerInterface.server.getConnections()) {
			names.add(ClientServerInterface.connectedPlayers.get(connection.getID()));
		}

		responses.responses.add(new RequestClientListResponse(names));
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

					List<HashMap<ListingMenuItem<String>, Integer>> listings;
					listings = Lists.newArrayList();

					for (String name : names) {

						final ContextMenu menu = new ContextMenu(
							BloodAndMithrilClient.getMouseScreenX(),
							BloodAndMithrilClient.getMouseScreenY(),
							new ContextMenu.ContextMenuItem(
								"Change name",
								new Task() {
									@Override
									public void execute() {
										UserInterface.addLayeredComponent(
											new TextInputWindow(
												BloodAndMithrilClient.WIDTH / 2 - 125,
												BloodAndMithrilClient.HEIGHT/2 + 50,
												250,
												100,
												"Change name",
												250,
												100,
												new JITTask() {
													@Override
													public void execute(Object... args) {
														ClientServerInterface.clientName = args[0].toString();
														ClientServerInterface.SendRequest.sendRequestConnectedPlayerNamesRequest();
													}
												},
												"Change",
												true
											)
										);
									}
								},
								Color.WHITE,
								Color.GREEN,
								Color.WHITE,
								null
							)
						);

						ListingMenuItem<String> item = new ListingMenuItem<String>(
							name,
							new Button(
								name,
								Fonts.defaultFont,
								0,
								0,
								name.length() * 10,
								16,
								new Task() {
									@Override
									public void execute() {
										menu.x = BloodAndMithrilClient.getMouseScreenX();
										menu.y = BloodAndMithrilClient.getMouseScreenY();
									}
								},
								Color.YELLOW,
								Color.GREEN,
								Color.YELLOW,
								UIRef.BL
							),
							menu
						);

						HashMap<ListingMenuItem<String>, Integer> map = Maps.newHashMap();
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
	}
}