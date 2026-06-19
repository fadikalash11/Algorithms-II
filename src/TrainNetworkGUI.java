import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TrainNetworkGUI extends JFrame {

    // ثوابت الهوية البصرية (يمكنك تعديل الألوان هنا لتطابق الشعار بدقة)
    public static final Color COLOR_BG = new Color(243, 244, 246);       // لون الخلفية العامة
    public static final Color COLOR_PANEL = new Color(255, 255, 255);    // لون لوحة التحكم الجانبية
    public static final Color COLOR_PRIMARY = new Color(30, 58, 138);    // اللون الأساسي للأزرار والعناوين
    public static final Color COLOR_ACCENT = new Color(245, 158, 11);    // لون التحديد والتنبيهات (ذهبي/برتقالي)
    public static final Color COLOR_TEXT = new Color(31, 41, 55);        // لون الخطوط والنصوص
    public static final Color COLOR_NODE = new Color(59, 130, 246);      // اللون الافتراضي للمحطات
    public static final Color COLOR_NODE_HIGHLIGHT = new Color(16, 185, 129); // لون محطات أقصر طريق

    private WeightedGraph graph;
    private Map<String, Point> nodePositions;

    private String selectedFrom = null;
    private String selectedTo = null;
    private List<String> currentShortestPath = new ArrayList<>();

    private GraphCanvas canvas;
    private JTextField txtNodeName, txtEdgeFrom, txtEdgeTo, txtEdgeWeight;
    private JTextArea txtOutput;

    public TrainNetworkGUI() {
        graph = new WeightedGraph();
        nodePositions = new HashMap<>();

        setTitle("Train Network Manager - NAutN UI");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        initUI();
    }

    private void initUI() {
        // بناء لوحة التحكم الجانبية (الطلب 7)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(300, 0));
        controlPanel.setBackground(COLOR_PANEL);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // إدارة وإضافة المحطات
        controlPanel.add(createHeaderLabel("إدارة المحطات"));
        txtNodeName = new JTextField();
        controlPanel.add(createLabeledField("اسم المحطة:", txtNodeName));
        JButton btnAddNode = createStyledButton("إضافة محطة");
        btnAddNode.addActionListener(e -> addNode());
        controlPanel.add(btnAddNode);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // إدارة وإضافة المسارات والأوزان
        controlPanel.add(createHeaderLabel("إدارة المسارات"));
        txtEdgeFrom = new JTextField();
        txtEdgeTo = new JTextField();
        txtEdgeWeight = new JTextField();
        controlPanel.add(createLabeledField("من محطة:", txtEdgeFrom));
        controlPanel.add(createLabeledField("إلى محطة:", txtEdgeTo));
        controlPanel.add(createLabeledField("المسافة (الوزن):", txtEdgeWeight));
        JButton btnAddEdge = createStyledButton("إضافة مسار");
        btnAddEdge.addActionListener(e -> addEdge());
        controlPanel.add(btnAddEdge);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5))); // مسافة صغيرة بين الأزرار
        JButton btnEditEdge = createStyledButton("تعديل مسار قائم");
        btnEditEdge.addActionListener(e -> editEdge());
        controlPanel.add(btnEditEdge);

        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // قسم استدعاء خوارزميات المسألة (الطلب 5 والطلب 6)
        controlPanel.add(createHeaderLabel("العمليات والتحليلات"));
        JButton btnSort = createStyledButton("ترتيب المحطات (حسب الاتصالات)");
        btnSort.addActionListener(e -> sortStations());
        controlPanel.add(btnSort);

        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton btnCycle = createStyledButton("التحقق من وجود حلقة (Cycle)");
        btnCycle.addActionListener(e -> checkCycle());
        controlPanel.add(btnCycle);

        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton btnClearSelection = createStyledButton("مسح التحديد (إلغاء أقصر مسار)");
        btnClearSelection.setBackground(COLOR_ACCENT);
        btnClearSelection.addActionListener(e -> clearSelection());
        controlPanel.add(btnClearSelection);

        // شاشة مراقبة العمليات النصية (Console Log داخلي)
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        txtOutput = new JTextArea();
        txtOutput.setEditable(false);
        txtOutput.setLineWrap(true);
        txtOutput.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtOutput);
        controlPanel.add(scrollPane);

        add(controlPanel, BorderLayout.EAST);

        // استدعاء كلاس الرسم المستقل وربطه بهذه النافذة
        canvas = new GraphCanvas(this);
        add(canvas, BorderLayout.CENTER);
        // ---- التعديل المحدث للتعامل مع ملف txt حقيقي (الطلب 2 والطلب 3) ----
        controlPanel.add(createHeaderLabel("استيراد وتصدير الشبكة (ملف نصي)"));

        JTextArea txtImportExportData = new JTextArea(4, 20);
        txtImportExportData.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane textScroll = new JScrollPane(txtImportExportData);
        controlPanel.add(textScroll);

        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.setBackground(COLOR_PANEL);
        btnPanel.setMaximumSize(new Dimension(300, 40));

        // 1. زر التصدير والحفظ في ملف network.txt
        JButton btnExport = new JButton("تصدير وحفظ");
        btnExport.setBackground(COLOR_PRIMARY);
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.addActionListener(e -> {
            try {
                // استدعاء تابع التصدير الخاص بك
                String exportedData = graph.exportGraph();
                txtImportExportData.setText(exportedData); // عرضه على الشاشة

                // كتابة النص داخل ملف حقيقي على الجاهز
                java.io.FileWriter writer = new java.io.FileWriter("network.txt");
                writer.write(exportedData);
                writer.close();

                log("تم تصدير الشبكة وحفظها بنجاح في ملف: network.txt");
            } catch (Exception ex) {
                log("خطأ أثناء الحفظ في الملف: " + ex.getMessage());
            }
        });

        // 2. زر الاستيراد والقراءة من ملف network.txt
        JButton btnImport = new JButton("قراءة واستيراد");
        btnImport.setBackground(COLOR_PRIMARY);
        btnImport.setForeground(Color.WHITE);
        btnImport.setFocusPainted(false);
        btnImport.addActionListener(e -> {
            try {
                java.io.File file = new java.io.File("network.txt");
                if (!file.exists()) {
                    log("خطأ: الملف network.txt غير موجود! قم بالتصدير أولاً لإنشائه.");
                    return;
                }

                // قراءة محتوى الملف النصي بالكامل
                java.util.Scanner scanner = new java.util.Scanner(file);
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine()).append("\n");
                }
                scanner.close();

                String fileContent = sb.toString().trim();
                txtImportExportData.setText(fileContent); // عرضه على الشاشة

                // استدعاء تابع الاستيراد الخاص بك لبناء الـ Graph في الخلفية
                graph.importGraph(fileContent);

                // مزامنة الإحداثيات على الشاشة ورسمها
                updateNodePositionsAfterImport();

                log("تم قراءة الملف network.txt واستيراد الشبكة بنجاح!");
                canvas.repaint();

            } catch (Exception ex) {
                log("خطأ أثناء قراءة الملف: " + ex.getMessage());
            }
        });

        btnPanel.add(btnExport);
        btnPanel.add(btnImport);
        controlPanel.add(btnPanel);

        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        // -----------------------------------------------------------
    }

    private void addNode() {
        String name = txtNodeName.getText().trim();
        if (!name.isEmpty() && !nodePositions.containsKey(name)) {
            graph.addNode(name, name);
            // إحداثيات افتراضية عشوائية عند الإنشاء، يمكن سحبها لاحقاً بالكامل بالماوس
            int x = 50 + (int) (Math.random() * (canvas.getWidth() - 100));
            int y = 50 + (int) (Math.random() * (canvas.getHeight() - 100));
            nodePositions.put(name, new Point(x, y));
            log("تم إضافة المحطة: " + name);
            txtNodeName.setText("");
            canvas.repaint();
        }
    }

    private void addEdge() {
        String from = txtEdgeFrom.getText().trim();
        String to = txtEdgeTo.getText().trim();
        try {
            int weight = Integer.parseInt(txtEdgeWeight.getText().trim());
            graph.addEdge(from, to, weight);
            log("تم إضافة مسار: " + from + " -> " + to + " (" + weight + ")");
            txtEdgeFrom.setText("");
            txtEdgeTo.setText("");
            txtEdgeWeight.setText("");
            canvas.repaint();
        } catch (NumberFormatException ex) {
            log("خطأ: يرجى إدخال رقم صحيح للوزن.");
        } catch (IllegalArgumentException ex) {
            log("خطأ: تأكد من أن المحطات مدخلة مسبقاً.");
        }
    }
    // --- أضف ميثود الواجهة هذه هنا لتأخذ البيانات وترسلها لكلاس الـ Graph ---
    private void editEdge() {
        String from = txtEdgeFrom.getText().trim();
        String to = txtEdgeTo.getText().trim();
        try {
            int weight = Integer.parseInt(txtEdgeWeight.getText().trim());

            // استدعاء الميثود البرمجية التي أضفناها في الخطوة الأولى
            graph.editEdge(from, to, weight);

            log("تم تعديل المسار بنجاح: " + from + " <-> " + to + " ليصبح الوزن الجديد: " + weight);

            // تفريغ الحقول النصية بعد التعديل الناجح
            txtEdgeFrom.setText("");
            txtEdgeTo.setText("");
            txtEdgeWeight.setText("");

            // إعادة رسم الشبكة فوراً لتحديث الأرقام على الشاشة
            canvas.repaint();

        } catch (NumberFormatException ex) {
            log("خطأ: يرجى إدخال رقم صحيح في خانة الوزن.");
        } catch (Exception ex) {
            // سيلتقط أي خطأ إذا كانت المحطات أو المسار غير موجود ويطبعه في الـ Console الداخلي
            log("خطأ أثناء التعديل: " + ex.getMessage());
        }
    }

    private void sortStations() {
        List<String> sorted = graph.sortStationsByConnections();
        StringBuilder sb = new StringBuilder("ترتيب المحطات حسب الاتصالات:\n");
        for (String s : sorted) {
            sb.append(s).append("\n");
        }
        log(sb.toString());
    }

    private void checkCycle() {
        boolean hasCycle = graph.hasCycle();
        if (hasCycle) {
            log("النتيجة: الشبكة تحتوي على حلقات مغلقة (Cycles).");
        } else {
            log("النتيجة: الشبكة حرة ولا تحتوي على أي حلقات.");
        }
    }

    private void clearSelection() {
        selectedFrom = null;
        selectedTo = null;
        currentShortestPath.clear();
        log("تم إلغاء تحديد المسارات الأقصر.");
        canvas.repaint();
    }

    // استدعاء خوارزمية حساب أقصر طريق الخاصة بك من كلاس WeightedGraph
    public void calculatePath() {
        try {
            Path p = graph.getShortestPath(selectedFrom, selectedTo);
            int distance = graph.getShortestDistance(selectedFrom, selectedTo);

            String pathStr = p.toString();
            if(pathStr.startsWith("[")) pathStr = pathStr.substring(1);
            if(pathStr.endsWith("]")) pathStr = pathStr.substring(0, pathStr.length() - 1);

            currentShortestPath = Arrays.asList(pathStr.split(", "));
            log("تم حساب المسار الأقصر! المسافة الإجمالية: " + distance);
            log("المسار المتبع: " + pathStr);

        } catch (Exception ex) {
            log("لا يوجد مسار متاح أو متصل بين المحطتين المحددتين.");
            currentShortestPath.clear();
        }
    }

    public void log(String msg) {
        txtOutput.append(msg + "\n");
        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
    }

    private JLabel createHeaderLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setForeground(COLOR_PRIMARY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JPanel createLabeledField(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(COLOR_PANEL);
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(COLOR_TEXT);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(300, 50));
        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setMaximumSize(new Dimension(300, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
    // --- أضف هذه الميثود هنا لمزامنة الرسم بعد عملية الاستيراد النصي ---
    private void updateNodePositionsAfterImport() {
        // نمر على كل المحطات الموجودة في كلاس الـ graph الخاص بك حالياً
        // ملاحظة: بما أن التابع الأصلي يعيد النص، يمكنك قراءة أسماء المحطات عبر تصديرها مؤقتاً أو الوصول للـ nodes إذا كانت مرئية
        // الطريقة الأكثر أماناً دون تعديل كلاسك هي قراءة المحطات المتوفرة وإضافتها للخريطة الرسومية إن لم تكن موجودة:
        String exported = graph.exportGraph();
        if (exported != null && !exported.isEmpty()) {
            String[] lines = exported.split("\n");
            for (String line : lines) {
                String[] parts = line.split("->");
                if (parts.length > 0) {
                    String nodeName = parts[0].trim();
                    // إذا كانت المحطة المستوردة ليس لها إحداثيات على الشاشة، نعطيها موقعاً عشوائياً
                    if (!nodePositions.containsKey(nodeName)) {
                        int x = 50 + (int) (Math.random() * (canvas.getWidth() - 100));
                        int y = 50 + (int) (Math.random() * (canvas.getHeight() - 100));
                        nodePositions.put(nodeName, new Point(x, y));
                    }
                }
            }
        }
    }

    // Getters & Setters للسماح لكلاس الرسم بالوصول الآمن لبيانات الواجهة الحالية
    public WeightedGraph getGraph() { return graph; }
    public Map<String, Point> getNodePositions() { return nodePositions; }
    public String getSelectedFrom() { return selectedFrom; }
    public void setSelectedFrom(String selectedFrom) { this.selectedFrom = selectedFrom; }
    public String getSelectedTo() { return selectedTo; }
    public void setSelectedTo(String selectedTo) { this.selectedTo = selectedTo; }
    public List<String> getCurrentShortestPath() { return currentShortestPath; }


}