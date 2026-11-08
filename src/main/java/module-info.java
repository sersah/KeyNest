module io.github.sersah.keynest {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.csv;
    requires java.desktop; // java.awt.Desktop for opening links in the browser

    opens io.github.sersah.keynest to javafx.fxml;
    exports io.github.sersah.keynest;
    exports io.github.sersah.keynest.popups;
}
