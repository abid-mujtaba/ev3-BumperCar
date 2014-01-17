package subsumption;

import lock.Lock;

/**
 * Implements the Arbitrator that is responsible for switching Behaviors in the subsumption mechanism.
 *
 * The Arbitrator takes a list of behaviors. The higher the index of the behavior in the list the greater its priority that is it can suppress all behaviors below it.
 */

public class Arbitrator extends Thread
{
    private final Lock mLock = new Lock();

    private final static long DEFAULT_INTERVAL = 100;        // in milliseconds
    private long mInterval;

    private Behavior[] mBehaviors;

    private final static int NONE = -1;
    private int mCurrentActionIndex = NONE;
    private int maxPriority;


    public Arbitrator(Behavior[] behaviors, long interval)
    {
        mInterval = interval;

        mBehaviors = behaviors;
        maxPriority = behaviors.length - 1;
    }


    public Arbitrator(Behavior[] behaviors)
    {
        this(behaviors, DEFAULT_INTERVAL);
    }


    @Override
    public void run()       // Overrides Thread.run(). This is the method that runs when one calls .start()
    {
        // Note: If there are no behaviors (= 0) we simply exit the run() method. There is nothing to arbitrate

        if (mBehaviors.length == 1)     // If there is only one behavior, we just run it and there is no need to poll it
        {
            new Thread(mBehaviors[0]).start();          // Start the single behavior
        }
        if (mBehaviors.length > 1)      // The usual behavior
        {
            action();           // We start the action. It repeats itself recursively.
        }
    }


    private void action()       // The action to be performed by the arbitrator on each tick
    {
        // We start with the highest priority behavior and work our way down

        for (int ii = maxPriority; ii > NONE; ii--)
        {
            if (mBehaviors[ii].takeControl())
            {
                if (mCurrentActionIndex < ii)       // The new behavior that wants control has higher priority than the current one so we suppress the latter
                {
                    if (mCurrentActionIndex != NONE)
                    {
                        mBehaviors[ mCurrentActionIndex ].suppress();
                    }

                    mCurrentActionIndex = ii;

                    new Thread( mBehaviors[ii] ).start();       // We start the new behavior
                }

                break;
            }
        }

        mLock.hold(mInterval);

        action();       // Recursive call
    }
}
