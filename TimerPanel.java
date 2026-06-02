import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TimerPanel extends JPanel implements RestTimer.TimerListener {

    private static final String[] DURATION_LABELS = {"60초", "90초", "120초", "180초", "300초"};
    private static final int[]    DURATION_SECS   = { 60,    90,    120,    180,    300 };

    private JComboBox<String> durationBox;
    private JButton startButton;
    private JButton stopButton;
    private JLabel timeLabel;
    private JProgressBar progressBar;
    private RestTimer timer;

    public TimerPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        setBorder(BorderFactory.createTitledBorder("휴식 타이머"));

        durationBox = new JComboBox<>(DURATION_LABELS);
        durationBox.setSelectedIndex(1);

        startButton = new JButton("시작");
        stopButton  = new JButton("정지");
        stopButton.setEnabled(false);

        timeLabel = new JLabel("00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        timeLabel.setPreferredSize(new Dimension(70, 22));

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(260, 22));
        progressBar.setStringPainted(true);
        progressBar.setString("대기");

        add(new JLabel("시간:"));
        add(durationBox);
        add(startButton);
        add(stopButton);
        add(timeLabel);
        add(progressBar);

        timer = new RestTimer(this);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int sec = DURATION_SECS[durationBox.getSelectedIndex()];
                timer.start(sec);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                progressBar.setValue(0);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                resetUI();
                progressBar.setString("정지");
                timeLabel.setText("00:00");
            }
        });
    }

    public void onTick(int remaining, int total) {
        int min = remaining / 60;
        int sec = remaining % 60;
        timeLabel.setText(String.format("%02d:%02d", min, sec));
        int progress = (int) (((total - remaining) * 100.0) / total);
        progressBar.setValue(progress);
        progressBar.setString(remaining + "초 남음");
    }

    public void onFinish() {
        timeLabel.setText("완료!");
        progressBar.setValue(100);
        progressBar.setString("완료");
        resetUI();
        Toolkit.getDefaultToolkit().beep();
    }

    private void resetUI() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
}
