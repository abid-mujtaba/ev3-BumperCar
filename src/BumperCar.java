import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
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

        log("Program Ends");
    }


    private static void initialize()            // Initializes the functionality of the BumperCar
    {
        // Initialize motors
        log("Intializing Motors");

        motorR.setSpeed(400);
        motorL.setSpeed(400);

        // Initialize IR sensor
        log("Initializing Sensor");

        sensor = new IRSensor();
        sensor.setDaemon(true);
        sensor.start();

        // Initialize Subsumption
        log("Initializing Subsumption");

        Behavior b1 = new DriverForward();
        Behavior b2 = new DetectObstacle();

        Behavior[] behaviors = {b1, b2};

        Arbitrator arbitrator = new Arbitrator(behaviors);          // An Arbitrator initiated using the behaviors list

        arbitrator.start();             // Begin arbitration.

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

        private int distance = 255;         // Initiated to infinity (of sorts)


        IRSensor()          // Constructor initiates the IR Sensor and the sampler we will use to fetch sensor data
        {
            Port port = LocalEV3.get().getPort("S4");           // Define the port used by the Sensor
            SensorModes sensor = new EV3IRSensor(port);         // Initiate the EV3 IR Sensor

            sampler = sensor.getMode("Distance");               // Define the sampler to be the IRSensor in "Distance" measuring mode
        }


        public synchronized int distance() { return distance; }          // Method for accessing the value of the last distance measured by the sensor


        public void stop_sensor()          // Called to make the IRSensor Thread stop by making it exit the run() method
        {
            stop = true;
            interrupt();                    // This call ensures that the thread exits any sleep() and wait() methods it might be stuck in
        }


        public void run()
        {
            float[] sample = new float[sampler.sampleSize()];

            while (! stop)          // This loops infinitely until the stop_sensor method is called which changes this boolean value
            {
                sampler.fetchSample(sample, 0);         // Get sample from sensor

                distance = (int) sample[0];

                log("Distance: " + distance);

                Delay.msDelay(100);             // We delay for 100ms before getting data from the IR sensor.
            }
        }
    }


    static class DetectObstacle implements Behavior
    {
        private boolean foundObstacle()
        {
            int dist = sensor.distance();

            return (dist < 30);          // Returns true if the sensor detects an object nearer than 30 cm
        }

        @Override
        public boolean takeControl() { return foundObstacle(); }            // If an obstacle is found this Behaviour takes control.

        @Override
        public void suppress() {}           // Since this is the highest priority behaviour suppress will never be called upon it

        @Override
        public void action()        // Upon obstacle detection we simply stop the motor
        {
            BumperCar.stop();
            BumperCar.sensor.stop_sensor();

            log("Program exited.");

            System.exit(0);                 // Exit the program.
        }
    }


    static class DriverForward implements Behavior
    {
        private boolean _suppressed = false;

        @Override
        public boolean takeControl()
        {
            return true;            // This Behavior always wants control.
        }

        @Override
        public void suppress()
        {
            _suppressed = true;          // Standard practice for suppressed method
        }


        @Override
        public void action()
        {
            _suppressed = false;            // The behavior's action was triggered so it is not suppressed any longer

            forward();                      // Make the Car move forward

            while (! _suppressed)           // Stay in the action() block until the Behavior is suppressed
            {
                Delay.msDelay(100);     // Add a delay here so that this loop doesn't go crazy.

                Thread.yield();         // Don't exit till suppressed
            }
        }
    }


    private static void log(String message)
    {
        System.out.println("log>\t" + message);
    }
}
