import java.io.*;

public class WorkoutStorage {
    public static final String DEFAULT_FILE = "workout_data.csv";

    public static class State {
        public double squat;
        public double deadlift;
        public double bench;
        public double press;
        public String assistance = "None";
        public String phase = "FIRST_THREE";
    }

    public static void save(State state, File file) throws IOException {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file));
            pw.println("squat,"      + state.squat);
            pw.println("deadlift,"   + state.deadlift);
            pw.println("bench,"      + state.bench);
            pw.println("press,"      + state.press);
            pw.println("assistance," + state.assistance);
            pw.println("phase,"      + state.phase);
        } finally {
            if (pw != null) pw.close();
        }
    }

    public static State load(File file) throws IOException {
        State state = new State();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (key.equals("squat"))           state.squat    = parseDoubleSafe(value);
                else if (key.equals("deadlift"))   state.deadlift = parseDoubleSafe(value);
                else if (key.equals("bench"))      state.bench    = parseDoubleSafe(value);
                else if (key.equals("press"))      state.press    = parseDoubleSafe(value);
                else if (key.equals("assistance")) state.assistance = value;
                else if (key.equals("phase"))      state.phase    = value;
            }
        } finally {
            if (br != null) br.close();
        }
        return state;
    }

    public static File defaultFile() {
        return new File(DEFAULT_FILE);
    }

    private static double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }
}
