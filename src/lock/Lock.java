package lock;

/**
 * Implements the base class Lock which when extended imparts to its children the capacity to carry out "synchronized" operations including wait()
 * and notify(). This allows a transparent mechanism for putting threads on hold and waking them up from said holds.
 */

public class Lock
{
    public static final class _Lock {}          // Inner empty class object that is used to anchor the locking mechanism
    protected final _Lock _lock = new _Lock();
    public _Lock lock() { return _lock; }       // Accessor method for the inner _Lock object _lock.


    public void resume()         // Method used to resume execution blocked by _lock object when it is stuck in the wait() call.
    {                            // This method is the opposite of hold() and is used to snap an object out of a blocking hold() call.
        synchronized(_lock)
        {
            _lock.notify();
        }
    }


    public void hold()                  // Used to hold further execution whilst yielding the Thread so that computational resources are not held up
    {                                   // NOTE: A call to _lock.notify() (where lock() returns _lock) will break the wait.
        try
        {
            synchronized(_lock)
            {
                _lock.wait();
            }
        }
        catch (InterruptedException e) { _print("_lock.wait() interrupted in Lock.hold()"); }
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
        catch (InterruptedException e) { _print(String.format("_lock.wait(%d) interrupted in Lock.hold()", interval)); }
    }


    private void _print(String msg)
    {
        System.out.println(msg);
    }
}
