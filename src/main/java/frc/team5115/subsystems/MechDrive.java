package frc.team5115.subsystems;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import frc.team5115.robot.Robot;

/**
 * Code by Forrest Milner for 5115
 */

public class MechDrive {
    //instances of the speed controllers
    private TalonSRX frontLeft;
    private TalonSRX frontRight;
    private TalonSRX backLeft;
    private TalonSRX backRight;
    private double targetAngle;

    private NavX navX;

    public MechDrive(NavX navX) {
        this.navX = navX;  //instantiation of the objects
        frontLeft = new TalonSRX(1);
        frontRight = new TalonSRX(2);
        backLeft = new TalonSRX(3);
        backRight = new TalonSRX(4);

        frontLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
        frontRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
        backLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
        backRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
        System.out.println("Mechanum drive started. Ensure you reset the angle of the navx, as field oriented drive");
        System.out.println("assumes 0 is the way driver is looking.");
    }

    public void stdDrive(double y,double yaw, double thrott) { //drive the mechanum base like a standard robot using just rotation and forward back motion
        this.mechDrive(y, 0, yaw, thrott);
    }

    public void mechDrive(double y, double x, double yaw, double throttle) {
        //lowest level drive statement.
        //called lots of times per seconds.
        //all wheels get a little bit of the forward. If we want to go forward, all motors go forward.

        final int reverseY = 1;
        final int reverseX = 1;
        final int reverseYaw = 1;

        final int reverseFL = 1;
        final int reverseBL = 1;
        final int reverseFR = 1;
        final int reverseBR = 1;

        x *= reverseX;
        y *= reverseY;
        yaw *= reverseYaw;

        double frontLeftSpd = (y + x + yaw) * reverseFL;
        double frontRightSpd = (y - x - yaw) * reverseFR;
        double backLeftSpd = (y - x + yaw) * reverseBL;
        double backRightSpd = (y + x - yaw) * reverseBR;

        frontLeftSpd *= throttle;
        frontRightSpd *= throttle;
        backLeftSpd *= throttle;
        backRightSpd *= throttle;

        //set the outputs. let the magic occur
        frontLeft.set(ControlMode.PercentOutput, frontLeftSpd);
        frontRight.set(ControlMode.PercentOutput, frontRightSpd);
        backLeft.set(ControlMode.PercentOutput, backLeftSpd);
        backRight.set(ControlMode.PercentOutput, backRightSpd);
    }

    public void frDrive(double y, double x, double yaw, double throttle) { //field relative drive.
        double currentRobotAngle = navX.getAngle();
        
        double newX;
        double newY;

        double sin = Math.sin(Math.toRadians(currentRobotAngle));
        double cos = Math.cos(Math.toRadians(currentRobotAngle));

        newY = sin*x + cos*y;
        newX = cos*x - sin*y;

        mechDrive(newY, newX, yaw, throttle);
    }

    public void resetTargetAngle() { //set the current target angle to where we currently are.
        targetAngle = navX.getAngle();
        System.out.println("RESET RBW: Target Angle: " + targetAngle + " Current Angle: " + navX.getAngle());
    }

    void angleHold(double currentAngle, double targetAngle, double y) {
        this.targetAngle = targetAngle;
        double kP = 0.02;
        //double kD = 0.01; Hey if you are implementing a d part, use the navx.getRate

        double P = kP*(targetAngle - currentAngle);
        //double D = kD*((currentAngle - lastAngle)/0.02); //finds the difference in the last tick.
        P = Math.max(-0.5, Math.min(0.5, P));
        this.stdDrive(y,P,1);
    }

    void angleHold(double targetAngle) { //Overridden magic.
        this.angleHold(0, targetAngle, 0);
    }

    public void RBW(double x, double y) { //rotate by wire
        double currentAngle = navX.getAngle();
        targetAngle += x*2.5; //at 50 ticks a second, this is 50 degrees a second because the max x is 1.
        angleHold(currentAngle, targetAngle, y);
    }
}
