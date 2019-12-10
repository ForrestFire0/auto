package frc.team5115.autotools;

//A list of instructions for the Auto Class to work through.
public class SimpleAutoSeries {

    //The series of events we need to do.
    private static Instruction[] steps;
    private static int currentStep = 0;

    public static void reset() {

        steps = new Instruction[]{
                new LocationInstruction(0, 120, 90), //Go to a location of interest
                new LocationInstruction(0, 0, 26), //Go to a location of interest
                //new CubeInstruction(10, 10), //find a cube. Pick it up.
                //new PortalInstruction( 0, 0, 180)
        };

        setCurrentStep(0);
    }

    /**
     * @return current step that we are working on.
     */
    public static Instruction getCurrentStep() {
        if(currentStep >= steps.length) return null;
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

    public static void setCurrentStep(int currentStep) {
        if(currentStep >= steps.length || currentStep < 0)
        SimpleAutoSeries.currentStep = currentStep;
        else System.out.println("Your have attempted to set the current step to something out of bounds.");
    }

}
