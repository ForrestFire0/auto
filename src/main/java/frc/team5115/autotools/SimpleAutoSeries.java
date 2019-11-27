package frc.team5115.autotools;

//A list of instructions for the Auto Class to work through.
public class SimpleAutoSeries {

    //The series of events we need to do.
    private static Instruction[] steps = {
            new LocationInstruction(0, 10, 0), //Go to a location of interest
            //new CubeInstruction(10, 10), //find a cube. Pick it up.
            //new PortalInstruction( 0, 0, 180)
    };
    private static int currentStep = 0;

    /**
     * @return current step that we are working on.
     */
    public static Instruction getCurrentStep() {
        return steps[currentStep];
    }

    /**
     * @return the next step to work on.
     */
    public static Instruction getNextStep() {
        currentStep++;
        if(currentStep >= steps.length) return null;
        return getCurrentStep();
    }

    public static int getStepNum() {
        return currentStep;
    }
}
