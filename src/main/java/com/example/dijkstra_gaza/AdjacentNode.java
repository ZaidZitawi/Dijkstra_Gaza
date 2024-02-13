package com.example.dijkstra_gaza;

public class AdjacentNode {
    private Node node;
    private double distance;

    public AdjacentNode(Node node, double distance) {
        this.node = node;
        this.distance = distance;
    }

    public Node getNode() {
        return node;
    }

    public double getDistance() {
        return distance;
    }
}
