import lejos.hardware.Sound;
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
    private static RegulatedMotor motorR = Motor.D;
    private static RegulatedMotor motorL = Motor.A;

    private static IRSensor sensor;
    private static Supervisor mSupervisor;

    private static int SPEED = 300;

    private static int RIGHT_ANGLE_ROTATION = 450;      // The rotation angle for an orthogonal (90 degrees) turn. This depends upon the construction of the robot.


    public static void main(String[] args)
    {
        log("Starting Program");

        initialize();

        mSupervisor.start();

        try { mSupervisor.join(); } catch (InterruptedException e) {}

        log("Program Ends");
    }


    private static void initialize()            // Initializes the functionality of the BumperCar
    {
        // Initialize motors
        log("Intializing Motors");

        motorR.setSpeed(SPEED);
        motorL.setSpeed(SPEED);

        motorR.resetTachoCount();
        motorL.resetTachoCount();

        log("motorR - TachoCount: " + motorR.getTachoCount());

        // Initialize IR sensor
        log("Initializing Sensor");

        sensor = new IRSensor();
        sensor.start();

        // Initialize the Modules and the Supervisor

        log("Initializing Modules and Supervisor");

        DriveForward driver = new DriveForward();
        DetectObstacle detector = new DetectObstacle(driver.output);

        Module[] modules = new Module[] {driver, detector};

        mSupervisor = new Supervisor(modules);

        log("Initialization Complete");
    }


    private static void exit()          // Method defined here so that the modules can access it and signal the Supervisor to exit all threads
    {
        mSupervisor.exit();
    }


    private static void stop()          // Stops both motors to stop the rover
    {
        log(String.format("STOP - %s", tachoCount()));

        motorR.stop(true);              // We pass true in so that the method returns immediately allowing the next line to execute almost simultaneously
        motorL.stop(false);
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


    private static void short_reverse()
    {
        log("SHORT REVERSE");

        motorR.rotate(-600, true);          // The boolean passed in means the method returns immediately allowing the motorL rotate to be called immediately so that the two motors are rotating backwards simultaneously
        motorL.rotate(-600, false);
    }


    private static void turn_right()
    {
        log("RIGHT TURN");

        log(String.format("Before turning - %s", tachoCount()));

        motorL.rotate(RIGHT_ANGLE_ROTATION);

        log(String.format("After turning - %s", tachoCount()));

        resetTacho();
    }


    private static void turn_left()
    {
        log("LEFT TURN");

        log(String.format("Before turning - %s", tachoCount()));

        motorR.rotate(RIGHT_ANGLE_ROTATION);

        log(String.format("After turning - %s", tachoCount()));

        resetTacho();
    }


    private static void resetTacho()
    {
        motorR.resetTachoCount();
        motorL.resetTachoCount();
    }


    private static String tachoCount()
    {
        return String.format("motorR: %d - motorL: %d", motorR.getTachoCount(), motorL.getTachoCount());
    }



    /*
     * The DriveForward module is responsible for moving the robot forward in a straight line. It has no sensor inputs. As long as it is uninhibited it keeps
     * the robot moving.
     */

    static class DriveForward extends Module
    {
        private boolean _exit = false;


        @Override
        public void run()
        {
            while (! _exit)
            {
                output.act();

                hold(100);          // Every 100 ms the DriveForward module commands the output to act (which it does unless it is inhibited)
            }

            log("Exiting DriveForward");
        }

        @Override
        public void exit() { _exit = true; }    // When called it sets the _exit flag to true which will cause the thread to eventually exit


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
        private boolean _exit = false;

        private Output mOutput;          // This is the output that will be inhibited by this module.
        private int mCount = 0;
        private final int MAXCOUNT = 3;     // Number of detected obstacles before the robot stops


        public DetectObstacle(Output output)         // We store a reference to the Output object we will want to inhibit
        {
            mOutput = output;
        }


        @Override
        public void run()
        {
            while (! _exit)
            {
                detect_obstacle();          // The module runs an obstacle detection routine every 100 ms

                hold(100);
            }

            log("Exiting DetectObstacle");
        }


        @Override
        public void exit() { _exit = true; }


        private void detect_obstacle()
        {
            if (sensor.distance() < 30)         // If we are close to an obstacle inhibit DriveForward.output thereby stopping the robot
            {
                log("Obstacle Detected");

                mOutput.inhibit();

                log("Inhibiting Output");

                if (++mCount >= MAXCOUNT)       // If the number of obstacles detected to date exceeds MAXCOUNT we stop execution
                {
                    log(String.format("Obstacle # %d. Stopping robot.", mCount));

                    Sound.twoBeeps();           // Beep twice to signal the end of the program

                    hold(200);      // Wait for 200 ms and then shutdown the program
                    BumperCar.exit();       // Initiate graceful exit strategy
                    hold(200);              // Wait for 200 ms to allow the exit to proceed
                }
                else                            // Carry out the behavior required by this module when an obstacle is detected
                {
                    hold(100);          // We stop the forward motion, reverse a bit to create space and then turn left
                    short_reverse();
                    turn_left();

                    mOutput.allow();        // Removes inhibition on the Output allowing that module to access it

                    log("Removing inhibition on Output");
                }
            }
        }
    }


    private static void log(String message)
    {
        System.out.println("log>\t" + message);
    }
}