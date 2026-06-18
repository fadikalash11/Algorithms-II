import javax.swing.event.ListDataEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WeightedGraph {
    private  class Node
    {
        private  String label;
        private String  symbol;
        private  List<Edge> edges=new ArrayList<>();

        public  Node(String label,String  symbol)
        {

            this.symbol=symbol;
            this.label=label;
        }


        @Override
        public String toString() {
            return label;
        }

        public void addEdge(Node to , int weight)
        {
            edges.add(new Edge(this,to,weight));
        }

        public  List<Edge>getEdges()
        {
            return edges;
        }
    }
    private  class Edge {
        private Node from ;
        private Node to ;
        private int weight;
        public  Edge(Node from ,Node to ,int weight)
        {
            this.from=from;
            this.to=to;
            this.weight=weight;
        }

        @Override
        public String toString() {
            return from+ "->" +to;  // A->B
        }
    }

    private Map<String,Node>nodes=new HashMap<>();

    public void addNode(String label ,String symbol) {
        nodes.putIfAbsent(label, new Node(label,symbol));
    }

    public void addEdge(String from, String to,int weight) {
        var fromNode = nodes.get(from);
        if (fromNode == null)
            throw new IllegalArgumentException();

        var toNode = nodes.get(to);
        if (toNode == null)
            throw new IllegalArgumentException();

        fromNode.addEdge(toNode,weight);
        toNode.addEdge(fromNode,weight);
    }

    public void print() {
        for (var node : nodes.values()) {
            var edges = node.getEdges();
            if (!edges.isEmpty())
                System.out.println(node + " is connected to " + edges);
        }
    }

    private  class NodeEntry{
        private Node node;
        private int priority;

        public NodeEntry(Node node, int priority) {
            this.node = node;
            this.priority = priority;
        }
    }
    public  int getShortestDistance(String from ,String to)
    {
        var fromNode=nodes.get(from);
        Map<Node,Integer>distunces=new HashMap<>();
        for (var node :nodes.values())
            distunces.put(node,Integer.MAX_VALUE);
        distunces.replace(fromNode,0);
        Set<Node>visited=new HashSet<>();


        Map<Node,Node>previousNodes;
        PriorityQueue<NodeEntry> queue=new PriorityQueue<>(
                Comparator.comparingInt(ne->ne.priority)
        );
        queue.add(new NodeEntry(fromNode,0));
        while (!queue.isEmpty())
        {
            var current=queue.remove().node;
            visited.add(current);
            for (var edge:current.getEdges())
            {

                if(visited.contains(edge.to))
                    continue;

                var NewDictence=distunces.get(current)+edge.weight;

                if(NewDictence<distunces.get(edge.to))
                    distunces.replace(edge.to,NewDictence);
                queue.add(new NodeEntry(edge.to,NewDictence));

            }


        }

        return distunces.get(nodes.get(to));
    }

    public  Path getShortestPath(String from ,String to)
    {
        var fromNode=nodes.get(from);
        if(fromNode==null)
            throw  new IllegalArgumentException();

        var toNode=nodes.get(to);
        if(toNode==null)
            throw new IllegalArgumentException();
        Map<Node,Integer>distunces=new HashMap<>();
        for (var node :nodes.values())
            distunces.put(node,Integer.MAX_VALUE);
        distunces.replace(fromNode,0);
        Map<Node,Node>previousNodes=new HashMap<>();
        Set<Node>visited=new HashSet<>();


        PriorityQueue<NodeEntry> queue=new PriorityQueue<>(
                Comparator.comparingInt(ne->ne.priority)
        );
        queue.add(new NodeEntry(fromNode,0));
        while (!queue.isEmpty())
        {
            var current=queue.remove().node;
            visited.add(current);
            for (var edge:current.getEdges())
            {

                if(visited.contains(edge.to))
                    continue;

                var NewDictence=distunces.get(current)+edge.weight;

                if(NewDictence<distunces.get(edge.to)) {
                    distunces.replace(edge.to, NewDictence);
                    previousNodes.put(edge.to, current);
                    queue.add(new NodeEntry(edge.to, NewDictence));
                }
            }


        }

        return buildPath(previousNodes,toNode);
    }

    private Path buildPath(Map<Node ,Node>previousNodes, Node toNode)
    {
        Stack<Node>stack=new Stack<>();
        stack.push(toNode);
        var previous=previousNodes.get(toNode);
        while (previous!=null)
        {
            stack.push(previous);
            previous=previousNodes.get(previous);
        }
        var path=new Path();
        while (!stack.isEmpty())
            path.add(stack.pop().label);
        return path;

    }

    public  String exportGraph()
    {
        StringBuilder sb=new StringBuilder();
        for(var node:nodes.values())
        {
            var edges=node.getEdges();
            if(edges.isEmpty())continue;
            sb.append(node.label).append(" -> ");

            List<String>edgeStrings=new ArrayList<>();
            for (var edge:edges)
            {
                edgeStrings.add(edge.to.label+"("+edge.weight+")");

            }
            sb.append(String.join(", ",edgeStrings)).append("\n");
        }
        return sb.toString().trim();
    }
    public  void importGraph(String  data)
    {
        String[] lines=data.split("\n");
        for (String line:lines)
        {
            if(line.trim().isEmpty())continue;

            String[] parts=line.split("->");
            String fromLabel=parts[0].trim();
            addNode(fromLabel,fromLabel);
            if (parts.length > 1) {
                String[] destinations = parts[1].split(",");
                for (String dest : destinations) {
                    dest = dest.trim(); // مثلاً: Homs(120)

                    int openParen = dest.indexOf('(');
                    int closeParen = dest.indexOf(')');

                    if (openParen != -1 && closeParen != -1) {
                        String toLabel = dest.substring(0, openParen).trim();
                        int weight = Integer.parseInt(dest.substring(openParen + 1, closeParen).trim());

                        addNode(toLabel, toLabel);
                        try {
                            addEdge(fromLabel, toLabel, weight);
                        } catch (Exception e) {
                        }
                    }
                }
            }


        }
    }




    public void processGraphFiles(String inputFilePath, String outputFilePath) {
        try {
            String data = Files.readString(Paths.get(inputFilePath));
            System.out.println("تمت قراءة الملف بنجاح. جاري بناء الشبكة...");

            importGraph(data);

            String exportedData = exportGraph();

            Files.writeString(Paths.get(outputFilePath), exportedData);
            System.out.println("تم حفظ الشبكة في الملف: " + outputFilePath);

        } catch (Exception e) {
            System.out.println("حدث خطأ أثناء التعامل مع الملفات: " + e.getMessage());
        }
    }
    public List<String> sortStationsByConnections() {
        List<Node> sortedNodes = new ArrayList<>(nodes.values());

        sortedNodes.sort((n1, n2) -> Integer.compare(n2.getEdges().size(), n1.getEdges().size()));

        List<String> result = new ArrayList<>();
        for (var node : sortedNodes) {
            result.add(node.label + " (" + node.getEdges().size() + " connections)");
        }
        return result;
    }public boolean hasCycle() {
        Set<Node> visited = new HashSet<>();
        for (var node : nodes.values()) {
            if (!visited.contains(node)) {
                if (hasCycle(node, null, visited))
                    return true;
            }
        }
        return false;
    }

    private boolean hasCycle(Node node, Node parent, Set<Node> visited) {
        visited.add(node);
        for (var edge : node.getEdges()) {
            if (edge.to == parent)
                continue;
            if (visited.contains(edge.to))
                return true;
            if (hasCycle(edge.to, node, visited))
                return true;
        }
        return false;
    }
    public void editEdge(String from, String to, int newWeight) {
        var fromNode = nodes.get(from);
        var toNode = nodes.get(to);

        if (fromNode == null || toNode == null)
            throw new IllegalArgumentException("المحطات المدخلة غير موجودة في الشبكة.");

        boolean found = false;

        for (var edge : fromNode.getEdges()) {
            if (edge.to == toNode) {
                edge.weight = newWeight;
                found = true;
            }
        }

        for (var edge : toNode.getEdges()) {
            if (edge.to == fromNode) {
                edge.weight = newWeight;
            }
        }

        if (!found) {
            throw new NoSuchElementException("لا يوجد مسار قائم بين هاتين المحطتين لتعديله.");
        }
    }
}
