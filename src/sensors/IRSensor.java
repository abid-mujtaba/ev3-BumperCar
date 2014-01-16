package sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

import lock.Lock;

/**
 * Implements a model used to access the IR (Infra-Red Sensor) on the EV3
 */

public class IRSensor extends Thread
{
    private SampleProvider sampler;
    private boolean stop = false;

    private int distance = 255;         // Initiated to infinity (of sorts)

    private final Lock mLock = new Lock();          // Lock used to hold and resume execution.


    public IRSensor()          // Constructor initiates the IR Sensor and the sampler we will use to fetch sensor data
    {
        Port port = LocalEV3.get().getPort("S4");           // Define the port used by the Sensor
        SensorModes sensor = new EV3IRSensor(port);         // Initiate the EV3 IR Sensor

        sampler = sensor.getMode("Distance");               // Define the sampler to be the IRSensor in "Distance" measuring mode

        this.setDaemon(true);               // Daemonize the sensor thread.
    }


    public synchronized int distance() { return distance; }          // Method for accessing the value of the last distance measured by the sensor


    public void stop_sensor()          // Called to make the IRSensor Thread stop by making it exit the run() method
    {
        stop = true;

        mLock.resume();     // Force interruption of mLock.hold() calls
    }


    public void run()
    {
        float[] sample = new float[sampler.sampleSize()];

        while (! stop)          // This loops infinitely until the stop_sensor method is called which changes this boolean value
        {
            sampler.fetchSample(sample, 0);         // Get sample from sensor

            distance = (int) sample[0];

            mLock.hold(100);            // Use mLock to hold for 100ms yielding the Thread in the process. Allows more efficient use of resources.
        }
    }
}
