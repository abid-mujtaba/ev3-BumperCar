import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;

import sensors.IRSensor;
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
        DetectObstacle obstacle = new DetectObstacle(driver);

        Thread t_driver = new Thread(driver, "Thread - Driver");
        Thread t_obstacle = new Thread(obstacle, "Thread - Obstacle");

        t_driver.start();
        t_obstacle.start();

        try
        {
            t_driver.join();
            t_obstacle.join();
        }
        catch (InterruptedException e) {}



//        // Initialize Subsumption
//        log("Initializing Subsumption");
//
//        Behavior b1 = new DriverForward();
//        Behavior b2 = new DetectObstacle();
//
//        Behavior[] behaviors = {b1, b2};
//
//        Arbitrator arbitrator = new Arbitrator(behaviors);          // An Arbitrator initiated using the behaviors list
//
//        arbitrator.start();             // Begin arbitration.

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





//    static class DetectObstacle implements Behavior
//    {
//        private boolean foundObstacle()
//        {
//            int dist = sensor.distance();
//
//            return (dist < 30);          // Returns true if the sensor detects an object nearer than 30 cm
//        }
//
//        @Override
//        public boolean takeControl() { log("DetectObstacle.takeControl()"); return foundObstacle(); }            // If an obstacle is found this Behaviour takes control.
//
//        @Override
//        public void suppress() { log("DetectObstacle.suppress()"); }           // Since this is the highest priority behaviour suppress will never be called upon it
//
//        @Override
//        public void action()        // Upon obstacle detection we simply stop the motor
//        {
//            log("DetectObstacle.action()");
//
//            BumperCar.stop();
//            BumperCar.sensor.stop_sensor();
//
//            log("Program exited.");
//
//            System.exit(0);                 // Exit the program.
//        }
//    }


    static class DetectObstacle extends Behavior
    {
        private Behavior mDriver;

        private boolean _suppressed = false;

        public DetectObstacle(Behavior driver)
        {
            mDriver = driver;
        }

        @Override
        public void suppress() {}       // This is the highest priority behavior so it is never suppressed so we leave this method empty

        @Override
        public boolean takeControl() { return sensor.distance() < 30; }     // This behavior takes control when the robot is very near an obstacle as detected by the sensor

        @Override
        public void run()
        {
            while (! _suppressed)
            {
                hold(100);

                if (sensor.distance() < 30)
                {
                    DriveForward.Lock dLock = mDriver.lock();
                    mDriver.suppress();

                    synchronized(dLock)
                    {
                        dLock.notify();
                    }

                    _suppressed = true;

                    sensor.stop_sensor();
                }
            }
        }
    }


    static class DriveForward extends Behavior
    {
        private boolean _suppressed = false;

        public void suppress() { _suppressed = true; }

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
