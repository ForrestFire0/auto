package frc.team5115.subsystems;

import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.Encoder;

public class EncoderMngr {

    private Encoder leftEncoder;
    private Encoder rightEncoder;

    private final int leftEncoder1 = 0;
    private final int leftEncoder2 = 0;
    private final int rightEncoder1 = 0;
    private final int rightEncoder2 = 0;

    public EncoderMngr() {
        leftEncoder = new Encoder(leftEncoder1, leftEncoder2, true, CounterBase.EncodingType.k4X);
        rightEncoder = new Encoder(rightEncoder1, rightEncoder2, true, CounterBase.EncodingType.k4X);
        leftEncoder.setDistancePerPulse(12.566 / 250);	// 250 CPR, 4 pulses per cycle, 12.566 inches circumference on wheel
        rightEncoder.setDistancePerPulse(12.566 / 250);

        leftEncoder.reset();
        rightEncoder.reset();
    }


    public double leftDist() {
        return leftEncoder.getDistance();
    }

    public double rightDist() {
        return rightEncoder.getDistance();
    }

    public double leftRate() {
        return leftEncoder.getRate();
    }

    public double rightRate() {
        return rightEncoder.getRate();
    }

}
