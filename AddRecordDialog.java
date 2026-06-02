import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class AddRecordDialog extends JDialog {

    private JComboBox<String> liftBox;
    private JTextField weightField;
    private JTextField repsField;
    private JLabel estimatedLabel;

    private double estimatedOneRM = 0;
    private String resultLift = "";
    private boolean applyRequested = false;

    public AddRecordDialog(JFrame parent) {
        super(parent, "기록 추가 / 추정 1RM", true);

        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 6));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        liftBox = new JComboBox<>(new String[]{
                "스쿼트", "데드리프트", "벤치프레스", "오버헤드프레스"
        });
        weightField   = new JTextField();
        repsField     = new JTextField();
        estimatedLabel = new JLabel("-");
        estimatedLabel.setFont(estimatedLabel.getFont().deriveFont(Font.BOLD, 14f));

        form.add(new JLabel("운동:"));         form.add(liftBox);
        form.add(new JLabel("AMRAP 무게 (kg):")); form.add(weightField);
        form.add(new JLabel("달성 reps:"));    form.add(repsField);
        form.add(new JLabel("추정 1RM:"));     form.add(estimatedLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton calcBtn   = new JButton("계산");
        JButton saveBtn   = new JButton("기록 저장");
        JButton applyBtn  = new JButton("1RM에 반영");
        JButton cancelBtn = new JButton("닫기");
        buttons.add(calcBtn);
        buttons.add(saveBtn);
        buttons.add(applyBtn);
        buttons.add(cancelBtn);

        calcBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { calculate(); }
        });
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { save(); }
        });
        applyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { applyToOneRM(); }
        });
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setVisible(false); }
        });

        add(form,    BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void calculate() {
        try {
            double w = Double.parseDouble(weightField.getText().trim());
            int    r = Integer.parseInt(repsField.getText().trim());
            estimatedOneRM = WorkoutCalculator.estimateOneRM(w, r);
            estimatedLabel.setText(String.format("%.1f kg", estimatedOneRM));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "숫자로 입력하세요.",
                    "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void save() {
        try {
            double w = Double.parseDouble(weightField.getText().trim());
            int    r = Integer.parseInt(repsField.getText().trim());
            double est = WorkoutCalculator.estimateOneRM(w, r);
            String lift = (String) liftBox.getSelectedItem();
            WorkoutHistory.Entry e = new WorkoutHistory.Entry(
                    WorkoutHistory.today(), lift, w, r, est);
            WorkoutHistory.append(e);
            estimatedOneRM = est;
            estimatedLabel.setText(String.format("%.1f kg", est));
            JOptionPane.showMessageDialog(this, "기록이 저장되었습니다.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "숫자로 입력하세요.",
                    "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "저장 실패: " + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyToOneRM() {
        if (estimatedOneRM <= 0) {
            calculate();
            if (estimatedOneRM <= 0) return;
        }
        resultLift = (String) liftBox.getSelectedItem();
        applyRequested = true;
        setVisible(false);
    }

    public boolean shouldApply()  { return applyRequested; }
    public String  getLift()      { return resultLift; }
    public double  getEstimated() { return estimatedOneRM; }
}
