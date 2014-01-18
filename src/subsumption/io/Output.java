package subsumption.io;

import lock.Lock;
import subsumption.Module;

/**
 * This models the output portion of a module, in particular the ability to be inhibited by higher layers by sending an inhibition signal.
 */

public abstract class Output extends Lock
{
    private boolean _inhibited = false;         // Flag for determining whether the output is inhibited.
    private boolean _active = false;

//    private Module mModule;

//    public Output(Module module)
//    {
//        mModule = module;       // We store the parent module for later use
//    }


    synchronized public void inhibit()
    {
        _inhibited = true;
        _active = false;
    }

    synchronized public void allow()        // Un-inhibits the output but does not run it (make it active)
    {
        _inhibited = false;
    }

    public final void act()
    {
        if (! (_inhibited || _active) )
        {
            _active = true;         // Indicates that the output is now active (it is performing the designated action)

            action();
        }
    }

    public abstract void action();
}
