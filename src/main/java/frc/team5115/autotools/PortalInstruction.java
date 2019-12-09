package frc.team5115.autotools;

public class PortalInstruction extends Instruction {

    private double orientation;

    PortalInstruction(double x, double y, double orientation) {
        super(x,y);
        this.orientation = orientation;
    }

    public int getPipeline() {
        return 0;
    }

    public String getType() {
        return "Portal";
    }

    @Override
    public String toString() {
        return "PortalInstruction at" +
                " x=" + x +
                " y=" + y +
                " and orientation=" + orientation +
                " stage=" + stage +
                '}';
    }
    @Override
    public boolean finishedWithStep() {
        return stage == 4;
    }

    @Override
    public double getOrientation() {
        return orientation;
    }
}

/*
Stages:
1. Use LL to move on in.
2. Use dead reconing to go the extra distance until SOMETHING HERE
 */
