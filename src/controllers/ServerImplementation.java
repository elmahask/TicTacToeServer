package controllers;

import Interface.ClientInterface;
import Interface.Player;
import Interface.ServerInterface;
import database.DatabaseConnection;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class ServerImplementation extends UnicastRemoteObject implements ServerInterface {
    
    ClientInterface clientInterface;
    
    Map<String, ClientInterface> clientsMap = new HashMap<>();
    
    DatabaseConnection databaseImplemntation;
    
    boolean isEnd = false;
    
    MainWindowController controller;
    
    Map<String, GameState> gameStateHashMap = new HashMap<>();
    
    List<String> list = new ArrayList<>();
    
    ClientInterface senderClient, recieverClient;
    
    GameState senderGameBoard, recieverGameBoard;
    
    public ServerImplementation(MainWindowController controller) throws RemoteException, ClassNotFoundException {
        this.controller = controller;
        databaseImplemntation = new DatabaseConnection();
    }
    
    @Override
    public boolean signUp(Player player) {
        return databaseImplemntation.signUpPlayer(player);
    }
    
    @Override
    public Player login(String userName, String password) {
        return databaseImplemntation.login(userName, password);
    }
    
    @Override
    public List<Player> displayClientList() {
        return databaseImplemntation.selectActivePlayers();
    }
    
    @Override
    public void registerClient(String userName, ClientInterface clientInterface) throws RemoteException {
        databaseImplemntation.setStatusActive(userName);
        for (Map.Entry<String, ClientInterface> client : clientsMap.entrySet()) {
            client.getValue().notifyOthers(userName);
        }
        clientsMap.put(userName, clientInterface);
        try {
            controller.refreshTableClients();
            notifyAll(userName);
        } catch (SQLException ex) {
            Logger.getLogger(ServerImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("client added");
    }
    
    @Override
    public void unregisterClient(String userName) {
        System.out.println("user name: " + userName);
        databaseImplemntation.setOffline(userName);
        clientsMap.remove(userName);
        System.out.println("client removed");
        try {
            if (controller != null) {
                controller.refreshTableClients();
            } else {
                System.out.println("controller: null");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendInvitation(Player sender, Player receiver) throws RemoteException {
        System.err.println("server Imp send Invetation");
        System.err.println("server Imp Sender " + sender.getUserName());
        System.err.println("server Imp receiver " + receiver.getUserName());
        ClientInterface receiverClient = clientsMap.get(receiver.getUserName());
        
        System.err.println("sender Invitation " + sender.getUserName());
        System.err.println("receiver Invitation " + receiver.getUserName());
        receiverClient.receiveInvitation(sender.getUserName(), receiver.getUserName());
    }
    
    @Override
    public void acceptInvitation(String sender, String receiver) throws RemoteException {
        senderClient = clientsMap.get(sender);
        list.add(sender);
        list.add(receiver);
        senderClient.acceptInvitation(sender, receiver);
        gameStateHashMap.put(sender, new GameState());
        // added to code
        ClientInterface receiverClient = clientsMap.get(receiver);  // receiver here
        receiverClient.acceptInvitation(sender, receiver);
        gameStateHashMap.put(receiver, new GameState());
    }
    
    @Override
    public void rejectInvitation(String sender, String receiver) throws RemoteException {
        senderClient = clientsMap.get(sender);
        senderClient.rejectInvitation(sender, receiver);
    }
    
    @Override
    public void sendMove(String sender, String reciever, int rowIndex,
            int columnIndex, char symbol) {
        if (reciever != null && sender != null && isEnd != true) {
            try {
                senderClient = clientsMap.get(sender);
                recieverClient = clientsMap.get(reciever);
                senderGameBoard = gameStateHashMap.get(sender);
                recieverGameBoard = gameStateHashMap.get(reciever); // add to code
                System.out.println("Before enter if game Board sender: " + senderGameBoard);
                System.out.println("Before enter if game Board receiver: " + recieverGameBoard);
                if (senderGameBoard != null) {
                    senderGameBoard.setCellInBoard(rowIndex, columnIndex, symbol);
                    if (symbol == 'x') {
                        if (senderGameBoard.isWin(symbol)) {
                            System.out.println("x Win ");
                            senderClient.winMessage();
                            recieverClient.loseMessage();
                            senderClient.makeRestVisible();
                            recieverClient.makeRestVisible();
                            list.remove(reciever);
                            list.remove(sender);
                            isEnd = true;
                            senderGameBoard.setCounter(0);
                            recieverGameBoard.setCounter(0);
                            databaseImplemntation.updateWin(sender);
                            databaseImplemntation.updateLose(reciever);
                            senderClient.updatePlayerScore(databaseImplemntation.getWinScore(sender),
                                    databaseImplemntation.getLoseScore(sender), databaseImplemntation.getDrawScore(sender));
                            recieverClient.updatePlayerScore(databaseImplemntation.getWinScore(reciever),
                                    databaseImplemntation.getLoseScore(reciever), databaseImplemntation.getDrawScore(reciever));
                        } else if (senderGameBoard.isDraw()) {
                            senderClient.drawMessage();
                            recieverClient.drawMessage();
                            senderClient.makeRestVisible();
                            recieverClient.makeRestVisible();
                            list.remove(reciever);
                            list.remove(sender);
                            isEnd = true;
                            senderGameBoard.setCounter(0);
                            recieverGameBoard.setCounter(0);
                            databaseImplemntation.updateDraw(sender);
                            databaseImplemntation.updateDraw(reciever);
                            senderClient.updatePlayerScore(databaseImplemntation.getWinScore(sender),
                                    databaseImplemntation.getLoseScore(sender), databaseImplemntation.getDrawScore(sender));
                            recieverClient.updatePlayerScore(databaseImplemntation.getWinScore(reciever),
                                    databaseImplemntation.getLoseScore(reciever), databaseImplemntation.getDrawScore(reciever));
                        }
                    }
                } else {
                    System.out.println("game board is null");
                }

                // add to code
                if (recieverGameBoard != null) {
                    recieverGameBoard.setCellInBoard(rowIndex, columnIndex, symbol);
                    
                    if (symbol == 'o') {
                        if (recieverGameBoard.isWin(symbol)) {
                            senderClient.winMessage();
                            recieverClient.loseMessage();
                            senderClient.makeRestVisible();
                            recieverClient.makeRestVisible();
                            list.remove(reciever);
                            list.remove(sender);
                            isEnd = true;
                            senderGameBoard.setCounter(0);
                            recieverGameBoard.setCounter(0);
                            databaseImplemntation.updateWin(sender);
                            databaseImplemntation.updateLose(reciever);
                            senderClient.updatePlayerScore(databaseImplemntation.getWinScore(sender),
                                    databaseImplemntation.getLoseScore(sender), databaseImplemntation.getDrawScore(sender));
                            recieverClient.updatePlayerScore(databaseImplemntation.getWinScore(reciever),
                                    databaseImplemntation.getLoseScore(reciever), databaseImplemntation.getDrawScore(reciever));
                        } else if (recieverGameBoard.isDraw()) {
                            senderClient.drawMessage();
                            recieverClient.drawMessage();
                            senderClient.makeRestVisible();
                            recieverClient.makeRestVisible();
                            list.remove(reciever);
                            list.remove(sender);
                            databaseImplemntation.updateDraw(sender);
                            databaseImplemntation.updateDraw(reciever);
                            isEnd = true;
                            senderGameBoard.setCounter(0);
                            recieverGameBoard.setCounter(0);
                            senderClient.updatePlayerScore(databaseImplemntation.getWinScore(sender),
                                    databaseImplemntation.getLoseScore(sender), databaseImplemntation.getDrawScore(sender));
                            recieverClient.updatePlayerScore(databaseImplemntation.getWinScore(reciever),
                                    databaseImplemntation.getLoseScore(reciever), databaseImplemntation.getDrawScore(reciever));
                        }
                    }
                } else {
                    System.out.println("game board is null");
                }
                senderClient.receiveMove(rowIndex, columnIndex, symbol);
                recieverClient.receiveMove(rowIndex, columnIndex, symbol);
            } catch (RemoteException ex) {
                Logger.getLogger(ServerImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("send Move --> sender :" + sender + "receiver : " + reciever);
        }
    }
    
    public List<Player> selectAllPlayers() {
        return databaseImplemntation.selectAllPlayers();
    }
    
    @Override
    public void sendMessage(String sender, String msg, String receiver) {
        senderClient = clientsMap.get(sender);
        recieverClient = clientsMap.get(receiver);
        try {
            senderClient.receiveMsg(sender, msg, receiver);
            recieverClient.receiveMsg(sender, msg, receiver);
        } catch (RemoteException ex) {
            Logger.getLogger(ServerImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendResetMove() {
        
        if (senderClient != null || recieverClient != null) {
            try {
                /*
                if(senderGameBoard != null && recieverGameBoard !=null){
               /* senderGameBoard.setCounter(0);
                recieverGameBoard.setCounter(0);
            
             
                }
                 */
                senderClient.receiveResetMove();
                recieverClient.receiveResetMove();
                senderClient.resetGame();
                recieverClient.resetGame();
                isEnd = false;
                clearGameBoard();
            } catch (RemoteException ex) {
                Logger.getLogger(ServerImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
    }
    
    public void clearGameBoard() {
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                senderGameBoard.setCellInBoard(i, j, ' ');
            }
        }
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                recieverGameBoard.setCellInBoard(i, j, ' ');
            }
        }
    }
    
    @Override
    public void notifyAll(String userName) throws RemoteException {
        displayClientList().forEach((_item) -> {
            Platform.runLater(() -> {
                Notifications notificationBuilder = Notifications.create()
                        .title("Online Player")
                        .text(userName + " Join For Game")
                        .darkStyle()
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.BOTTOM_RIGHT);
                AudioClip note = new AudioClip(ServerImplementation.this.getClass().getResource("/sources/definite.mp3").toString());
                note.play();
                notificationBuilder.showInformation();
            });
        });
    }
    
    @Override
    public void sendToOnlineClientErrorMessage(String userName) throws RemoteException {
        //  clientInterface.receiveErrorMesssage();
        //senderClient.receiveErrorMesssage(userName);
        if (clientInterface != null) {
            clientInterface = clientsMap.get(userName);
            clientInterface.receiveErrorMesssage(userName);
            System.out.println("client Interface in send to online client:  " + clientInterface);
        }
    }
    
    @Override
    public boolean isPlaying(String receiver) {
        for (int i = 0; i < list.size(); i++) {
            String get = list.get(i);
            if (receiver.equals(get)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void makeResetInVisible() throws RemoteException {
        senderClient.makeRestInVisible();
        recieverClient.makeRestInVisible();
    }
    
    @Override
    public boolean checkPlayerOnline(String userName) throws RemoteException {
        
        return databaseImplemntation.getStatus(userName) == 1;
        
    }
    
    @Override
    public void getScoreDuringInit(String userName) throws RemoteException {
        
        clientInterface = clientsMap.get(userName);
        
        if (clientInterface != null) {
            clientInterface.updateScoreDuringInit(databaseImplemntation.getWinScore(userName),
                    databaseImplemntation.getLoseScore(userName), databaseImplemntation.getDrawScore(userName));
        }
        
    }
    
    @Override
    public void makePlayersOffline() {
        List<Player> players = displayClientList();
        for (Player player : players) {
            databaseImplemntation.setOffline(player.getUserName());
            if (clientInterface != null) {
                clientInterface = clientsMap.get(player.getUserName());
                try {
                    clientInterface.receiveErrorMesssage(player.getUserName());
           
                } catch (RemoteException ex) {
                    Logger.getLogger(ServerImplementation.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
