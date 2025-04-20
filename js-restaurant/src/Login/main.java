/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Login;


import java.util.Locale;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author HP
 */
public class main extends Application {
    
    private double x = 0;
    private double y = 0; 
    @Override
    public void start(Stage stage) throws Exception {
        
        Locale.setDefault(Locale.ENGLISH);
        Parent root = FXMLLoader.load(getClass().getResource("/Views/login.fxml"));
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
       // stage.getIcons().add(new Image(Main.class.getResourceAsStream("/Resources/Images/logo.jpg")));
        stage.setResizable(false);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
