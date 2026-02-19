package com.longx.intelligent.app.imessage.server.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.longx.intelligent.app.imessage.server.util.EnvironmentUtil;
import com.longx.intelligent.app.imessage.server.util.JsonUtil;
import com.longx.intelligent.app.imessage.server.value.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LONG on 2024/11/30 at 10:58 AM.
 */
public class LaunchUi {
    private static class InstanceHolder{
        private static final LaunchUi INSTANCE = new LaunchUi();
    }
    public static LaunchUi getInstance(){
        return InstanceHolder.INSTANCE;
    }
    private static final int MAIN_UI_WIDTH = 775;
    private static final int MAIN_UI_HEIGHT = 500;
    private final JFrame frame;
    private JPanel rootPane;
    private JButton launchButton;
    private JTextField portField;
    private JTextField mySqlUrlField;
    private JTextField mySqlUsernameField;
    private JPasswordField mySqlPasswordField;
    private JTextField redisDatabaseField;
    private JTextField redisHostField;
    private JTextField redisPortField;
    private JPasswordField redisPasswordField;
    private JTextField mailServerHostField;
    private JTextField mailServerPortField;
    private JTextField mailServerUsernameField;
    private JPasswordField mailServerPasswordField;
    private JTextField mailServerDefaultEncodingField;
    private JCheckBox mailServerAuthCheckBox;
    private JCheckBox mailServerEnableStarttlsCheckBox;
    private JCheckBox mailServerRequiredStarttlsCheckBox;
    private JLabel labelMysql;
    private JLabel labelRedis;
    private JLabel labelMailServer;
    private Runnable launchAction;

    private LaunchUi(){
        frame = new JFrame(Constants.APP_NAME);
        frame.setSize(MAIN_UI_WIDTH, MAIN_UI_HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        createUiComponentsNotInForm();
        setupUi();
        setupYiers();
    }

    public void show(){
        frame.setContentPane(rootPane);
        frame.setVisible(true);
        restoreData();
    }

    public void close(){
        saveData();
        frame.dispose();
    }

    private void setupUi() {
        EnvironmentUtil.System system = EnvironmentUtil.determineSystem();
        Font defaultFont = labelMysql.getFont();
        int defaultFontSize = defaultFont.getSize();
        String defaultFontName = defaultFont.getName();
        Font selectedFont;
        if (system.equals(EnvironmentUtil.System.Mac)) {
            selectedFont = new Font("PingFang SC", Font.BOLD | Font.ITALIC, defaultFontSize);
        } else if (system.equals(EnvironmentUtil.System.Windows)) {
            selectedFont = new Font("Microsoft YaHei UI", Font.BOLD, defaultFontSize);
        } else {
            selectedFont = new Font(defaultFontName, Font.BOLD | Font.ITALIC, defaultFontSize);
        }
        labelMysql.setFont(selectedFont);
        labelRedis.setFont(selectedFont);
        labelMailServer.setFont(selectedFont);
        labelMysql.setPreferredSize(new Dimension(labelMysql.getPreferredSize().width + 2, labelMysql.getPreferredSize().height));
        labelRedis.setPreferredSize(new Dimension(labelRedis.getPreferredSize().width + 2, labelRedis.getPreferredSize().height));
        labelMailServer.setPreferredSize(new Dimension(labelMailServer.getPreferredSize().width + 2, labelMailServer.getPreferredSize().height));
    }

    private void createUiComponentsNotInForm() {
    }

    private void setupYiers() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                saveData();
            }
        });
        launchButton.addActionListener(e -> {
            if(launchAction != null) launchAction.run();
        });
    }

    private void saveData(){
        try {
            Map<String, String> properties = getProperties();
            File configFile = new File(EnvironmentUtil.getApplicationDirectory(), Constants.CONFIG_JSON_FILE_NAME);
            JsonUtil.writeObjectToJsonFile(properties, configFile, true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void restoreData(){
        try {
            File configFile = new File(EnvironmentUtil.getApplicationDirectory(), Constants.CONFIG_JSON_FILE_NAME);
            Map<String, String> properties = JsonUtil.loadObjectFromJsonFile(configFile, new TypeReference<>() {
            });
            showProperties(properties);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Map<String, String> getProperties(){
        Map<String, String> properties = new HashMap<>();
        if(portField.getText() != null && !portField.getText().isEmpty())
            properties.put("server.port", portField.getText());
        if(mySqlUrlField.getText() != null && !mySqlUrlField.getText().isEmpty())
            properties.put("spring.datasource.url", mySqlUrlField.getText());
        if(mySqlUsernameField.getText() != null && !mySqlUsernameField.getText().isEmpty())
            properties.put("spring.datasource.username", mySqlUsernameField.getText());
        if(mySqlPasswordField.getPassword() != null && !new String(mySqlPasswordField.getPassword()).isEmpty())
            properties.put("spring.datasource.password", new String(mySqlPasswordField.getPassword()));
        if(redisDatabaseField.getText() != null && !redisDatabaseField.getText().isEmpty())
            properties.put("spring.data.redis.database", redisDatabaseField.getText());
        if(redisHostField.getText() != null && !redisHostField.getText().isEmpty())
            properties.put("spring.data.redis.host", redisHostField.getText());
        if(redisPortField.getText() != null && !redisPortField.getText().isEmpty())
            properties.put("spring.data.redis.port", redisPortField.getText());
        if(redisPasswordField.getPassword() != null && !new String(redisPasswordField.getPassword()).isEmpty())
            properties.put("spring.data.redis.password", new String(redisPasswordField.getPassword()));
        if(mailServerHostField.getText() != null && !mailServerHostField.getText().isEmpty())
            properties.put("spring.mail.host", mailServerHostField.getText());
        if(mailServerPortField.getText() != null && !mailServerPortField.getText().isEmpty())
            properties.put("spring.mail.port", mailServerPortField.getText());
        if(mailServerUsernameField.getText() != null && !mailServerUsernameField.getText().isEmpty())
            properties.put("spring.mail.username", mailServerUsernameField.getText());
        if(mailServerPasswordField.getText() != null && !mailServerPasswordField.getText().isEmpty())
            properties.put("spring.mail.password", new String(mailServerPasswordField.getPassword()));
        if(mailServerDefaultEncodingField.getText() != null && !mailServerDefaultEncodingField.getText().isEmpty())
            properties.put("spring.mail.default-encoding", mailServerDefaultEncodingField.getText());
        properties.put("spring.mail.properties.mail.smtp.auth", mailServerAuthCheckBox.isSelected() ? "true" : "false");
        properties.put("spring.mail.properties.mail.smtp.starttls.enable", mailServerEnableStarttlsCheckBox.isSelected() ? "true" : "false");
        properties.put("spring.mail.properties.mail.smtp.starttls.required", mailServerRequiredStarttlsCheckBox.isSelected() ? "true" : "false");
        return properties;
    }

    private void showProperties(Map<String, String> properties){
        properties.forEach((key, value) -> {
            switch (key){
                case "server.port":
                    portField.setText(value);
                    break;
                case "spring.datasource.url":
                    mySqlUrlField.setText(value);
                    break;
                case "spring.datasource.username":
                    mySqlUsernameField.setText(value);
                    break;
                case "spring.datasource.password":
                    mySqlPasswordField.setText(value);
                    break;
                case "spring.data.redis.database":
                    redisDatabaseField.setText(value);
                    break;
                case "spring.data.redis.host":
                    redisHostField.setText(value);
                    break;
                case "spring.data.redis.port":
                    redisPortField.setText(value);
                    break;
                case "spring.data.redis.password":
                    redisPasswordField.setText(value);
                    break;
                case "spring.mail.host":
                    mailServerHostField.setText(value);
                    break;
                case "spring.mail.port":
                    mailServerPortField.setText(value);
                    break;
                case "spring.mail.username":
                    mailServerUsernameField.setText(value);
                    break;
                case "spring.mail.password":
                    mailServerPasswordField.setText(value);
                    break;
                case "spring.mail.default-encoding":
                    mailServerDefaultEncodingField.setText(value);
                    break;
                case "spring.mail.properties.mail.smtp.auth":
                    if(value.equals("true")){
                        mailServerAuthCheckBox.setSelected(true);
                    }else if(value.equals("false")){
                        mailServerAuthCheckBox.setSelected(false);
                    }
                    break;
                case "spring.mail.properties.mail.smtp.starttls.enable":
                    if(value.equals("true")){
                        mailServerEnableStarttlsCheckBox.setSelected(true);
                    }else if(value.equals("false")){
                        mailServerEnableStarttlsCheckBox.setSelected(false);
                    }
                    break;
                case "spring.mail.properties.mail.smtp.starttls.required":
                    if(value.equals("true")){
                        mailServerRequiredStarttlsCheckBox.setSelected(true);
                    }else if(value.equals("false")){
                        mailServerRequiredStarttlsCheckBox.setSelected(false);
                    }
                    break;
            }
        });
    }

    public void setLaunchAction(Runnable launchAction) {
        this.launchAction = launchAction;
    }
}
