package bloodandmithril.util;

import bloodandmithril.core.Copyright;

/**
 * A task that can be called with a vararg of parameters
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface JITTask {

  /**
   * Execute this JIT Task
   */
  public void execute(Object... args);
}