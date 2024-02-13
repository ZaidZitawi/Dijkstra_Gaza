package com.example.dijkstra_gaza;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Node {
    private String name;
    private double latitude;
    private double longitude;

    //implements the connection between nodes
    private Set<AdjacentNode> adjacentNodes = new HashSet<>();
    public Node(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void addAdjacentNode(Node adjacent, double distance) {
         //رايح جاي
        adjacentNodes.add(new AdjacentNode(adjacent, distance));
        adjacent.adjacentNodes.add(new AdjacentNode(this, distance));
    }

    public Set<AdjacentNode> getAdjacentNodes() {
        return adjacentNodes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return Objects.equals(name, node.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", adjacentNodes=" + adjacentNodes +
                '}';
    }
}
