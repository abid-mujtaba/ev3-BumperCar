package subsumption;

/**
 * This is the part of the application that actually takes the various layers and modules and launches them in the correct
 * sequence
 */

public class Supervisor extends Thread
{
    private Module[] mModules;

    public Supervisor(Module[] modules)
    {
        mModules = modules;
    }

    @Override
    public void run()
    {
        super.run();

        try
        {
            Thread[] threads = new Thread[mModules.length];

            for (int ii = 0; ii < mModules.length; ii++)        // We create a thread for each module and start it
            {
                threads[ii] = new Thread(mModules[ii]);

                threads[ii].start();
            }

            for (Thread thread: threads)        // we join all threads consecutively. This block will be exited when ALL threads have finished executing
            {
                thread.join();
            }
        }
        catch (InterruptedException e) {}
    }
}
