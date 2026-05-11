package View.Customers.ProductPanel;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * A banner slideshow component with auto-sliding (carousel) and slide left/right animation.
 */
public class BannerSlideshow extends JPanel {

    private final List<Image> bannerImages = new ArrayList<>();
    private int currentIndex = 0;
    private Timer autoSlideTimer;
    private Timer animationTimer;

    // Animation state
    private boolean isAnimating = false;
    private int slideDirection = 0; // -1 = left (next), 1 = right (prev)
    private double animationProgress = 0.0; // 0.0 -> 1.0
    private static final int ANIMATION_DURATION_MS = 400;
    private static final int ANIMATION_FPS = 60;

    // Dot indicator
    private static final int DOT_SIZE = 10;
    private static final int DOT_GAP = 8;
    private static final Color DOT_ACTIVE = new Color(255, 255, 255);
    private static final Color DOT_INACTIVE = new Color(255, 255, 255, 100);

    // Navigation buttons
    private static final int NAV_BTN_SIZE = 36;
    private static final Color NAV_BTN_BG = new Color(0, 0, 0, 80);
    private static final Color NAV_BTN_HOVER = new Color(0, 0, 0, 140);

    private boolean hoveredLeft = false;
    private boolean hoveredRight = false;
    private boolean mouseInside = false;

    public BannerSlideshow() {
        setOpaque(false);
        setPreferredSize(new Dimension(0, 200));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseInside = true;
                if (autoSlideTimer != null) autoSlideTimer.stop();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseInside = false;
                hoveredLeft = false;
                hoveredRight = false;
                if (autoSlideTimer != null) autoSlideTimer.start();
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isAnimating) return;
                Rectangle leftBtn = getLeftBtnRect();
                Rectangle rightBtn = getRightBtnRect();
                if (leftBtn.contains(e.getPoint())) {
                    slidePrev();
                } else if (rightBtn.contains(e.getPoint())) {
                    slideNext();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Rectangle leftBtn = getLeftBtnRect();
                Rectangle rightBtn = getRightBtnRect();
                boolean newLeft = leftBtn.contains(e.getPoint());
                boolean newRight = rightBtn.contains(e.getPoint());
                if (newLeft != hoveredLeft || newRight != hoveredRight) {
                    hoveredLeft = newLeft;
                    hoveredRight = newRight;
                    setCursor(new Cursor((hoveredLeft || hoveredRight) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });
    }

    /**
     * Load banner images from resource paths.
     */
    public void loadBannerImages(String[] resourcePaths) {
        bannerImages.clear();
        for (String path : resourcePaths) {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                bannerImages.add(icon.getImage());
            }
        }
        currentIndex = 0;
        repaint();
    }

    /**
     * Start auto-sliding every intervalMs milliseconds.
     */
    public void startAutoSlide(int intervalMs) {
        if (autoSlideTimer != null) {
            autoSlideTimer.stop();
        }
        autoSlideTimer = new Timer(intervalMs, e -> slideNext());
        autoSlideTimer.setRepeats(true);
        autoSlideTimer.start();
    }

    /**
     * Stop auto-sliding.
     */
    public void stopAutoSlide() {
        if (autoSlideTimer != null) {
            autoSlideTimer.stop();
        }
    }

    private void slideNext() {
        if (isAnimating || bannerImages.isEmpty()) return;
        slideDirection = -1; // slide left
        startAnimation();
    }

    private void slidePrev() {
        if (isAnimating || bannerImages.isEmpty()) return;
        slideDirection = 1; // slide right
        startAnimation();
    }

    private void startAnimation() {
        isAnimating = true;
        animationProgress = 0.0;
        long startTime = System.currentTimeMillis();

        animationTimer = new Timer(1000 / ANIMATION_FPS, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            animationProgress = Math.min(1.0, (double) elapsed / ANIMATION_DURATION_MS);

            // Ease out cubic
            animationProgress = 1.0 - Math.pow(1.0 - animationProgress, 3);

            if (animationProgress >= 1.0) {
                animationTimer.stop();
                isAnimating = false;
                // Update index
                if (slideDirection == -1) {
                    currentIndex = (currentIndex + 1) % bannerImages.size();
                } else {
                    currentIndex = (currentIndex - 1 + bannerImages.size()) % bannerImages.size();
                }
                animationProgress = 0.0;
            }
            repaint();
        });
        animationTimer.start();
    }

    private Rectangle getLeftBtnRect() {
        int y = (getHeight() - NAV_BTN_SIZE) / 2;
        return new Rectangle(10, y, NAV_BTN_SIZE, NAV_BTN_SIZE);
    }

    private Rectangle getRightBtnRect() {
        int y = (getHeight() - NAV_BTN_SIZE) / 2;
        return new Rectangle(getWidth() - NAV_BTN_SIZE - 10, y, NAV_BTN_SIZE, NAV_BTN_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (bannerImages.isEmpty()) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        int arc = 16;

        // Clip to rounded rect
        Shape roundedClip = new RoundRectangle2D.Double(0, 0, w, h, arc, arc);
        g2.setClip(roundedClip);

        if (isAnimating) {
            // Draw current and next/prev images sliding
            int nextIndex;
            if (slideDirection == -1) {
                nextIndex = (currentIndex + 1) % bannerImages.size();
            } else {
                nextIndex = (currentIndex - 1 + bannerImages.size()) % bannerImages.size();
            }

            int offset = (int) (w * animationProgress * slideDirection);

            // Current image sliding out
            drawScaledImage(g2, bannerImages.get(currentIndex), offset, 0, w, h);
            // Next image sliding in
            drawScaledImage(g2, bannerImages.get(nextIndex), offset - w * slideDirection, 0, w, h);
        } else {
            // Static: draw current
            drawScaledImage(g2, bannerImages.get(currentIndex), 0, 0, w, h);
        }

        // Gradient overlay at bottom for dots visibility
        GradientPaint gradient = new GradientPaint(0, h - 50, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 100));
        g2.setPaint(gradient);
        g2.fillRect(0, h - 50, w, 50);

        // Draw navigation buttons (only when mouse inside)
        if (mouseInside) {
            drawNavButton(g2, getLeftBtnRect(), hoveredLeft, true);
            drawNavButton(g2, getRightBtnRect(), hoveredRight, false);
        }

        // Draw dot indicators
        drawDotIndicators(g2, w, h);

        g2.dispose();
    }

    private void drawScaledImage(Graphics2D g2, Image img, int x, int y, int w, int h) {
        // Scale image to cover the area (cover mode)
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        if (imgW <= 0 || imgH <= 0) {
            g2.drawImage(img, x, y, w, h, null);
            return;
        }

        double scaleX = (double) w / imgW;
        double scaleY = (double) h / imgH;
        double scale = Math.max(scaleX, scaleY);

        int scaledW = (int) (imgW * scale);
        int scaledH = (int) (imgH * scale);
        int offsetX = x + (w - scaledW) / 2;
        int offsetY = y + (h - scaledH) / 2;

        g2.drawImage(img, offsetX, offsetY, scaledW, scaledH, null);
    }

    private void drawNavButton(Graphics2D g2, Rectangle rect, boolean hovered, boolean isLeft) {
        g2.setColor(hovered ? NAV_BTN_HOVER : NAV_BTN_BG);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 18, 18);

        // Draw arrow
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = rect.x + rect.width / 2;
        int cy = rect.y + rect.height / 2;
        int arrowSize = 8;

        if (isLeft) {
            g2.drawLine(cx + 3, cy - arrowSize, cx - 3, cy);
            g2.drawLine(cx - 3, cy, cx + 3, cy + arrowSize);
        } else {
            g2.drawLine(cx - 3, cy - arrowSize, cx + 3, cy);
            g2.drawLine(cx + 3, cy, cx - 3, cy + arrowSize);
        }
    }

    private void drawDotIndicators(Graphics2D g2, int panelWidth, int panelHeight) {
        int count = bannerImages.size();
        if (count <= 1) return;

        int totalWidth = count * DOT_SIZE + (count - 1) * DOT_GAP;
        int startX = (panelWidth - totalWidth) / 2;
        int y = panelHeight - DOT_SIZE - 12;

        for (int i = 0; i < count; i++) {
            int x = startX + i * (DOT_SIZE + DOT_GAP);
            if (i == currentIndex && !isAnimating) {
                g2.setColor(DOT_ACTIVE);
            } else {
                g2.setColor(DOT_INACTIVE);
            }
            g2.fillOval(x, y, DOT_SIZE, DOT_SIZE);
        }
    }

    /**
     * Clean up timers when this component is removed.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        stopAutoSlide();
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
