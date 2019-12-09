package frc.team5115.autotools;

//stores an interesting game piece. Location in relative to
abstract public class Instruction {
    //distances in feet.
    double x;
    double y;

    int stage;

    //in order to create a new instruction...
    Instruction(double x, double y) {
        this.x = x;
        this.y = y;
        stage = 1;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getType() {
        return "Undefined";
    }

    public int getStage() {
        return stage;
    }

    public void nextStage() {
        stage++;
        System.out.println("Moving to stage " + stage + " in step " + toString());
    }

    abstract public double getOrientation();

    abstract public int getPipeline();

    abstract public String toString();

    public abstract boolean finishedWithStep();
}