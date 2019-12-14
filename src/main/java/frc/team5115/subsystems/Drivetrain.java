package frc.team5115.subsystems;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import frc.team5115.autotools.DriveBase;
import frc.team5115.robot.Robot;

public class Drivetrain implements DriveBase {
    //instances of the speed controllers
    private TalonSRX frontLeft;
    private TalonSRX frontRight;
    private TalonSRX backLeft;
    private TalonSRX backRight;

    private double targetAngle; //during regular operation, the drive train keeps control of the drive. This is the angle that it targets.

    private double rightSpd;
    private double leftSpd;

    private NavX navX;

    public Drivetrain(NavX navX) {
        this.navX = navX;  //instantiation of the objects

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

    @Override
    public void stop() {
        drive(0,0,0);
    }

    @Override
    public void drive(double y, double x, double throttle) { //Change the drive output
        //called lots of times per seconds.
        y *= -1;

        //todome test this.
        //Math.sqrt(3.4* Math.log(x + y + 1));

        leftSpd = Math.min((x + y) * throttle, 1);
        rightSpd = Math.min((x - y) * throttle, 1);

        leftSpd = Math.max(leftSpd, -1);
        rightSpd = Math.max(rightSpd, -1);

//        System.out.println("Setting Right Pair to :" + (int) rightSpd * 100);
//        System.out.println("Setting Left Pair to :" + (int) leftSpd * 100);

        backLeft.set(ControlMode.PercentOutput, leftSpd);
        backRight.set(ControlMode.PercentOutput, rightSpd);
    }

    @Override
    public void resetTargetAngle() { //set the current target angle to where we currently are.
        targetAngle = navX.getAngle();
        System.out.println("RESET RBW: Target Angle: " + targetAngle + " Current Angle: " + navX.getAngle());
    }

    @Override
    public void angleHold(double currentAngle, double targetAngle, double y) {
        this.targetAngle = targetAngle;
        double kP = 0.02;
        //double kD = 0.01; Hey if you are implementing a d part, use the navx.getRate

        double P = kP*(targetAngle - currentAngle);
        //double D = kD*((currentAngle - lastAngle)/0.02); //finds the difference in the last tick.
        P = Math.max(-0.5, Math.min(0.5, P));
        this.drive(y,P,1);
    }

    public void driveByWire(double x, double y) { //rotate by wire
        double currentAngle = navX.getAngle();
        targetAngle += x*2.5; //at 50 ticks a second, this is 50 degrees a second because the max x is 1.
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

        backRight.set(ControlMode.PercentOutput, 0);
        System.out.println("Done collecting. Max speed was " + maxMotorSpeed);
    }

    void delay(double millis) {
        long micros = (long) millis*1000;

        try {
            Thread.sleep(micros);
        } catch (InterruptedException e) {
            System.out.println("For some reason the sleep failed... Here's the exception: " + e.getMessage());
        }
    }

    @Override
    public double getAvgSpd() {
        double rightSpd = frontRight.getSelectedSensorVelocity();
        double leftSpd = -backLeft.getSelectedSensorVelocity();

        final double wheelSpd = ((rightSpd + leftSpd) / 2) * 1.53846 * Math.PI / 4090;
        System.out.println("Wheel Speeds = " + wheelSpd);
        return wheelSpd;
    }
}