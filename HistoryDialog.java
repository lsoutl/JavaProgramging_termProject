import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HistoryDialog extends JDialog {

    public HistoryDialog(JFrame parent) {
        super(parent, "운동 기록", true);
        setLayout(new BorderLayout(8, 8));

        String[] columns = {"날짜", "운동", "무게(kg)", "Reps", "추정 1RM(kg)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        try {
            List<WorkoutHistory.Entry> entries = WorkoutHistory.loadAll();
            for (WorkoutHistory.Entry e : entries) {
                model.addRow(new Object[]{
                        e.date,
                        e.lift,
                        String.format("%.1f", e.weight),
                        e.reps,
                        String.format("%.1f", e.estimated1RM)
                });
            }
        } catch (IOException ex) {
            // empty table
        }

        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("Dialog", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { setVisible(false); }
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new JLabel(String.format("총 %d건", model.getRowCount())));
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        setSize(600, 400);
        setLocationRelativeTo(parent);
    }
}
