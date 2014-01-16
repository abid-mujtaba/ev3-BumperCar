package subsumption;

import lock.BaseLock;

/**
 * Implements the abstract base class of the Behavior model used in subsumption.
 *
 * This class extends BaseLock and thereby inherits thread holding mechanisms such as wait, notify and best of all hold.
 *
 * Actual behaviors should extend this class and implement the abstract methods.
 */

public abstract class Behavior extends BaseLock implements Runnable
{
    public abstract void run();         // Required to implement Runnable

    public abstract boolean takeControl();      // Polled continuously to determine whether this Behavior needs to take Control and be ran.

    public abstract void suppress();            // Method called to suppress the behavior (when a higher priority behavior is run)
}