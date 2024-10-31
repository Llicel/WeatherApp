package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


	@Override
	public void start(Stage stage) throws Exception {
		//creating root node
		Parent root = FXMLLoader.load(getClass().getResource("Framework.fxml"));
		//passing the root into our scene
		stage.setScene(new Scene(root));
		stage.show();
		
	}
	public static void main(String[] args) {
		launch();
	}

}
