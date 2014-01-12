import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * Main class of the BumperClass project.
 *
 * The car is supposed to move forward until it encounters an obstacle at which point it stops.
 */

public class BumperCar
{
    private static RegulatedMotor motorR = Motor.A;
    private static RegulatedMotor motorL = Motor.D;

    private static IRSensor sensor;


    public static void main(String[] args)
    {
        log("Starting Program");

        initialize();

        forward();
        Delay.msDelay(2000);
        stop();

        sensor.stop_sensor();           // Signals to the IRSensor that it should stop

        log("Program Ends");
    }


    private static void initialize()            // Initializes the functionality of the BumperCar
    {
        // Initialize motors
        motorR.setSpeed(400);
        motorL.setSpeed(400);

        motorR.setAcceleration(800);
        motorL.setAcceleration(800);

        // Initialize IR sensor
        sensor = new IRSensor();
        sensor.setDaemon(true);
        sensor.start();

        log("Initialization Complete");
    }


    private static void stop()          // Stops both motors to stop the rover
    {
        log("STOP");

        motorR.stop();
        motorL.stop();
    }


    private static void forward()
    {
        log("FORWARD");

        motorR.forward();
        motorL.forward();
    }


    private static void reverse()
    {
        log("REVERSE");

        motorR.backward();
        motorL.backward();
    }


    static class IRSensor extends Thread
    {
        private SampleProvider sampler;
        private boolean stop = false;


        IRSensor()          // Constructor initiates the IR Sensor and the sampler we will use to fetch sensor data
        {
            Port port = LocalEV3.get().getPort("S4");           // Define the port used by the Sensor
            SensorModes sensor = new EV3IRSensor(port);         // Initiate the EV3 IR Sensor

            sampler = sensor.getMode("Distance");               // Define the sampler to be the IRSensor in "Distance" measuring mode
        }


        public void stop_sensor()          // Called to make the IRSensor Thread stop by making it exit the run() method
        {
            stop = true;
        }


        public void run()
        {
            float[] sample = new float[sampler.sampleSize()];
            int distance;

            while (! stop)          // This loops infinitely until the stop_sensor method is called which changes this boolean value
            {
                sampler.fetchSample(sample, 0);         // Get sample from sensor

                distance = (int) sample[0];

                log("Distance: " + distance);

                Delay.msDelay(100);             // We delay for 100ms before getting data from the IR sensor.
            }
        }
    }


    private static void log(String message)
    {
        System.out.println("log>\t" + message);
    }
}
