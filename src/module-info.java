module dice_parser {
	requires org.junit.jupiter.api;
	requires javafx.graphics;
	requires javafx.fxml;
	requires javafx.controls;
	opens gui to javafx.graphics, javafx.fxml;
}