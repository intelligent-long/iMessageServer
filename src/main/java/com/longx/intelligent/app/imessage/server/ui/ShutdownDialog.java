package com.longx.intelligent.app.imessage.server.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by LONG on 2026/1/26 at 12:45 PM.
 */
public class ShutdownDialog {

    private final JDialog dialog;

    public ShutdownDialog(JFrame owner) {
        dialog = new JDialog(owner, "正在关闭", true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setSize(360, 120);
        dialog.setLocationRelativeTo(owner);

        JLabel label = new JLabel("正在安全关闭服务，请稍候…", SwingConstants.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(label, BorderLayout.CENTER);
        dialog.add(progressBar, BorderLayout.SOUTH);
    }

    public void showDialog() {
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    public void closeDialog() {
        SwingUtilities.invokeLater(() -> dialog.dispose());
    }
}
