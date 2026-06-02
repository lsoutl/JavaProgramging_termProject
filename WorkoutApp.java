import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class WorkoutApp {
    public static void main(String[] args) {
        applyNimbusDark();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WorkoutFrame();
            }
        });
    }

    private static void applyNimbusDark() {
        UIManager.put("control",                   new Color( 28,  30,  36));
        UIManager.put("info",                      new Color( 38,  42,  50));
        UIManager.put("nimbusBase",                new Color( 18,  22,  32));
        UIManager.put("nimbusAlertYellow",         new Color(248, 187,   0));
        UIManager.put("nimbusDisabledText",        new Color(110, 116, 128));
        UIManager.put("nimbusFocus",               new Color(255, 107,  53));
        UIManager.put("nimbusGreen",               new Color( 74, 222, 128));
        UIManager.put("nimbusInfoBlue",            new Color( 96, 165, 250));
        UIManager.put("nimbusLightBackground",     new Color( 36,  39,  46));
        UIManager.put("nimbusOrange",              new Color(255, 107,  53));
        UIManager.put("nimbusRed",                 new Color(239,  68,  68));
        UIManager.put("nimbusSelectedText",        new Color(255, 255, 255));
        UIManager.put("nimbusSelectionBackground", new Color(255, 107,  53));
        UIManager.put("text",                      new Color(240, 242, 246));

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }
}
