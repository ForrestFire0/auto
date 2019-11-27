package frc.team5115.autotools;

public class CubeInstruction extends Instruction {

    CubeInstruction(double x, double y) {
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

    @Override
    public double getOrientation() {
        try {
            throw new Throwable("Error! Cannot get Orientation from a cube!");
        } catch (Throwable throwable) {
            System.out.println("Somehow you failed to fail. (Cube Instructions Ln35)");
        }
        return 0;
    }
}
/*
Stages:
1. Point at where we assume the cube to be
2. Use the limelight to move toward a cube. Motors on and other such.
 */