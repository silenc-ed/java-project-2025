
package View.Customers;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Modern flat ListMenu with rounded button items, transparent background,
 * and hover effects for a contemporary look.
 */
public class ListMenu extends javax.swing.JPanel {

    private EventMenuSelected event;
    private Runnable eventProfile;
    private Runnable eventUserNameClicked;
    private JPanel menuItemsPanel;
    private int selectedIndex = -1;
    private java.util.List<JPanel> menuButtons = new java.util.ArrayList<>();

    // Styling constants
    private static final Color ITEM_BG = new Color(255, 255, 255, 180);         // Semi-transparent white
    private static final Color ITEM_BG_HOVER = new Color(255, 255, 255, 230);   // Brighter on hover
    private static final Color ITEM_BG_SELECTED = new Color(255, 255, 255, 255); // Fully opaque when selected
    private static final Color TEXT_COLOR = new Color(80, 60, 120);              // Purple-ish text
    private static final Color TEXT_SELECTED = new Color(101, 78, 163);          // Matches gradient start color
    private static final Font ITEM_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font ITEM_FONT_SELECTED = new Font("Segoe UI", Font.BOLD, 14);
    private static final int CORNER_RADIUS = 12;

    public void addEventMenuSelected(EventMenuSelected event) {
        this.event = event;
    }

    public void addEventProfileClicked(Runnable eventProfile) {
        this.eventProfile = eventProfile;
    }

    public void addEventUserNameClicked(Runnable eventUserNameClicked) {
        this.eventUserNameClicked = eventUserNameClicked;
    }

    public void setFullName(String name) {
        fullnameLabel.setText(name);
        fullnameLabel.repaint();
    }

    public void setMenu(String[] items) {
        menuItemsPanel.removeAll();
        menuButtons.clear();
        selectedIndex = -1;

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            JPanel btn = createMenuItem(items[i], index);
            menuButtons.add(btn);
            menuItemsPanel.add(btn);
            menuItemsPanel.add(Box.createVerticalStrut(6)); // spacing between items
        }

        menuItemsPanel.revalidate();
        menuItemsPanel.repaint();
    }

    /**
     * Creates a single rounded menu item button panel.
     */
    private JPanel createMenuItem(String text, int index) {
        JPanel item = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg;
                if (selectedIndex == index) {
                    bg = ITEM_BG_SELECTED;
                } else if (getClientProperty("hovered") == Boolean.TRUE) {
                    bg = ITEM_BG_HOVER;
                } else {
                    bg = ITEM_BG;
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);

                // Draw subtle left accent bar when selected
                if (selectedIndex == index) {
                    g2.setColor(TEXT_SELECTED);
                    g2.fillRoundRect(0, 4, 4, getHeight() - 8, 4, 4);
                }

                g2.dispose();
            }
        };

        item.setOpaque(false);
        item.setLayout(new BorderLayout());
        item.setPreferredSize(new Dimension(240, 40));
        item.setMaximumSize(new Dimension(240, 40));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel("   " + text);
        label.setFont(ITEM_FONT);
        label.setForeground(TEXT_COLOR);
        item.add(label, BorderLayout.CENTER);

        // Hover effects
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.putClientProperty("hovered", Boolean.TRUE);
                if (selectedIndex != index) {
                    label.setForeground(TEXT_SELECTED);
                }
                item.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.putClientProperty("hovered", Boolean.FALSE);
                if (selectedIndex != index) {
                    label.setForeground(TEXT_COLOR);
                }
                item.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setSelectedIndex(index);
                if (event != null) {
                    event.selected(index);
                }
            }
        });

        return item;
    }

    /**
     * Updates the visual state of all menu items to reflect the new selection.
     */
    public void setSelectedIndex(int index) {
        selectedIndex = index;
        for (int i = 0; i < menuButtons.size(); i++) {
            JPanel btn = menuButtons.get(i);
            JLabel label = (JLabel) btn.getComponent(0);
            if (i == index) {
                label.setFont(ITEM_FONT_SELECTED);
                label.setForeground(TEXT_SELECTED);
            } else {
                label.setFont(ITEM_FONT);
                label.setForeground(TEXT_COLOR);
            }
            btn.repaint();
        }
    }

    /**
     * Clears the current selection so no menu item is highlighted.
     */
    public void clearSelection() {
        selectedIndex = -1;
        for (int i = 0; i < menuButtons.size(); i++) {
            JPanel btn = menuButtons.get(i);
            JLabel label = (JLabel) btn.getComponent(0);
            label.setFont(ITEM_FONT);
            label.setForeground(TEXT_COLOR);
            btn.repaint();
        }
    }

    public ListMenu() {
        setOpaque(false);
        setLayout(new BorderLayout());

        // --- Menu items area (scrollable) ---
        menuItemsPanel = new JPanel();
        menuItemsPanel.setOpaque(false);
        menuItemsPanel.setLayout(new BoxLayout(menuItemsPanel, BoxLayout.Y_AXIS));
        menuItemsPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scrollPane = new JScrollPane(menuItemsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom user panel ---
        JPanel userPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 40)); // subtle dark background for the user panel
                g2.fillRoundRect(10, 0, getWidth() - 20, getHeight(), 12, 12);
                g2.dispose();
            }
        };
        userPanel.setOpaque(false);
        userPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 12));

        fullnameLabel = new JLabel("fullname");
        fullnameLabel.setForeground(new Color(255, 255, 255, 220));
        fullnameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fullnameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        fullnameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                fullnameLabel.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                fullnameLabel.setForeground(new Color(255, 255, 255, 220));
            }
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (eventUserNameClicked != null) {
                    eventUserNameClicked.run();
                }
            }
        });

        menuUserIcon = new JLabel();
        menuUserIcon.setHorizontalAlignment(SwingConstants.RIGHT);
        try {
            menuUserIcon.setIcon(new ImageIcon(getClass().getResource("/Default/Icon/menu.png")));
        } catch (Exception e) {
            menuUserIcon.setText("");
        }

        menuUserIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuUserIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (eventProfile != null) {
                    eventProfile.run();
                }
            }
        });

        userPanel.add(fullnameLabel);
        userPanel.add(menuUserIcon);

        // Wrapper to add bottom padding to user panel
        JPanel userPanelWrapper = new JPanel(new BorderLayout());
        userPanelWrapper.setOpaque(false);
        userPanelWrapper.add(userPanel, BorderLayout.CENTER);
        userPanelWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        add(userPanelWrapper, BorderLayout.SOUTH);
        // removed preferred size to allow full stretching
    }

    // Variables
    private JLabel fullnameLabel;
    private JLabel menuUserIcon;
}
