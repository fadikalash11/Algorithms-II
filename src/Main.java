//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {




    WeightedGraph graph = new WeightedGraph();

    System.out.println("=== 1. اختبار استيراد وتصدير الملفات (الطلبات 2 و 3) ===");

    String testData = "Damascus -> Homs(120), Daraa(90)\n" +
            "Aleppo -> Homs(180), Tartous(95)\n" +
            "Homs -> Tartous(90)";

    graph.importGraph(testData);
    System.out.println("شكل الشبكة بعد الاستيراد:\n" + graph.exportGraph());

    System.out.println("\n=== 2. اختبار أقصر طريق - Dijkstra (الطلب 4) ===");
    try {
        int distance = graph.getShortestDistance("Damascus", "Tartous");
        System.out.println("أقصر مسافة من دمشق إلى طرطوس هي: " + distance);
    } catch (Exception e) {
        System.out.println("عذراً، المسار غير موجود أو هناك خطأ.");
    }

    System.out.println("\n=== 3. اختبار اكتشاف الدورات - Cycle (الطلب 5) ===");
    boolean cycle = graph.hasCycle();
    System.out.println("هل تحتوي الشبكة على دورة مغلقة؟ " + (cycle ? "نعم" : "لا"));

    System.out.println("جاري إضافة مسار عودة من طرطوس إلى دمشق لخلق دورة...");
    graph.addEdge("Tartous", "Damascus", 200);
    System.out.println("هل تحتوي الشبكة على دورة مغلقة الآن؟ " + (graph.hasCycle() ? "نعم" : "لا"));

    System.out.println("\n=== 4. اختبار ترتيب المحطات - Sorting (الطلب 6) ===");
    List<String> sortedStations = graph.sortStationsByConnections();
    System.out.println("المحطات مرتبة من الأكثر ازدحاماً للأقل:");
    for (String station : sortedStations) {
        System.out.println("- " + station);











//    graph.addNode("A");
//    graph.addNode("B");
//    graph.addNode("C");
//    graph.addNode("D");
//
//    graph.addEdge("A","B");
//    graph.addEdge("B","C");
//    graph.addEdge("C","A");
//    graph.addEdge("D","A");
//
//    graph.print();
//    graph.traverseBreadthFirst("A");
//    System.out.println();
//  var list=graph.topologicalSort();
//   System.out.println(list);
//    System.out.println();
//    System.out.print(graph.hasCycle());

//
//    System.out.println("Pre order ");
//    mytree.traversePreorder();
//    System.out.println();
//    System.out.println("In order ");
//    mytree.traverseInorder();
//    System.out.println();
//    System.out.println("Post order ");
//mytree.traversePostorder();
//    System.out.println();
//    System.out.println(mytree.heigth());
//    System.out.println(mytree.min());

    }}
