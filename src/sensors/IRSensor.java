package sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * Implements a model used to access the IR (Infra-Red Sensor) on the EV3
 */

public class IRSensor extends Thread
{
    private SampleProvider sampler;
    private boolean stop = false;

    private int distance = 255;         // Initiated to infinity (of sorts)

    private static class Lock {}
    private final Lock _lock = new Lock();


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
//        interrupt();                    // This call ensures that the thread exits any sleep() and wait() methods it might be stuck in
        _lock.notify();

    }


    public void run()
    {
        float[] sample = new float[sampler.sampleSize()];

        while (! stop)          // This loops infinitely until the stop_sensor method is called which changes this boolean value
        {
            sampler.fetchSample(sample, 0);         // Get sample from sensor

            distance = (int) sample[0];

            synchronized (_lock)
            {
                try { _lock.wait(100); } catch (InterruptedException e) {}
            }

//                log("Distance: " + distance);

//                Delay.msDelay(100);             // We delay for 100ms before getting data from the IR sensor.
        }
    }
}
