public class WorkoutCalculator {
    public static final double[] WARMUP = {0.40, 0.50, 0.60};
    public static final double[] DELOAD = {0.40, 0.50, 0.60};
    public static final double[] WEEK1  = {0.65, 0.75, 0.85};
    public static final double[] WEEK2  = {0.70, 0.80, 0.90};
    public static final double[] WEEK3  = {0.75, 0.85, 0.95};

    public static final double INCREMENT_LOWER = 5.0;
    public static final double INCREMENT_UPPER = 2.5;

    private double squat1RM;
    private double deadlift1RM;
    private double bench1RM;
    private double press1RM;

    public WorkoutCalculator(double squat, double deadlift, double bench, double press) {
        this.squat1RM    = squat;
        this.deadlift1RM = deadlift;
        this.bench1RM    = bench;
        this.press1RM    = press;
    }

    public double squatTM()    { return squat1RM    * 0.9; }
    public double deadliftTM() { return deadlift1RM * 0.9; }
    public double benchTM()    { return bench1RM    * 0.9; }
    public double pressTM()    { return press1RM    * 0.9; }

    public double tmForPhase(double baseTM, boolean lowerBody, CyclePhase phase) {
        if (phase.isIncreased()) {
            return baseTM + (lowerBody ? INCREMENT_LOWER : INCREMENT_UPPER);
        }
        return baseTM;
    }

    public double[] weekSets(double tm, int week) {
        double[] pct;
        if (week == 1)      pct = WEEK1;
        else if (week == 2) pct = WEEK2;
        else                pct = WEEK3;

        double[] sets = new double[3];
        for (int i = 0; i < 3; i++) sets[i] = roundTo2_5(tm * pct[i]);
        return sets;
    }

    public double[] warmupSets(double tm) {
        double[] sets = new double[3];
        for (int i = 0; i < 3; i++) sets[i] = roundTo2_5(tm * WARMUP[i]);
        return sets;
    }

    public double[] deloadSets(double tm) {
        double[] sets = new double[3];
        for (int i = 0; i < 3; i++) sets[i] = roundTo2_5(tm * DELOAD[i]);
        return sets;
    }

    public double fsl(double tm) { return roundTo2_5(tm * WEEK1[0]); }
    public double bbb(double tm) { return roundTo2_5(tm * 0.5); }

    public static double roundTo2_5(double value) {
        return Math.round(value / 2.5) * 2.5;
    }

    public static double estimateOneRM(double weight, int reps) {
        if (reps <= 0 || weight <= 0) return 0;
        return weight * (1.0 + reps / 30.0);
    }
}
