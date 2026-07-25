package com.mycompany.login;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;

/**
 * A JPanel that paints a background image scaled to fill the panel,
 * resizing along with it. Add your normal child components (labels,
 * text fields, buttons) to this panel as usual — they'll sit on top.
 *
 * The image is automatically enhanced (contrast + saturation boosted)
 * once at load time, so washed-out / hazy source photos look clearer
 * without needing to edit the image file itself.
 */
public class BackgroundPanel extends JPanel {

    private BufferedImage backgroundImage;

    // Tweak these to taste.
    private float contrastFactor = 1.25f;   // >1.0 = more contrast
    private float brightnessOffset = -10f;  // negative = slightly darker (pairs well with more contrast)
    private float saturationBoost = 1.35f;  // >1.0 = more vivid colors

    public BackgroundPanel() {
        setOpaque(true); // we're fully painting the background ourselves
    }

    /** Load the image from a classpath resource, e.g. "/com/mycompany/login/images/signal.jpg" */
    public void setBackgroundImage(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Background image not found: " + resourcePath);
                return;
            }
            BufferedImage raw = javax.imageio.ImageIO.read(url);
            backgroundImage = enhance(raw);
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Adjust enhancement strength before calling setBackgroundImage, if desired. */
    public void setEnhancement(float contrastFactor, float brightnessOffset, float saturationBoost) {
        this.contrastFactor = contrastFactor;
        this.brightnessOffset = brightnessOffset;
        this.saturationBoost = saturationBoost;
    }

    private BufferedImage enhance(BufferedImage src) {
        if (src == null) {
            return null;
        }

        // Step 1: contrast + brightness via RescaleOp
        BufferedImage working = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        working.getGraphics().drawImage(src, 0, 0, null);

        RescaleOp rescale = new RescaleOp(contrastFactor, brightnessOffset, null);
        rescale.filter(working, working);

        // Step 2: saturation boost via per-pixel HSB adjustment
        int width = working.getWidth();
        int height = working.getHeight();
        int[] pixels = working.getRGB(0, 0, width, height, null, 0, width);

        float[] hsb = new float[3];
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;

            Color.RGBtoHSB(r, g, b, hsb);
            float newSat = Math.min(1f, hsb[1] * saturationBoost);
            int rgb = Color.HSBtoRGB(hsb[0], newSat, hsb[2]);

            pixels[i] = (a << 24) | (rgb & 0x00ffffff);
        }
        working.setRGB(0, 0, width, height, pixels, 0, width);

        return working;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // clears background first since setOpaque(true)

        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}