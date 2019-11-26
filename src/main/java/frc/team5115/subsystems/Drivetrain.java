package frc.team5115.subsystems;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.Timer;
import frc.team5115.robot.Robot;

public class Drivetrain {
    //instances of the speed controllers
    private TalonSRX frontLeft;
    private TalonSRX frontRight;
    private TalonSRX backLeft;
    private TalonSRX backRight;
    private double targetAngle;

    public Drivetrain() {  //instantiation of the objects
        frontLeft = new TalonSRX(1);
        frontRight = new TalonSRX(2);
        backLeft = new TalonSRX(3);
        backRight = new TalonSRX(4);

        frontLeft.set(ControlMode.Follower, backLeft.getDeviceID());
        frontRight.set(ControlMode.Follower, backRight.getDeviceID());

        frontLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
        frontRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
        backLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
        backRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
    }

    public void drive(double y, double x, double throttle) { //Change the drive output
        //called lots of times per seconds.
        y *= -1;

        double leftSpd = (x + y) * throttle;
        double rightSpd = (x - y) * throttle;
        //set the outputs. let the magic occur
        //convert ticks to meters
        backLeft.set(ControlMode.PercentOutput, leftSpd);
        backRight.set(ControlMode.PercentOutput, rightSpd);

        System.out.println("rightSpd: " + rightSpd + "  Encoder Speed: " + backLeft.getSelectedSensorVelocity());
        System.out.println("leftSpd: " + leftSpd + "  Encoder Speed: " + backRight.getSelectedSensorVelocity());
    }

    public void resetTargetAngle() { //set the current target angle to where we currently are.
        targetAngle = Robot.navX.getAngle();
        System.out.println("RESET RBW: Target Angle: " + targetAngle + " Current Angle: " + Robot.navX.getAngle());
    }

    void angleHold(double currentAngle, double targetAngle, double y) {
        this.targetAngle = targetAngle;
        double kP = 0.02;
        //double kD = 0.01; Hey if you are implementing a d part, use the navx.getRate

        double P = kP*(targetAngle - currentAngle);
        //double D = kD*((currentAngle - lastAngle)/0.02); //finds the difference in the last tick.
        P = Math.max(-0.5, Math.min(0.5, P));
        this.drive(y,P,1);
    }

    void angleHold(double targetAngle) { //Overridden magic.
        this.angleHold(0, targetAngle, 0);
    }

    public void driveByWire(double x, double y) { //rotate by wire
        double currentAngle = Robot.navX.getAngle();
        targetAngle += x*2.5; //at 50 ticks a second, this is 50 degrees a second because the max x is 1.
        angleHold(currentAngle, targetAngle, y);
    }

    public void knightlyDrive(double x, double y) { //
        double currentAngle = Robot.navX.getAngle();
        targetAngle = targetAngle - x*3*y; //at 50 ticks a second, this is 50 degrees a second because the max x is 1.
        //The faster we are moving forward (switch this data with encoder data) The faster we should rotate.
        angleHold(currentAngle, targetAngle, y);
    }

    public void tester() {
        System.out.println("Starting Tester.");
        System.out.println("Setting backright to full speed.");
        double maxMotorSpeed = 0;
        for (double i = 0; i < 1; i+=0.1) {
            backRight.set(ControlMode.PercentOutput, i);
            delay(50);
        }
        System.out.println("Motor on. Collecting values");
        for(int i = 0; i < 100; i++) {
           maxMotorSpeed = Math.max(backRight.getSelectedSensorVelocity(), maxMotorSpeed);
           delay(10);
        }

        frontRight.set(ControlMode.PercentOutput, 0);
        System.out.println("Done collecting. Max speed was " + maxMotorSpeed);
    }

    public void delay(double millis) {
        long micros = (long) millis*1000;

        try {
            Thread.sleep(micros);
        } catch (InterruptedException e) {
            System.out.println("For some reason the sleep failed... Here's the exception: " + e.getMessage());
        }
    }
}