package net.ME1312.Galaxi.Engine.Standalone;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

public class AboutWindow extends JDialog {
    private JPanel window;
    private JLabel appIcon;
    private JLabel appName;
    private JLabel appVersion;
    private JTextArea appAuthors;
    private JTextArea appDescription;
    private JLabel appWebsite;
    private JButton close;

    public AboutWindow(final JFrame parent, final PluginInfo app, final double scale) {
        super(parent, "About " + app.getDisplayName());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize((int) (600 * scale), (int) (300 * scale));
        setResizable(false);
        setContentPane(window);
        setModal(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (parent != null) setLocation(
                Math.abs(parent.getX() + ((parent.getWidth() - getWidth()) / 2)),
                Math.abs(parent.getY() + ((parent.getHeight() - getHeight()) / 2))
        ); else setLocation(
                (int) Math.abs((screen.getWidth() - getWidth()) / 2),
                (int) Math.abs((screen.getHeight() - getHeight()) / 2)
        );

        appIcon.setIcon(new ImageIcon(((app.getIcon() != null)?app:Galaxi.getInstance().getEngineInfo()).getIcon()
                .getScaledInstance((int) (100 * scale), (int) (100 * scale), Image.SCALE_SMOOTH)));

        appName.setText(app.getDisplayName());
        appName.setFont(appVersion.getFont().deriveFont((float) (36 * scale)));

        String versionText = app.getVersion().toFullExtendedString();
        if (app.getBuild() != null) versionText += " (" + app.getBuild().toString() + ')';
        if (app.getState() != null) versionText += " [" + app.getState() + "]";
        appVersion.setText(versionText.substring(0, 1).toUpperCase() + versionText.substring(1));
        appVersion.setFont(appVersion.getFont().deriveFont((float) (12 * scale)));

        String authorsText = "";
        int i = 0;
        for (String author : app.getAuthors()) {
            i++;
            if (i > 1) {
                if (app.getAuthors().size() > 2) authorsText += ", ";
                else if (app.getAuthors().size() == 2) authorsText += ' ';
                if (i == app.getAuthors().size()) authorsText += "and ";
            }
            authorsText += author;
        }
        appAuthors.setOpaque(false);
        appAuthors.setMargin(new Insets(0, 0, 0, 0));
        appAuthors.setText("By " + authorsText);
        appAuthors.setFont(appVersion.getFont().deriveFont((float) (12 * scale)));

        if (app.getDescription() != null) {
            appDescription.setOpaque(false);
            appDescription.setMargin(new Insets(0, 0, 0, 0));
            appDescription.setText(app.getDescription());
            appDescription.setFont(appVersion.getFont().deriveFont((float) (12 * scale)));
        } else appDescription.setVisible(false);

        if (app.getWebsite() != null) {
            appWebsite.setText(app.getWebsite().toString());
            appWebsite.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            appWebsite.setFont(appVersion.getFont().deriveFont((float) (12 * scale)));
            appWebsite.setForeground(new Color(0, 0, 238));
            appWebsite.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(app.getWebsite().toURI());
                    } catch (Exception ex) {
                        Galaxi.getInstance().getAppInfo().getLogger().error.println(ex);
                    }
                }

                @SuppressWarnings("unchecked")
                @Override
                public void mouseEntered(MouseEvent e) {
                    Font font = appWebsite.getFont();
                    Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>) font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    appWebsite.setFont(font.deriveFont(attributes));
                }

                @SuppressWarnings("unchecked")
                @Override
                public void mouseExited(MouseEvent e) {
                    Font font = appWebsite.getFont();
                    Map<TextAttribute, Object> attributes = (Map<TextAttribute, Object>) font.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, null);
                    appWebsite.setFont(font.deriveFont(attributes));
                }
            });
        } else appWebsite.setVisible(false);

        close.setDefaultCapable(false);
        close.setFont(close.getFont().deriveFont((float) (12 * scale)));
        close.setText("\u00A0Close\u00A0");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    public void open() {
        setVisible(true);
        toFront();
    }
}
