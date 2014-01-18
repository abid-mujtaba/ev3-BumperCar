import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

import sensors.IRSensor;
import subsumption.Arbitrator;
import subsumption.Behavior;
import subsumption.Module;
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

//        Behavior driver = new DriveForward();
//        Behavior obstacle = new DetectObstacle();
//
//        Behavior[] behaviors = {driver, obstacle};
//
//        Arbitrator arbitrator = new Arbitrator(behaviors);
//
//        arbitrator.start();
//
//        try { arbitrator.join(); } catch (InterruptedException e) {}

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


//    static class DetectObstacle extends Behavior
//    {
//        @Override
//        public void suppress() {}       // This is the highest priority behavior so it is never suppressed so we leave this method empty
//
//        @Override
//        public boolean takeControl() { return sensor.distance() < 30; }     // This behavior takes control when the robot is very near an obstacle as detected by the sensor
//
//        @Override
//        public void run()
//        {
//            sensor.stop_sensor();
//
//            hold(200);              // Wait 200 ms and then stop the program
//            System.exit(0);         // Stop the program entirely
//        }
//    }
//
//
//    static class DriveForward extends Behavior
//    {
//        private boolean _suppressed = false;
//
//        public void suppress()
//        {
//            _suppressed = true;
//
//            resume();       // We break the hold() to stop the action.
//        }
//
//        public boolean takeControl() { return true; }       // Returning true here means this Behavior ALWAYS wants control that is it is the default behavior
//
//        @Override
//        public void run()
//        {
//            forward();
//
//            while (! _suppressed)
//            {
//                hold();
//            }
//
//            stop();
//        }
//    }

    /*
     * The DriveForward module is responsible for moving the robot forward in a straight line. It has no sensor inputs. As long as it is uninhibited it keeps
     * the robot moving.
     */

    static class DriveForward extends Module
    {
        @Override
        public void run()       // Extremely simply method. Basically it calls forward then holds. If we resume from the hold it will loop around and call forward again
        {
            while (true)
            {
                output.act();

                hold(100);          // Every 100 ms the DriveForward module commands the output to act (which it does unless it is inhibited)
            }
        }


        public Output output = new Output()
        {
            public void action()
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


    private static void log(String message)
    {
        System.out.println("log>\t" + message);
    }
}
