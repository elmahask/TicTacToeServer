package controllers;

import Interface.Player;
import database.DatabaseConnection;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainWindowController implements Initializable {

    @FXML
    private Label errorMessage;

    @FXML
    private TableView<Player> tableView;

    @FXML
    private TableColumn<Player, ImageView> status;

    @FXML
    private TableColumn<Player, String> username;

    @FXML
    private TableColumn<Player, String> gender;

    @FXML
    private TableColumn<Player, Integer> totalGames;

    @FXML
    private ToggleButton toggleButton;

    private boolean serverStatus;
    ServerImplementation serverImplementation;
    Registry registry;
    DatabaseConnection databaseConnection;
    ResultSet resultSet;
    ObservableList<Player> playersData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (serverImplementation == null) {
            try {
                serverImplementation = new ServerImplementation(this);
            } catch (RemoteException | ClassNotFoundException ex) {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        serverStatus = false;
        try {
            databaseConnection = new DatabaseConnection();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void refreshTableClients() throws SQLException {
        resultSet = databaseConnection.selectAll();
        ObservableList<Player> playerData = FXCollections.observableArrayList();
        playerData.clear();
        while (resultSet.next()) {
            ImageView online = new ImageView(new Image("/sources/online.png"));
            ImageView offline = new ImageView(new Image("/sources/offline.png"));
            ImageView listImage = null;
            if (resultSet.getInt("status") == 1) {
                listImage = online;
            }
            if (resultSet.getInt("status") == 0) {
                listImage = offline;
            }
            String userName = resultSet.getString("username");
            //int totalGames = databaseConnection.getWinScore(userName)  + databaseConnection.getDrawScore(userName) + 
            //  databaseConnection.getLoseScore(userName)  ;

            int totalGames = resultSet.getInt("win") + resultSet.getInt("draw") + resultSet.getInt("lose");
            // playerData.add(new Player(listImage, resultSet.getString("username"), resultSet.getString("gender"), resultSet.getInt("totalGames")));
            playerData.add(new Player(resultSet.getInt("status"), listImage, userName, resultSet.getString("gender"), totalGames));
        }

        status.setCellValueFactory(new PropertyValueFactory<>("myImage"));
        username.setCellValueFactory(new PropertyValueFactory<>("userName"));
        gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        totalGames.setCellValueFactory(new PropertyValueFactory<>("totalMatches"));

        tableView.setItems(playerData);
    }

    // refresh clients in the table
    @FXML
    void refreshClients(ActionEvent event) throws SQLException {
        refreshTableClients();
    }

    @FXML
    void disconnectClient(ActionEvent event) throws SQLException, RemoteException {
        errorMessage.setVisible(false);
        Player selectedClientsData = tableView.getSelectionModel().getSelectedItem();
        if (selectedClientsData != null) {
            System.out.println("Selected player: " + selectedClientsData.getUserName()
                    + "\t" + selectedClientsData.getStatus());
            if (selectedClientsData.getStatus() == 0) {
                errorMessage.setText("The client is already disconnected");
                errorMessage.setVisible(true);
            } else {
                System.out.println("sendToOnlineClientErrorMessage");
                boolean returnResult = databaseConnection.setOffline(selectedClientsData.getUserName());
                serverImplementation.sendToOnlineClientErrorMessage(selectedClientsData.getUserName());
                errorMessage.setVisible(false);
                System.out.println(selectedClientsData.getUserName());
                if (returnResult == false) {
                    errorMessage.setText("Operation failed!");
                    errorMessage.setVisible(true);
                }
            }
        } else {
            errorMessage.setText("You need to choose client first!");
            errorMessage.setVisible(true);
        }
        refreshTableClients();
    }

    @FXML
    void deleteClient(ActionEvent event) throws SQLException {
        errorMessage.setVisible(false);
        Player selectedClientsData = tableView.getSelectionModel().getSelectedItem();
        if (selectedClientsData != null) {
            boolean returnResult = databaseConnection.deleteClient(selectedClientsData.getUserName());
            if (returnResult == false) {
                errorMessage.setText("Operation failed!");
                errorMessage.setVisible(true);
            }
        } else {
            errorMessage.setText("You need to choose client first!");
            errorMessage.setVisible(true);
        }
        refreshTableClients();
    }

    @FXML
    void toggleButtonActon(ActionEvent event) throws ClassNotFoundException {
        if (toggleButton.isSelected() && serverStatus == false) {
            serverActivate();
            try {
                refreshTableClients();
            } catch (SQLException ex) {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            serverStatus = true;
            toggleButton.setSelected(true);
            toggleButton.setText("Server Active");
            toggleButton.setStyle("-fx-background-color: #008C72; -fx-text-fill: #fff; -fx-background-radius: 99");
            System.out.println("Server Active");
        } else {
            serverInactivate();
            setPlayersInactiveDialog();

            serverStatus = false;
            toggleButton.setSelected(false);
            toggleButton.setText("Sever Inactive");
            toggleButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: #fff; -fx-background-radius: 99");
            System.out.println("Server Inactive");
        }
    }

    private void serverActivate() throws ClassNotFoundException {
        try {
            if (serverImplementation == null) {
                serverImplementation = new ServerImplementation(this);
            }
            registry = LocateRegistry.createRegistry(1099);
            registry.rebind("servicename", serverImplementation);
        } catch (RemoteException ex) {
            System.out.println("Server is already running. Kindly close the running server then try to activate it again. #iti");
        }
    }

    public void serverInactivate() {
        try {
            if (serverStatus) {

                registry.unbind("servicename");
                UnicastRemoteObject.unexportObject(registry, true);
                System.out.println("before enter make players offline");

            }
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setPlayersInactiveDialog() {
        if (serverImplementation != null) {

            serverImplementation.makePlayersOffline();

        }
    }
}
