package Problem2;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TrainNetworkGUI extends JFrame {
    public static final Color COLOR_BG = new Color(243, 244, 246);
    public static final Color COLOR_PANEL = new Color(255, 255, 255);
    public static final Color COLOR_PRIMARY = new Color(30, 58, 138);
    public static final Color COLOR_ACCENT = new Color(245, 158, 11);
    public static final Color COLOR_TEXT = new Color(31, 41, 55);
    public static final Color COLOR_NODE = new Color(59, 130, 246);
    public static final Color COLOR_NODE_HIGHLIGHT = new Color(16, 185, 129);
    private WeightedGraph graph = new WeightedGraph();
    private Map<String, Point> nodePositions = new HashMap();
    private String selectedFrom = null;
    private String selectedTo = null;
    private List<String> currentShortestPath = new ArrayList();
    private GraphCanvas canvas;
    private JTextField txtNodeName;
    private JTextField txtEdgeFrom;
    private JTextField txtEdgeTo;
    private JTextField txtEdgeWeight;
    private JTextArea txtOutput;

    public TrainNetworkGUI() {
        this.setTitle("Train Network Manager - NAutN UI");
        this.setSize(1000, 700);
        this.setDefaultCloseOperation(3);
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(COLOR_BG);
        this.initUI();
    }

    private void initUI() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, 1));
        controlPanel.setPreferredSize(new Dimension(300, 0));
        controlPanel.setBackground(COLOR_PANEL);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        controlPanel.add(this.createHeaderLabel("إدارة المحطات"));
        this.txtNodeName = new JTextField();
        controlPanel.add(this.createLabeledField("اسم المحطة:", this.txtNodeName));
        JButton btnAddNode = this.createStyledButton("إضافة محطة");
        btnAddNode.addActionListener((e) -> this.addNode());
        controlPanel.add(btnAddNode);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(this.createHeaderLabel("إدارة المسارات"));
        this.txtEdgeFrom = new JTextField();
        this.txtEdgeTo = new JTextField();
        this.txtEdgeWeight = new JTextField();
        controlPanel.add(this.createLabeledField("من محطة:", this.txtEdgeFrom));
        controlPanel.add(this.createLabeledField("إلى محطة:", this.txtEdgeTo));
        controlPanel.add(this.createLabeledField("المسافة (الوزن):", this.txtEdgeWeight));
        JButton btnAddEdge = this.createStyledButton("إضافة مسار");
        btnAddEdge.addActionListener((e) -> this.addEdge());
        controlPanel.add(btnAddEdge);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        JButton btnEditEdge = this.createStyledButton("تعديل مسار قائم");
        btnEditEdge.addActionListener((e) -> this.editEdge());
        controlPanel.add(btnEditEdge);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(this.createHeaderLabel("العمليات والتحليلات"));
        JButton btnSort = this.createStyledButton("ترتيب المحطات (حسب الاتصالات)");
        btnSort.addActionListener((e) -> this.sortStations());
        controlPanel.add(btnSort);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton btnCycle = this.createStyledButton("التحقق من وجود حلقة (Cycle)");
        btnCycle.addActionListener((e) -> this.checkCycle());
        controlPanel.add(btnCycle);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton btnClearSelection = this.createStyledButton("مسح التحديد (إلغاء أقصر مسار)");
        btnClearSelection.setBackground(COLOR_ACCENT);
        btnClearSelection.addActionListener((e) -> this.clearSelection());
        controlPanel.add(btnClearSelection);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.txtOutput = new JTextArea();
        this.txtOutput.setEditable(false);
        this.txtOutput.setLineWrap(true);
        this.txtOutput.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(this.txtOutput);
        controlPanel.add(scrollPane);
        this.add(controlPanel, "East");
        this.canvas = new GraphCanvas(this);
        this.add(this.canvas, "Center");
        controlPanel.add(this.createHeaderLabel("استيراد وتصدير الشبكة (ملف نصي)"));
        JTextArea txtImportExportData = new JTextArea(4, 20);
        txtImportExportData.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane textScroll = new JScrollPane(txtImportExportData);
        controlPanel.add(textScroll);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.setBackground(COLOR_PANEL);
        btnPanel.setMaximumSize(new Dimension(300, 40));
        JButton btnExport = new JButton("تصدير وحفظ");
        btnExport.setBackground(COLOR_PRIMARY);
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.addActionListener((e) -> {
            try {
                String exportedData = this.graph.exportGraph();
                txtImportExportData.setText(exportedData);
                FileWriter writer = new FileWriter("network.txt");
                writer.write(exportedData);
                writer.close();
                this.log("تم تصدير الشبكة وحفظها بنجاح في ملف: network.txt");
            } catch (Exception ex) {
                this.log("خطأ أثناء الحفظ في الملف: " + ex.getMessage());
            }

        });
        JButton btnImport = new JButton("قراءة واستيراد");
        btnImport.setBackground(COLOR_PRIMARY);
        btnImport.setForeground(Color.WHITE);
        btnImport.setFocusPainted(false);
        btnImport.addActionListener((e) -> {
            try {
                File file = new File("network.txt");
                if (!file.exists()) {
                    this.log("خطأ: الملف network.txt غير موجود! قم بالتصدير أولاً لإنشائه.");
                    return;
                }

                Scanner scanner = new Scanner(file);
                StringBuilder sb = new StringBuilder();

                while(scanner.hasNextLine()) {
                    sb.append(scanner.nextLine()).append("\n");
                }

                scanner.close();
                String fileContent = sb.toString().trim();
                txtImportExportData.setText(fileContent);
                this.graph.importGraph(fileContent);
                this.updateNodePositionsAfterImport();
                this.log("تم قراءة الملف network.txt واستيراد الشبكة بنجاح!");
                this.canvas.repaint();
            } catch (Exception ex) {
                this.log("خطأ أثناء قراءة الملف: " + ex.getMessage());
            }

        });
        btnPanel.add(btnExport);
        btnPanel.add(btnImport);
        controlPanel.add(btnPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private void addNode() {
        String name = this.txtNodeName.getText().trim();
        if (!name.isEmpty() && !this.nodePositions.containsKey(name)) {
            this.graph.addNode(name, name);
            int x = 50 + (int)(Math.random() * (double)(this.canvas.getWidth() - 100));
            int y = 50 + (int)(Math.random() * (double)(this.canvas.getHeight() - 100));
            this.nodePositions.put(name, new Point(x, y));
            this.log("تم إضافة المحطة: " + name);
            this.txtNodeName.setText("");
            this.canvas.repaint();
        }

    }

    private void addEdge() {
        String from = this.txtEdgeFrom.getText().trim();
        String to = this.txtEdgeTo.getText().trim();

        try {
            int weight = Integer.parseInt(this.txtEdgeWeight.getText().trim());
            this.graph.addEdge(from, to, weight);
            this.log("تم إضافة مسار: " + from + " -> " + to + " (" + weight + ")");
            this.txtEdgeFrom.setText("");
            this.txtEdgeTo.setText("");
            this.txtEdgeWeight.setText("");
            this.canvas.repaint();
        } catch (NumberFormatException var4) {
            this.log("خطأ: يرجى إدخال رقم صحيح للوزن.");
        } catch (IllegalArgumentException var5) {
            this.log("خطأ: تأكد من أن المحطات مدخلة مسبقاً.");
        }

    }

    private void editEdge() {
        String from = this.txtEdgeFrom.getText().trim();
        String to = this.txtEdgeTo.getText().trim();

        try {
            int weight = Integer.parseInt(this.txtEdgeWeight.getText().trim());
            this.graph.editEdge(from, to, weight);
            this.log("تم تعديل المسار بنجاح: " + from + " <-> " + to + " ليصبح الوزن الجديد: " + weight);
            this.txtEdgeFrom.setText("");
            this.txtEdgeTo.setText("");
            this.txtEdgeWeight.setText("");
            this.canvas.repaint();
        } catch (NumberFormatException var4) {
            this.log("خطأ: يرجى إدخال رقم صحيح في خانة الوزن.");
        } catch (Exception ex) {
            this.log("خطأ أثناء التعديل: " + ex.getMessage());
        }

    }

    private void sortStations() {
        List<String> sorted = this.graph.sortStationsByConnections();
        StringBuilder sb = new StringBuilder("ترتيب المحطات حسب الاتصالات:\n");

        for(String s : sorted) {
            sb.append(s).append("\n");
        }

        this.log(sb.toString());
    }

    private void checkCycle() {
        boolean hasCycle = this.graph.hasCycle();
        if (hasCycle) {
            this.log("النتيجة: الشبكة تحتوي على حلقات مغلقة (Cycles).");
        } else {
            this.log("النتيجة: الشبكة حرة ولا تحتوي على أي حلقات.");
        }

    }

    private void clearSelection() {
        this.selectedFrom = null;
        this.selectedTo = null;
        this.currentShortestPath.clear();
        this.log("تم إلغاء تحديد المسارات الأقصر.");
        this.canvas.repaint();
    }

    public void calculatePath() {
        try {
            Path p = this.graph.getShortestPath(this.selectedFrom, this.selectedTo);
            int distance = this.graph.getShortestDistance(this.selectedFrom, this.selectedTo);
            String pathStr = p.toString();
            if (pathStr.startsWith("[")) {
                pathStr = pathStr.substring(1);
            }

            if (pathStr.endsWith("]")) {
                pathStr = pathStr.substring(0, pathStr.length() - 1);
            }

            this.currentShortestPath = Arrays.asList(pathStr.split(", "));
            this.log("تم حساب المسار الأقصر! المسافة الإجمالية: " + distance);
            this.log("المسار المتبع: " + pathStr);
        } catch (Exception var4) {
            this.log("لا يوجد مسار متاح أو متصل بين المحطتين المحددتين.");
            this.currentShortestPath.clear();
        }

    }

    public void log(String msg) {
        this.txtOutput.append(msg + "\n");
        this.txtOutput.setCaretPosition(this.txtOutput.getDocument().getLength());
    }

    private JLabel createHeaderLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", 1, 16));
        lbl.setForeground(COLOR_PRIMARY);
        lbl.setAlignmentX(0.5F);
        return lbl;
    }

    private JPanel createLabeledField(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(COLOR_PANEL);
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(COLOR_TEXT);
        panel.add(lbl, "North");
        panel.add(textField, "Center");
        panel.setMaximumSize(new Dimension(300, 50));
        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", 1, 14));
        btn.setMaximumSize(new Dimension(300, 40));
        btn.setAlignmentX(0.5F);
        return btn;
    }

    private void updateNodePositionsAfterImport() {
        String exported = this.graph.exportGraph();
        if (exported != null && !exported.isEmpty()) {
            String[] lines = exported.split("\n");

            for(String line : lines) {
                String[] parts = line.split("->");
                if (parts.length > 0) {
                    String nodeName = parts[0].trim();
                    if (!this.nodePositions.containsKey(nodeName)) {
                        int x = 50 + (int)(Math.random() * (double)(this.canvas.getWidth() - 100));
                        int y = 50 + (int)(Math.random() * (double)(this.canvas.getHeight() - 100));
                        this.nodePositions.put(nodeName, new Point(x, y));
                    }
                }
            }
        }

    }

    public WeightedGraph getGraph() {
        return this.graph;
    }

    public Map<String, Point> getNodePositions() {
        return this.nodePositions;
    }

    public String getSelectedFrom() {
        return this.selectedFrom;
    }

    public void setSelectedFrom(String selectedFrom) {
        this.selectedFrom = selectedFrom;
    }

    public String getSelectedTo() {
        return this.selectedTo;
    }

    public void setSelectedTo(String selectedTo) {
        this.selectedTo = selectedTo;
    }

    public List<String> getCurrentShortestPath() {
        return this.currentShortestPath;
    }
}
