import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LiftPanel extends RoundedPanel {
    private static final int[] WARMUP_REPS = {5, 5, 3};
    private static final int[] WEEK1_REPS  = {5, 5, 5};
    private static final int[] WEEK2_REPS  = {3, 3, 3};
    private static final int[] WEEK3_REPS  = {5, 3, 1};
    private static final int[] DELOAD_REPS = {5, 5, 5};

    // Modern fitness-app palette
    private static final Color CARD_BG       = new Color( 36,  39,  46);
    private static final Color CARD_BORDER   = new Color( 55,  60,  70);
    private static final Color DIVIDER       = new Color( 50,  55,  64);
    private static final Color TEXT_PRIMARY  = new Color(240, 242, 246);
    private static final Color TEXT_SECONDARY= new Color(150, 156, 168);
    private static final Color TEXT_MUTED    = new Color(110, 116, 128);
    private static final Color ACCENT        = new Color(255, 107,  53); // PR / AMRAP
    private static final Color PILL_BG       = new Color( 50,  60,  78);
    private static final Color PILL_FG       = new Color(160, 200, 255);

    private static final Font TITLE_FONT  = new Font("Dialog", Font.BOLD,   18);
    private static final Font TM_FONT     = new Font("Dialog", Font.BOLD,   15);
    private static final Font PHASE_FONT  = new Font("Dialog", Font.PLAIN,  12);
    private static final Font HEADER_FONT = new Font("Dialog", Font.PLAIN,  12);
    private static final Font LABEL_FONT  = new Font("Dialog", Font.BOLD,   14);
    private static final Font CELL_FONT   = new Font("Dialog", Font.PLAIN,  16);
    private static final Font AMRAP_FONT  = new Font("Dialog", Font.BOLD,   16);
    private static final Font ASSIST_FONT = new Font("Dialog", Font.PLAIN,  13);

    private JLabel titleLabel;
    private JLabel tmLabel;
    private JLabel phasePill;
    private JLabel[] warmupLabels = new JLabel[3];
    private JLabel[] weekRowLabels = new JLabel[3];
    private JLabel[][] setLabels = new JLabel[3][3];
    private JLabel assistanceLabel;

    public LiftPanel(String liftName) {
        super(14, CARD_BG, CARD_BORDER);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(14, 18, 14, 18));

        titleLabel = new JLabel(liftName);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_PRIMARY);

        tmLabel = new JLabel("TM —");
        tmLabel.setFont(TM_FONT);
        tmLabel.setForeground(TEXT_PRIMARY);

        phasePill = pill("—");

        assistanceLabel = new JLabel(" ");
        assistanceLabel.setFont(ASSIST_FONT);
        assistanceLabel.setForeground(TEXT_SECONDARY);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(titleLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(tmLabel);
        right.add(phasePill);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildTable() {
        JPanel tableWrap = new JPanel();
        tableWrap.setOpaque(false);
        tableWrap.setLayout(new BoxLayout(tableWrap, BoxLayout.Y_AXIS));

        tableWrap.add(columnHeaderRow());
        tableWrap.add(divider());

        tableWrap.add(warmupRow());
        tableWrap.add(divider());

        for (int w = 0; w < 3; w++) {
            tableWrap.add(weekRow(w));
            if (w < 2) tableWrap.add(divider());
        }
        return tableWrap;
    }

    private JComponent columnHeaderRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(2, 0, 8, 0));
        row.add(headerLabel(""));
        row.add(headerLabel("Set 1"));
        row.add(headerLabel("Set 2"));
        row.add(headerLabel("Set 3"));
        return row;
    }

    private JComponent warmupRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 8, 0));
        row.add(rowTitleLabel("워밍업"));
        for (int s = 0; s < 3; s++) {
            warmupLabels[s] = valueLabel("-", false);
            row.add(warmupLabels[s]);
        }
        return row;
    }

    private JComponent weekRow(int w) {
        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 8, 0));
        weekRowLabels[w] = rowTitleLabel("-");
        row.add(weekRowLabels[w]);
        for (int s = 0; s < 3; s++) {
            boolean amrap = (s == 2);
            setLabels[w][s] = valueLabel("-", amrap);
            row.add(setLabels[w][s]);
        }
        return row;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));
        JComponent div = divider();
        footer.add(div, BorderLayout.NORTH);
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 0, 0, 0));
        inner.add(assistanceLabel);
        footer.add(inner, BorderLayout.CENTER);
        return footer;
    }

    private JLabel headerLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(HEADER_FONT);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JLabel rowTitleLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setFont(LABEL_FONT);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private JLabel valueLabel(String text, boolean amrap) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(amrap ? AMRAP_FONT : CELL_FONT);
        l.setForeground(amrap ? ACCENT : TEXT_PRIMARY);
        return l;
    }

    private JLabel pill(String text) {
        JLabel l = new JLabel(text);
        l.setFont(PHASE_FONT);
        l.setForeground(PILL_FG);
        l.setOpaque(true);
        l.setBackground(PILL_BG);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }

    private JComponent divider() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(DIVIDER);
        p.setPreferredSize(new Dimension(1, 1));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return p;
    }

    private static String formatCell(double weight, int reps, boolean amrap) {
        return String.format("%.1f × %d%s", weight, reps, amrap ? "+" : "");
    }

    public void update(double tm, double[] warmup, double[][] weekSets,
                       CyclePhase phase, String assistanceName, double assistanceWeight) {
        tmLabel.setText(String.format("TM  %.1f kg", tm));
        phasePill.setText(phase.toString());

        for (int s = 0; s < 3; s++) {
            warmupLabels[s].setText(formatCell(warmup[s], WARMUP_REPS[s], false));
        }

        if (phase.isDeload()) {
            weekRowLabels[0].setText("디로딩");
            for (int s = 0; s < 3; s++) {
                setLabels[0][s].setText(formatCell(weekSets[0][s], DELOAD_REPS[s], false));
                setLabels[0][s].setForeground(TEXT_PRIMARY);
                setLabels[0][s].setFont(CELL_FONT);
            }
            weekRowLabels[1].setText("");
            weekRowLabels[2].setText("");
            for (int w = 1; w < 3; w++) {
                for (int s = 0; s < 3; s++) {
                    setLabels[w][s].setText("");
                }
            }
            assistanceLabel.setText("디로딩 주차 · 보조 운동 생략");
        } else {
            int startWeek = phase.firstWeekNumber();
            int[][] allReps = {WEEK1_REPS, WEEK2_REPS, WEEK3_REPS};
            for (int w = 0; w < 3; w++) {
                weekRowLabels[w].setText(String.format("%d주차", startWeek + w));
                for (int s = 0; s < 3; s++) {
                    boolean amrap = (s == 2);
                    setLabels[w][s].setText(formatCell(weekSets[w][s], allReps[w][s], amrap));
                    setLabels[w][s].setForeground(amrap ? ACCENT : TEXT_PRIMARY);
                    setLabels[w][s].setFont(amrap ? AMRAP_FONT : CELL_FONT);
                }
            }
            if (assistanceName == null || assistanceName.equals("None")) {
                assistanceLabel.setText(" ");
            } else {
                assistanceLabel.setText(String.format(
                        "보조 %s · %.1f kg × 5세트", assistanceName, assistanceWeight));
            }
        }
    }

    public void clear() {
        tmLabel.setText("TM —");
        phasePill.setText("—");
        for (int s = 0; s < 3; s++) warmupLabels[s].setText("-");
        for (int w = 0; w < 3; w++) {
            weekRowLabels[w].setText("-");
            for (int s = 0; s < 3; s++) setLabels[w][s].setText("-");
        }
        assistanceLabel.setText(" ");
    }
}
