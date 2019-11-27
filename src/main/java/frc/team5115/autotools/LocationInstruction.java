package frc.team5115.autotools;

public class LocationInstruction extends Instruction{

    private double orientation;

    LocationInstruction(double x, double y, double orientation) {
        super(x,y);
        this.orientation = orientation;
    }

    public double getOrientation() {
        return orientation;
    }

    public String getType() {
        return "Location";
    }

    @Override
    public int getPipeline() {
        return -1;
    }

    @Override
    public String toString() {
        return "LocationInstruction at" +
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
}
/*
Stages:
1. Point at the location
2. Move toward the location
3. Point at the new orientation.
 */