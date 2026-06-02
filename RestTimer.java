import javax.swing.SwingUtilities;

public class RestTimer implements Runnable {

    public interface TimerListener {
        void onTick(int remaining, int total);
        void onFinish();
    }

    private Thread thread;
    private int totalSeconds;
    private volatile int remainingSeconds;
    private volatile boolean running;
    private final TimerListener listener;

    public RestTimer(TimerListener listener) {
        this.listener = listener;
    }

    public void start(int seconds) {
        if (running) stop();
        this.totalSeconds = seconds;
        this.remainingSeconds = seconds;
        this.running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    public boolean isRunning() {
        return running;
    }

    public void run() {
        try {
            while (running && remainingSeconds > 0) {
                final int r = remainingSeconds;
                final int t = totalSeconds;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (listener != null) listener.onTick(r, t);
                    }
                });
                Thread.sleep(1000);
                remainingSeconds--;
            }
            if (running) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (listener != null) listener.onFinish();
                    }
                });
            }
        } catch (InterruptedException e) {
            // stopped externally
        } finally {
            running = false;
        }
    }
}
