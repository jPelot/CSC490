module dice_parser {
	requires org.junit.jupiter.api;
	requires javafx.base;
	requires javafx.graphics;
	requires javafx.fxml;
	requires javafx.controls;
	opens gui to javafx.graphics, javafx.fxml;
	
	opens test to javafx.graphics, javafx.fxml;
}