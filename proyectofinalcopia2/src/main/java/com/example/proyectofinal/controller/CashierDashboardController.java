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
import java.util.List;
import java.util.Optional;

public class CashierDashboardController {
    @FXML private Label lblUser;
    @FXML private TabPane tabPane;

    @FXML private TableView<Client> tblClients;
    @FXML private TableColumn<Client, String> colClientId;
    @FXML private TableColumn<Client, String> colClientName;
    @FXML private TableColumn<Client, String> colClientEmail;
    @FXML private TextField txtClientId, txtClientName, txtClientEmail, txtClientPhone, txtClientAddress;


    @FXML private TextField txtAccountNumberDeposit, txtAmountDeposit;
    @FXML private TextField txtAccountNumberWithdrawal, txtAmountWithdrawal;
    @FXML private TextField txtSourceAccount, txtDestinationAccount, txtTransferAmount;
    @FXML private TextArea txtTransactionResult;


    @FXML private TextField txtAccountQuery;
    @FXML private TextArea txtQueryResult;

    private BankService bankService;
    private Employee user;
    private AuthenticationService authService;

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
        this.authService = bankService.getAuthService();
        initializeData();
    }

    public void setUser(Employee user) {
        this.user = user;
        lblUser.setText("Cajero: " + user.getName());
    }

    @FXML
    private void initialize() {
        configureTables();
    }

    private void configureTables() {
        colClientId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void initializeData() {
        loadClients();
    }

    private void loadClients() {
        List<Client> clients = bankService.getAllClients();
        tblClients.setItems(FXCollections.observableArrayList(clients));
    }

    @FXML
    private void handleRegisterClient() {
        try {
            String id = txtClientId.getText();
            String name = txtClientName.getText();
            String email = txtClientEmail.getText();
            String phone = txtClientPhone.getText();
            String address = txtClientAddress.getText();
            String registrationDate = java.time.LocalDate.now().toString();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
                showAlert("Error", "ID, Nombre y Email son campos obligatorios");
                return;
            }

            if (bankService.findClientById(id) != null) {
                showAlert("Error", "Ya existe un cliente con este ID");
                return;
            }

            TextInputDialog userDialog = new TextInputDialog();
            userDialog.setTitle("Crear Credenciales del Cliente");
            userDialog.setHeaderText("Configurar usuario para: " + name);
            userDialog.setContentText("Usuario:");
            userDialog.getEditor().setText(generateSuggestedUser(name, id));

            Optional<String> userResult = userDialog.showAndWait();
            if (userResult.isEmpty() || userResult.get().trim().isEmpty()) {
                showAlert("Error", "Debe especificar un usuario para el cliente");
                return;
            }

            String username = userResult.get().trim();

            if (authService.userExists(username)) {
                showAlert("Error", "El usuario '" + username + "' ya existe. Por favor elija otro.");
                return;
            }

            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Crear Credenciales del Cliente");
            passwordDialog.setHeaderText("Configurar contraseña para: " + name);
            passwordDialog.setContentText("Contraseña:");
            passwordDialog.getEditor().setText("cliente123");

            Optional<String> passwordResult = passwordDialog.showAndWait();
            if (passwordResult.isEmpty() || passwordResult.get().trim().isEmpty()) {
                showAlert("Error", "Debe especificar una contraseña para el cliente");
                return;
            }

            String password = passwordResult.get().trim();

            Client client = new Client(id, name, email, phone, address, registrationDate, username, password);
            bankService.registerClient(client);

            loadClients();
            clearClientFields();
            showAlert("Éxito", " Cliente registrado correctamente\n\n" +
                    "ID: " + id + "\n" +
                    "Nombre: " + name + "\n" +
                    "Usuario: " + username + "\n" +
                    "Contraseña: " + password + "\n" +
                    "Se ha creado una cuenta de ahorros automáticamente.");

        } catch (Exception e) {
            showAlert("Error", "Datos inválidos: " + e.getMessage());
        }
    }
    @FXML
    private void handleDeleteClient() {
        Client selectedClient = tblClients.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Eliminar Cliente");
            confirmation.setHeaderText("¿Está seguro de eliminar al cliente?");
            confirmation.setContentText("Cliente: " + selectedClient.getName() + "\nID: " + selectedClient.getId());

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                bankService.deleteClient(selectedClient.getId());
                loadClients();
                showAlert("Éxito", "Cliente eliminado correctamente");
            }
        } else {
            showAlert("Error", "Seleccione un cliente para eliminar");
        }
    }

    private String generateSuggestedUser(String name, String id) {
        return name.toLowerCase()
                .replace(" ", ".")
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u") + "." + id.toLowerCase();
    }

    @FXML
    private void handleDeposit() {
        try {
            String accountNumber = txtAccountNumberDeposit.getText().trim();
            String amountText = txtAmountDeposit.getText().trim();

            if (accountNumber.isEmpty() || amountText.isEmpty()) {
                txtTransactionResult.setText("Error: Complete todos los campos");
                return;
            }

            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                txtTransactionResult.setText("Error: El monto debe ser mayor a cero");
                return;
            }

            BankAccount account = bankService.findAccountByNumber(accountNumber);
            if (account != null) {
                if (account.deposit(amount)) {
                    bankService.registerGlobalTransaction(account.getTransactions().get(account.getTransactions().size() - 1));
                    txtTransactionResult.setText(" Depósito exitoso: $" + amount + " a cuenta " + accountNumber +
                            "\nNuevo saldo: $" + String.format("%.2f", account.getBalance()));
                    clearTransactionFields();
                } else {
                    txtTransactionResult.setText(" Error: No se pudo realizar el depósito");
                }
            } else {
                txtTransactionResult.setText(" Error: Cuenta no encontrada. Verifique el número de cuenta.");
            }
        } catch (NumberFormatException e) {
            txtTransactionResult.setText(" Error: Ingrese un monto válido");
        } catch (Exception e) {
            txtTransactionResult.setText(" Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleWithdraw() {
        try {
            String accountNumber = txtAccountNumberWithdrawal.getText().trim();
            String amountText = txtAmountWithdrawal.getText().trim();

            if (accountNumber.isEmpty() || amountText.isEmpty()) {
                txtTransactionResult.setText("Error: Complete todos los campos");
                return;
            }

            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                txtTransactionResult.setText("Error: El monto debe ser mayor a cero");
                return;
            }

            BankAccount account = bankService.findAccountByNumber(accountNumber);
            if (account != null) {
                if (account instanceof CheckingAccount) {
                    double cost = amount * 0.02;
                    double total = amount + cost;
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Confirmar Retiro - Cuenta Corriente");
                    confirmation.setHeaderText("Cuenta Corriente - Costo del 2% aplicable");
                    confirmation.setContentText(String.format(
                            "Monto a retirar: $%.2f\nCosto adicional (2%%): $%.2f\nTotal debitado: $%.2f\n\n¿Desea continuar?",
                            amount, cost, total));

                    if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        txtTransactionResult.setText("Retiro cancelado por el usuario");
                        return;
                    }
                }

                if (account.withdraw(amount)) {
                    bankService.registerGlobalTransaction(account.getTransactions().get(account.getTransactions().size() - 1));

                    String message = " Retiro exitoso: $" + amount + " de cuenta " + accountNumber;
                    if (account instanceof CheckingAccount) {
                        double cost = amount * 0.02;
                        message += String.format("\nCosto del 2%% aplicado: $%.2f", cost);
                    }
                    message += "\nNuevo saldo: $" + String.format("%.2f", account.getBalance());

                    txtTransactionResult.setText(message);
                    clearTransactionFields();
                } else {
                    txtTransactionResult.setText(" Error: Fondos insuficientes o monto inválido");
                }
            } else {
                txtTransactionResult.setText(" Error: Cuenta no encontrada. Verifique el número de cuenta.");
            }
        } catch (NumberFormatException e) {
            txtTransactionResult.setText(" Error: Ingrese un monto válido");
        } catch (Exception e) {
            txtTransactionResult.setText(" Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleTransfer() {
        try {
            String sourceAccount = txtSourceAccount.getText().trim();
            String destinationAccount = txtDestinationAccount.getText().trim();
            String amountText = txtTransferAmount.getText().trim();

            if (sourceAccount.isEmpty() || destinationAccount.isEmpty() || amountText.isEmpty()) {
                txtTransactionResult.setText("Error: Complete todos los campos");
                return;
            }

            double amount = Double.parseDouble(amountText);

            if (amount <= 0) {
                txtTransactionResult.setText("Error: El monto debe ser mayor a cero");
                return;
            }

            if (sourceAccount.equals(destinationAccount)) {
                txtTransactionResult.setText("Error: La cuenta origen y destino no pueden ser la misma");
                return;
            }

            BankAccount source = bankService.findAccountByNumber(sourceAccount);
            BankAccount destination = bankService.findAccountByNumber(destinationAccount);

            if (source != null && destination != null) {

                if (source instanceof CheckingAccount) {
                    double cost = amount * 0.02;
                    double total = amount + cost;
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Confirmar Transferencia - Cuenta Corriente");
                    confirmation.setHeaderText("Cuenta Corriente - Costo del 2% aplicable");
                    confirmation.setContentText(String.format(
                            "Monto a transferir: $%.2f\nCosto adicional (2%%): $%.2f\nTotal debitado: $%.2f\n\n¿Desea continuar?",
                            amount, cost, total));

                    if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        txtTransactionResult.setText("Transferencia cancelada por el usuario");
                        return;
                    }
                }

                if (source.transfer(destination, amount)) {
                    bankService.registerGlobalTransaction(source.getTransactions().get(source.getTransactions().size() - 1));

                    String message = " Transferencia exitosa: $" + amount + " de " + sourceAccount + " a " + destinationAccount;
                    if (source instanceof CheckingAccount) {
                        double cost = amount * 0.02;
                        message += String.format("\nCosto del 2%% aplicado: $%.2f", cost);
                    }
                    message += "\nNuevo saldo cuenta origen: $" + String.format("%.2f", source.getBalance());

                    txtTransactionResult.setText(message);
                    clearTransactionFields();
                } else {
                    txtTransactionResult.setText(" Error: No se pudo realizar la transferencia (fondos insuficientes)");
                }
            } else {
                txtTransactionResult.setText(" Error: Cuenta(s) no encontrada(s). Verifique los números de cuenta.");
            }
        } catch (NumberFormatException e) {
            txtTransactionResult.setText(" Error: Ingrese un monto válido");
        } catch (Exception e) {
            txtTransactionResult.setText(" Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckBalance() {
        try {
            String accountNumber = txtAccountQuery.getText().trim();

            if (accountNumber.isEmpty()) {
                txtQueryResult.setText("Error: Ingrese un número de cuenta");
                return;
            }

            BankAccount account = bankService.findAccountByNumber(accountNumber);

            if (account != null) {
                double balance = account.checkBalance();
                String accountType = getAccountType(account);

                String info = "=== INFORMACIÓN DE CUENTA ===\n" +
                        "Número: " + accountNumber + "\n" +
                        "Tipo: " + accountType + "\n" +
                        "Titular: " + account.getHolder().getName() + "\n" +
                        "Saldo disponible: $" + String.format("%.2f", balance) + "\n" +
                        "Fecha apertura: " + account.getOpeningDate();

                if (account instanceof CheckingAccount) {
                    info += "\n  Esta cuenta tiene un costo del 2% en retiros y transferencias";
                }

                txtQueryResult.setText(info);
            } else {
                txtQueryResult.setText(" Error: Cuenta no encontrada");
            }
        } catch (Exception e) {
            txtQueryResult.setText(" Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckClientAccounts() {
        Client selectedClient = tblClients.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            StringBuilder accountsInfo = new StringBuilder();
            accountsInfo.append("=== CUENTAS DEL CLIENTE ===\n");
            accountsInfo.append("Cliente: ").append(selectedClient.getName()).append("\n");
            accountsInfo.append("ID: ").append(selectedClient.getId()).append("\n");
            accountsInfo.append("Email: ").append(selectedClient.getEmail()).append("\n");
            accountsInfo.append("Teléfono: ").append(selectedClient.getPhone()).append("\n\n");

            List<BankAccount> clientAccounts = bankService.getAccountsByClient(selectedClient.getId());

            if (clientAccounts.isEmpty()) {
                accountsInfo.append("El cliente no tiene cuentas registradas.\n");
            } else {
                accountsInfo.append("Total de cuentas: ").append(clientAccounts.size()).append("\n\n");

                for (BankAccount account : clientAccounts) {
                    accountsInfo.append(" Número de Cuenta: ").append(account.getAccountNumber()).append("\n");
                    accountsInfo.append(" Tipo: ").append(getAccountType(account)).append("\n");
                    accountsInfo.append(" Saldo: $").append(String.format("%.2f", account.getBalance())).append("\n");
                    accountsInfo.append(" Fecha Apertura: ").append(account.getOpeningDate()).append("\n");

                    if (account instanceof SavingsAccount) {
                        SavingsAccount savings = (SavingsAccount) account;
                        accountsInfo.append(" Tasa Interés: ").append(String.format("%.1f%%", savings.getInterestRate() * 100)).append("\n");
                        accountsInfo.append(" Saldo Mínimo: $").append(String.format("%.2f", savings.getMinimumBalance())).append("\n");
                    } else if (account instanceof CheckingAccount) {
                        CheckingAccount checking = (CheckingAccount) account;
                        accountsInfo.append(" Límite Sobregiro: $").append(String.format("%.2f", checking.getOverdraftLimit())).append("\n");
                        accountsInfo.append("  Costo Transacciones: 2%\n");
                    } else if (account instanceof BusinessAccount) {
                        BusinessAccount business = (BusinessAccount) account;
                        accountsInfo.append(" Empresa: ").append(business.getCompanyName()).append("\n");
                        accountsInfo.append(" NIT: ").append(business.getNit()).append("\n");
                        accountsInfo.append(" Límite Transferencia: $").append(String.format("%.2f", business.getTransferLimit())).append("\n");
                    }

                    accountsInfo.append("\n----------------------------------------\n\n");
                }
            }

            txtQueryResult.setText(accountsInfo.toString());
        } else {
            showAlert("Error", "Seleccione un cliente de la tabla para consultar sus cuentas");
        }
    }

    @FXML
    private void handleCreateClientAccount() {
        Client selectedClient = tblClients.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {

            ChoiceDialog<String> dialog = new ChoiceDialog<>("Ahorros", "Ahorros", "Corriente", "Empresarial");
            dialog.setTitle("Crear Nueva Cuenta");
            dialog.setHeaderText("Seleccione el tipo de cuenta para: " + selectedClient.getName());
            dialog.setContentText("Tipo de cuenta:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String accountType = result.get();
                BankAccount newAccount = null;
                String accountNumber = generateAccountNumber(accountType);

                switch (accountType) {
                    case "Ahorros":
                        newAccount = new SavingsAccount(accountNumber, 0.0, selectedClient,
                                java.time.LocalDate.now().toString(), 0.02, 100.0);
                        break;
                    case "Corriente":
                        newAccount = new CheckingAccount(accountNumber, 0.0, selectedClient,
                                java.time.LocalDate.now().toString(), 1000.0);
                        break;
                    case "Empresarial":

                        TextInputDialog companyDialog = new TextInputDialog();
                        companyDialog.setTitle("Cuenta Empresarial");
                        companyDialog.setHeaderText("Información de la Empresa");
                        companyDialog.setContentText("Nombre de la empresa:");

                        Optional<String> companyName = companyDialog.showAndWait();
                        if (companyName.isPresent() && !companyName.get().isEmpty()) {
                            newAccount = new BusinessAccount(accountNumber, 0.0, selectedClient,
                                    java.time.LocalDate.now().toString(), companyName.get(),
                                    "NIT-" + System.currentTimeMillis(), 5000.0);
                        } else {
                            showAlert("Error", "El nombre de la empresa es obligatorio");
                            return;
                        }
                        break;
                }

                if (newAccount != null) {
                    bankService.registerAccount(newAccount);
                    showAlert("Éxito", " Cuenta " + accountType + " creada exitosamente\n\n" +
                            "Número de cuenta: " + accountNumber + "\n" +
                            "Cliente: " + selectedClient.getName() + "\n" +
                            "Saldo inicial: $0.00");
                    loadClients();


                    txtQueryResult.setText("Nueva cuenta creada:\n" +
                            "Número: " + accountNumber + "\n" +
                            "Tipo: " + accountType + "\n" +
                            "Cliente: " + selectedClient.getName() + "\n" +
                            "Fecha: " + java.time.LocalDate.now().toString());
                }
            }
        } else {
            showAlert("Error", "Seleccione un cliente de la tabla para crear una cuenta");
        }
    }

    @FXML
    private void handleGenerateClientReport() {
        Client selectedClient = tblClients.getSelectionModel().getSelectedItem();
        if (selectedClient != null) {
            StringBuilder report = new StringBuilder();
            report.append("=== REPORTE COMPLETO DEL CLIENTE ===\n\n");
            report.append("👤 INFORMACIÓN PERSONAL\n");
            report.append("ID: ").append(selectedClient.getId()).append("\n");
            report.append("Nombre: ").append(selectedClient.getName()).append("\n");
            report.append("Email: ").append(selectedClient.getEmail()).append("\n");
            report.append("Teléfono: ").append(selectedClient.getPhone()).append("\n");
            report.append("Dirección: ").append(selectedClient.getAddress()).append("\n");
            report.append("Fecha Registro: ").append(selectedClient.getRegistrationDate()).append("\n");

            List<BankAccount> clientAccounts = bankService.getAccountsByClient(selectedClient.getId());

            report.append("\n RESUMEN FINANCIERO\n");
            report.append("Total cuentas: ").append(clientAccounts.size()).append("\n");

            double totalBalance = clientAccounts.stream().mapToDouble(BankAccount::getBalance).sum();
            report.append("Saldo total: $").append(String.format("%.2f", totalBalance)).append("\n");

            report.append("\n DETALLE DE CUENTAS\n");
            report.append("═══════════════════════════════════════\n\n");

            if (clientAccounts.isEmpty()) {
                report.append("No hay cuentas registradas.\n");
            } else {
                for (BankAccount account : clientAccounts) {
                    report.append("Número: ").append(account.getAccountNumber()).append("\n");
                    report.append(account.generateReport()).append("\n");
                    report.append("----------------------------------------\n\n");
                }
            }

            report.append("\n ESTADÍSTICAS\n");
            report.append("Cuentas de Ahorro: ").append(
                    clientAccounts.stream().filter(c -> c instanceof SavingsAccount).count()).append("\n");
            report.append("Cuentas Corrientes: ").append(
                    clientAccounts.stream().filter(c -> c instanceof CheckingAccount).count()).append("\n");
            report.append("Cuentas Empresariales: ").append(
                    clientAccounts.stream().filter(c -> c instanceof BusinessAccount).count()).append("\n");

            txtQueryResult.setText(report.toString());
        } else {
            showAlert("Error", "Seleccione un cliente de la tabla para generar el reporte");
        }
    }


    private String getAccountType(BankAccount account) {
        if (account instanceof SavingsAccount) return "Ahorros";
        if (account instanceof CheckingAccount) return "Corriente (2% costo)";
        if (account instanceof BusinessAccount) return "Empresarial";
        return "Desconocido";
    }

    private String generateAccountNumber(String type) {
        List<BankAccount> allAccounts = bankService.getAllAccounts();
        int nextNumber = allAccounts.size() + 1;

        switch (type) {
            case "Ahorros": return "AHO-" + String.format("%03d", nextNumber);
            case "Corriente": return "CTE-" + String.format("%03d", nextNumber);
            case "Empresarial": return "EMP-" + String.format("%03d", nextNumber);
            default: return "GEN-" + String.format("%03d", nextNumber);
        }
    }

    private void clearClientFields() {
        txtClientId.clear();
        txtClientName.clear();
        txtClientEmail.clear();
        txtClientPhone.clear();
        txtClientAddress.clear();
    }

    private void clearTransactionFields() {
        txtAccountNumberDeposit.clear();
        txtAmountDeposit.clear();
        txtAccountNumberWithdrawal.clear();
        txtAmountWithdrawal.clear();
        txtSourceAccount.clear();
        txtDestinationAccount.clear();
        txtTransferAmount.clear();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}