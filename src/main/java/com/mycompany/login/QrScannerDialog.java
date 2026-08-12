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
import java.util.function.Consumer;

/**
 * A modal dialog showing a live webcam feed, continuously scanning for a
 * QR code. Expects the format QrCodeGenerator produces ("username:password").
 * When a code is found, calls onScanned with the decoded text and closes.
 */
public class QrScannerDialog extends JDialog {

    private Webcam webcam;
    private volatile boolean running = true;
    private final Consumer<String> onScanned;
    private final JLabel previewLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Point a student's QR code at the camera...", SwingConstants.CENTER);

    public QrScannerDialog(Window owner, Consumer<String> onScanned) {
        super(owner, "Scan QR Code", ModalityType.APPLICATION_MODAL);
        this.onScanned = onScanned;

        webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(owner, "No webcam was found on this computer.");
            return;
        }

        // --- FIX: Dynamically set a supported view size ---
        setSupportedViewSize(webcam);

        buildUI();
        setSize(540, 480);
        setLocationRelativeTo(owner);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopScanning();
            }
        });

        Thread scanThread = new Thread(this::scanLoop, "qr-scan-thread");
        scanThread.setDaemon(true);
        scanThread.start();

        setVisible(true); // blocks here (modal) until dispose() is called by the scan thread
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

        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(480, 360));
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.BLACK);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            stopScanning();
            dispose();
        });
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomRow.add(cancelButton);

        content.add(statusLabel, BorderLayout.NORTH);
        content.add(previewLabel, BorderLayout.CENTER);
        content.add(bottomRow, BorderLayout.SOUTH);
    }

    private void scanLoop() {
        webcam.open();
        MultiFormatReader reader = new MultiFormatReader();

        while (running) {
            BufferedImage image = webcam.getImage();
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