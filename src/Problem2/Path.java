package Problem2;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.ArrayList;
import java.util.List;

public class Path {
    private List<String> nodes = new ArrayList();

    public Path() {
    }

    public void add(String node) {
        this.nodes.add(node);
    }

    public String toString() {
        return this.nodes.toString();
    }
}
