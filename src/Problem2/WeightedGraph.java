//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package Problem2;//

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

public class WeightedGraph {
    private Map<String, Node> nodes = new HashMap();

    public WeightedGraph() {
    }

    public void addNode(String label, String symbol) {
        this.nodes.putIfAbsent(label, new Node(label, symbol));
    }

    public void addEdge(String from, String to, int weight) {
        Node fromNode = (Node)this.nodes.get(from);
        if (fromNode == null) {
            throw new IllegalArgumentException();
        } else {
            Node toNode = (Node)this.nodes.get(to);
            if (toNode == null) {
                throw new IllegalArgumentException();
            } else {
                fromNode.addEdge(toNode, weight);
                toNode.addEdge(fromNode, weight);
            }
        }
    }

    public void print() {
        for(Node node : this.nodes.values()) {
            List<Edge> edges = node.getEdges();
            if (!edges.isEmpty()) {
                PrintStream var10000 = System.out;
                String var10001 = String.valueOf(node);
                var10000.println(var10001 + " is connected to " + String.valueOf(edges));
            }
        }

    }

    public int getShortestDistance(String from, String to) {
        Node fromNode = (Node)this.nodes.get(from);
        Map<Node, Integer> distunces = new HashMap();

        for(Node node : this.nodes.values()) {
            distunces.put(node, Integer.MAX_VALUE);
        }

        distunces.replace(fromNode, 0);
        Set<Node> visited = new HashSet();
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingInt((NodeEntry ne) -> ne.priority));        queue.add(new NodeEntry(fromNode, 0));

        while(!queue.isEmpty()) {
            Node current = ((NodeEntry)queue.remove()).node;
            visited.add(current);

            for(Edge edge : current.getEdges()) {
                if (!visited.contains(edge.to)) {
                    int NewDictence = (Integer)distunces.get(current) + edge.weight;
                    if (NewDictence < (Integer)distunces.get(edge.to)) {
                        distunces.replace(edge.to, NewDictence);
                    }

                    queue.add(new NodeEntry(edge.to, NewDictence));
                }
            }
        }

        return (Integer)distunces.get(this.nodes.get(to));
    }

    public Path getShortestPath(String from, String to) {
        Node fromNode = (Node)this.nodes.get(from);
        if (fromNode == null) {
            throw new IllegalArgumentException();
        } else {
            Node toNode = (Node)this.nodes.get(to);
            if (toNode == null) {
                throw new IllegalArgumentException();
            } else {
                Map<Node, Integer> distunces = new HashMap();

                for(Node node : this.nodes.values()) {
                    distunces.put(node, Integer.MAX_VALUE);
                }

                distunces.replace(fromNode, 0);
                Map<Node, Node> previousNodes = new HashMap();
                Set<Node> visited = new HashSet();
                PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingInt((NodeEntry ne) -> ne.priority));                queue.add(new NodeEntry(fromNode, 0));

                while(!queue.isEmpty()) {
                    Node current = ((NodeEntry)queue.remove()).node;
                    visited.add(current);

                    for(Edge edge : current.getEdges()) {
                        if (!visited.contains(edge.to)) {
                            int NewDictence = (Integer)distunces.get(current) + edge.weight;
                            if (NewDictence < (Integer)distunces.get(edge.to)) {
                                distunces.replace(edge.to, NewDictence);
                                previousNodes.put(edge.to, current);
                                queue.add(new NodeEntry(edge.to, NewDictence));
                            }
                        }
                    }
                }

                return this.buildPath(previousNodes, toNode);
            }
        }
    }

    private Path buildPath(Map<Node, Node> previousNodes, Node toNode) {
        Stack<Node> stack = new Stack();
        stack.push(toNode);

        for(Node previous = (Node)previousNodes.get(toNode); previous != null; previous = (Node)previousNodes.get(previous)) {
            stack.push(previous);
        }

        Path path = new Path();

        while(!stack.isEmpty()) {
            path.add(((Node)stack.pop()).label);
        }

        return path;
    }

    public String exportGraph() {
        StringBuilder sb = new StringBuilder();

        for(Node node : this.nodes.values()) {
            List<Edge> edges = node.getEdges();
            if (!edges.isEmpty()) {
                sb.append(node.label).append(" -> ");
                List<String> edgeStrings = new ArrayList();

                for(Edge edge : edges) {
                    edgeStrings.add(edge.to.label + "(" + edge.weight + ")");
                }

                sb.append(String.join(", ", edgeStrings)).append("\n");
            }
        }

        return sb.toString().trim();
    }

    public void importGraph(String data) {
        String[] lines = data.split("\n");

        for(String line : lines) {
            if (!line.trim().isEmpty()) {
                String[] parts = line.split("->");
                String fromLabel = parts[0].trim();
                this.addNode(fromLabel, fromLabel);
                if (parts.length > 1) {
                    String[] destinations = parts[1].split(",");

                    for(String dest : destinations) {
                        dest = dest.trim();
                        int openParen = dest.indexOf(40);
                        int closeParen = dest.indexOf(41);
                        if (openParen != -1 && closeParen != -1) {
                            String toLabel = dest.substring(0, openParen).trim();
                            int weight = Integer.parseInt(dest.substring(openParen + 1, closeParen).trim());
                            this.addNode(toLabel, toLabel);

                            try {
                                this.addEdge(fromLabel, toLabel, weight);
                            } catch (Exception var19) {
                            }
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
            this.importGraph(data);
            String exportedData = this.exportGraph();
            Files.writeString(Paths.get(outputFilePath), exportedData);
            System.out.println("تم حفظ الشبكة في الملف: " + outputFilePath);
        } catch (Exception e) {
            System.out.println("حدث خطأ أثناء التعامل مع الملفات: " + e.getMessage());
        }

    }

    public List<String> sortStationsByConnections() {
        List<Node> sortedNodes = new ArrayList(this.nodes.values());
        sortedNodes.sort((n1, n2) -> Integer.compare(n2.getEdges().size(), n1.getEdges().size()));
        List<String> result = new ArrayList();

        for(Node node : sortedNodes) {
            String var10001 = node.label;
            result.add(var10001 + " (" + node.getEdges().size() + " connections)");
        }

        return result;
    }

    public boolean hasCycle() {
        Set<Node> visited = new HashSet();

        for(Node node : this.nodes.values()) {
            if (!visited.contains(node) && this.hasCycle(node, (Node)null, visited)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasCycle(Node node, Node parent, Set<Node> visited) {
        visited.add(node);

        for(Edge edge : node.getEdges()) {
            if (edge.to != parent) {
                if (visited.contains(edge.to)) {
                    return true;
                }

                if (this.hasCycle(edge.to, node, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void editEdge(String from, String to, int newWeight) {
        Node fromNode = (Node)this.nodes.get(from);
        Node toNode = (Node)this.nodes.get(to);
        if (fromNode != null && toNode != null) {
            boolean found = false;

            for(Edge edge : fromNode.getEdges()) {
                if (edge.to == toNode) {
                    edge.weight = newWeight;
                    found = true;
                }
            }

            for(Edge edge : toNode.getEdges()) {
                if (edge.to == fromNode) {
                    edge.weight = newWeight;
                }
            }

            if (!found) {
                throw new NoSuchElementException("لا يوجد مسار قائم بين هاتين المحطتين لتعديله.");
            }
        } else {
            throw new IllegalArgumentException("المحطات المدخلة غير موجودة في الشبكة.");
        }
    }

    private class Node {
        private String label;
        private String symbol;
        private List<Edge> edges;

        public Node(String label, String symbol) {
            Objects.requireNonNull(WeightedGraph.this);
            super();
            this.edges = new ArrayList();
            this.symbol = symbol;
            this.label = label;
        }

        public String toString() {
            return this.label;
        }

        public void addEdge(Node to, int weight) {
            this.edges.add(WeightedGraph.this.new Edge(this, to, weight));
        }

        public List<Edge> getEdges() {
            return this.edges;
        }
    }

    private class Edge {
        private Node from;
        private Node to;
        private int weight;

        public Edge(Node from, Node to, int weight) {
            Objects.requireNonNull(WeightedGraph.this);
            super();
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public String toString() {
            String var10000 = String.valueOf(this.from);
            return var10000 + "->" + String.valueOf(this.to);
        }
    }

    private class NodeEntry {
        private Node node;
        private int priority;

        public NodeEntry(Node node, int priority) {
            Objects.requireNonNull(WeightedGraph.this);
            super();
            this.node = node;
            this.priority = priority;
        }
    }
}
