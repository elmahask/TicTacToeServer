package main;

import controllers.MainWindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class TicTacToeServer extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Tik-Tak Server | iTi Project");
        stage.getIcons().add(new Image("/sources/tic-tac-toe.png"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest((WindowEvent t) -> {
            //get instance from SignupWindowController
            System.out.println("You are exit");
            MainWindowController mwc = (MainWindowController) loader.getController();
            mwc.setPlayersInactiveDialog();
             mwc.serverInactivate();
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
