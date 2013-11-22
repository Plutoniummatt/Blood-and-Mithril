package bloodandmithril.util;

/**
 * A task that can be called with a vararg of parameters
 *
 * @author Matt
 */
public interface JITTask {

  /**
   * Execute this JIT Task
   */
  public void execute(Object... args);
}