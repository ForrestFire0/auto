package frc.team5115.subsystems;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import frc.team5115.autotools.DriveBase;
import frc.team5115.autotools.Instruction;
import frc.team5115.autotools.SimpleAutoSeries;
import frc.team5115.robot.Robot;

import static java.lang.Math.*;

/*The base class for all autonomous using the limelight.*/

public class Auto {
    private final NavX navX;
    private final DriveBase dt;

    private NetworkTableEntry tx; // Measure of X offset angle
    private NetworkTableEntry ty; // Measure of Y offset angle
    private NetworkTableEntry tv;
    private NetworkTableEntry pipeline;

    private double xLoc;
    private double yLoc;
    private double currentAngle;
    private Instruction currentStep;

    private int currentPipeline;

    private final double maxForwardSpeed = 0.3; //The forward speed multiplier for moving.
    private final int maxAngleError = 10; //maximum angle error.
    private boolean finished = false;


    /**
     * Creates the limelight table entries.
     */
    // Load in the network tables
    public Auto(DriveBase dt, NavX navX) {
        this.dt = dt;
        this.navX = navX;

        NetworkTable limelight = NetworkTableInstance.getDefault().getTable("limelight");
        tx = limelight.getEntry("tx"); //Angle in x of degrees
        ty = limelight.getEntry("ty"); //Angle in y of degrees
        tv = limelight.getEntry("tv"); //have target?
        pipeline = limelight.getEntry("pipeline");
        pipeline.setNumber(0);
        currentPipeline = 0;
        SimpleAutoSeries.reset();
        currentStep = SimpleAutoSeries.getCurrentStep();//get the first step to work on.

        System.out.println("Starting Autonomous. Starting Step 1: "  + currentStep);
    }

    public void runAuto() {

        if(finished) {
            dt.stop();
            System.out.println("Restart Auto Using button 9");
            return;
        }

        IMUCalc(); //Calculate the current speed and such...
        currentStep = SimpleAutoSeries.getCurrentStep(); //Get the current step that we are working on...

        if (currentStep == null) {
            System.out.println("Error: Current Step is Null");
            return;
        }

        switch (currentStep.getType()) {
            case "Location":
                switch (currentStep.getStage()) {
                    case 1:
                        if (aim(currentStep)) { //aims at the next step before preceding.
                            currentStep.nextStage();
                        }
                        break;
                    case 2:
                        if (navigate(currentStep)) { //navagate to the point
                            System.out.println("Done with the navigate section of the step...");
                            currentStep.nextStage();
                        }
                        break;
                    case 3:
                        if (aim(currentStep.getOrientation())) { //aim at an angle
                            currentStep.nextStage();
                        }
                        break;
                    default:
                        System.out.println("Error: Stage not recognized. Current Stage: " + currentStep.getStage() + " in Step " + SimpleAutoSeries.getStepNum());
                }
                break;
            case "Portal":
                switch (currentStep.getStage()) {
                    case 1:
                        if (lineUp(currentStep)) {
                            currentStep.nextStage();
                        }
                        break;
                    case 2:
                        if (deadRecon(currentStep)) {
                            currentStep.nextStage();
                            //xLoc = currentStep.getX() This line needs to set the location of the robot once the step has completed.
                        }
                        break;

                }
                break;
            case "Cube":
                switch (currentStep.getStage()) {
                    case 1:
                        if (basicVA(currentStep, false)) {
                            currentStep.nextStage();
                        }
                    case 2:
                        if(basicVA(currentStep, true)) {
                            currentStep.nextStage();
                        }
                }
        }

        if (currentStep.finishedWithStep()) {
            System.out.println("Finished with Step: " + currentStep);

            currentStep = SimpleAutoSeries.getNextStep();
            if (currentStep == null) {
                System.out.println("Nice! You finished the auto routine! Congrats from Forrest in the past!");
                finished = true;
            } else
            System.out.println("Moved on to next step, which is: " + currentStep);
        }
    }

    private boolean deadRecon(Instruction step) {
        dt.angleHold(currentAngle, step.getOrientation() - 180, maxForwardSpeed/2); //look strait into the portal.
        return false; //todome This needs to stop when the step has completed.
    }

    private boolean aim(double orientation) {
        dt.angleHold(currentAngle, orientation, 0);
        return abs(currentAngle - orientation) < maxAngleError;
    }

    /**
     * @param currentStep The game peice to aim at
     * @return error that we are off by.
     */
    private boolean basicVA(Instruction currentStep, boolean moveForward) {
        setPipeline(currentStep.getPipeline()); //this ensures that we are looking at the right pipeline for the object.
        double angle;
        if (tv.getDouble(0) == 1) { // if we dont have a target
            angle = tx.getDouble(0) + currentAngle;
        } else {
            System.out.println("No target found. Pointing at object.");
            aim(currentStep); //just pointing generally.
            return false;
        }

        double throttle = moveForward? 3/tx.getDouble(180) : 0; //3 degrees off is full throttle
        throttle = min(throttle, maxForwardSpeed); //max speed 0.5. Also add a minimum speed of 0.1.
        dt.angleHold(currentAngle, angle, throttle);

        if(!moveForward) {
            return abs(angle) < maxAngleError;
        } else {
            return doWeHaveACubeYet();
        }
    }

    private boolean doWeHaveACubeYet() {
        System.out.println("Error: No Method for tengo cube. Currently button 10.");
        //todome fill this in with some sensor.
        return Robot.joy.getRawButton(10);
    }

    //Navigate to a game piece. If we have made it too the game piece it will stop the
    private boolean navigate(Instruction currentStep) {
        double targetX = currentStep.getX();
        double targetY = currentStep.getY();
        double currentX = xLoc;
        double currentY = yLoc;

        double deltaX = targetX - currentX; //get the difference in x values;
        double deltaY = targetY - currentY; //get the difference in y values;

        if (abs(deltaX) < 5 && abs(deltaY) < 5) {
            System.out.println("deltaX = " + deltaX);
            System.out.println("deltaY = " + deltaY);
            dt.drive(0,0,0);
            return true;
        }

        double radians = atan2(deltaY, deltaX); //uses tangent to get angle.
        double angle = toDegrees(radians); //returns angle in radians.

        double throttle = sqrt(pow(deltaX, 2) + pow(deltaY, 2)) / 300;
        throttle = min(throttle + 0.1, maxForwardSpeed); //max speed 0.5. Also add a minimum speed of 0.1.
        dt.angleHold(currentAngle, angle, -throttle);
        return false;
    }

    private boolean aim(Instruction currentStep) {
        double targetX = currentStep.getX();
        double targetY = currentStep.getY();

        double deltaX = targetX - xLoc; //get the difference in x values;
        double deltaY = targetY - yLoc; //get the difference in y values;

        double radians = atan2(deltaY, deltaX); //uses tangent to get angle.
        double angle = toDegrees(radians); //returns angle in radians.

        dt.angleHold(currentAngle, angle, 0);
//        System.out.println("Current Angle = " + currentAngle);
//        System.out.println("angle = " + angle);
//        System.out.println("MaxAngleErro = " + maxAngleError);
        return abs(currentAngle - angle) < maxAngleError; //If we are close to our target.
    }

    public void IMUCalc() { //update the current location of the robot based on the Accelerometer. This is some serious bulshit right here.

        currentAngle = navX.getAngle();
        double forwardSpeed = dt.getAvgSpd();
        double deltaY = sin(currentAngle) * forwardSpeed; //converts from M/s to inches/sec then * 0.02 seconds to get deltaInches.
        double deltaX = cos(currentAngle) * forwardSpeed;
        yLoc += deltaY; //adds deltas to total.
        xLoc += deltaX; //adds deltas to total
//        System.out.println("IMUCalC: CurrentAngle: " + (int) currentAngle + "|yLoc: " + (int) yLoc + "|xLoc: " + (int) xLoc);
//        System.out.println("Delta X: " + deltaX + "|Delta Y: " + deltaY);
//        System.out.println("YVelocity: " + forwardSpeed);
    }

    /**
     * prints tx, ty, tv.
     */
    public void debug() {
        System.out.println("tx: " + tx.getDouble(0));
        System.out.println("ty: " + ty.getDouble(0));
        System.out.println("tv: " + tv.getDouble(0));
    }

    //returns the location relative to the line up.
    private double[] locFromLL() {

        //calculate values from navx and other things.
        System.out.println("update3DPoints: Using Math");
        final double targetHeight = 36; //is it 36 inches???
        final double cameraHeight = 8; //update but it probably doesn't matter.
        final double cameraAngle = 23; //update
        double hypotenuse = (targetHeight - cameraHeight) / tan(toRadians(ty.getDouble(0) + cameraAngle)); //
        //System.out.println(ty.getDouble(0) + cameraAngle + " = angle");
        //System.out.print("/" + Math.tan(Math.toRadians(ty.getDouble(0) + cameraAngle)));
        double yaw = currentAngle + tx.getDouble(0); //angle from the wall. Remember: negative is pointing to left, positive is to the right.
        double yOffset = -sin(yaw) * hypotenuse;
        double xOffset = -cos(yaw) * hypotenuse;
        //The following turns adjusts the x and y values from the limelight to get the

        final double relativeLLx = 0; //Positive value means the limelight is to the right of the center, while negative is to the left.
        final double relativeLLy = -13; //negative 20 means that the robot location is 20 inches. behind the limelight.
        //System.out.print("X=" + xOffset + " - " + relativeLLx*cos(getYaw) + " - " + relativeLLy*sin(getYaw));

        xOffset = xOffset - (relativeLLx * cos(currentAngle)) - (relativeLLy * sin(currentAngle));
        //System.out.println(" = " + xOffset);
        //System.out.print("Y=" + yOffset + " - " + relativeLLy*cos(getYaw) + " - " + relativeLLx*sin(getYaw));
        yOffset = yOffset - (relativeLLy * cos(currentAngle)) - (relativeLLx * sin(currentAngle));
        //System.out.println(" = " + yOffset);

        return new double[]{xOffset, yOffset};
    }

    private double sin(double n) {
        return Math.sin(toRadians(n));
    }

    private double cos(double n) {
        return Math.cos(toRadians(n));
    }


    /**
     * @param angleRequested the angle you want the robot to hold.
     * @return safeAngle is the maximum angle the robot can hold.
     */
    private double safeAngle(double angleRequested) {
        //given the angle requested, the change we need to make, and how much more we can turn without it going off the screen, return the maximun angle we can turn, given that it is NOT safe.
        //THIS IS LIKE 99% working. I tested a crap ton on codeHS.
        //angleRequested: The new angle we want to hold, RTF (relative to field).
        //getYaw: The current angle held RTF.
        //currentOffset: where the target is in the cameras vision. NOT RTF.
        double currentOffset = tx.getDouble(0);
        double degreesLeft = 30 - abs(currentOffset);//the amount of degrees we can move before we go off the field, ABS.
        //System.out.println("There are " + degreesLeft + " degrees left before the camera looses sight.");

        if (angleRequested - currentAngle > 30 + currentOffset) { //to the right
            System.out.println("safeAnlge: Limited. Trying to move TO FAR TO THE RIGHT");
            //limit it to only come way to the right. add degrees left to the current angle.
            return currentAngle + degreesLeft;
        } else if (angleRequested - currentAngle < -30 + currentOffset) { //to the LEFT
            System.out.println("safeAnlge: Limited. Trying to move TO FAR TO THE LEFT");
            //limit it to only come way to the left. subtract degrees left to the current angle.
            //because it should be to the left, which is negative.
            return currentAngle - degreesLeft;
        }

        //everything checked out. Send value back.
        return angleRequested;
    }

    /**
     * @param targetY how far out from the target you want to point at.
     * @return the angle we want to hold relative to the target. 0 is strait ahead.
     */
    private double getAngleFromTargetPoint(double currentX, double currentY, double targetY) {
        //takes in two points, x and y, that are relative to the limelight target / wall. returns the angle that the robot needs to hold, relative to the wall.
        //an angle of 0 is strait at the target, while 90 is all the way
        double targetX = 0; //on the line out

        double deltaX = targetX - currentX; //get the difference in x values;
        //System.out.print("getAngleFrom...: TgtX: " + (int) targetX + " - currentX: " + (int) currentX + " = ");
        //System.out.println((int) deltaX + " = deltaX");

        double deltaY = abs(targetY - currentY); //get the difference in y values;
        //System.out.print("getAngleFrom...: TgtY " + (int) targetY + " - CurrentY" + (int) currentY + " = ");
        //System.out.println((int) deltaY + " = deltaY");

        double radians = atan2(deltaY, deltaX); //uses tangent to get angle.
        return toDegrees(radians); //returns angle in radians.
    }

    /**
     * @return the target point in 3d space. Minimum -30 inches.
     */
    private double locateTargetPoint(double yOffset) {
        //this finds the y value that we need to look at.
        //return -40; //returning 2 feet out from wall to the limelight at the moment. Once we get better at following things then we can
        return min(0.67 * yOffset, -30); //takes the smaller of the two values. Once the calculated target point is further forward than -30, the program designateds -30 as the target location.
        //Also note that this is the center of the robot, not the front of the robot. Add the relativeLLy to get the distance to the front of the robot.
    }

    /**
     * Lines up the robot to the target found by the limelight.
     */
    private boolean lineUp(Instruction target) {
        setPipeline(target.getPipeline());
        if (tv.getDouble(0) == 0) { //no target found.
            System.out.println("main: ERROR : NO TARGET FOUND");
            dt.drive(0, 0, 0);
            return false;
        }

        currentAngle = relativize((int) navX.getYaw());  //get the yaw from the navx. MUST BE UPDATED TO BE RELATIVE TO THIS FRAME.

        double[] locs = locFromLL();//acquire new points, aka xOffset and yOffset. Adjust them to be robot center oriented.

        //System.out.println("main: X: " + (int) xOffset + " Y: " + (int) yOffset + " Angle: " + (int) getYaw);

        double xOffset = locs[0];
        double yOffset = locs[1];
        double targetAngle;
        double targetY = locateTargetPoint(yOffset);
        System.out.println("findAngle: TargetY: " + targetY);
        double angle = getAngleFromTargetPoint(xOffset, yOffset, targetY);
        angle = safeAngle(angle);
        targetAngle = currentAngle + ((angle - currentAngle) / 2);

        //System.out.println("main: targetAngle: " + (int) targetAngle);

        double followingTrackSpeed = calcFollowingSpeed(yOffset); //NOT how far out we start linearly slowing... higher num = slowing more.
        System.out.println(followingTrackSpeed);
        dt.angleHold(currentAngle, targetAngle, followingTrackSpeed);//followingTrackSpeed);

        return yOffset > -20;
    }

    /**
     * @return speed in which one should travel to the target. based on yOffset.
     * preconditions: yOffset
     */
    private double calcFollowingSpeed(double yOffset) {
        int distance = 200; //The distance where the slowing begins.
        double max = -0.5;   //The distance maximum motor values.
        return max((yOffset / distance) - 0.3, max); //subtract in order to ensure we always keep moving.
    }

    private float relativize(int yaw) { //this assumes two targets, one looking at 0 and 180.

        int modYaw = yaw + 89;
        int leftOver = (modYaw % 180) - 89;
        if (leftOver < -90) {
            leftOver += 180;
        }

        return leftOver;
    }

    private void setPipeline(int pipe) {
        if (pipe != currentPipeline) { //if the new value is different than the past values, change it up.
            pipeline.setNumber(pipe);
            currentPipeline = pipe;
            System.out.println("Changed Pipeline to " + pipe);
        }
    }

    public void IMURESET() {
        xLoc = 0;
        yLoc = 0;
        SimpleAutoSeries.reset();
    }
}


/*
Forrest: Tele-auto
    In: LL
    Out: Requested Movement of the robot.

Olivia and Laura: Feedback, PIDs.
    In: Positions and states of Robot parts
    Out: (End) Movements of motors and pneumatics on robot. Drive train
Mathew: Scouting Apps, AI for Vision
    In: (Begin) Nothing, get camera feed himself
    Out: Location of game objects relative to robot. (Format and abstraction TBD)
Rohit: Video Encoding
    In: None, Camera feed
    Out: (End) None, delivered to robot.
Luke: Controller Wrapper. Easy to switch controllers, going from axes to movement.
    In: (Begin) Controllers
    Out: X and Y from controller
Jerrison: Autonomous stuff. Map out init profiles.
*/