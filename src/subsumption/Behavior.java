package subsumption;

/**
 * Implements the base class of the Behavior model used in subsumption.
 *
 * Actual behaviors should extend this class and implement the abstract methods.
 */

public abstract class Behavior implements Runnable
{
    public static final class Lock {}

    protected final Lock _lock = new Lock();

    public Lock lock() { return _lock; }

    public abstract void run();         // Required to implement Runnable

    public abstract boolean takeControl();      // Polled continuously to determine whether this Behavior needs to take Control and be ran.

    public abstract void suppress();            // Method called to suppress the behavior (when a higher priority behavior is run)
}
