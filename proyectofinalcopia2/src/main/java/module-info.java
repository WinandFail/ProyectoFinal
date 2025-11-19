module com.example.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.proyectofinal to javafx.fxml;
    opens com.example.proyectofinal.controller to javafx.fxml;
    opens com.example.proyectofinal.model to javafx.fxml;

    exports com.example.proyectofinal;
    exports com.example.proyectofinal.controller;
    exports com.example.proyectofinal.model;
}