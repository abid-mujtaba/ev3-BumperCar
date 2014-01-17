import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

import sensors.IRSensor;
import subsumption.Arbitrator;
import subsumption.Behavior;

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
        sensor.start();

        Behavior driver = new DriveForward();
        Behavior obstacle = new DetectObstacle();

        Behavior[] behaviors = {driver, obstacle};

        Arbitrator arbitrator = new Arbitrator(behaviors);

        arbitrator.start();

        try { arbitrator.join(); } catch (InterruptedException e) {}

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


    static class DetectObstacle extends Behavior
    {
        @Override
        public void suppress() {}       // This is the highest priority behavior so it is never suppressed so we leave this method empty

        @Override
        public boolean takeControl() { return sensor.distance() < 30; }     // This behavior takes control when the robot is very near an obstacle as detected by the sensor

        @Override
        public void run()
        {
            sensor.stop_sensor();

            hold(200);              // Wait 200 ms and then stop the program
            System.exit(0);         // Stop the program entirely
        }
    }


    static class DriveForward extends Behavior
    {
        private boolean _suppressed = false;

        public void suppress()
        {
            _suppressed = true;

            resume();       // We break the hold() to stop the action.
        }

        public boolean takeControl() { return true; }       // Returning true here means this Behavior ALWAYS wants control that is it is the default behavior

        @Override
        public void run()
        {
            forward();

            while (! _suppressed)
            {
                hold();
            }

            stop();
        }
    }


    private static void log(String message)
    {
        System.out.println("log>\t" + message);
    }
}
