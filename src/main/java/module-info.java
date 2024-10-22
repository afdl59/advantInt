module com.example.advantecniaint {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.advantecniaint to javafx.fxml;
    exports com.example.advantecniaint;
    exports com.example.advantecniaint.buttons;
    opens com.example.advantecniaint.buttons to javafx.fxml;
}