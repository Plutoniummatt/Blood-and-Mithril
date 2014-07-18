package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * A task that can be executed
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Task {

  /**
   * Execute the implementation of this task.
   */
  public void execute();
}
