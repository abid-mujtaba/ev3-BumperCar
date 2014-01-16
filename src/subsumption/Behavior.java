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
    public Lock lock() { return _lock; }            // Provides external access to the Lock object


    public abstract void run();         // Required to implement Runnable

    public abstract boolean takeControl();      // Polled continuously to determine whether this Behavior needs to take Control and be ran.

    public abstract void suppress();            // Method called to suppress the behavior (when a higher priority behavior is run)


    public void hold()                  // Used to hold further execution whilst yielding the Thread so that computational resources are not held up
    {                                   // NOTE: A call to _lock.notify() (where lock() returns _lock) will break the wait.
        try
        {
            synchronized(_lock)
            {
                _lock.wait();
            }
        }
        catch (InterruptedException e) { _print("_lock.wait() interrupted in Behavior.hold()"); }
    }


    public void hold(long interval)     // Used to hold further execution until the specified interval of time has passed; whilst yielding the Thread
    {                                   // NOTE: A call to _lock.notify() (where lock() returns _lock) will break the wait.
        try
        {
            synchronized(_lock)
            {
                _lock.wait(interval);
            }
        }
        catch (InterruptedException e) { _print(String.format("_lock.wait(%d) interrupted in Behavior.hold()", interval)); }
    }


    private void _print(String msg)
    {
        System.out.println(msg);
    }
}