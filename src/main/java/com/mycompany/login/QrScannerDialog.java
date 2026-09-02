package com.mycompany.login;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;

/**
 * A modal dialog showing a live webcam feed, continuously scanning for a
 * QR code. Expects the format QrCodeGenerator produces ("username:password").
 * When a code is found, calls onScanned with the decoded text and closes.
 *
 * If more than one camera is connected (e.g. a laptop's built-in camera
 * plus a USB webcam plugged in for a front-desk kiosk), a dropdown lets
 * the user choose which one to scan with instead of always defaulting to
 * whatever Webcam.getDefault() happens to pick.
 */
public class QrScannerDialog extends JDialog {

    private Webcam webcam;
    private volatile boolean running = false;
    private Thread scanThread;
    private final Consumer<String> onScanned;
    private final JLabel previewLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Point a student's QR code at the camera...", SwingConstants.CENTER);
    private final JComboBox<Webcam> cameraSelector = new JComboBox<>();

    public QrScannerDialog(Window owner, Consumer<String> onScanned) {
        super(owner, "Scan QR Code", ModalityType.APPLICATION_MODAL);
        this.onScanned = onScanned;

        List<Webcam> available = Webcam.getWebcams();
        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "No webcam was found on this computer.");
            return;
        }

        buildUI();
        setSize(560, 540);
        setLocationRelativeTo(owner);

        for (Webcam cam : available) {
            cameraSelector.addItem(cam);
        }
        // Prefer the system default if it's in the list, otherwise just the first one.
        Webcam preferred = Webcam.getDefault();
        cameraSelector.setSelectedItem(available.contains(preferred) ? preferred : available.get(0));

        cameraSelector.addActionListener(e -> switchCamera((Webcam) cameraSelector.getSelectedItem()));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopScanning();
            }
        });

        // Only shown when there's an actual choice to make -- with a single
        // camera the dropdown would just be clutter.
        cameraSelector.setVisible(available.size() > 1);

        switchCamera((Webcam) cameraSelector.getSelectedItem());

        setVisible(true); // blocks here (modal) until dispose() is called
    }

    /**
     * Finds and sets a supported resolution for the camera hardware.
     */
    private void setSupportedViewSize(Webcam webcam) {
        Dimension[] viewSizes = webcam.getViewSizes();
        if (viewSizes != null && viewSizes.length > 0) {
            // Pick a size close to VGA (640x480 or 320x240) if present, otherwise default to the best supported size
            Dimension selectedSize = viewSizes[viewSizes.length - 1]; // Default to largest supported
            for (Dimension d : viewSizes) {
                if (d.width == 640 && d.height == 480) {
                    selectedSize = d;
                    break;
                } else if (d.width == 320 && d.height == 240) {
                    selectedSize = d;
                }
            }
            webcam.setViewSize(selectedSize);
        } else {
            // Fallback to standard VGA
            webcam.setViewSize(WebcamResolution.VGA.getSize());
        }
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.setBackground(Color.WHITE);
        setContentPane(content);

        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.setOpaque(false);

        JPanel cameraRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        cameraRow.setOpaque(false);
        cameraRow.add(new JLabel("Camera:"));
        cameraRow.add(cameraSelector);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        topPanel.add(cameraRow, BorderLayout.NORTH);
        topPanel.add(statusLabel, BorderLayout.SOUTH);

        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(480, 360));
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.BLACK);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            stopScanning();
            dispose();
        });
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomRow.add(cancelButton);

        content.add(topPanel, BorderLayout.NORTH);
        content.add(previewLabel, BorderLayout.CENTER);
        content.add(bottomRow, BorderLayout.SOUTH);
    }

    /** Stops scanning on whatever camera is currently open, then opens and starts scanning on the new one. */
    private void switchCamera(Webcam newCamera) {
        if (newCamera == null || newCamera == webcam) {
            return;
        }

        stopScanning();

        webcam = newCamera;
        statusLabel.setText("Point a student's QR code at the camera...");

        setSupportedViewSize(webcam);

        running = true;
        scanThread = new Thread(this::scanLoop, "qr-scan-thread");
        scanThread.setDaemon(true);
        scanThread.start();
    }

    private void scanLoop() {
        Webcam activeWebcam = webcam;
        try {
            activeWebcam.open();
        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                    statusLabel.setText("Could not open this camera -- try selecting a different one."));
            return;
        }

        MultiFormatReader reader = new MultiFormatReader();

        while (running && webcam == activeWebcam) {
            BufferedImage image = activeWebcam.getImage();
            if (image == null) {
                sleep();
                continue;
            }

            Image scaled = image.getScaledInstance(480, 360, Image.SCALE_FAST);
            SwingUtilities.invokeLater(() -> previewLabel.setIcon(new ImageIcon(scaled)));

            try {
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = reader.decode(bitmap);
                String text = result.getText();

                running = false;
                SwingUtilities.invokeLater(() -> {
                    stopScanning();
                    dispose();
                    onScanned.accept(text);
                });

            } catch (NotFoundException ignored) {
                // No QR code in this frame -- keep looping.
            } catch (Exception e) {
                e.printStackTrace();
            }

            sleep();
        }

        if (activeWebcam.isOpen()) {
            activeWebcam.close();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException ignored) {
        }
    }

    private void stopScanning() {
        running = false;
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}