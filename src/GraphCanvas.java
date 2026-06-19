import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GraphCanvas extends JPanel {
    private TrainNetworkGUI frame;
    private String draggedNode = null;
    private final int NODE_RADIUS = 20;

    public GraphCanvas(TrainNetworkGUI frame) {
        this.frame = frame;
        setBackground(TrainNetworkGUI.COLOR_BG);

        // التقاط التفاعل بالماوس لتحريك المحطات واختيار أقصر طريق
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String clickedNode = getClickedNode(e.getPoint());
                if (clickedNode != null) {
                    draggedNode = clickedNode; // تفعيل السحب والتحريك

                    // منطق النقر للتنقل وحساب أقصر مسار
                    if (frame.getSelectedFrom() == null) {
                        frame.setSelectedFrom(clickedNode);
                        frame.log("تم تحديد الانطلاق: " + clickedNode);
                    } else if (frame.getSelectedTo() == null && !clickedNode.equals(frame.getSelectedFrom())) {
                        frame.setSelectedTo(clickedNode);
                        frame.log("تم تحديد الوصول: " + clickedNode);
                        frame.calculatePath();
                    } else if (frame.getSelectedFrom() != null && frame.getSelectedTo() != null) {
                        frame.setSelectedFrom(clickedNode);
                        frame.setSelectedTo(null);
                        frame.getCurrentShortestPath().clear();
                        frame.log("تم تحديد الانطلاق: " + clickedNode);
                    }
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
                    frame.getNodePositions().put(draggedNode, e.getPoint());
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private String getClickedNode(Point p) {
        for (Map.Entry<String, Point> entry : frame.getNodePositions().entrySet()) {
            if (entry.getValue().distance(p) <= NODE_RADIUS + 10) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. رسم المسارات (Edges) بالاعتماد على تابع exportGraph التابع لك
        String exported = frame.getGraph().exportGraph();
        if (exported != null && !exported.isEmpty()) {
            String[] lines = exported.split("\n");
            for (String line : lines) {
                String[] parts = line.split("->");
                if (parts.length < 2) continue;
                String from = parts[0].trim();
                Point pFrom = frame.getNodePositions().get(from);

                String[] dests = parts[1].split(",");
                for (String dest : dests) {
                    int openIdx = dest.indexOf('(');
                    int closeIdx = dest.indexOf(')');
                    if (openIdx != -1 && closeIdx != -1) {
                        String to = dest.substring(0, openIdx).trim();
                        String weightStr = dest.substring(openIdx + 1, closeIdx).trim();
                        Point pTo = frame.getNodePositions().get(to);

                        if (pFrom != null && pTo != null) {
                            // تمييز ألوان الخطوط إذا كانت تنتمي لأقصر مسار محتسب
                            boolean isPathEdge = false;
                            List<String> currentShortestPath = frame.getCurrentShortestPath();
                            if(currentShortestPath.size() > 1) {
                                for(int i=0; i < currentShortestPath.size() - 1; i++) {
                                    if((currentShortestPath.get(i).equals(from) && currentShortestPath.get(i+1).equals(to)) ||
                                            (currentShortestPath.get(i).equals(to) && currentShortestPath.get(i+1).equals(from))) {
                                        isPathEdge = true;
                                        break;
                                    }
                                }
                            }

                            if (isPathEdge) {
                                g2.setColor(TrainNetworkGUI.COLOR_ACCENT);
                                g2.setStroke(new BasicStroke(3));
                            } else {
                                g2.setColor(Color.LIGHT_GRAY);
                                g2.setStroke(new BasicStroke(1));
                            }
                            g2.drawLine(pFrom.x, pFrom.y, pTo.x, pTo.y);

                            // كتابة وزن المسار
                            g2.setColor(TrainNetworkGUI.COLOR_TEXT);
                            g2.setFont(new Font("Arial", Font.PLAIN, 12));
                            int midX = (pFrom.x + pTo.x) / 2;
                            int midY = (pFrom.y + pTo.y) / 2;
                            g2.drawString(weightStr, midX, midY - 5);
                        }
                    }
                }
            }
        }

        // 2. رسم المحطات (Nodes) وتلوينها حسب حالتها
        for (Map.Entry<String, Point> entry : frame.getNodePositions().entrySet()) {
            String nodeName = entry.getKey();
            Point p = entry.getValue();

            if (nodeName.equals(frame.getSelectedFrom()) || nodeName.equals(frame.getSelectedTo())) {
                g2.setColor(TrainNetworkGUI.COLOR_ACCENT); // لون المحطات المحددة مباشرة للانطلاق/الوصول
            } else if (frame.getCurrentShortestPath().contains(nodeName)) {
                g2.setColor(TrainNetworkGUI.COLOR_NODE_HIGHLIGHT); // لون المحطات العابرة ضمن أقصر طريق
            } else {
                g2.setColor(TrainNetworkGUI.COLOR_NODE); // اللون الافتراضي للمحطة
            }

            g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g2.setColor(Color.WHITE);
            g2.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            g2.setColor(TrainNetworkGUI.COLOR_TEXT);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(nodeName, p.x - NODE_RADIUS, p.y - NODE_RADIUS - 5);
        }
    }
}