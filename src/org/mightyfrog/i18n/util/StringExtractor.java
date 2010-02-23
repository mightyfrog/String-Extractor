package org.mightyfrog.i18n.util;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

/**
 *
 *
 */
public class StringExtractor extends JFrame {
    //
    private static final Pattern SINGLE_Q_PTRN =
        Pattern.compile("'([^\'|\n]*)'");
    private static final Pattern DOUBLE_Q_PTRN =
        Pattern.compile("\"([^\"]*)\"");

    //
    private JMenu fileMenu = null;
    private JMenuItem openMI = null;
    private JMenuItem clearMI = null;

    //
    private JMenu optionMenu = null;
    private JMenu quoteMenu = null;
    private JCheckBoxMenuItem singleQMI = null;
    private JCheckBoxMenuItem doubleQMI = null;
    private JMenuItem aboutMI = null;

    //
    private final JTextArea TA = new JTextArea() {
            //
            private final String STR = I18N.get("message.0");

            {
                setEditable(false);
                setTransferHandler(new DataTransferHandler());
            }

            /** */
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (getText().length() == 0) {
                    int w = getWidth();
                    int h = getHeight();
                    int sw = g.getFontMetrics().stringWidth(STR);
                    g.setColor(SystemColor.textInactiveText);
                    g.drawString(STR, (w - sw) /2, h / 2);
                }
            }
        };

    /**
     *
     */
    public StringExtractor() {
        setTitle(I18N.get("frame.title"));
        setIconImage(new ImageIcon(StringExtractor.class.
                                   getResource("icon.png")).getImage());

        setJMenuBar(createMenuBar());
        add(new JScrollPane(TA));

        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     *
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ClassNotFoundException
            // InstantiationException
            // IllegalAccessException
            // javax.swing.UnsupportedLookAndFeelException
        }
        EventQueue.invokeLater(new Runnable() {
                /** */
                @Override
                public void run() {
                    new StringExtractor();
                }
            });
    }

    //
    //
    //

    /**
     *
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createFileMenu());
        menuBar.add(createOptionMenu());

        return menuBar;
    }

    /**
     *
     */
    private JMenu createFileMenu() {
        this.fileMenu = new JMenu(I18N.get("menu.file"));
        this.openMI = new JMenuItem(I18N.get("menuitem.open.file"));
        this.clearMI = new JMenuItem(I18N.get("menuitem.clear"));

        this.openMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    JFileChooser fc = new JFileChooser();
                    fc.showOpenDialog(StringExtractor.this);
                    File file = fc.getSelectedFile();
                    if (file != null) {
                        extractString(file);
                    }
                }
            });

        this.clearMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    TA.setText(null);
                }
            });

        this.fileMenu.add(this.openMI);
        this.fileMenu.addSeparator();
        this.fileMenu.add(this.clearMI);

        return this.fileMenu;
    }

    /**
     *
     */
    private JMenu createOptionMenu() {
        this.optionMenu = new JMenu(I18N.get("menu.options"));
        this.quoteMenu = new JMenu(I18N.get("menu.quote.style"));
        this.singleQMI = new JCheckBoxMenuItem(I18N.get("menuitem.single"));
        this.doubleQMI = new JCheckBoxMenuItem(I18N.get("menuitem.double"));
        this.aboutMI = new JMenuItem(I18N.get("menuitem.about"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(this.singleQMI);
        bg.add(this.doubleQMI);
        this.doubleQMI.setSelected(true);

        this.aboutMI.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    showAboutDialog();
                }
            });

        this.optionMenu.add(this.quoteMenu);
        this.quoteMenu.add(this.singleQMI);
        this.quoteMenu.add(this.doubleQMI);
        this.optionMenu.addSeparator();
        this.optionMenu.add(this.aboutMI);

        return this.optionMenu;
    }

    /**
     *
     */
    private Pattern getPattern() {
        return this.doubleQMI.isSelected() ? DOUBLE_Q_PTRN : SINGLE_Q_PTRN;
    }

    /**
     *
     */
    private void extractString(File file) {
        TA.append(file.getPath() + "\n");
        LineNumberReader lnr = null;
        try {
            lnr = new LineNumberReader(new FileReader(file));
            String line = null;
            while ((line = lnr.readLine()) != null) {
                Matcher m = getPattern().matcher(line);
                while (m.find()) {
                    String s = line.substring(m.start(), m.end());
                    TA.append("Line " + lnr.getLineNumber() + ": " + s + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (lnr != null) {
                try {
                    lnr.close();
                } catch (IOException e) {
                }
            }
        }
        TA.append("\n");
    }

    /**
     *
     */
    private final DataFlavor createURIListFlavor() {
        DataFlavor df = null;
        try {
            df = new DataFlavor("text/uri-list;class=java.lang.String");
        } catch (ClassNotFoundException e) {
            // shouldn't happen
        }

        return df;
    }

    /**
     *
     * @param uriList
     */
    private final List<File> textURIListToFileList(String uriList) {
        List<File> list = new ArrayList<File>(1);
        StringTokenizer st = new StringTokenizer(uriList, "\r\n");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.startsWith("#")) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                URI uri = new URI(s);
                File file = new File(uri);
                if (file.length() != 0) {
                    list.add(file);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }

        return list;
    }

    /**
     *
     */
    private void showAboutDialog() {
        String version = I18N.get("dialog.0",
                                  "Shigehiro Soejima",
                                  "mightyfrog.gc@gmail.com",
                                  "@TIMESTAMP@");
        JOptionPane.showMessageDialog(this, version);
    }

    //
    //
    //

    /**
     *
     */
    private class DataTransferHandler extends TransferHandler {
        /** */
        @Override
        public boolean canImport(JComponent comp,
                                 DataFlavor[] transferFlavors) {
            return true;
        }

        /** */
        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(JComponent comp, Transferable t) {
            try {
                List<File> list = null;
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    list = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } else {
                    String data =
                        (String) t.getTransferData(createURIListFlavor());
                    list = textURIListToFileList(data);
                }
                Collections.sort(list);
                for (int i = 0; i < list.size(); i++) {
                    extractString(list.get(i));
                }
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

            return true;
        }
    }
}
