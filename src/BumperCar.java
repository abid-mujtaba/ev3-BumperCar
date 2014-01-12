import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

/**
 * Main class of the BumperClass project.
 */

public class BumperCar
{
    private static RegulatedMotor motorR = Motor.A;
    private static RegulatedMotor motorL = Motor.D;


    public static void main(String[] args)
    {
        log("Starting Program");

        forward();

        Delay.msDelay(4000);

        stop();

        log("Program Ends");
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


    private static void log(String message)
    {
        System.out.println("log>\t" + message);
    }
}
