package net.ME1312.Galaxi.Engine.Standalone;

import net.ME1312.Galaxi.Engine.Library.Log.HTMLogger;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.Command.ConsoleCommandSender;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static net.ME1312.Galaxi.Engine.GalaxiOption.MAX_CONSOLE_WINDOW_SCROLLBACK;

public final class ConsoleWindow extends OutputStream {
    private static final int MAX_SCROLLBACK = (Util.getDespiteException(new Util.ExceptionReturnRunnable<Integer>() {
        @Override
        public Integer run() throws Throwable {
            return Integer.parseInt(MAX_CONSOLE_WINDOW_SCROLLBACK.usr());
        }
    }, 0) > 0)?Integer.parseInt(MAX_CONSOLE_WINDOW_SCROLLBACK.usr()):MAX_CONSOLE_WINDOW_SCROLLBACK.get();
    private static final String RESET_VALUE = "\n\u00A0\n\u00A0";
    private Class<?> READER;
    private Object reader;
    private JFrame window;
    private JPanel panel;
    private JTextField input;
    private boolean ifocus = false;
    private Boolean iauto = false;
    private int iautopos = 0;
    private NamedContainer<Integer, List<CharSequence>> icache = null;
    private TextFieldPopup popup;
    private JTextPane log;
    private JScrollPane vScroll;
    private JScrollBar hScroll;
    private List<Integer> eScroll = new ArrayList<Integer>();
    private JPanel find;
    private JTextField findT;
    private JButton findN;
    private JButton findP;
    private JButton findD;
    private int findO = 0;
    private int findI = 0;
    private boolean open = false;
    private boolean first = true;
    private int fontSize = 12;
    ByteArrayOutputStream scache = new ByteArrayOutputStream();
    private AnsiUIOutputStream stream = HTMLogger.wrap(new OutputStream() {

        private int countLines(String str) {
            int count = 0;
            for (int i = 0; i < str.length(); i++) if (str.charAt(i) == '\n') count++;
            return count;
        }

        @Override
        public void write(int b) throws IOException {
            scache.write(b);
            if (b == '\n') {
                try {
                    int lines;
                    String content;
                    while (log.getSelectionStart() == log.getSelectionEnd() && (lines = countLines(content = log.getDocument().getText(0, log.getDocument().getLength()))) > MAX_SCROLLBACK) {
                        int lineBreak = 1;
                        for (lines -= MAX_SCROLLBACK; lines > 0; lines--) lineBreak = content.indexOf('\n', lineBreak + 1);
                        if (lineBreak >= 2 && log.getSelectionStart() == log.getSelectionEnd()) {
                            log.getDocument().remove(2, lineBreak);
                        } else break;
                    }
                } catch (Exception e) {} try {
                    HTMLEditorKit kit = (HTMLEditorKit) log.getEditorKit();
                    HTMLDocument doc = (HTMLDocument) log.getDocument();
                    kit.insertHTML(doc, doc.getLength() - 2, new String(scache.toByteArray(), "UTF-8"), 0, 0, null);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            hScroll();
                        }
                    });
                } catch (Exception e) {}
                scache = new ByteArrayOutputStream();
            }
        }
    }, new HTMLogger.HTMConstructor<AnsiUIOutputStream>() {
        @Override
        public AnsiUIOutputStream construct(OutputStream raw, OutputStream wrapped) {
            return new AnsiUIOutputStream(raw, wrapped);
        }
    });
    private boolean[] kpressed = new boolean[65535];
    private KeyEventDispatcher keys = new KeyEventDispatcher() {
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            switch (event.getID()) {
                case KeyEvent.KEY_PRESSED:
                    kpressed[event.getKeyCode()] = true;
                    break;
                case KeyEvent.KEY_RELEASED:
                    kpressed[event.getKeyCode()] = false;
                    break;
            }
            if (window.isVisible() && window.isFocused()) {
                if (event.getID() == KeyEvent.KEY_PRESSED) switch (event.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (ifocus)
                            popup.prev(input);
                        break;
                    case KeyEvent.VK_DOWN:
                        if (ifocus)
                            popup.next(input);
                        break;
                    case KeyEvent.VK_ESCAPE:
                        if (find.isVisible()) {
                            find.setVisible(false);
                            findI = 0;
                            findO = 0;
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        if (find.isVisible() && !ifocus)
                            ConsoleWindow.this.find(kpressed[KeyEvent.VK_SHIFT] != Boolean.TRUE);
                        break;
                    case KeyEvent.VK_TAB:
                        if (!ifocus) {
                            input.requestFocusInWindow();
                        } else if (input.getText().length() > 0 && !input.getText().equals(">")) try {
                            int position = input.getCaretPosition();
                            List<CharSequence> candidates = new LinkedList<CharSequence>();
                            if (icache == null || iauto == Boolean.FALSE) {
                                READER.getMethod("complete", String.class, int.class, List.class).invoke(reader, (input.getText().startsWith(">"))?input.getText().substring(1):input.getText(), position, candidates);
                                icache = new NamedContainer<>(position, candidates);
                                iautopos = 0;
                            } else {
                                candidates = icache.get();
                                iautopos++;
                                if (iautopos >= candidates.size()) iautopos = 0;
                            }
                            if (candidates.size() > 0) {
                                iauto = true;
                                input.setText(candidates.get(iautopos).toString());
                            }
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    input.requestFocusInWindow();
                                }
                            });
                        } catch (Exception e) {
                            Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
                        }
                        break;
                }

            }
            return false;
        }
    };

    public ConsoleWindow(final Object reader, final boolean exit) {
        if (Util.getDespiteException(new Util.ExceptionReturnRunnable<Boolean>() {
            @Override
            public Boolean run() throws Throwable {
                return !(READER = Class.forName("net.ME1312.Galaxi.Engine.Library.ConsoleReader")).isInstance(reader);
            }
        }, true)) throw new ClassCastException(reader.getClass().getCanonicalName() + " is not a valid ConsoleReader");
        this.reader = reader;
        this.window = new JFrame();

        JMenuBar jMenu = new JMenuBar();
        JMenu menu = new JMenu("\u00A0Log\u00A0");
        JMenuItem item = new JMenuItem("Clear Screen");
        item.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ConsoleWindow.this.clear();
            }
        });
        menu.add(item);
        item = new JMenuItem("Reload Log");
        item.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                log.setText(RESET_VALUE);
                ConsoleWindow.this.loadContent();
            }
        });
        menu.add(item);
        jMenu.add(menu);

        menu = new JMenu("\u00A0Search\u00A0");
        item = new JMenuItem("Find");
        item.setAccelerator(KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (find.isVisible()) {
                    find.setVisible(false);
                    findI = 0;
                    findO = 0;
                } else {
                    find.setVisible(true);
                    findT.selectAll();
                    findT.requestFocusInWindow();
                }
            }
        });
        menu.add(item);
        item = new JMenuItem("Find Next");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (find.isVisible()) {
                    ConsoleWindow.this.find(true);
                } else {
                    find.setVisible(true);
                    findT.selectAll();
                    findT.requestFocusInWindow();
                }
            }
        });
        menu.add(item);
        item = new JMenuItem("Find Previous");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (find.isVisible()) {
                    ConsoleWindow.this.find(false);
                } else {
                    find.setVisible(true);
                    findT.selectAll();
                    findT.requestFocusInWindow();
                }
            }
        });
        menu.add(item);
        jMenu.add(menu);

        menu = new JMenu("\u00A0View\u00A0");
        item = new JMenuItem("Scroll to Top");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                vScroll.getVerticalScrollBar().setValue(0);
            }
        });
        menu.add(item);
        item = new JMenuItem("Scroll to Bottom");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                vScroll.getVerticalScrollBar().setValue(vScroll.getVerticalScrollBar().getMaximum() - vScroll.getVerticalScrollBar().getVisibleAmount());
            }
        });
        menu.add(item);
        menu.addSeparator();
        item = new JCheckBoxMenuItem("Use ANSI Formatting");
        item.setSelected(true);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                stream.ansi(((AbstractButton) event.getSource()).getModel().isSelected());
                log.setText(RESET_VALUE);
                ConsoleWindow.this.loadContent();
            }
        });
        menu.add(item);
        item = new JMenuItem("Reset Text Size");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                HTMLDocument doc = (HTMLDocument) log.getDocument();
                fontSize = 12;
                doc.getStyleSheet().addRule("body {font-size: " + fontSize + ";}\n");
                ConsoleWindow.this.hScroll();
            }
        });
        menu.add(item);
        item = new JMenuItem("Bigger Text");
        item.setAccelerator(KeyStroke.getKeyStroke('=', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                HTMLDocument doc = (HTMLDocument) log.getDocument();
                fontSize += 2;
                doc.getStyleSheet().addRule("body {font-size: " + fontSize + ";}\n");
                ConsoleWindow.this.hScroll();
            }
        });
        menu.add(item);
        item = new JMenuItem("Smaller Text");
        item.setAccelerator(KeyStroke.getKeyStroke('-', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                HTMLDocument doc = (HTMLDocument) log.getDocument();
                fontSize -= 2;
                doc.getStyleSheet().addRule("body {font-size: " + fontSize + ";}\n");
                ConsoleWindow.this.hScroll();
            }
        });
        menu.add(item);
        jMenu.add(menu);

        window.setJMenuBar(jMenu);
        window.setContentPane(panel);
        window.pack();
        Util.isException(new Util.ExceptionRunnable() {
            @Override
            public void run() throws Throwable {
                window.setIconImage(((Galaxi.getInstance().getAppInfo().getIcon() == null)?Galaxi.getInstance().getEngineInfo():Galaxi.getInstance().getAppInfo()).getIcon());
            }
        });
        window.setTitle(Galaxi.getInstance().getAppInfo().getDisplayName());
        window.setSize(1024, 576);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - window.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - window.getHeight()) / 2);
        window.setLocation(x, y);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(Galaxi.getInstance().getEngineInfo().getName() + "::AWT_Shutdown") {
                    @Override
                    public void run() {
                        try {
                            if (exit) {
                                Class.forName("net.ME1312.Galaxi.Engine.GalaxiEngine").getMethod("stop").invoke(Galaxi.getInstance());
                            } else {
                                Util.reflect(Class.forName("net.ME1312.Galaxi.Engine.Library.ConsoleReader").getDeclaredField("window"),
                                        Class.forName("net.ME1312.Galaxi.Engine.GalaxiEngine").getMethod("getConsoleReader").invoke(Galaxi.getInstance()), null);
                                close();
                            }
                        } catch (Exception ex) {
                            Galaxi.getInstance().getAppInfo().getLogger().error.println(ex);
                        }
                    }
                }.start();
            }
        });
        window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                hScroll();
            }
        });
        vScroll.setBorder(BorderFactory.createEmptyBorder());
        hScroll.setVisible(false);
        new SmartScroller(vScroll, SmartScroller.VERTICAL, SmartScroller.END);
        log.setContentType("text/html");
        log.setEditorKit(new HTMLEditorKit());
        StyleSheet style = new StyleSheet();
        String font;
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, ConsoleWindow.class.getResourceAsStream("/net/ME1312/Galaxi/Engine/Library/Files/GalaxiFont.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            font = f.getFontName();
            input.setFont(f.deriveFont(14f));
        } catch (Exception e) {
            font = "Courier";
        }
        style.addRule("body {color: #dcdcdc; font-family: " + font + "; font-size: 12;}\n");
        log.setDocument(new HTMLDocument(style));
        log.setBorder(BorderFactory.createLineBorder(new Color(40, 44, 45)));
        new TextFieldPopup(log, false);
        ((AbstractDocument) log.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                super.insertString(fb, offset, string, attr);
                hScroll();
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, text, attrs);
                hScroll();
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                hScroll();
            }
        });
        log.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                    try {
                        switch (e.getURL().getProtocol().toLowerCase()) {
                            case "galaxi.execute":
                                e.getURL().openConnection().connect();
                                break;
                            case "file":
                                Desktop.getDesktop().open(new File(e.getURL().toURI()));
                                break;
                            case "mailto":
                                Desktop.getDesktop().mail(e.getURL().toURI());
                                break;
                            default:
                                Desktop.getDesktop().browse(e.getURL().toURI());
                                break;
                        }
                    } catch (Exception ex) {
                        Galaxi.getInstance().getAppInfo().getLogger().error.println(ex);
                    }
                }
            }
        });


        popup = new TextFieldPopup(input, true);
        input.setBorder(BorderFactory.createLineBorder(new Color(40, 44, 45), 4));
        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (input.getText().length() > 0 && !input.getText().equals(">")) {
                    final String command = (input.getText().startsWith(">"))?input.getText().substring(1):input.getText();
                    new Thread(Galaxi.getInstance().getEngineInfo().getName() + "::AWT_Command") {
                        @Override
                        public void run() {
                            try {
                                ConsoleCommandSender.get().command(command);
                            } catch (Exception e) {
                                Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
                            }
                        }
                    }.start();
                    popup.commands.add(command);
                    input.setText("");
                }
            }
        });
        ((AbstractDocument) input.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (offset < 1) {
                    return;
                }
                super.insertString(fb, offset, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (offset < 1) {
                    length = Math.max(0, length - 1);
                    offset = input.getDocument().getLength();
                    input.setCaretPosition(offset);
                }

                if (iauto == Boolean.TRUE) {
                    iauto = null;
                } else if (iauto == null) {
                    iauto = false;
                }
                if (popup.history == Boolean.TRUE) {
                    popup.history = null;
                } else if (popup.history == null) {
                    popup.history = false;
                }

                try {
                    super.replace(fb, offset, length, text, attrs);
                } catch (BadLocationException e) {
                    super.replace(fb, 1, length, text, attrs);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (offset < 1) {
                    length = Math.max(0, length + offset - 1);
                    offset = 1;
                }
                if (length > 0) {
                    super.remove(fb, offset, length);
                }
            }
        });
        input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                ifocus = true;
            }

            @Override
            public void focusLost(FocusEvent e) {
                ifocus = false;
            }
        });

        vScroll.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent event) {
                if (!eScroll.contains(event.getValue())) {
                    eScroll.add(event.getValue());
                    hScroll.setValue(event.getValue());
                } else {
                    eScroll.remove((Object) event.getValue());
                }
            }
        });
        hScroll.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent event) {
                if (!eScroll.contains(event.getValue())) {
                    eScroll.add(event.getValue());
                    vScroll.getHorizontalScrollBar().setValue(event.getValue());
                } else {
                    eScroll.remove((Object) event.getValue());
                }
            }
        });

        new TextFieldPopup(findT, false);
        findT.setBorder(BorderFactory.createLineBorder(new Color(40, 44, 45), 4));
        ((AbstractDocument) findT.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                super.insertString(fb, offset, string, attr);
                findI = 0;
                findO = 0;
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, text, attrs);
                findI = 0;
                findO = 0;
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                findI = 0;
                findO = 0;
            }
        });
        findP.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (findP.getModel().isPressed()) findP.setBackground(new Color(40, 44, 45));
                else findP.setBackground(new Color(69, 73, 74));
            }
        });
        findP.setBorder(new ButtonBorder(40, 44, 45, 4));
        findP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ConsoleWindow.this.find(false);
            }
        });
        findN.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (findN.getModel().isPressed()) findN.setBackground(new Color(40, 44, 45));
                else findN.setBackground(new Color(69, 73, 74));
            }
        });
        findN.setBorder(new ButtonBorder(40, 44, 45, 4));
        findN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ConsoleWindow.this.find(true);
            }
        });
        findD.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (findD.getModel().isPressed()) findD.setBackground(new Color(40, 44, 45));
                else findD.setBackground(new Color(69, 73, 74));
            }
        });
        findD.setBorder(new ButtonBorder(40, 44, 45, 4));
        findD.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                find.setVisible(false);
                findI = 0;
                findO = 0;
            }
        });

        log.setText(RESET_VALUE);
        loadContent();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keys);
        open();
    }

    private void open() {
        if (!open) {
            window.setVisible(true);
            this.open = true;
        }
        window.toFront();
    }

    private void loadContent() {
        try (FileInputStream reader = new FileInputStream((File) Class.forName("net.ME1312.Galaxi.Engine.Library.Log.FileLogger").getMethod("getFile").invoke(null))) {
            int b;
            while ((b = reader.read()) != -1) {
                write(b);
            }
        } catch (Exception e) {
            Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
        }
        hScroll();
    }

    @Override
    public void write(int b) {
        try {
            if (first) {
                first = false;
                stream.write("\u00A0".getBytes("UTF-8"));
            }

            if (b == '\n') stream.write("\u00A0".getBytes("UTF-8"));
            stream.write(b);
            if (b == '\n') stream.write("\u00A0".getBytes("UTF-8"));
        } catch (IOException e) {
            Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
        }
    }

    private void clear() {
        log.setText(RESET_VALUE);
        hScroll();
    }

    @Override
    public void close() {
        if (open) {
            this.open = false;
            if (find.isVisible()) {
                find.setVisible(false);
                findI = 0;
                findO = 0;
            }
            window.setVisible(false);
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keys);
    }

    private void hScroll() {
        hScroll.setMaximum(vScroll.getHorizontalScrollBar().getMaximum());
        hScroll.setMinimum(vScroll.getHorizontalScrollBar().getMinimum());
        hScroll.setVisibleAmount(vScroll.getHorizontalScrollBar().getVisibleAmount());
        hScroll.setVisible(input.isVisible() && hScroll.getVisibleAmount() < hScroll.getMaximum());
    }

    private void find(boolean direction) {
        if (!direction) findI -= findO + 1;
        String find = findT.getText().toLowerCase();
        log.requestFocusInWindow();

        if (find.length() > 0) {
            Document document = log.getDocument();
            int findLength = find.length();
            try {
                boolean found = false;

                if (findI < 0 || findI + findLength >= document.getLength()) {
                    if (direction) {
                        findI = 1;
                    } else {
                        findI = document.getLength() - findLength;
                    }
                }

                while (findLength <= document.getLength()) {
                    String match = document.getText(findI, findLength).toLowerCase();
                    if (match.equals(find)) {
                        found = true;
                        break;
                    }
                    if (direction) findI++;
                    else findI--;
                }

                if (found) {
                    Rectangle viewRect = log.modelToView(findI);
                    log.scrollRectToVisible(viewRect);

                    log.setCaretPosition(findI + findLength);
                    log.moveCaretPosition(findI);

                    findI += findLength;
                    findO = findLength;
                }

            } catch (BadLocationException e) {
                findI = -2;
                JOptionPane.showMessageDialog(window,
                        ((findO > 0)?"There are no more results\nSearch again to start from the " + ((direction)?"top":"bottom"):"Couldn't find \"" + findT.getText() + "\""),
                        "Find",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private class TextFieldPopup extends JPanel {
        LinkedList<String> commands = new LinkedList<String>();
        Boolean history = false;
        int hpos = -1;
        String hcache = "";

        public TextFieldPopup(JTextComponent field, boolean command) {
            JPopupMenu menu = new JPopupMenu();

            if (field.isEditable()) {
                if (command) {
                    Action backward = new TextAction("Previous Command") {
                        public void actionPerformed(ActionEvent e) {
                            prev(getFocusedComponent());
                        }
                    };
                    menu.add(backward);

                    Action forward = new TextAction("Next Command") {
                        public void actionPerformed(ActionEvent e) {
                            next(getFocusedComponent());
                        }
                    };
                    menu.add(forward);
                    menu.addSeparator();
                }

                Action cut = new DefaultEditorKit.CutAction();
                cut.putValue(Action.NAME, "Cut");
                cut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
                menu.add(cut);
            }

            Action copy = new DefaultEditorKit.CopyAction();
            copy.putValue(Action.NAME, "Copy");
            copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
            menu.add(copy);

            if (field.isEditable()) {
                Action paste = new DefaultEditorKit.PasteAction();
                paste.putValue(Action.NAME, "Paste");
                paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
                menu.add(paste);
            }

            Action find = new TextAction("Find Selection") {
                public void actionPerformed(ActionEvent e) {
                    JTextComponent field = getFocusedComponent();
                    if (field.getSelectedText() != null && field.getSelectedText().length() > 0) {
                        findT.setText(field.getSelectedText());
                        findI = 0;
                        findO = 0;
                        ConsoleWindow.this.find.setVisible(true);
                        find(true);
                    }
                }
            };
            find.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_MASK, true));
            menu.add(find);

            Action selectAll = new TextAction("Select All") {
                public void actionPerformed(ActionEvent e) {
                    JTextComponent field = getFocusedComponent();
                    field.selectAll();
                    field.requestFocusInWindow();
                }
            };
            selectAll.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true));
            menu.add(selectAll);

            field.setComponentPopupMenu(menu);
        }

        public void next(JTextComponent field) {
            if (field.isEditable()) {
                LinkedList<String> list = new LinkedList<String>(commands);
                Collections.reverse(list);
                if (history == Boolean.FALSE) {
                    hcache = (field.getText().startsWith(">"))?field.getText().substring(1):field.getText();
                    hpos = -1;
                } else {
                    hpos--;
                    if (hpos < -1) hpos = -1;
                }
                if (hpos >= 0) {
                    history = true;
                    field.setText(list.get(hpos));
                } else field.setText(hcache);
                field.setCaretPosition(field.getText().length());
            }
        }

        public void prev(JTextComponent field) {
            if (field.isEditable()) {
                LinkedList<String> list = new LinkedList<String>(commands);
                Collections.reverse(list);
                if (history == Boolean.FALSE) {
                    hcache = (field.getText().startsWith(">"))?field.getText().substring(1):field.getText();
                    hpos = 0;
                } else {
                    hpos++;
                }
                if (hpos >= list.size()) hpos = list.size() - 1;
                if (hpos >= 0) {
                    history = true;
                    field.setText(list.get(hpos));
                }
            }
        }
    }
    private class AnsiUIOutputStream extends HTMLogger {
        public AnsiUIOutputStream(OutputStream raw, OutputStream wrapped) {
            super(raw, wrapped);
        }

        public void ansi(boolean value) {
            ansi = value;
        }

        @Override
        protected void processChangeWindowTitle(String label) {
            window.setTitle(Galaxi.getInstance().getAppInfo().getDisplayName() + ((label.length() <= 0)?"":" \u2014 " + label));
        }

        @Override
        protected void processEraseLine(int mode) throws IOException {
            processDeleteLine(1);
        }

        @Override
        protected void processEraseScreen(int mode) throws IOException {
            if (ansi) log.setText(RESET_VALUE);
        }

        @Override
        protected void processDeleteLine(int amount) throws IOException {
            if (ansi) try {
                String content = log.getDocument().getText(0, log.getDocument().getLength());
                while (amount > 0) {
                    int lastLineBreak = content.lastIndexOf('\n');
                    int length = log.getDocument().getLength() - lastLineBreak - 2;
                    if (lastLineBreak >= 0 && length > 0) {
                        log.getDocument().remove(lastLineBreak, length);
                    }
                    amount--;
                }
            } catch (Exception e) {}
        }

        @Override
        public void close() throws IOException {
            open = false;
            super.close();
        }
    }
    private class SmartScroller implements AdjustmentListener {
        private final static int HORIZONTAL = 0;
        private final static int VERTICAL = 1;

        private final static int START = 0;
        private final static int END = 1;

        private int viewportPosition;

        private JScrollBar scrollBar;
        private boolean adjustScrollBar = true;

        private int previousValue = -1;
        private int previousMaximum = -1;

        public SmartScroller(JScrollPane scrollPane)
        {
            this(scrollPane, VERTICAL, END);
        }

        public SmartScroller(JScrollPane scrollPane, int viewportPosition)
        {
            this(scrollPane, VERTICAL, viewportPosition);
        }

        public SmartScroller(JScrollPane scrollPane, int scrollDirection, int viewportPosition)
        {
            if (scrollDirection != HORIZONTAL
                    &&  scrollDirection != VERTICAL)
                throw new IllegalArgumentException("invalid vScroll direction specified");

            if (viewportPosition != START
                    &&  viewportPosition != END)
                throw new IllegalArgumentException("invalid viewport position specified");

            this.viewportPosition = viewportPosition;

            if (scrollDirection == HORIZONTAL)
                scrollBar = scrollPane.getHorizontalScrollBar();
            else
                scrollBar = scrollPane.getVerticalScrollBar();

            scrollBar.addAdjustmentListener( this );

            //  Turn off automatic scrolling for text components

            Component view = scrollPane.getViewport().getView();

            if (view instanceof JTextComponent)
            {
                JTextComponent textComponent = (JTextComponent)view;
                DefaultCaret caret = (DefaultCaret)textComponent.getCaret();
                caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            }
        }

        @Override
        public void adjustmentValueChanged(final AdjustmentEvent e)
        {
            checkScrollBar(e);
        }

        /*
         *  Analyze every adjustment event to determine when the viewport
         *  needs to be repositioned.
         */
        private void checkScrollBar(AdjustmentEvent e)
        {
            //  The vScroll bar listModel contains information needed to determine
            //  whether the viewport should be repositioned or not.

            JScrollBar scrollBar = (JScrollBar)e.getSource();
            BoundedRangeModel listModel = scrollBar.getModel();
            int value = listModel.getValue();
            int extent = listModel.getExtent();
            int maximum = listModel.getMaximum();

            boolean valueChanged = previousValue != value;
            boolean maximumChanged = previousMaximum != maximum;

            //  Check if the user has manually repositioned the scrollbar

            if (valueChanged && !maximumChanged)
            {
                if (viewportPosition == START)
                    adjustScrollBar = value != 0;
                else
                    adjustScrollBar = value + extent >= maximum;
            }

            //  Reset the "value" so we can reposition the viewport and
            //  distinguish between a user vScroll and a program vScroll.
            //  (ie. valueChanged will be false on a program vScroll)

            if (adjustScrollBar && viewportPosition == END)
            {
                //  Scroll the viewport to the end.
                scrollBar.removeAdjustmentListener( this );
                value = maximum - extent;
                scrollBar.setValue( value );
                scrollBar.addAdjustmentListener( this );
            }

            if (adjustScrollBar && viewportPosition == START)
            {
                //  Keep the viewport at the same relative viewportPosition
                scrollBar.removeAdjustmentListener( this );
                value = value + maximum - previousMaximum;
                scrollBar.setValue( value );
                scrollBar.addAdjustmentListener( this );
            }

            previousValue = value;
            previousMaximum = maximum;
        }
    }
    private class ButtonBorder implements Border {
        private int radius;
        private Color color;

        public ButtonBorder(int red, int green, int blue, int radius) {
            this.color = new Color(red, green, blue);
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(color);
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }
}
