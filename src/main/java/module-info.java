module com.example.dijkstra_gaza {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dijkstra_gaza to javafx.fxml;
    exports com.example.dijkstra_gaza;
}