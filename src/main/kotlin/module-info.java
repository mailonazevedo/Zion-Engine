module com.app.zionengine {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens com.app.zionengine to javafx.fxml;
    exports com.app.zionengine;
}