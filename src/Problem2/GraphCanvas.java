//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package Problem2;//

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JPanel;

public class GraphCanvas extends JPanel {
    private TrainNetworkGUI frame;
    private String draggedNode = null;
    private final int NODE_RADIUS = 20;

    public GraphCanvas(final TrainNetworkGUI frame) {
        this.frame = frame;
        this.setBackground(TrainNetworkGUI.COLOR_BG);
        MouseAdapter mouseAdapter = new MouseAdapter() {
            {
                Objects.requireNonNull(GraphCanvas.this);
            }

            public void mousePressed(MouseEvent e) {
                String clickedNode = GraphCanvas.this.getClickedNode(e.getPoint());
                if (clickedNode != null) {
                    GraphCanvas.this.draggedNode = clickedNode;
                    if (frame.getSelectedFrom() == null) {
                        frame.setSelectedFrom(clickedNode);
                        frame.log("تم تحديد الانطلاق: " + clickedNode);
                    } else if (frame.getSelectedTo() == null && !clickedNode.equals(frame.getSelectedFrom())) {
                        frame.setSelectedTo(clickedNode);
                        frame.log("تم تحديد الوصول: " + clickedNode);
                        frame.calculatePath();
                    } else if (frame.getSelectedFrom() != null && frame.getSelectedTo() != null) {
                        frame.setSelectedFrom(clickedNode);
                        frame.setSelectedTo((String)null);
                        frame.getCurrentShortestPath().clear();
                        frame.log("تم تحديد الانطلاق: " + clickedNode);
                    }

                    GraphCanvas.this.repaint();
                }

            }

            public void mouseReleased(MouseEvent e) {
                GraphCanvas.this.draggedNode = null;
            }

            public void mouseDragged(MouseEvent e) {
                if (GraphCanvas.this.draggedNode != null) {
                    frame.getNodePositions().put(GraphCanvas.this.draggedNode, e.getPoint());
                    GraphCanvas.this.repaint();
                }

            }
        };
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
    }

    private String getClickedNode(Point p) {
        for(Map.Entry<String, Point> entry : this.frame.getNodePositions().entrySet()) {
            if (((Point)entry.getValue()).distance(p) <= (double)30.0F) {
                return (String)entry.getKey();
            }
        }

        return null;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String exported = this.frame.getGraph().exportGraph();
        if (exported != null && !exported.isEmpty()) {
            String[] lines = exported.split("\n");

            for(String line : lines) {
                String[] parts = line.split("->");
                if (parts.length >= 2) {
                    String from = parts[0].trim();
                    Point pFrom = (Point)this.frame.getNodePositions().get(from);
                    String[] dests = parts[1].split(",");

                    for(String dest : dests) {
                        int openIdx = dest.indexOf(40);
                        int closeIdx = dest.indexOf(41);
                        if (openIdx != -1 && closeIdx != -1) {
                            String to = dest.substring(0, openIdx).trim();
                            String weightStr = dest.substring(openIdx + 1, closeIdx).trim();
                            Point pTo = (Point)this.frame.getNodePositions().get(to);
                            if (pFrom != null && pTo != null) {
                                boolean isPathEdge = false;
                                List<String> currentShortestPath = this.frame.getCurrentShortestPath();
                                if (currentShortestPath.size() > 1) {
                                    for(int i = 0; i < currentShortestPath.size() - 1; ++i) {
                                        if (((String)currentShortestPath.get(i)).equals(from) && ((String)currentShortestPath.get(i + 1)).equals(to) || ((String)currentShortestPath.get(i)).equals(to) && ((String)currentShortestPath.get(i + 1)).equals(from)) {
                                            isPathEdge = true;
                                            break;
                                        }
                                    }
                                }

                                if (isPathEdge) {
                                    g2.setColor(TrainNetworkGUI.COLOR_ACCENT);
                                    g2.setStroke(new BasicStroke(3.0F));
                                } else {
                                    g2.setColor(Color.LIGHT_GRAY);
                                    g2.setStroke(new BasicStroke(1.0F));
                                }

                                g2.drawLine(pFrom.x, pFrom.y, pTo.x, pTo.y);
                                g2.setColor(TrainNetworkGUI.COLOR_TEXT);
                                g2.setFont(new Font("Arial", 0, 12));
                                int midX = (pFrom.x + pTo.x) / 2;
                                int midY = (pFrom.y + pTo.y) / 2;
                                g2.drawString(weightStr, midX, midY - 5);
                            }
                        }
                    }
                }
            }
        }

        for(Map.Entry<String, Point> entry : this.frame.getNodePositions().entrySet()) {
            String nodeName = (String)entry.getKey();
            Point p = (Point)entry.getValue();
            if (!nodeName.equals(this.frame.getSelectedFrom()) && !nodeName.equals(this.frame.getSelectedTo())) {
                if (this.frame.getCurrentShortestPath().contains(nodeName)) {
                    g2.setColor(TrainNetworkGUI.COLOR_NODE_HIGHLIGHT);
                } else {
                    g2.setColor(TrainNetworkGUI.COLOR_NODE);
                }
            } else {
                g2.setColor(TrainNetworkGUI.COLOR_ACCENT);
            }

            g2.fillOval(p.x - 20, p.y - 20, 40, 40);
            g2.setColor(Color.WHITE);
            g2.drawOval(p.x - 20, p.y - 20, 40, 40);
            g2.setColor(TrainNetworkGUI.COLOR_TEXT);
            g2.setFont(new Font("Arial", 1, 14));
            g2.drawString(nodeName, p.x - 20, p.y - 20 - 5);
        }

    }
}
