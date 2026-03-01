package com.longx.intelligent.app.imessage.server.ui;

import com.longx.intelligent.app.imessage.server.context.SpringContextHolder;
import com.longx.intelligent.app.imessage.server.util.EnvironmentUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Created by LONG on 2024/12/1 at 10:03 PM.
 */
public class LogUi {
    private static boolean INSTANCE_CREATED = false;
    private static class InstanceHolder {
        private static final LogUi INSTANCE = new LogUi();
    }
    public static LogUi getInstance() {
        INSTANCE_CREATED = true;
        return InstanceHolder.INSTANCE;
    }
    public static LogUi getIfExists() {
        return INSTANCE_CREATED ? InstanceHolder.INSTANCE : null;
    }
    private static final int MAIN_UI_WIDTH = 1000;
    private static final int MAIN_UI_HEIGHT = 600;
    private final JFrame frame;
    private JPanel rootPane;
    private JTextArea logArea;
    private JScrollPane scrollPane;

    private LogUi(){
        frame = new JFrame(Constants.APP_NAME);
        frame.setSize(MAIN_UI_WIDTH, MAIN_UI_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        createUiComponentsNotInForm();
        setupUi();
        setupYiers();
    }

    public void show(){
        frame.setContentPane(rootPane);
        frame.setVisible(true);
        boolean showTraySuccess = showSystemTray();
        if(showTraySuccess) {
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }else {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }

    public void close(){
        frame.dispose();
    }

    private void setupUi() {
        logArea.setDocument(new RestrictiveDocument(logArea, Constants.MAX_LOG_AREA_LINES));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
    }

    private void createUiComponentsNotInForm() {
    }

    private void setupYiers() {
    }

    public void append(String text){
        if(logArea != null) {
            SwingUtilities.invokeLater(() -> {
                logArea.append(text);
            });
        }
    }

    private boolean showSystemTray(){
        EnvironmentUtil.System system = EnvironmentUtil.determineSystem();
        if(system.equals(EnvironmentUtil.System.Windows)){
            return showLibSystemTray() || showJavaAWTSystemTray();
        }else {
            return showJavaAWTSystemTray() || showLibSystemTray();
        }
    }

    private boolean showJavaAWTSystemTray() {
        try {
            if (!SystemTray.isSupported()) {
                JOptionPane.showMessageDialog(frame, "不支持系统托盘");
                return false;
            }
            SystemTray systemTray = SystemTray.getSystemTray();
            PopupMenu popupMenu = new PopupMenu();
            MenuItem menuItemOpenWindow = new MenuItem("打开窗口");
            menuItemOpenWindow.addActionListener(e -> {
                frame.setVisible(true);
            });
            MenuItem menuItemOpenExit = new MenuItem("退出");
            menuItemOpenExit.addActionListener(e -> {
                ShutdownDialog shutdownDialog = new ShutdownDialog(frame);
                // UI 线程安全
                new Thread(shutdownDialog::showDialog).start();
                new Thread(() -> {
                    int code = SpringContextHolder.gracefulShutdownAndWait();
                    shutdownDialog.closeDialog();
                    System.exit(code);
                }, "spring-shutdown-thread").start();
            });
            popupMenu.insert(Constants.APP_NAME, 0);
            popupMenu.getItem(0).setEnabled(false);
            popupMenu.add(menuItemOpenWindow);
            popupMenu.add(menuItemOpenExit);
            systemTray.add(new TrayIcon(UiUtil.svgToImage("/static/drawable/imessage-icon-tray.svg"), Constants.APP_NAME, popupMenu));
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            JOptionPane.showMessageDialog(frame, "系统托盘创建失败");
            return false;
        }
    }

    private boolean showLibSystemTray() {
        try{
            dorkbox.systemTray.SystemTray systemTray = dorkbox.systemTray.SystemTray.get();
            if (systemTray == null) {
                JOptionPane.showMessageDialog(frame, "不支持系统托盘");
                return false;
            }
            systemTray.setImage(UiUtil.svgToImage("/static/drawable/imessage-icon-tray.svg"));
            systemTray.setStatus(Constants.APP_NAME);
            systemTray.setTooltip(Constants.APP_NAME);
            systemTray.getMenu().add(new dorkbox.systemTray.MenuItem("打开窗口", e -> frame.setVisible(true)));
            systemTray.getMenu().add(
                    new dorkbox.systemTray.MenuItem("退出", e -> {
                        ShutdownDialog shutdownDialog = new ShutdownDialog(frame);
                        new Thread(shutdownDialog::showDialog).start();
                        new Thread(() -> {
                            int code = SpringContextHolder.gracefulShutdownAndWait();
                            shutdownDialog.closeDialog();
                            System.exit(code);
                        }, "spring-shutdown-thread").start();
                    })
            );
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            JOptionPane.showMessageDialog(frame, "系统托盘创建失败");
            return false;
        }
    }
}
