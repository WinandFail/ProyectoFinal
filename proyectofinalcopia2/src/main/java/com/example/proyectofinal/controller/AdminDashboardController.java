package com.example.proyectofinal.controller;

import com.example.proyectofinal.model.*;
import com.example.proyectofinal.service.BankService;
import com.example.proyectofinal.service.AuthenticationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {
    @FXML private Label lblUser;
    @FXML private TabPane tabPane;

    @FXML private TableView<Employee> tblEmployees;
    @FXML private TableColumn<Employee, String> colEmployeeId;
    @FXML private TableColumn<Employee, String> colEmployeeName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, Double> colSalary;
    @FXML private TextField txtEmployeeId, txtEmployeeName, txtEmployeeEmail, txtEmployeePhone;
    @FXML private TextField txtEmployeeAddress, txtSalary;
    @FXML private ComboBox<String> cmbPosition;
    @FXML private DatePicker dtpHireDate;

    @FXML private TableView<Transaction> tblTransactions;
    @FXML private TableColumn<Transaction, String> colTransactionId;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colDescription;

    @FXML private TextArea txtReports;

    @FXML private TableView<BankAccount> tblAccounts;
    @FXML private TableColumn<BankAccount, String> colAccountNumber;
    @FXML private TableColumn<BankAccount, String> colAccountType;
    @FXML private TableColumn<BankAccount, String> colAccountHolder;
    @FXML private TableColumn<BankAccount, Double> colAccountBalance;
    @FXML private TableColumn<BankAccount, String> colOpeningDate;

    private BankService bankService;
    private Administrator user;
    private AuthenticationService authService;

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
        this.authService = bankService.getAuthService();
        initializeData();
    }

    public void setUser(Administrator user) {
        this.user = user;
        lblUser.setText("Administrador: " + user.getName() + " Nivel: " + user.getAccessLevel());
    }

    @FXML
    private void initialize() {
        configureTables();
        configureCombobox();
        configureDatePicker();
    }

    private void configureTables() {

        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));

        colTransactionId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colAccountNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colAccountType.setCellValueFactory(cellData -> {
            BankAccount account = cellData.getValue();
            if (account instanceof SavingsAccount) {
                return new javafx.beans.property.SimpleStringProperty("Ahorros");
            } else if (account instanceof CheckingAccount) {
                return new javafx.beans.property.SimpleStringProperty("Corriente (2% costo)");
            } else if (account instanceof BusinessAccount) {
                return new javafx.beans.property.SimpleStringProperty("Empresarial");
            }
            return new javafx.beans.property.SimpleStringProperty("Desconocido");
        });
        colAccountHolder.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHolder().getName()));
        colAccountBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colOpeningDate.setCellValueFactory(new PropertyValueFactory<>("openingDate"));
    }

    private void configureCombobox() {
        cmbPosition.setItems(FXCollections.observableArrayList(
                "Cajero",
                "Administrador"
        ));
        cmbPosition.setValue("Cajero");
    }

    private void configureDatePicker() {
        dtpHireDate.setValue(LocalDate.now());
        dtpHireDate.setPromptText("DD/MM/AAAA");
    }

    private void initializeData() {
        loadEmployees();
        loadTransactions();
        loadAccounts();
    }

    private void loadEmployees() {
        List<Employee> employees = bankService.getAllEmployees();
        tblEmployees.setItems(FXCollections.observableArrayList(employees));
    }

    private void loadTransactions() {
        List<Transaction> transactions = bankService.getAllTransactions();
        tblTransactions.setItems(FXCollections.observableArrayList(transactions));
    }

    private void loadAccounts() {
        List<BankAccount> accounts = bankService.getAllAccounts();
        tblAccounts.setItems(FXCollections.observableArrayList(accounts));
    }

    @FXML
    private void handleRegisterEmployee() {
        try {
            String id = txtEmployeeId.getText().trim();
            String name = txtEmployeeName.getText().trim();
            String email = txtEmployeeEmail.getText().trim();
            String phone = txtEmployeePhone.getText().trim();
            String address = txtEmployeeAddress.getText().trim();
            String position = cmbPosition.getValue();
            String salaryText = txtSalary.getText().trim();
            LocalDate hireDate = dtpHireDate.getValue();


            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || salaryText.isEmpty()) {
                showAlert("Error", "Complete todos los campos obligatorios");
                return;
            }

            if (position == null || position.isEmpty()) {
                showAlert("Error", "Seleccione un cargo para el empleado");
                return;
            }

            if (hireDate == null) {
                showAlert("Error", "Seleccione una fecha de contratación");
                return;
            }


            if (employeeWithIdExists(id)) {
                showAlert("Error", "El ID de empleado ya existe\n\n" +
                        "ID: " + id + "\n" +
                        "Por favor, ingrese un ID único diferente.");
                return;
            }

            double salary = Double.parseDouble(salaryText);
            if (salary <= 0) {
                showAlert("Error", "El salario debe ser mayor a cero");
                return;
            }


            String hireDateStr = hireDate.toString();


            TextInputDialog userDialog = new TextInputDialog();
            userDialog.setTitle("Crear Credenciales");
            userDialog.setHeaderText("Configurar usuario para: " + name);
            userDialog.setContentText("Usuario:");
            userDialog.getEditor().setText(generateSuggestedUser(name, id));

            Optional<String> userResult = userDialog.showAndWait();
            if (userResult.isEmpty() || userResult.get().trim().isEmpty()) {
                showAlert("Error", "Debe especificar un usuario para el empleado");
                return;
            }

            String username = userResult.get().trim();


            if (authService.userExists(username)) {
                showAlert("Error", "El usuario '" + username + "' ya existe. Por favor elija otro.");
                return;
            }

            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Crear Credenciales");
            passwordDialog.setHeaderText("Configurar contraseña para: " + name);
            passwordDialog.setContentText("Contraseña:");
            passwordDialog.getEditor().setText("123456");

            Optional<String> passwordResult = passwordDialog.showAndWait();
            if (passwordResult.isEmpty() || passwordResult.get().trim().isEmpty()) {
                showAlert("Error", "Debe especificar una contraseña para el empleado");
                return;
            }

            String password = passwordResult.get().trim();

            Employee employee;
            if (position.equals("Administrador")) {
                employee = new Administrator(id, name, email, phone, address, position, salary,
                        hireDateStr, "TOTAL", username, password);
            } else {
                employee = new Employee(id, name, email, phone, address, position, salary,
                        hireDateStr, username, password);
            }

            bankService.registerEmployee(employee);

            loadEmployees();
            clearEmployeeFields();
            showAlert("Éxito", "Empleado registrado correctamente\n\n" +
                    "ID: " + id + "\n" +
                    "Nombre: " + name + "\n" +
                    "Cargo: " + position + "\n" +
                    "Usuario: " + username + "\n" +
                    "Contraseña: " + password + "\n" +
                    "Salario: $" + String.format("%.2f", salary) + "\n" +
                    "Fecha Contratación: " + hireDateStr);

        } catch (NumberFormatException e) {
            showAlert("Error", "Ingrese un salario válido (ej: 2500.00)");
        } catch (Exception e) {
            showAlert("Error", "Error al registrar empleado: " + e.getMessage());
        }
    }

    private String generateSuggestedUser(String name, String id) {
        return name.toLowerCase()
                .replace(" ", ".")
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u") + "." + id.toLowerCase();
    }

    private boolean employeeWithIdExists(String id) {
        List<Employee> employees = bankService.getAllEmployees();
        return employees.stream()
                .anyMatch(employee -> employee.getId().equalsIgnoreCase(id));
    }

    @FXML
    private void handleDeleteEmployee() {
        Employee selectedEmployee = tblEmployees.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmar Eliminación");
            confirmation.setHeaderText("¿Está seguro de eliminar este empleado?");
            confirmation.setContentText("Empleado: " + selectedEmployee.getName() +
                    "\nID: " + selectedEmployee.getId() +
                    "\nCargo: " + selectedEmployee.getPosition() +
                    "\nFecha Contratación: " + selectedEmployee.getHireDate());

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (bankService.deleteEmployee(selectedEmployee.getId())) {
                    loadEmployees();
                    showAlert("Éxito", " Empleado eliminado correctamente\n\n" +
                            "Nombre: " + selectedEmployee.getName() +
                            "\nID: " + selectedEmployee.getId());
                } else {
                    showAlert("Error", "No se pudo eliminar el empleado");
                }
            }
        } else {
            showAlert("Error", "Seleccione un empleado de la tabla para eliminar");
        }
    }

    @FXML
    private void handleGenerateClientReport() {
        try {
            String report = bankService.generateClientReport();
            txtReports.setText(report);
            showAlert("Éxito", "Reporte de clientes generado correctamente");
        } catch (Exception e) {
            showAlert("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateTransactionReport() {
        try {
            String report = bankService.generateTransactionReport();
            txtReports.setText(report);
            showAlert("Éxito", "Reporte de transacciones generado correctamente");
        } catch (Exception e) {
            showAlert("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateAccountReport() {
        try {
            String report = bankService.generateAccountReport();
            txtReports.setText(report);
            showAlert("Éxito", "Reporte de cuentas generado correctamente");
        } catch (Exception e) {
            showAlert("Error", "Error al generar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAllAccounts() {
        try {
            List<BankAccount> allAccounts = bankService.getAllAccounts();
            StringBuilder report = new StringBuilder("=== TODAS LAS CUENTAS DEL BANCO ===\n\n");
            report.append("RESUMEN GENERAL\n");
            report.append("Total de cuentas: ").append(allAccounts.size()).append("\n");

            long savingsAccounts = allAccounts.stream().filter(c -> c instanceof SavingsAccount).count();
            long checkingAccounts = allAccounts.stream().filter(c -> c instanceof CheckingAccount).count();
            long businessAccounts = allAccounts.stream().filter(c -> c instanceof BusinessAccount).count();

            double totalBalance = allAccounts.stream().mapToDouble(BankAccount::getBalance).sum();
            double averageBalance = allAccounts.isEmpty() ? 0 : totalBalance / allAccounts.size();

            report.append("Cuentas de Ahorro: ").append(savingsAccounts).append("\n");
            report.append("Cuentas Corriente: ").append(checkingAccounts).append("\n");
            report.append("Cuentas Empresariales: ").append(businessAccounts).append("\n");
            report.append("Saldo total del banco: $").append(String.format("%.2f", totalBalance)).append("\n");
            report.append("Saldo promedio por cuenta: $").append(String.format("%.2f", averageBalance)).append("\n\n");

            report.append(" DETALLE DE CUENTAS\n");
            report.append("═══════════════════════════════════════\n\n");

            for (BankAccount account : allAccounts) {
                report.append(" Número: ").append(account.getAccountNumber()).append("\n");
                report.append(" Titular: ").append(account.getHolder().getName()).append("\n");
                report.append(" ID Titular: ").append(account.getHolder().getId()).append("\n");
                report.append(" Tipo: ");

                if (account instanceof SavingsAccount) {
                    report.append("Ahorros\n");
                    SavingsAccount savings = (SavingsAccount) account;
                    report.append("   Tasa Interés: ").append(String.format("%.1f%%", savings.getInterestRate() * 100)).append("\n");
                    report.append("   Saldo Mínimo: $").append(String.format("%.2f", savings.getMinimumBalance())).append("\n");
                } else if (account instanceof CheckingAccount) {
                    report.append("Corriente  (2% costo)\n");
                    CheckingAccount checking = (CheckingAccount) account;
                    report.append("   Límite Sobregiro: $").append(String.format("%.2f", checking.getOverdraftLimit())).append("\n");
                } else if (account instanceof BusinessAccount) {
                    report.append("Empresarial\n");
                    BusinessAccount business = (BusinessAccount) account;
                    report.append("   Empresa: ").append(business.getCompanyName()).append("\n");
                    report.append("   NIT: ").append(business.getNit()).append("\n");
                    report.append("   Límite Transferencia: $").append(String.format("%.2f", business.getTransferLimit())).append("\n");
                }

                report.append("   Saldo: $").append(String.format("%.2f", account.getBalance())).append("\n");
                report.append("   Fecha Apertura: ").append(account.getOpeningDate()).append("\n");
                report.append("   Transacciones: ").append(account.getTransactions().size()).append("\n");
                report.append("----------------------------------------\n\n");
            }

            txtReports.setText(report.toString());
            showAlert("Éxito", " Vista de todas las cuentas generada correctamente");
        } catch (Exception e) {
            showAlert("Error", "Error al generar vista de cuentas: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewSuspiciousTransactions() {
        try {
            List<Transaction> suspicious = bankService.getSuspiciousTransactions();
            StringBuilder report = new StringBuilder(" TRANSACCIONES SOSPECHOSAS (> $10,000)\n\n");

            if (suspicious.isEmpty()) {
                report.append(" No se encontraron transacciones sospechosas\n");
                report.append("Todas las transacciones están dentro de los límites normales\n");
            } else {
                report.append("  Se encontraron ").append(suspicious.size()).append(" transacciones sospechosas\n\n");

                double totalSuspicious = suspicious.stream().mapToDouble(Transaction::getAmount).sum();
                report.append(" Total en transacciones sospechosas: $").append(String.format("%.2f", totalSuspicious)).append("\n\n");

                report.append(" DETALLE DE TRANSACCIONES SOSPECHOSAS\n");
                report.append("═══════════════════════════════════════\n\n");

                for (Transaction transaction : suspicious) {
                    report.append(" ID: ").append(transaction.getId()).append("\n");
                    report.append(" Tipo: ").append(transaction.getType()).append("\n");
                    report.append(" Monto: $").append(String.format("%.2f", transaction.getAmount())).append("\n");
                    report.append(" Fecha: ").append(transaction.getDate()).append("\n");
                    report.append(" Descripción: ").append(transaction.getDescription()).append("\n");
                    report.append("----------------------------------------\n");
                }

                report.append("\n RECOMENDACIÓN: Revisar estas transacciones manualmente\n");
            }

            txtReports.setText(report.toString());
            showAlert("Monitoreo", suspicious.isEmpty() ?
                    " No hay transacciones sospechosas" :
                    " Se encontraron " + suspicious.size() + " transacciones sospechosas");
        } catch (Exception e) {
            showAlert("Error", "Error al obtener transacciones sospechosas: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateCompleteReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append(" REPORTE COMPLETO DEL SISTEMA BANCARIO\n");
            report.append("Fecha: ").append(java.time.LocalDateTime.now().toString()).append("\n");
            report.append("Generado por: ").append(user.getName()).append("\n");
            report.append("═══════════════════════════════════════\n\n");

            report.append(" RESUMEN EJECUTIVO\n");
            List<Client> clients = bankService.getAllClients();
            List<Employee> employees = bankService.getAllEmployees();
            List<BankAccount> accounts = bankService.getAllAccounts();
            List<Transaction> transactions = bankService.getAllTransactions();

            report.append("Total Clientes: ").append(clients.size()).append("\n");
            report.append("Total Empleados: ").append(employees.size()).append("\n");
            report.append("Total Cuentas: ").append(accounts.size()).append("\n");
            report.append("Total Transacciones: ").append(transactions.size()).append("\n");

            double totalBalance = accounts.stream().mapToDouble(BankAccount::getBalance).sum();
            report.append("Saldo Total en el Banco: $").append(String.format("%.2f", totalBalance)).append("\n\n");

            report.append(" DISTRIBUCIÓN DE CUENTAS\n");
            long savings = accounts.stream().filter(c -> c instanceof SavingsAccount).count();
            long checking = accounts.stream().filter(c -> c instanceof CheckingAccount).count();
            long business = accounts.stream().filter(c -> c instanceof BusinessAccount).count();

            report.append("Cuentas de Ahorro: ").append(savings).append(" (").append(calculatePercentage(savings, accounts.size())).append("%)\n");
            report.append("Cuentas Corriente: ").append(checking).append(" (").append(calculatePercentage(checking, accounts.size())).append("%)\n");
            report.append("Cuentas Empresariales: ").append(business).append(" (").append(calculatePercentage(business, accounts.size())).append("%)\n\n");

            report.append(" TOP 5 CUENTAS CON MAYOR SALDO\n");
            accounts.stream()
                    .sorted((c1, c2) -> Double.compare(c2.getBalance(), c1.getBalance()))
                    .limit(5)
                    .forEach(account -> {
                        report.append("• ").append(account.getAccountNumber())
                                .append(" - ").append(account.getHolder().getName())
                                .append(": $").append(String.format("%.2f", account.getBalance()))
                                .append("\n");
                    });
            report.append("\n");

            report.append(" ÚLTIMAS TRANSACCIONES\n");
            transactions.stream()
                    .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
                    .limit(10)
                    .forEach(transaction -> {
                        report.append("• ").append(transaction.getDate())
                                .append(" - ").append(transaction.getType())
                                .append(": $").append(String.format("%.2f", transaction.getAmount()))
                                .append(" - ").append(transaction.getDescription())
                                .append("\n");
                    });

            txtReports.setText(report.toString());
            showAlert("Éxito", " Reporte completo generado correctamente");
        } catch (Exception e) {
            showAlert("Error", "Error al generar reporte completo: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateData() {
        try {
            loadEmployees();
            loadTransactions();
            loadAccounts();
            showAlert("Éxito", " Datos actualizados correctamente");
        } catch (Exception e) {
            showAlert("Error", "Error al actualizar datos: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportReport() {
        try {
            String report = txtReports.getText();
            if (report.isEmpty()) {
                showAlert("Error", "No hay reporte para exportar");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Exportar Reporte");
            confirmation.setHeaderText("Reporte listo para exportar");
            confirmation.setContentText("El reporte ha sido preparado para exportación.\n\n" +
                    "En una implementación real, se guardaría como archivo PDF o Excel.");
            confirmation.showAndWait();

        } catch (Exception e) {
            showAlert("Error", "Error al exportar reporte: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Cerrar Sesión");
            confirmation.setHeaderText("¿Está seguro que desea cerrar sesión?");
            confirmation.setContentText("Será redirigido al login.");

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyectofinal/view/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) lblUser.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Banco UQ - Login");
            }
        } catch (IOException e) {
            showAlert("Error", "No se pudo cargar la pantalla de login: " + e.getMessage());
        }
    }


    private double calculatePercentage(long value, long total) {
        if (total == 0) return 0;
        return Math.round((value * 100.0 / total) * 10.0) / 10.0;
    }

    private void clearEmployeeFields() {
        txtEmployeeId.clear();
        txtEmployeeName.clear();
        txtEmployeeEmail.clear();
        txtEmployeePhone.clear();
        txtEmployeeAddress.clear();
        txtSalary.clear();
        cmbPosition.setValue("Cajero");
        dtpHireDate.setValue(LocalDate.now());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}