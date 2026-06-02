import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkoutHistory {
    public static final String FILE = "workout_history.csv";

    public static class Entry {
        public String date;
        public String lift;
        public double weight;
        public int reps;
        public double estimated1RM;

        public Entry() {}

        public Entry(String date, String lift, double weight, int reps, double est) {
            this.date = date;
            this.lift = lift;
            this.weight = weight;
            this.reps = reps;
            this.estimated1RM = est;
        }

        public String toCsv() {
            return date + "," + lift + "," + weight + "," + reps + "," + estimated1RM;
        }

        public static Entry fromCsv(String line) {
            String[] parts = line.split(",");
            if (parts.length < 5) return null;
            try {
                return new Entry(
                        parts[0].trim(),
                        parts[1].trim(),
                        Double.parseDouble(parts[2].trim()),
                        Integer.parseInt(parts[3].trim()),
                        Double.parseDouble(parts[4].trim()));
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static void append(Entry e) throws IOException {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(FILE, true));
            pw.println(e.toCsv());
        } finally {
            if (pw != null) pw.close();
        }
    }

    public static List<Entry> loadAll() throws IOException {
        List<Entry> list = new ArrayList<>();
        File f = new File(FILE);
        if (!f.exists()) return list;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Entry e = Entry.fromCsv(line);
                if (e != null) list.add(e);
            }
        } finally {
            if (br != null) br.close();
        }
        return list;
    }

    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}
