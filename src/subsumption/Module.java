package subsumption;

import lock.Lock;

/**
 * Models the module as defined by Brooks. The primary building block of a layer.
 */

// Tutorial on Subsumption Architecture: http://mwarnerwu.wordpress.com/research/subsumption-architec


public abstract class Module extends Lock implements Runnable
{
    public synchronized void exit() {}      // Used to signal to the module that it needs to gracefully terminate. The exact means in which this is achieved is left up to the implementation
}
