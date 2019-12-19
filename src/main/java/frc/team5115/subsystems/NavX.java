package frc.team5115.subsystems;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;


public class NavX {
    private AHRS navx; //turn baby.
    private double angle; //angle is total accumulated.
    private double yaw; //relative to start, from -180 to 180.

    public NavX() {
        navx = new AHRS(SPI.Port.kMXP);
        navx.reset(); //reset to the start orientation
    }

    /**
     * resets the navx yaw value to 0. Relative to the current position of the robot.
     */
    public void navxAngleReset() {
        navx.reset(); //reset to the field orientation
        System.out.println("Angle has been reset.");
        System.out.println(navx.getYaw() + " = 0");
    }
    /**
     * @return totalAccumulated Angle -gazillion to a gazillion
     */
    double getAngle() {
        return angle;
    }

    /**
     * @return angle -180 to 180
     */
    double getYaw() {
        return yaw;
    }

    /**
     * Run a update the values.
     */
    public void runTick() {
        angle = navx.getAngle();
        yaw = navx.getYaw();
    }
}