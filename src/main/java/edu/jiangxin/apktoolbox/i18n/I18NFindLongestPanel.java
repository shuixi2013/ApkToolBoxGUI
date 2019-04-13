package edu.jiangxin.apktoolbox.i18n;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.jiangxin.apktoolbox.swing.extend.JEasyPanel;
import edu.jiangxin.apktoolbox.utils.Constants;
import edu.jiangxin.apktoolbox.utils.Utils;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class I18NFindLongestPanel extends JEasyPanel {
    private static final long serialVersionUID = 1L;
    
    private static final int PANEL_WIDTH = Constants.DEFAULT_WIDTH - 50;

    private static final int PANEL_HIGHT = 110;
    
    private static final int CHILD_PANEL_HIGHT = 30;
    
    private static final int CHILD_PANEL_LEFT_WIDTH = 600;
    
    private static final int CHILD_PANEL_RIGHT_WIDTH = 130;

    List<I18NInfo> infos = new ArrayList<I18NInfo>();

    private JPanel sourcePanel;

    private JTextField srcTextField;

    private JButton srcButton;

    private JPanel itemPanel;

    private JTextField itemTextField;

    private JLabel itemLabel;

    private JPanel operationPanel;

    private JButton findButton;

    public I18NFindLongestPanel() throws HeadlessException {
        super();
        Utils.setJComponentSize(this, PANEL_WIDTH, PANEL_HIGHT);
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);

        createSourcePanel();
        add(sourcePanel);
        
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createItemPanel();
        add(itemPanel);
        
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createOperationPanel();
        add(operationPanel);
    }

    private void createOperationPanel() {
        operationPanel = new JPanel();
        Utils.setJComponentSize(operationPanel, PANEL_WIDTH, CHILD_PANEL_HIGHT);
        operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.X_AXIS));
        
        findButton = new JButton(bundle.getString("i18n.longest.find"));
        Utils.setJComponentSize(findButton, CHILD_PANEL_RIGHT_WIDTH, CHILD_PANEL_HIGHT);
        findButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                infos.clear();
                File srcFile = new File(srcTextField.getText());
                if (!srcFile.exists() || !srcFile.isDirectory()) {
                    logger.error("srcFile is invalid");
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18NFindLongestPanel.this, "Source directory is invalid", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    srcTextField.requestFocus();
                    return;
                }
                String srcPath;
                try {
                    srcPath = srcFile.getCanonicalPath();
                } catch (IOException e2) {
                    logger.error("getCanonicalPath fail");
                    return;
                }
                conf.setProperty("i18n.longest.src.dir", srcPath);

                String item = itemTextField.getText();
                if (StringUtils.isEmpty(item)) {
                    logger.error("item is empty");
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18NFindLongestPanel.this, "item is empty", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    itemTextField.requestFocus();
                    return;
                }

                conf.setProperty("i18n.longest.items", item);
                sort(srcPath, itemTextField.getText());
                if (CollectionUtils.isEmpty(infos)) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18NFindLongestPanel.this, "Failed, please see the log", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    I18NInfo info = infos.get(0);
                    StringBuilder sb = new StringBuilder();
                    sb.append("length: ").append(info.length).append(System.getProperty("line.separator"))
                            .append("text: ").append(info.text).append(System.getProperty("line.separator"))
                            .append("path: ").append(info.path);
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18NFindLongestPanel.this, sb.toString(), "INFO",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        operationPanel.add(findButton);
    }

    private void createItemPanel() {
        itemPanel = new JPanel();
        Utils.setJComponentSize(itemPanel, PANEL_WIDTH, CHILD_PANEL_HIGHT);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
        
        itemTextField = new JTextField();
        Utils.setJComponentSize(itemTextField, CHILD_PANEL_LEFT_WIDTH, CHILD_PANEL_HIGHT);
        itemTextField.setText(conf.getString("i18n.longest.items"));

        itemLabel = new JLabel("Items");
        Utils.setJComponentSize(itemLabel, CHILD_PANEL_RIGHT_WIDTH, CHILD_PANEL_HIGHT);

        itemPanel.add(itemTextField);
        itemPanel.add(Box.createHorizontalGlue());
        itemPanel.add(itemLabel);
    }

    private void createSourcePanel() {
        sourcePanel = new JPanel();
        Utils.setJComponentSize(sourcePanel, PANEL_WIDTH, CHILD_PANEL_HIGHT);
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        
        srcTextField = new JTextField();
        Utils.setJComponentSize(srcTextField, CHILD_PANEL_LEFT_WIDTH, CHILD_PANEL_HIGHT);
        srcTextField.setText(conf.getString("i18n.longest.src.dir"));

        srcButton = new JButton("Source Directory");
        Utils.setJComponentSize(srcButton, CHILD_PANEL_RIGHT_WIDTH, CHILD_PANEL_HIGHT);
        srcButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("select a directory");
                int ret = jfc.showDialog(new JLabel(), null);
                switch (ret) {
                case JFileChooser.APPROVE_OPTION:
                    File file = jfc.getSelectedFile();
                    srcTextField.setText(file.getAbsolutePath());
                    break;
                default:
                    break;
                }

            }
        });

        sourcePanel.add(srcTextField);
        sourcePanel.add(Box.createHorizontalGlue());
        sourcePanel.add(srcButton);
    }

    private String getCanonicalPath(File file) {
        if (file == null) {
            logger.error("file is null");
            return null;
        }
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.error("getCanonicalPath failed: " + file.getAbsolutePath(), e);
            return null;
        }
    }

    private void sort(String sourceBaseStr, String itemName) {
        File[] sourceParentFiles = new File(sourceBaseStr).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("values");
            }
        });
        if (sourceParentFiles == null) {
            logger.error("None valid directory found");
            return;
        }
        for (File sourceParentFile : sourceParentFiles) {
            File sourceFile = new File(sourceParentFile, "strings.xml");
            if (sourceFile.exists()) {
                SAXBuilder builder = new SAXBuilder();
                Document sourceDoc;
                try {
                    sourceDoc = builder.build(sourceFile);
                } catch (JDOMException | IOException e) {
                    logger.error("build failed: " + sourceFile, e);
                    continue;
                }
                Element sourceRoot = sourceDoc.getRootElement();
                for (Element child : sourceRoot.getChildren()) {
                    String value = child.getAttributeValue("name");
                    if (value != null && value.equals(itemName)) {
                        String text = child.getText();
                        if (text != null) {
                            I18NInfo info = new I18NInfo(getCanonicalPath(sourceFile), text, text.length());
                            infos.add(info);
                            break;
                        }
                    }
                }

            }

        }
        Collections.sort(infos, new Comparator<I18NInfo>() {
            @Override
            public int compare(I18NInfo o1, I18NInfo o2) {
                return o2.length - o1.length;
            }
        });

        logger.info(infos);
    }
}

class I18NInfo {
    String path;
    String text;
    int length;

    public I18NInfo(String path, String text, int length) {
        this.path = path;
        this.text = text;
        this.length = length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "I18NInfo [path=" + path + ", text=" + text + ", length=" + length + "]";
    }
}
