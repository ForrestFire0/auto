package frc.team5115.subsystems;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.SPI;
import frc.team5115.robot.Robot;

// Docs: http://docs.limelightvision.io/en/latest/networktables_api.html

public class manueverinator {

    NetworkTable limelight;
    private static NetworkTableEntry tx; // Measure of X offset angle
    private static NetworkTableEntry ty; // Measure of Y offset angle
    private NetworkTableEntry tv;
    private NetworkTableEntry camtran;
    private NetworkTableEntry LED;
    private NetworkTableEntry CAM;

    // Variables needed in calculation(s) -> All are static because thats what limelight wants ¯\_(ツ)_/¯

    private double xOffset; // The horizontal shift the robot needs to make in order to align. FROM THE CENTER OF THE ROBOT.
    private double yOffset; // The vertical shift the robot needs to make in order to align. FROM THE CENTER OF THE ROBOT
    public double hypotenuse; // Pythagorean of x-off and y-off
    private AHRS navx; //turn baby.
    private float getYaw;

    private double[] emptyDoubleArray = new double[6];

    private int ticksSinceTargetSeen;
    /* *
    *   update camera height
    *   update target height
    *   update camera angle
     */

    private final double Kp = 0.2; //a modifier to the aim function.
    private final double deadZoneDegrees = 5;
    private final double followingTrackSpeed = 0.3;

    // Load in the network tables
    public manueverinator(){
        limelight = NetworkTableInstance.getDefault().getTable("limelight");
        tx = limelight.getEntry("tx"); //Angle in x of degrees
        ty = limelight.getEntry("ty"); //Angle in y of degrees
        tv = limelight.getEntry("tv"); //have target?
        camtran = limelight.getEntry("camtran"); //Raw 3d positioning
        LED = limelight.getEntry("ledMode");
        CAM = limelight.getEntry("camMode");

        navx = new AHRS(SPI.Port.kMXP);
        navx.reset(); //reset to the start orientation
    }

    public void navxAngleReset() {
        navx.reset(); //reset to the field orientation
        System.out.println("Angle has been reset.");
        System.out.println(navx.getYaw() + " = 0");
    }

    /**
     * DEAR FUTURE FORREST: There are two possible applications for this program.
     *
     * If you want to keep on working on tracking the target WHEN THE TARGET MOVES:
     * When the limelight looses the target, it should keep the rate it had previously. This way, if the target moves away to fast, the robot will continue to move.
     *
     * If you want to work on finding a stable target:
     * Update the program to search for a target.
     *      The question now becomes which way to start rotating to find the target.
     *      USE THE PREVIOUS CONTROL FROM THE JOYSTICK. This way the robot can keep going the way it was steered, and look that way first.
     *      You can tell the drivers to look a little to the left of the target and then go right, therefore ensuring that the robot is at the correct angle of rotation.
     *
     */
    public void aim() {

        if(tv.getDouble(0) == 1) {
            double heading_error = -tx.getDouble(0); //how far off is it from the center of the screen
            double steering_adjust = 0; //a var that is the processes value that is added and taken from each wheel.
            if (Math.abs(heading_error) > deadZoneDegrees) //if it is an error greater than one (significant)
            {
                steering_adjust = Kp * heading_error;
            } else {
                steering_adjust = 0;
            }

            //Help discovering errors:
            System.out.println("Heading Error: " + heading_error);
            System.out.println("Steering Adjustment: " + steering_adjust);

            Robot.dt.drive(0,-steering_adjust,0.30); //drive baby
        }
        else {
            System.out.println("No target found. Stopping.");
            //In the furure, maybe we should add a scanning function.
            Robot.dt.drive(0,0,0);
        }
    }

    public void debug() {
        System.out.println("tx: " + tx.getDouble(0));
        System.out.println("ty: " + ty.getDouble(0));
        System.out.println("tv: " + tv.getDouble(0));
        //System.out.println(camtran);
    }

    private void update3dPoints() {
        double[] _3dStuff = camtran.getDoubleArray(emptyDoubleArray);

        if (_3dStuff[2] > 1) { //values dont make sense. The y offset should be negative but its not, which means that it is doo doo info.
            //calculate values from navx and other things.
            double yaw = getYaw + tx.getDouble(0); //angle from the wall. Remember: negative is pointing to left, positive is to the right.
            xOffset = -sin(yaw);
            yOffset = cos(yaw);
        } else {
            //get values from the 3d pnp.
            xOffset = _3dStuff[0];
            yOffset = _3dStuff[2];
        }
        //The following turns adjusts the x and y values from the limelight to get the

        final double relativeLLx = 0; //Positive value means the limelight is to the right of the center, while negative is to the left.
        final double relativeLLy = 13; //negative 20 means that the robot location is 20 inches. behind the limelight.

        //System.out.print("X=" + xOffset + " - " +
         //       relativeLLx*cos(getYaw) + " - " +
         //       relativeLLy*sin(getYaw));

        xOffset = xOffset - (relativeLLx*cos(getYaw)) - (relativeLLy*sin(getYaw));
        //System.out.println(" = " + xOffset);


        //System.out.print("Y=" + yOffset + " - " +
        //        relativeLLy*cos(getYaw) + " - " +
         //       relativeLLx*sin(getYaw));

        yOffset = yOffset - (relativeLLy*cos(getYaw)) - (relativeLLx*sin(getYaw));
        //System.out.println(" = " + yOffset);

    }

    private double sin(double n) {
        return Math.sin(Math.toRadians(n));
    }

    private double cos(double n) {
        return Math.cos(Math.toRadians(n));
    }

    /**
     * angle the angle the robot needs to hold.
     *
     */


    private double findAngle() {
        if (yOffset < -40) { //We are close to the wall, so no matter making it anything but the goal point.
            return 0;
        }
        //else
        double targetY = locateTargetPoint();
        double angle = getAngleFromTargetPoint(targetY);
        return safeAngle(angle);
    }

    private double safeAngle(double angleRequested) { //given the angle requested, the change we need to make, and how much more we can turn without it going off the screen, return the maximun angle we can turn, given that it is NOT safe.
        //THIS IS LIKE 99% working. I tested a crap ton on codeHS.
        //angleRequested: The new angle we want to hold, RTF (relative to field).
        //getYaw: The current angle held RTF.
        //currentOffset: where the target is in the cameras vision. NOT RTF.
        double currentOffset = tx.getDouble(0);
        double degreesLeft = 30 - Math.abs(currentOffset);//the amount of degrees we can move before we go off the field, ABS.
        //System.out.println("There are " + degreesLeft + " degrees left before the camera looses sight.");

        if (angleRequested-getYaw > 30 + currentOffset) { //to the right
            System.out.println("Limited. Trying to move TO FAR TO THE RIGHT");
            //limit it to only come way to the right. add degrees left to the current angle.
            return getYaw + degreesLeft;
        }
        else if (angleRequested-getYaw < -30 + currentOffset) { //to the LEFT
            System.out.println("Limited. Trying to move TO FAR TO THE LEFT");
            //limit it to only come way to the left. subtract degrees left to the current angle.
            //because it should be to the left, which is negative.
            return getYaw - degreesLeft;
        }

        //everything checked out. Send value back.
        return angleRequested;
    }


    private double getAngleFromTargetPoint(double targetY) { //takes in two points, x and y, that are relative to the limelight target / wall. returns the angle that the robot needs to hold, relative to the wall.
        //an angle of 0 is strait at the target, while 90 is all the way
        double currentX = xOffset;
        double currentY = yOffset;
        double targetX = 0; //on the line out

        System.out.print("X: " + (int) targetX + " - " + (int) currentX + " = ");
        double deltaX = targetX - currentX; //get the difference in x values;
        System.out.println((int)deltaX + " = deltaX");
        System.out.print(" Y: " + (int) targetY + " - " + (int) currentY + " = ");
        double deltaY = Math.abs(targetY - currentY); //get the difference in y values;
        System.out.println((int)deltaY);

        double radians = Math.atan2(deltaX,deltaY); //uses tangent to get angle.
        return Math.toDegrees(radians); //returns angle in radians.
    }

    private double locateTargetPoint() { //this finds the y value that we need to look at.
        //return -40; //returning 2 feet out from wall to the limelight at the moment. Once we get better at following things then we can
        return -limit(yOffset/2, 30,200); //which will give us a nice curve.
        //Also note that this is the center of the robot, not the front of the robot. Add the relativeLLy to get the distance to the front of the robot.
    }


    private double limit(double num, double min, double max) {
        return Math.min(Math.max(max,num), min);
    }

    public void lineUp() {
        if(tv.getDouble(0) == 0) { //no target found.
            System.out.println("ERROR : NO TARGET FOUND");
            Robot.dt.drive(0,0,0);
            return;
        }

        getYaw = relativize(navx.getYaw());  //get the yaw from the navx. MUST BE UPDATED TO BE RELATIVE TO THIS FRAME.

        update3dPoints();//acquire new points, aka xOffset and yOffset. Adjust them to be robot center oriented.

        System.out.println("X: " + (int) xOffset + " Y: " + (int) yOffset + " Angle: " + (int) getYaw);

        double targetAngle = findAngle(); //get the angle we need. RELATIVE TO WALL
        System.out.println("Current Angle: " + getYaw + "Target Angle: " + targetAngle); //this returns the angle relative to the wall. 0 is strait at the wall, while 90 is completely right and -90 is completely left.

        Robot.dt.angleHold(getYaw,targetAngle);

         /* Note: The last phase of this 'follow curve' function is unwritten. It lines up, and rotates toward the wall, but then doesn't move forward finally. This final step
            depends on the form of the robot. What do we have to do? Move flush against the wall? Do we have a sonar to get the right distance?
          */
    }

    private float relativize(float yaw) {
        //this function needs to change the frame of the current position to the way we are looking at the wall. To start, we will line it up with the wall to get a good reference.
        // in a real game we will need it to dynamically detect where it is lining up to.
        return yaw;
    }

    float getGetYaw() {
        return navx.getYaw();
    }
}

