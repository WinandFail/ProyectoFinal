package com.example.proyectofinal.controller;

import com.example.proyectofinal.model.Administrator;
import com.example.proyectofinal.model.Client;
import com.example.proyectofinal.model.Employee;
import com.example.proyectofinal.model.User;
import com.example.proyectofinal.service.AuthenticationService;
import com.example.proyectofinal.service.BankService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;

    private final AuthenticationService authService;
    private final BankService bankService;

    public LoginController() {
        this.bankService = new BankService();
        this.authService = bankService.getAuthService();
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleLogin() {
        String user = txtUser.getText();
        String password = txtPassword.getText();

        if (user.isEmpty() || password.isEmpty()) {
            showAlert("Por favor ingrese usuario y contraseña");
            return;
        }

        User authenticatedUser = authService.authenticate(user, password);

        if (authenticatedUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                Parent root;

                if (authenticatedUser instanceof Administrator) {
                    loader.setLocation(getClass().getResource("/com/example/proyectofinal/view/AdminDashboard.fxml"));
                    root = loader.load();
                    AdminDashboardController controller = loader.getController();
                    controller.setBankService(bankService);
                    controller.setUser((Administrator) authenticatedUser);
                } else if (authenticatedUser instanceof Employee) {
                    loader.setLocation(getClass().getResource("/com/example/proyectofinal/view/CashierDashboard.fxml"));
                    root = loader.load();
                    CashierDashboardController controller = loader.getController();
                    controller.setBankService(bankService);
                    controller.setUser((Employee) authenticatedUser);
                } else {
                    loader.setLocation(getClass().getResource("/com/example/proyectofinal/view/ClientDashboard.fxml"));
                    root = loader.load();
                    ClientDashboardController controller = loader.getController();
                    controller.setBankService(bankService);
                    controller.setUser((Client) authenticatedUser);
                }
                Stage stage = (Stage) txtUser.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Sistema Bancario - Dashboard");

            } catch (IOException e) {
                showAlert("No se pudo cargar la interfaz: " + e.getMessage());
            }
        } else {
            showAlert("Usuario o contraseña incorrectos");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}