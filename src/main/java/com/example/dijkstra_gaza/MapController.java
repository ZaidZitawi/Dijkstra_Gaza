package com.example.dijkstra_gaza;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MapController implements Initializable {

    @FXML
    private ImageView mapImageView;
    @FXML
    private AnchorPane wp;
    @FXML
    private ComboBox<String> from;
    @FXML
    private ComboBox<String> to;
    @FXML
    private Button findPathButton;
    @FXML
    private TextArea path;
    @FXML
    private TextArea cost;
    private List<Line> pathLines = new ArrayList<>();

    //these geographical data is used for converting coordinates on maps to screen coordinates.
    private final double leftBottomLat = 31.224277;
    private final double leftBottomLong = 34.165328;
    private final double leftTopLat = 31.595872;
    private final double rightBottomLong = 34.590684;
    //image width and height
    private double scaleX;
    private double scaleY;
    private int clickCount = 0; // track clicks


    //stores network of cities where name of city with the node (City and St)
    private Map<String, Node> nodes = new HashMap<>();


    //Reads data from a fileو initializes nodes with lat and log and sets up adjacencies (connections between nodes with distances).
    private void loadData() throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File("src/data.txt"))) {
            int numberOfPlaces = scanner.nextInt();//store number of cities
            int numberOfAdjacencies = scanner.nextInt();//same for adj
            scanner.nextLine();

            for (int i = 0; i < numberOfPlaces; i++) {
                String line = scanner.nextLine();
                String[] parts = line.split("\\s+");
                double lat = Double.parseDouble(parts[parts.length - 2]);
                double lon = Double.parseDouble(parts[parts.length - 1]);

                String name = String.join(" ", Arrays.copyOf(parts, parts.length - 2));
                //add the data into the nodes hashmap
                nodes.put(name, new Node(name, lat, lon));
                drawLocation(name, lat, lon);
            }

            //reading the other part of file
            for (int i = 0; i < numberOfAdjacencies; i++) {
                String line = scanner.nextLine();
                String[] parts = line.split("#");
                Node node1 = nodes.get(parts[0]);// retrive node from hashmap nodes based on the key city name which is parts[0]
                Node node2 = nodes.get(parts[1]);

                //calculating the distance between node 1 and 2
                if (node1 != null && node2 != null) {
                    double distance = calculateDistance(node1.getLatitude(), node1.getLongitude(),
                            node2.getLatitude(), node2.getLongitude());
                    node1.addAdjacentNode(node2, distance);
                }
            }
        }
    }


    //fill the comboboxes with cities using the stream
    private void populateComboBoxes() {
        List<String> placeNames = nodes.values().stream()
                .map(Node::getName)
                .filter(name -> !(name.startsWith("S") && Character.isDigit(name.charAt(1))))
                .sorted()
                .collect(Collectors.toList());

        from.getItems().setAll(placeNames);
        to.getItems().setAll(placeNames);
    }

    //clear the map when choosing a new path
    private void clearPreviousPath() {
        for (Line line : pathLines) {
            wp.getChildren().remove(line);
        }
        pathLines.clear();
    }



    private void drawLocation(String name, double lat, double lon) throws FileNotFoundException {
        // Check if the name starts with "S" followed by a digit
        if (name.startsWith("S") && Character.isDigit(name.charAt(1))) {
            return;
        }

        double x = (lon - leftBottomLong) * scaleX;
        double y = (leftTopLat - lat) * scaleY;
        Image image = new Image(new FileInputStream("hghgh.jpg"));
        ImageView locationIcon = new ImageView(image);
        locationIcon.setFitHeight(24);
        locationIcon.setFitWidth(24);

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.BLACK);
        nameLabel.setFont(new Font("Arial", 11));

        Tooltip tooltip = new Tooltip(name);
        Tooltip.install(locationIcon, tooltip);
        Tooltip.install(nameLabel, tooltip);

        HBox locationGroup = new HBox();
        locationGroup.getChildren().addAll(locationIcon, nameLabel);
        locationGroup.setLayoutX(x - locationIcon.getFitWidth() / 2);
        locationGroup.setLayoutY(y - locationIcon.getFitHeight() / 2);
        locationGroup.setAlignment(Pos.CENTER);
        wp.getChildren().add(locationGroup);

        //mouse event handler
        locationGroup.setOnMouseClicked(event -> {
            clickCount++;
            if (clickCount == 1) {
                // First click - select the starting point
                from.getSelectionModel().select(name);
            } else if (clickCount == 2) {
                // Second click - select the destination
                to.getSelectionModel().select(name);
            } else if (clickCount == 3) {
                // Third click - clear the previous path and reset selections
                clearPreviousPath();
                from.getSelectionModel().clearSelection();
                to.getSelectionModel().clearSelection();
                path.clear();
                cost.clear();
                clickCount = 1; // Reset clickCount to 1 for the next path
                from.getSelectionModel().select(name); // Select the new starting point
            }
        });
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //make the image bindable
        mapImageView.boundsInLocalProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getWidth() > 0 && newValue.getHeight() > 0) {
                calculateScale(newValue.getWidth(), newValue.getHeight());
                try {
                    loadData();
                    populateComboBoxes();
//                    setupComboBoxListeners();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        findPathButton.setOnAction(event -> {
            String fromPlace = from.getValue();
            String toPlace = to.getValue();

            if (fromPlace != null && toPlace != null) {
                List<Node> shortestPath = findShortestPath(fromPlace, toPlace);/////////////////////////////
                displayPathAndCost(shortestPath);
                drawPathOnMap(shortestPath);
            }
        });

    }

    //converting the geograpical coordinates into pixle cordinate
    private void calculateScale(double width, double height) {
        scaleX = width / (rightBottomLong - leftBottomLong);//how much pixles in the interface represents one deg in lon
        scaleY = height / (leftTopLat - leftBottomLat);//same
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    private List<Node> findShortestPath(String from, String to) {
        //This map is used to track the shortest known distance from the start node to every other node in the graph.
        Map<Node, Double> distances = new HashMap<>();
        //keeps track of the shortest path tree. It is used to reconstruct the shortest path from the start node to the dest
        Map<Node, Node> predecessors = new HashMap<>();
        //select the next node to visit based on the shortest distance calculated so far.
        PriorityQueue<Node> nodesQueue = new PriorityQueue<>(Comparator.comparing(distances::get));

        for (Node node : nodes.values()) {// return Each Node object represents a location or a point on the map
            distances.put(node, Double.MAX_VALUE);//each node with distance but the distance not set yet
        }

        Node start = nodes.get(from);
        if (start == null) return Collections.emptyList();

        distances.put(start, 0.0);
        nodesQueue.addAll(nodes.values());//put the nodes (cities and streets)
        while (!nodesQueue.isEmpty()) {
            Node current = nodesQueue.poll();


            //choose the minimum distance between sourc and dest
            for (AdjacentNode adjacentNode : current.getAdjacentNodes()) {
                Node adjacent = adjacentNode.getNode();
                double newDist = distances.get(current) + adjacentNode.getDistance();

                if (newDist < distances.get(adjacent)) {
                    distances.put(adjacent, newDist);
                    predecessors.put(adjacent, current);

                    nodesQueue.remove(adjacent);
                    nodesQueue.add(adjacent);
                }
            }
        }


        //returns the shortest path in an arraylist
        return reconstructPath(predecessors, start, nodes.get(to));
    }

    private List<Node> reconstructPath(Map<Node, Node> predecessors, Node start, Node end) {
        LinkedList<Node> path = new LinkedList<>();
        if (end == null || !predecessors.containsKey(end)) return path;//end base case

        Node at = end;
        while (at != null && !at.equals(start)) {
            path.addFirst(at);
            at = predecessors.get(at);
        }

        if (at != null) path.addFirst(start);
        return path;
    }


    private void displayPathAndCost(List<Node> path) {
        this.path.clear();
        this.cost.clear();
        if (path.isEmpty()) {
            this.path.setText("No path found.");
            cost.setText("0");
            return;
        }

        String pathString = path.stream().map(Node::getName).collect(Collectors.joining("\n"));
        Double totalCost = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node node = path.get(i);
            Node nextNode = path.get(i + 1);
            totalCost += calculateDistance(node.getLatitude(), node.getLongitude(), nextNode.getLatitude(), nextNode.getLongitude());
        }

        this.path.setText(pathString);
        cost.setText(String.format("%.2f km", totalCost));
    }

    private void drawPathOnMap(List<Node> path) {
        clearPreviousPath();
        for (int i = 0; i < path.size() - 1; i++) {
            Node node = path.get(i);
            Node nextNode = path.get(i + 1);

            double x1 = (node.getLongitude() - leftBottomLong) * scaleX;
            double y1 = (leftTopLat - node.getLatitude()) * scaleY;
            double x2 = (nextNode.getLongitude() - leftBottomLong) * scaleX;
            double y2 = (leftTopLat - nextNode.getLatitude()) * scaleY;

            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.BLUE);
            line.setStrokeWidth(2);
            pathLines.add(line);
            wp.getChildren().add(line);
        }
    }


}
