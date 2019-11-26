package frc.team5115.autotools;

public class CubeInstruction extends Instruction {

    public CubeInstruction(double x, double y) {
        super(x, y);
    }

    public int getPipeline() {
        return 3;
    }

    public String getType() {
        return "Cube";
    }

    @Override
    public String toString() {
        return "CubeInstruction at" +
                " x=" + x +
                " y=" + y +
                " stage=" + stage +
                '}';
    }
    @Override
    public boolean finishedWithStep() {
        return stage == 3;
    }
}
/*
Stages:
1. Point at where we assume the cube to be
2. Use the limelight to move toward a cube. Motors on and other such.
 */