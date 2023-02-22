package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlatformInfoWindow extends JDialog {
    private JPanel window;
    private JLabel icon;
    private JLabel name;
    private JLabel tagLine;
    private JLabel infoLabel;
    private JTextArea info;
    private JButton close;
    private JButton copy;
    private JLabel copied;

    public PlatformInfoWindow(final JFrame parent, PluginInfo app, final double scale) {
        super(parent, "Platform Information");
        PluginInfo engine = Engine.getInstance().getEngineInfo();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize((int) (600 * scale), (int) (300 * scale));
        setResizable(false);
        setContentPane(window);
        setModal(true);

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screen = toolkit.getScreenSize();
        if (parent != null) setLocation(
                Math.abs(parent.getX() + ((parent.getWidth() - getWidth()) / 2)),
                Math.abs(parent.getY() + ((parent.getHeight() - getHeight()) / 2))
        ); else setLocation(
                (int) Math.abs((screen.getWidth() - getWidth()) / 2),
                (int) Math.abs((screen.getHeight() - getHeight()) / 2)
        );

        icon.setIcon(new ImageIcon(engine.getIcon()
                .getScaledInstance((int) (100 * scale), (int) (100 * scale), Image.SCALE_SMOOTH)));

        name.setText(engine.getDisplayName());
        name.setFont(tagLine.getFont().deriveFont((float) (36 * scale)));

        tagLine.setText(engine.getDescription());
        tagLine.setFont(tagLine.getFont().deriveFont((float) (12 * scale)));

        StringBuilder infoText = new StringBuilder();
        for (String item : app.getPlatformStack()) {
            infoText.append(item);
            if (item.endsWith(",")) infoText.append('\n');
        }

        info.setOpaque(false);
        info.setMargin(new Insets(0, 0, 0, 0));
        info.setText(infoText.toString());
        info.setFont(tagLine.getFont().deriveFont((float) (12 * scale)));
        infoLabel.setFont(tagLine.getFont().deriveFont((float) (18 * scale)));

        close.setDefaultCapable(false);
        close.setFont(close.getFont().deriveFont((float) (12 * scale)));
        close.setText("\u00A0Close\u00A0");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        final StringSelection clipboard = new StringSelection(infoText.toString());
        final Container<Timer> timer = new Container<>();
        copy.setDefaultCapable(false);
        copy.setFont(copy.getFont().deriveFont((float) (12 * scale)));
        copy.setText("\u00A0Copy\u00A0");
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (timer.value != null) timer.value.stop();
                toolkit.getSystemClipboard().setContents(clipboard, clipboard);
                copied.setVisible(true);
                (timer.value = new Timer(2500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        copied.setVisible(false);
                        timer.value.stop();
                    }
                })).start();

            }
        });

        copied.setVisible(false);
        copied.setFont(copied.getFont().deriveFont((float) (12 * scale)));
    }

    public void open() {
        setVisible(true);
        toFront();
    }
}
