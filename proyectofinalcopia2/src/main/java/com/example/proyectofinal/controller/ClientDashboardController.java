package com.example.proyectofinal.controller;

import com.example.proyectofinal.model.*;
import com.example.proyectofinal.service.BankService;
import javafx.beans.property.SimpleStringProperty;
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

public class ClientDashboardController {
    @FXML private Label lblUser;
    @FXML private Label lblTotalBalance;

    @FXML private TabPane tabPane;

    @FXML private Label lblClientId;
    @FXML private Label lblClientName;
    @FXML private Label lblClientEmail;
    @FXML private Label lblClientPhone;
    @FXML private Label lblClientAddress;
    @FXML private Label lblTotalAccounts;
    @FXML private Label lblTotalBalanceSummary;

    @FXML private TableView<BankAccount> tblAccounts;
    @FXML private TableColumn<BankAccount, String> colAccountNumber;
    @FXML private TableColumn<BankAccount, String> colAccountType;
    @FXML private TableColumn<BankAccount, Double> colAccountBalance;
    @FXML private TableColumn<BankAccount, String> colAccountDetails;

    @FXML private ComboBox<BankAccount> cmbAccountsOperations;
    @FXML private TextField txtDepositAmount;
    @FXML private TextField txtWithdrawalAmount;
    @FXML private TextField txtDestinationAccount;
    @FXML private TextField txtTransferAmount;
    @FXML private TextArea txtTransactionResult;
    @FXML private Label lblSelectedAccountInfo;
    @FXML private Label lblCostInfo;
    @FXML private Label lblTransferLimitInfo;

    @FXML private TableView<Transaction> tblMovements;
    @FXML private TableColumn<Transaction, String> colMovementType;
    @FXML private TableColumn<Transaction, Double> colMovementAmount;
    @FXML private TableColumn<Transaction, String> colMovementDate;
    @FXML private TableColumn<Transaction, String> colMovementDescription;

    private BankService bankService;
    private Client user;

    public void setUser(Client user) {
        if (user == null) {
            showAlert("Error de Sistema", "No se pudo cargar la información del usuario. Por favor, contacte al administrador.");
            return;
        }

        this.user = user;
        lblUser.setText("Cliente: " + user.getName());

        if (bankService != null) {
            initializeData();
        }
    }

    public void setBankService(BankService bankService) {
        this.bankService = bankService;

        if (user != null) {
            initializeData();
        }
    }

    @FXML
    private void initialize() {
        configureTables();
        configureComboBox();
        configureListeners();
    }

    private void configureTables() {
        colAccountNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        colAccountType.setCellValueFactory(cellData -> {
            BankAccount account = cellData.getValue();
            return new SimpleStringProperty(getAccountTypeString(account));
        });

        colAccountBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));

        colAccountDetails.setCellValueFactory(cellData -> {
            BankAccount account = cellData.getValue();
            return new SimpleStringProperty(getAccountDetails(account));
        });

        colMovementType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMovementAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMovementDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMovementDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void configureComboBox() {
        cmbAccountsOperations.setCellFactory(param -> new ListCell<BankAccount>() {
            @Override
            protected void updateItem(BankAccount account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (%s) - Saldo: $%.2f",
                            account.getAccountNumber(),
                            getAccountTypeString(account),
                            account.getBalance()));
                }
            }
        });

        cmbAccountsOperations.setButtonCell(new ListCell<BankAccount>() {
            @Override
            protected void updateItem(BankAccount account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText("Seleccione una cuenta");
                } else {
                    setText(String.format("%s (%s) - $%.2f",
                            account.getAccountNumber(),
                            getAccountTypeString(account),
                            account.getBalance()));
                }
            }
        });
    }

    private void configureListeners() {
        cmbAccountsOperations.valueProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateAccountOperationsInfo(newSelection);

                if (tabPane.getSelectionModel().getSelectedItem().getText().contains("Movimientos")) {
                    loadMovementsInRealTime(newSelection);
                }
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().contains("Movimientos")) {
                BankAccount selectedAccount = cmbAccountsOperations.getValue();
                if (selectedAccount != null) {
                    loadMovementsInRealTime(selectedAccount);
                }
            }
        });

        tblAccounts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cmbAccountsOperations.setValue(newSelection);
            }
        });
    }

    private void updateAccountOperationsInfo(BankAccount account) {
        lblSelectedAccountInfo.setText("Cuenta seleccionada: " + account.getAccountNumber() + " - " + getAccountTypeString(account));

        if (account instanceof SavingsAccount) {
            lblCostInfo.setText(" Cuenta de Ahorros - Sin costos de transacción");

            lblTransferLimitInfo.setText("");
        } else if (account instanceof CheckingAccount) {
            lblCostInfo.setText("  Cuenta Corriente - 2% de costo en retiros y transferencias");
            lblTransferLimitInfo.setText("");
        } else if (account instanceof BusinessAccount) {
            BusinessAccount business = (BusinessAccount) account;
            lblCostInfo.setText("💼 Cuenta Empresarial - Sin costos adicionales");
            lblTransferLimitInfo.setText(String.format(" Límite de transferencia: $%.2f", business.getTransferLimit()));
        }
    }

    private String getAccountTypeString(BankAccount account) {
        if (account instanceof SavingsAccount) {
            return "Ahorros";
        } else if (account instanceof CheckingAccount) {
            return "Corriente";
        } else if (account instanceof BusinessAccount) {
            return "Empresarial";
        }
        return "Desconocido";
    }

    private String getAccountDetails(BankAccount account) {
        if (account instanceof SavingsAccount) {
            return "Sin costos";
        } else if (account instanceof CheckingAccount) {
            return "2% costo en retiros/transferencias";
        } else if (account instanceof BusinessAccount) {
            BusinessAccount business = (BusinessAccount) account;
            return String.format("Límite transferencia: $%.2f", business.getTransferLimit());
        }
        return "";
    }

    private void initializeData() {
        if (user == null) {
            showAlert("Error de Datos", "No se pudo cargar la información del usuario");
            return;
        }
        if (bankService == null) {
            showAlert("Error de Sistema", "El servicio bancario no está disponible");
            return;
        }

        try {
            loadAccounts();
            updateProfileInformation();
            updateTotalBalance();

            if (!tblAccounts.getItems().isEmpty()) {
                tblAccounts.getSelectionModel().selectFirst();
                cmbAccountsOperations.setValue(tblAccounts.getItems().getFirst());
            } else {
                showAlert("Información", "No tiene cuentas asociadas en el sistema");
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudieron cargar los datos: " + e.getMessage());
        }
    }

    private void loadAccounts() {
        try {
            BankAccount selectedAccount = cmbAccountsOperations.getValue();
            String selectedAccountNumber = (selectedAccount != null) ? selectedAccount.getAccountNumber() : null;

            List<BankAccount> accounts = bankService.getAccountsByClient(user.getId());
            tblAccounts.setItems(FXCollections.observableArrayList(accounts));
            cmbAccountsOperations.setItems(FXCollections.observableArrayList(accounts));

            if (selectedAccountNumber != null) {
                for (BankAccount account : cmbAccountsOperations.getItems()) {
                    if (account.getAccountNumber().equals(selectedAccountNumber)) {
                        cmbAccountsOperations.setValue(account);
                        break;
                    }
                }
            } else if (!accounts.isEmpty()) {

                cmbAccountsOperations.setValue(accounts.getFirst());
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudieron cargar las cuentas: " + e.getMessage());
        }
    }

    private void updateTotalBalance() {
        try {
            List<BankAccount> accounts = bankService.getAccountsByClient(user.getId());
            double totalBalance = accounts.stream()
                    .mapToDouble(BankAccount::getBalance)
                    .sum();
            lblTotalBalance.setText(String.format("Saldo Total: $%.2f", totalBalance));
            lblTotalBalanceSummary.setText(String.format("$%.2f", totalBalance));
        } catch (Exception e) {
            showAlert("Error", "No se pudo actualizar el saldo total: " + e.getMessage());
        }
    }

    private void updateProfileInformation() {
        try {
            lblClientId.setText(user.getId());
            lblClientName.setText(user.getName());
            lblClientEmail.setText(user.getEmail());
            lblClientPhone.setText(user.getPhone());
            lblClientAddress.setText(user.getAddress());

            List<BankAccount> accounts = bankService.getAccountsByClient(user.getId());
            lblTotalAccounts.setText(String.valueOf(accounts.size()));
        } catch (Exception e) {
            showAlert("Error", "No se pudo actualizar la información del perfil: " + e.getMessage());
        }
    }

    private void updateDataKeepingSelection() {
        try {
            BankAccount selectedAccount = cmbAccountsOperations.getValue();
            String selectedAccountNumber = (selectedAccount != null) ? selectedAccount.getAccountNumber() : null;

            loadAccounts();
            updateTotalBalance();
            updateProfileInformation();

            if (selectedAccountNumber != null) {
                for (BankAccount account : cmbAccountsOperations.getItems()) {
                    if (account.getAccountNumber().equals(selectedAccountNumber)) {
                        cmbAccountsOperations.setValue(account);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar datos manteniendo selección: " + e.getMessage());
        }
    }

    @FXML
    private void handleWithdraw() {
        BankAccount selectedAccount = cmbAccountsOperations.getValue();
        if (selectedAccount != null) {
            try {
                String amountText = txtWithdrawalAmount.getText().trim();
                if (amountText.isEmpty()) {
                    txtTransactionResult.setText(" Error: Ingrese un monto para retirar");
                    return;
                }

                double amount = Double.parseDouble(amountText);

                if (amount <= 0) {
                    txtTransactionResult.setText(" Error: El monto debe ser mayor a cero");
                    return;
                }

                if (selectedAccount instanceof SavingsAccount) {
                    withdrawFromSavingsAccount((SavingsAccount) selectedAccount, amount);
                } else if (selectedAccount instanceof CheckingAccount) {
                    withdrawFromCheckingAccount((CheckingAccount) selectedAccount, amount);
                } else if (selectedAccount instanceof BusinessAccount) {
                    withdrawFromBusinessAccount((BusinessAccount) selectedAccount, amount);
                } else {
                    txtTransactionResult.setText(" Error: Tipo de cuenta no soportado");
                }

            } catch (NumberFormatException e) {
                txtTransactionResult.setText(" Error: Ingrese un monto válido (ej: 50.00)");
            }
        } else {
            txtTransactionResult.setText(" Error: Seleccione una cuenta del ComboBox");
        }
    }

    private void withdrawFromSavingsAccount(SavingsAccount savingsAccount, double amount) {
        double currentBalance = savingsAccount.getBalance();

        if (amount > currentBalance) {
            txtTransactionResult.setText(" Error: Fondos insuficientes\n" +
                    "Saldo disponible: $" + String.format("%.2f", currentBalance) + "\n" +
                    "Monto solicitado: $" + String.format("%.2f", amount));
            return;
        }

        if (amount == currentBalance) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Retirar Todo el Saldo");
            confirmation.setHeaderText("  Va a retirar todo el saldo de la cuenta");
            confirmation.setContentText(
                    "Cuenta: " + savingsAccount.getAccountNumber() + " (Ahorros)\n\n" +
                            "Saldo actual: $" + String.format("%.2f", currentBalance) + "\n" +
                            "Monto a retirar: $" + String.format("%.2f", amount) + "\n\n" +
                            "¿Desea retirar todo el saldo disponible?"
            );

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                txtTransactionResult.setText("Retiro cancelado por el usuario");
                return;
            }
        }

        if (savingsAccount.withdraw(amount)) {
            bankService.registerGlobalTransaction(savingsAccount.getTransactions().get(savingsAccount.getTransactions().size() - 1));

            String message = " Retiro exitoso: $" + String.format("%.2f", amount) +
                    " de cuenta " + savingsAccount.getAccountNumber();

            if (savingsAccount.getBalance() == 0) {
                message += "\n ¡Retiró todo el saldo disponible!";
            }

            message += "\nNuevo saldo: $" + String.format("%.2f", savingsAccount.getBalance());

            txtTransactionResult.setText(message);
            updateDataKeepingSelection();
            updateMovementsIfNecessary();
            clearTransactionFields();

            showInformativeMessage("Retiro Exitoso",
                    "Se retiró $" + String.format("%.2f", amount) + " correctamente");
        } else {
            txtTransactionResult.setText(" Error: No se pudo realizar el retiro");
        }
    }

    private void withdrawFromCheckingAccount(CheckingAccount checkingAccount, double amount) {
        double cost = amount * 0.02;
        double totalDebited = amount + cost;

        if (checkingAccount.getBalance() < totalDebited) {
            txtTransactionResult.setText(" Error: Fondos insuficientes. Incluye costo del 2% ($" +
                    String.format("%.2f", cost) + ")");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Retiro - Cuenta Corriente");
        confirmation.setHeaderText("  ATENCIÓN: Costo del 2% aplicable");
        confirmation.setContentText(
                "Cuenta: " + checkingAccount.getAccountNumber() + " (Corriente)\n\n" +
                        "Monto a retirar: $" + String.format("%.2f", amount) + "\n" +
                        "Costo adicional (2%): $" + String.format("%.2f", cost) + "\n" +
                        "Total a debitar: $" + String.format("%.2f", totalDebited) + "\n\n" +
                        "¿Desea continuar con el retiro?"
        );

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            txtTransactionResult.setText("Retiro cancelado por el usuario");
            return;
        }

        if (checkingAccount.withdraw(amount)) {
            bankService.registerGlobalTransaction(checkingAccount.getTransactions().get(checkingAccount.getTransactions().size() - 1));

            String message = " Retiro exitoso: $" + String.format("%.2f", amount) +
                    " de cuenta " + checkingAccount.getAccountNumber() +
                    "\nCosto del 2% aplicado: $" + String.format("%.2f", cost) +
                    "\nNuevo saldo: $" + String.format("%.2f", checkingAccount.getBalance());

            txtTransactionResult.setText(message);
            updateDataKeepingSelection();
            updateMovementsIfNecessary();
            clearTransactionFields();

            showInformativeMessage("Retiro Exitoso",
                    "Se retiró $" + String.format("%.2f", amount) + " correctamente");
        } else {
            txtTransactionResult.setText(" Error: No se pudo realizar el retiro");
        }
    }

    private void withdrawFromBusinessAccount(BusinessAccount businessAccount, double amount) {

        if (businessAccount.withdraw(amount)) {
            bankService.registerGlobalTransaction(businessAccount.getTransactions().get(businessAccount.getTransactions().size() - 1));

            String message = " Retiro exitoso: $" + String.format("%.2f", amount) +
                    " de cuenta " + businessAccount.getAccountNumber() +
                    "\n Retiro empresarial completado" +
                    "\nNuevo saldo: $" + String.format("%.2f", businessAccount.getBalance());

            txtTransactionResult.setText(message);
            updateDataKeepingSelection();
            updateMovementsIfNecessary();
            clearTransactionFields();

            showInformativeMessage("Retiro Exitoso",
                    "Se retiró $" + String.format("%.2f", amount) + " correctamente");
        } else {
            txtTransactionResult.setText(" Error: Fondos insuficientes o monto inválido");
        }
    }

    @FXML
    private void handleDeposit() {
        BankAccount selectedAccount = cmbAccountsOperations.getValue();
        if (selectedAccount != null) {
            try {
                String amountText = txtDepositAmount.getText().trim();
                if (amountText.isEmpty()) {
                    txtTransactionResult.setText(" Error: Ingrese un monto para depositar");
                    return;
                }

                double amount = Double.parseDouble(amountText);
                if (amount > 0) {
                    if (selectedAccount.deposit(amount)) {
                        bankService.registerGlobalTransaction(selectedAccount.getTransactions().get(selectedAccount.getTransactions().size() - 1));
                        txtTransactionResult.setText(" Depósito exitoso: $" + String.format("%.2f", amount) +
                                " en cuenta " + selectedAccount.getAccountNumber() +
                                "\nNuevo saldo: $" + String.format("%.2f", selectedAccount.getBalance()));

                        updateDataKeepingSelection();
                        updateMovementsIfNecessary();
                        clearTransactionFields();

                        showInformativeMessage("Depósito Exitoso",
                                "Se depositó $" + String.format("%.2f", amount) + " correctamente");
                    } else {
                        txtTransactionResult.setText(" Error: No se pudo realizar el depósito");
                    }
                } else {
                    txtTransactionResult.setText(" Error: El monto debe ser mayor a cero");
                }
            } catch (NumberFormatException e) {
                txtTransactionResult.setText(" Error: Ingrese un monto válido (ej: 100.50)");
            }
        } else {
            txtTransactionResult.setText(" Error: Seleccione una cuenta del ComboBox");
        }
    }

    @FXML
    private void handleTransfer() {
        BankAccount sourceAccount = cmbAccountsOperations.getValue();
        if (sourceAccount != null) {
            try {
                String destinationAccountText = txtDestinationAccount.getText().trim();
                String amountText = txtTransferAmount.getText().trim();

                if (destinationAccountText.isEmpty() || amountText.isEmpty()) {
                    txtTransactionResult.setText(" Error: Complete todos los campos");
                    return;
                }

                double amount = Double.parseDouble(amountText);

                if (amount <= 0) {
                    txtTransactionResult.setText(" Error: El monto debe ser mayor a cero");
                    return;
                }


                if (destinationAccountText.equals(sourceAccount.getAccountNumber())) {
                    txtTransactionResult.setText(" Error: No puede transferir a la misma cuenta");
                    return;
                }


                if (sourceAccount instanceof BusinessAccount) {
                    BusinessAccount business = (BusinessAccount) sourceAccount;
                    if (amount > business.getTransferLimit()) {
                        txtTransactionResult.setText(" Error: El monto excede el límite de transferencia de $" +
                                String.format("%.2f", business.getTransferLimit()));
                        return;
                    }
                }

                BankAccount destinationAccountObj = bankService.findAccountByNumber(destinationAccountText);
                if (destinationAccountObj != null) {

                    if (sourceAccount instanceof CheckingAccount) {
                        double cost = amount * 0.02;
                        double totalDebited = amount + cost;


                        if (sourceAccount.getBalance() < totalDebited) {
                            txtTransactionResult.setText(" Error: Fondos insuficientes. Incluye costo del 2% ($" +
                                    String.format("%.2f", cost) + ")");
                            return;
                        }

                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirmar Transferencia - Cuenta Corriente");
                        confirmation.setHeaderText("  ATENCIÓN: Costo del 2% aplicable");
                        confirmation.setContentText(
                                "Cuenta origen: " + sourceAccount.getAccountNumber() + " (Corriente)\n" +
                                        "Cuenta destino: " + destinationAccountText + "\n\n" +
                                        "Monto a transferir: $" + String.format("%.2f", amount) + "\n" +
                                        "Costo adicional (2%): $" + String.format("%.2f", cost) + "\n" +
                                        "Total a debitar: $" + String.format("%.2f", totalDebited) + "\n\n" +
                                        "¿Desea continuar con la transferencia?"
                        );

                        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                            txtTransactionResult.setText("Transferencia cancelada por el usuario");
                            return;
                        }
                    } else if (sourceAccount instanceof BusinessAccount) {
                        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmation.setTitle("Confirmar Transferencia - Cuenta Empresarial");
                        confirmation.setHeaderText(" Transferencia Empresarial");
                        confirmation.setContentText(
                                "Cuenta origen: " + sourceAccount.getAccountNumber() + " (Empresarial)\n" +
                                        "Cuenta destino: " + destinationAccountText + "\n\n" +
                                        "Monto a transferir: $" + String.format("%.2f", amount) + "\n" +
                                        "Límite de transferencia: $" +
                                        String.format("%.2f", ((BusinessAccount) sourceAccount).getTransferLimit()) + "\n\n" +
                                        "¿Desea continuar con la transferencia?"
                        );

                        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                            txtTransactionResult.setText("Transferencia cancelada por el usuario");
                            return;
                        }
                    }

                    if (sourceAccount.transfer(destinationAccountObj, amount)) {
                        bankService.registerGlobalTransaction(sourceAccount.getTransactions().get(sourceAccount.getTransactions().size() - 1));

                        String message = " Transferencia exitosa: $" + String.format("%.2f", amount) +
                                " a cuenta " + destinationAccountText;


                        if (sourceAccount instanceof CheckingAccount) {
                            double cost = amount * 0.02;
                            message += "\n Costo del 2% aplicado: $" + String.format("%.2f", cost);
                        } else if (sourceAccount instanceof BusinessAccount) {
                            message += "\n Transferencia empresarial completada";
                        }

                        message += "\nNuevo saldo cuenta origen: $" + String.format("%.2f", sourceAccount.getBalance());

                        txtTransactionResult.setText(message);

                        updateDataKeepingSelection();
                        updateMovementsIfNecessary();
                        clearTransactionFields();

                        showInformativeMessage("Transferencia Exitosa",
                                "Se transfirió $" + String.format("%.2f", amount) + " correctamente");
                    } else {
                        txtTransactionResult.setText(" Error: No se pudo realizar la transferencia (fondos insuficientes)");
                    }
                } else {
                    txtTransactionResult.setText(" Error: Cuenta destino no encontrada");
                }
            } catch (NumberFormatException e) {
                txtTransactionResult.setText(" Error: Ingrese un monto válido");
            }
        } else {
            txtTransactionResult.setText(" Error: Seleccione una cuenta de origen del ComboBox");
        }
    }


    private void updateMovementsIfNecessary() {

        if (tabPane.getSelectionModel().getSelectedItem().getText().contains("Movimientos")) {
            BankAccount selectedAccount = cmbAccountsOperations.getValue();
            if (selectedAccount != null) {
                loadMovementsInRealTime(selectedAccount);
            }
        }
    }


    private void loadMovementsInRealTime(BankAccount account) {
        List<Transaction> movements = account.getTransactions();

        movements.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        tblMovements.setItems(FXCollections.observableArrayList(movements));

        if (movements.isEmpty()) {
            txtTransactionResult.setText("️ No hay movimientos registrados para la cuenta: " + account.getAccountNumber());
        } else {
            txtTransactionResult.setText(" Movimientos cargados para cuenta: " + account.getAccountNumber() +
                    "\nTotal de transacciones: " + movements.size());
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

    private void clearTransactionFields() {
        txtDepositAmount.clear();
        txtWithdrawalAmount.clear();
        txtDestinationAccount.clear();
        txtTransferAmount.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformativeMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}