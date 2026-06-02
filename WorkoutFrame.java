import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.swing.*;

public class WorkoutFrame extends JFrame {
    private static final int ICON_SIZE = 64;

    private static final String SQUAT_IMG    = "squat";
    private static final String DEADLIFT_IMG = "deadlift";
    private static final String BENCH_IMG    = "bench";
    private static final String PRESS_IMG    = "press";

    private JLabel squatLabel    = new JLabel("스쿼트:");
    private JLabel deadliftLabel = new JLabel("데드리프트:");
    private JLabel benchLabel    = new JLabel("벤치프레스:");
    private JLabel pressLabel    = new JLabel("오버헤드프레스:");

    private JLabel squatIcon    = new JLabel();
    private JLabel deadliftIcon = new JLabel();
    private JLabel benchIcon    = new JLabel();
    private JLabel pressIcon    = new JLabel();

    private JTextField squatField    = new JTextField();
    private JTextField deadliftField = new JTextField();
    private JTextField benchField    = new JTextField();
    private JTextField pressField    = new JTextField();

    private JComboBox<String> assistanceBox =
            new JComboBox<>(new String[]{"None", "FSL", "SSL", "BBB"});

    private JComboBox<CyclePhase> cycleBox =
            new JComboBox<>(CyclePhase.values());

    private LiftPanel squatPanel    = new LiftPanel("스쿼트");
    private LiftPanel deadliftPanel = new LiftPanel("데드리프트");
    private LiftPanel benchPanel    = new LiftPanel("벤치프레스");
    private LiftPanel pressPanel    = new LiftPanel("오버헤드프레스");

    private JComponent inputPanelHolder;
    private JButton editButton;

    public WorkoutFrame() {
        setTitle("Workout Routine Manager");
        setSize(820, 920);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.setBackground(new Color(20, 22, 28));
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(true);
        }

        c.add(createInputPanel(),  BorderLayout.NORTH);
        c.add(createResultPanel(), BorderLayout.CENTER);
        c.add(createSouthPanel(),  BorderLayout.SOUTH);

        setJMenuBar(createMenuBar());

        autoLoad();
        loadExerciseImages();

        setVisible(true);
    }

    private JComponent createInputPanel() {
        squatIcon.setIcon(createPlaceholderIcon());
        deadliftIcon.setIcon(createPlaceholderIcon());
        benchIcon.setIcon(createPlaceholderIcon());
        pressIcon.setIcon(createPlaceholderIcon());

        configureExerciseLabel(squatLabel,    "스쿼트");
        configureExerciseLabel(deadliftLabel, "데드리프트");
        configureExerciseLabel(benchLabel,    "벤치프레스");
        configureExerciseLabel(pressLabel,    "오버헤드프레스");

        styleField(squatField);
        styleField(deadliftField);
        styleField(benchField);
        styleField(pressField);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        addGridRow(grid, 0, squatIcon,    squatLabel,    squatField);
        addGridRow(grid, 1, deadliftIcon, deadliftLabel, deadliftField);
        addGridRow(grid, 2, benchIcon,    benchLabel,    benchField);
        addGridRow(grid, 3, pressIcon,    pressLabel,    pressField);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(grid, new GridBagConstraints());

        JPanel card = new RoundedPanel(14,
                new Color(36, 39, 46), new Color(55, 60, 70));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel sectionTitle = new JLabel("1RM 입력");
        sectionTitle.setFont(new Font("Dialog", Font.BOLD, 15));
        sectionTitle.setForeground(new Color(150, 156, 168));
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        card.add(sectionTitle, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        outer.add(card, BorderLayout.CENTER);
        inputPanelHolder = outer;
        return outer;
    }

    private void addGridRow(JPanel grid, int row,
                            JLabel icon, JLabel label, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 0, 6, 18);
        grid.add(icon, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 22);
        grid.add(label, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 0);
        grid.add(field, gbc);
    }

    private void configureExerciseLabel(JLabel label, String text) {
        label.setText(text);
        label.setForeground(new Color(240, 242, 246));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
    }

    private void styleField(JTextField f) {
        UIDefaults overrides = new UIDefaults();
        javax.swing.Painter<JComponent> noPaint = new javax.swing.Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) { }
        };
        overrides.put("TextField[Enabled].backgroundPainter", noPaint);
        overrides.put("TextField[Focused].backgroundPainter", noPaint);
        overrides.put("TextField[Disabled].backgroundPainter", noPaint);
        overrides.put("TextField[Selected].backgroundPainter", noPaint);
        overrides.put("TextField[Focused+Selected].backgroundPainter", noPaint);
        f.putClientProperty("Nimbus.Overrides", overrides);
        f.putClientProperty("Nimbus.Overrides.InheritDefaults", false);

        f.setOpaque(true);
        f.setBackground(new Color(46, 50, 60));
        f.setForeground(new Color(240, 242, 246));
        f.setCaretColor(new Color(255, 107, 53));
        f.setSelectionColor(new Color(255, 107, 53));
        f.setSelectedTextColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        f.setFont(new Font("Dialog", Font.PLAIN, 18));
        f.setHorizontalAlignment(SwingConstants.RIGHT);

        Dimension d = new Dimension(220, 40);
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        f.setMinimumSize(d);
    }

    private static ImageIcon createPlaceholderIcon() {
        BufferedImage img = new BufferedImage(
                ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(55, 58, 65));
        g.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
        g.setColor(new Color(95, 100, 110));
        g.drawRect(0, 0, ICON_SIZE - 1, ICON_SIZE - 1);
        g.setColor(new Color(160, 165, 175));
        g.setFont(new Font("Dialog", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        String txt = "loading…";
        int tw = fm.stringWidth(txt);
        g.drawString(txt, (ICON_SIZE - tw) / 2, ICON_SIZE / 2 + fm.getAscent() / 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private void loadExerciseImages() {
        new Thread(new Runnable() {
            public void run() {
                fetchAndApply(squatIcon,    SQUAT_IMG);
                fetchAndApply(deadliftIcon, DEADLIFT_IMG);
                fetchAndApply(benchIcon,    BENCH_IMG);
                fetchAndApply(pressIcon,    PRESS_IMG);
            }
        }).start();
    }

    private void fetchAndApply(final JLabel target, String basename) {
        try {
            File f = findImageFile(basename);
            if (f == null) {
                System.err.println("이미지 파일 없음: " + basename + ".jpg/.png");
                return;
            }
            BufferedImage img = ImageIO.read(f);
            if (img == null) return;

            Image scaled = img.getScaledInstance(
                    ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            final ImageIcon icon = new ImageIcon(scaled);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    target.setIcon(icon);
                    target.revalidate();
                    target.repaint();
                }
            });
        } catch (Exception ex) {
            System.err.println("이미지 로드 실패: " + basename + " — " + ex.getMessage());
        }
    }

    private File findImageFile(String basename) {
        String[] exts = {".jpg", ".jpeg", ".png"};
        for (String ext : exts) {
            File f = new File(basename + ext);
            if (f.exists()) return f;
        }
        return null;
    }

    private JComponent createResultPanel() {
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        sizeLiftPanel(squatPanel);
        sizeLiftPanel(deadliftPanel);
        sizeLiftPanel(benchPanel);
        sizeLiftPanel(pressPanel);

        stack.add(squatPanel);
        stack.add(Box.createVerticalStrut(12));
        stack.add(deadliftPanel);
        stack.add(Box.createVerticalStrut(12));
        stack.add(benchPanel);
        stack.add(Box.createVerticalStrut(12));
        stack.add(pressPanel);

        JScrollPane scroll = new JScrollPane(stack,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void sizeLiftPanel(LiftPanel p) {
        p.setPreferredSize(new Dimension(720, 340));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JPanel createSouthPanel() {
        JPanel south = new JPanel(new BorderLayout());
        south.add(new TimerPanel(),     BorderLayout.NORTH);
        south.add(createControlPanel(), BorderLayout.SOUTH);
        return south;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        Font ctrlFont = new Font("Dialog", Font.BOLD, 14);

        JLabel cycleLbl = new JLabel("사이클:");
        cycleLbl.setFont(ctrlFont);
        panel.add(cycleLbl);
        cycleBox.setFont(ctrlFont);
        panel.add(cycleBox);

        JLabel assistLbl = new JLabel("보조 볼륨:");
        assistLbl.setFont(ctrlFont);
        panel.add(assistLbl);
        assistanceBox.setFont(ctrlFont);
        panel.add(assistanceBox);

        JButton calcButton = new JButton("계산");
        calcButton.setFont(ctrlFont);
        calcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onCalculate(); }
        });
        panel.add(calcButton);

        editButton = new JButton("1RM 수정");
        editButton.setFont(ctrlFont);
        editButton.setVisible(false);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { showInputPanel(); }
        });
        panel.add(editButton);

        JButton resetButton = new JButton("초기화");
        resetButton.setFont(ctrlFont);
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onReset(); }
        });
        panel.add(resetButton);

        return panel;
    }

    private void showInputPanel() {
        if (inputPanelHolder != null) {
            inputPanelHolder.setVisible(true);
            editButton.setVisible(false);
            revalidate();
            repaint();
        }
    }

    private void hideInputPanel() {
        if (inputPanelHolder != null) {
            inputPanelHolder.setVisible(false);
            editButton.setVisible(true);
            revalidate();
            repaint();
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem saveItem = new JMenuItem("저장");
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onSave(); }
        });

        JMenuItem loadItem = new JMenuItem("불러오기");
        loadItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onLoad(); }
        });

        JMenuItem resetItem = new JMenuItem("초기화");
        resetItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onReset(); }
        });

        JMenuItem exitItem = new JMenuItem("종료");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { System.exit(0); }
        });

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(resetItem);
        fileMenu.add(exitItem);

        JMenu recordMenu = new JMenu("기록");

        JMenuItem addRecordItem = new JMenuItem("기록 추가 / 추정 1RM");
        addRecordItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onAddRecord(); }
        });

        JMenuItem viewRecordItem = new JMenuItem("기록 보기");
        viewRecordItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onViewHistory(); }
        });

        recordMenu.add(addRecordItem);
        recordMenu.add(viewRecordItem);

        bar.add(fileMenu);
        bar.add(recordMenu);
        return bar;
    }

    private void onAddRecord() {
        AddRecordDialog dialog = new AddRecordDialog(this);
        dialog.setVisible(true);
        if (dialog.shouldApply()) {
            JTextField target = fieldForLift(dialog.getLift());
            if (target != null) {
                double newOneRM = WorkoutCalculator.roundTo2_5(dialog.getEstimated());
                target.setText(formatLoaded(newOneRM));
                JOptionPane.showMessageDialog(this,
                        String.format("%s 1RM이 %.1f kg으로 갱신되었습니다.",
                                dialog.getLift(), newOneRM));
            }
        }
    }

    private void onViewHistory() {
        new HistoryDialog(this).setVisible(true);
    }

    private JTextField fieldForLift(String lift) {
        if ("스쿼트".equals(lift))           return squatField;
        if ("데드리프트".equals(lift))       return deadliftField;
        if ("벤치프레스".equals(lift))       return benchField;
        if ("오버헤드프레스".equals(lift))   return pressField;
        return null;
    }

    private void onCalculate() {
        try {
            double squat    = Double.parseDouble(squatField.getText().trim());
            double deadlift = Double.parseDouble(deadliftField.getText().trim());
            double bench    = Double.parseDouble(benchField.getText().trim());
            double press    = Double.parseDouble(pressField.getText().trim());

            String assistance = (String) assistanceBox.getSelectedItem();
            CyclePhase phase  = (CyclePhase) cycleBox.getSelectedItem();
            WorkoutCalculator calc = new WorkoutCalculator(squat, deadlift, bench, press);

            updateLift(squatPanel,    calc, calc.squatTM(),    true,  assistance, phase);
            updateLift(deadliftPanel, calc, calc.deadliftTM(), true,  assistance, phase);
            updateLift(benchPanel,    calc, calc.benchTM(),    false, assistance, phase);
            updateLift(pressPanel,    calc, calc.pressTM(),    false, assistance, phase);

            hideInputPanel();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "1RM 값을 숫자로 모두 입력하세요.",
                    "입력 오류",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateLift(LiftPanel panel, WorkoutCalculator calc, double baseTM,
                            boolean lowerBody, String assistance, CyclePhase phase) {
        double tm = calc.tmForPhase(baseTM, lowerBody, phase);
        double[] warmup = calc.warmupSets(tm);

        double[][] weekSets = new double[3][];
        if (phase.isDeload()) {
            weekSets[0] = calc.deloadSets(tm);
            weekSets[1] = new double[]{0, 0, 0};
            weekSets[2] = new double[]{0, 0, 0};
        } else {
            weekSets[0] = calc.weekSets(tm, 1);
            weekSets[1] = calc.weekSets(tm, 2);
            weekSets[2] = calc.weekSets(tm, 3);
        }

        double assistWeight = 0;
        if (!phase.isDeload() && assistance != null) {
            if (assistance.equals("FSL"))      assistWeight = calc.fsl(tm);
            else if (assistance.equals("SSL")) assistWeight = weekSets[0][1];
            else if (assistance.equals("BBB")) assistWeight = calc.bbb(tm);
        }
        panel.update(tm, warmup, weekSets, phase, assistance, assistWeight);
    }

    private void onReset() {
        squatField.setText("");
        deadliftField.setText("");
        benchField.setText("");
        pressField.setText("");
        assistanceBox.setSelectedIndex(0);
        cycleBox.setSelectedIndex(0);
        squatPanel.clear();
        deadliftPanel.clear();
        benchPanel.clear();
        pressPanel.clear();
        showInputPanel();
    }

    private void onSave() {
        try {
            WorkoutStorage.State state = new WorkoutStorage.State();
            state.squat      = parseOrZero(squatField.getText());
            state.deadlift   = parseOrZero(deadliftField.getText());
            state.bench      = parseOrZero(benchField.getText());
            state.press      = parseOrZero(pressField.getText());
            state.assistance = (String) assistanceBox.getSelectedItem();
            state.phase      = ((CyclePhase) cycleBox.getSelectedItem()).name();

            WorkoutStorage.save(state, WorkoutStorage.defaultFile());
            JOptionPane.showMessageDialog(this,
                    "저장되었습니다.\n" + WorkoutStorage.defaultFile().getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "저장 실패: " + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLoad() {
        File file = WorkoutStorage.defaultFile();
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "저장된 데이터가 없습니다.");
            return;
        }
        try {
            WorkoutStorage.State state = WorkoutStorage.load(file);
            applyState(state);
            JOptionPane.showMessageDialog(this, "불러왔습니다.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "불러오기 실패: " + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void autoLoad() {
        File file = WorkoutStorage.defaultFile();
        if (!file.exists()) return;
        try {
            WorkoutStorage.State state = WorkoutStorage.load(file);
            applyState(state);
        } catch (IOException ignored) {
        }
    }

    private void applyState(WorkoutStorage.State state) {
        squatField.setText(formatLoaded(state.squat));
        deadliftField.setText(formatLoaded(state.deadlift));
        benchField.setText(formatLoaded(state.bench));
        pressField.setText(formatLoaded(state.press));
        assistanceBox.setSelectedItem(state.assistance);
        try {
            cycleBox.setSelectedItem(CyclePhase.valueOf(state.phase));
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static String formatLoaded(double v) {
        if (v == 0) return "";
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private static double parseOrZero(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }
}
