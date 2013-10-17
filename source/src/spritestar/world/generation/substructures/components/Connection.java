package spritestar.world.generation.substructures.components;

import java.util.List;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.Line;
import spritestar.world.generation.Component;

/**
 * A Component which connects to another Component to form a Connection
 *
 * @author Sam
 */
public abstract class Connection extends Component {
	private static final long serialVersionUID = -6439580243998076769L;

	/** The line on the surface this connection connects through. */
	public final Line connectionLine;

	/** The line of the floor this connection attaches to. */
	public final Line floorLine;

	/**
	 * @param connectionsToGenerateFrom - the List of Connections which need Generating.
	 * @param boundaries - The Boundaries of this Component.
	 * @param generateConnection - whether this connection should be generated from or not.
	 * This would be false if the connection being made is already connected to another.
	 * Ie, it's from connectionsToGenerateFrom.
	 */
	public Connection(List<Connection> connectionsToGenerateFrom, Boundaries boundaries, Line connection, Line floor, boolean generateConnection) {
		super(boundaries);
		this.connectionLine = connection;
		this.floorLine = floor;
		if (generateConnection) {
			connectionsToGenerateFrom.add(this);
		}
	}


	@Override
	protected void generateComponent(List<Connection> connectionsToGenerateFrom) {
		generateConnection();
	}


	protected abstract void generateConnection();


	@Override
  @Deprecated
	public void addConnection(Connection connection) {
		// There's usually a better way than adding a connection to a connection.
	}
}