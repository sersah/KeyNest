module io.github.sersah.keynest {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.csv;
    requires java.logging;

    opens io.github.sersah.keynest to javafx.fxml;
    exports io.github.sersah.keynest;
    exports io.github.sersah.keynest.popups;
    exports io.github.sersah.keynest.vault;
}
