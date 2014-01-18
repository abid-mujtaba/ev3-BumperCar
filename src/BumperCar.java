import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

import sensors.IRSensor;

import subsumption.Module;
import subsumption.Supervisor;
import subsumption.io.Output;

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

        // Initialize the Modules and the Supervisor

        log("Initializing Modules and Supervisor");

        DriveForward driver = new DriveForward();
        DetectObstacle detector = new DetectObstacle(driver.output);

        Module[] modules = new Module[] {driver, detector};

        Supervisor supervisor = new Supervisor(modules);

        supervisor.start();

        try { supervisor.join(); } catch (InterruptedException e) {}

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


    /*
     * The DriveForward module is responsible for moving the robot forward in a straight line. It has no sensor inputs. As long as it is uninhibited it keeps
     * the robot moving.
     */

    static class DriveForward extends Module
    {
        @Override
        public void run()
        {
            while (true)
            {
                output.act();

                hold(100);          // Every 100 ms the DriveForward module commands the output to act (which it does unless it is inhibited)
            }
        }


        public Output output = new Output()
        {
            public void action()        // When the output is asked to act it simply commands the robot to move forward
            {
                forward();
            }


            @Override
            public synchronized void inhibit()
            {
                super.inhibit();        // It is recommended that super.inhibit() be call so that the default action is performed

                stop();     // We want the forward motion to stop when the output is inhibited. This is our personal preference.
            }
        };
    }

    /*
     * The DetectObstacle module is responsible for detecting an obstacle in this case stopping the robot dead in its tracks
     */

    static class DetectObstacle extends Module
    {
        private Output mOutput;          // This is the output that will be inhibited by this module.


        public DetectObstacle(Output output)         // We store a reference to the Output object we will want to inhibit
        {
            mOutput = output;
        }


        @Override
        public void run()
        {
            while (true)
            {
                detect_obstacle();          // The module runs an obstacle detection routine every 100 ms

                hold(100);
            }
        }

        private void detect_obstacle()
        {
            if (sensor.distance() < 30)         // If we are close to an obstacle inhibit DriveForward.output thereby stopping the robot
            {
                mOutput.inhibit();

                hold(200);      // Wait for 200 ms and then shutdown the program
                System.exit(0);
            }
        }
    }


    private static void log(String message)
    {
        System.out.println("log>\t" + message);
    }
}
