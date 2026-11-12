package io.github.sersah.keynest;

import io.github.sersah.keynest.popups.AccountDialog;
import io.github.sersah.keynest.popups.RemoveAccount;
import io.github.sersah.keynest.vault.Account;
import io.github.sersah.keynest.vault.VaultSession;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseController
{
    private static final Logger LOG = Logger.getLogger(DatabaseController.class.getName());
    private static final String GITHUB_URL = "https://github.com/sersah";
    private static final Comparator<Account> BY_NAME = Comparator.comparing(Account::name, String.CASE_INSENSITIVE_ORDER);

    private boolean showPw = false;

    @FXML
    private TableView<Account> table;

    @FXML
    private TableColumn<Account, String> nameColumn;

    @FXML
    private TableColumn<Account, String> emailColumn;

    @FXML
    private TableColumn<Account, String> passwordColumn;

    @FXML
    private Label accountCounter;

    @FXML
    private CheckBox showPasswordCheckbox;

    private VaultSession session;

    @FXML
    private void initialize()
    {
        nameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().name()));
        emailColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().email()));
        passwordColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().password()));

        nameColumn.setReorderable(false);
        emailColumn.setReorderable(false);
        passwordColumn.setReorderable(false);

        passwordColumn.setVisible(false);


        table.setPlaceholder(new Label("No accounts here!"));
        nameColumn.setSortable(false);
    }

    /** Called by the LoginController after the vault was unlocked. */
    public void setSession(VaultSession session)
    {
        this.session = session;
        ObservableList<Account> accounts = FXCollections.observableArrayList(session.getAccounts());
        accounts.sort(BY_NAME);
        table.setItems(accounts);
        updateCounter();
    }

    @FXML
    private void showPasswords()
    {
        showPw = !showPw;
        passwordColumn.setVisible(showPw);
        showPasswordCheckbox.setSelected(showPw);
    }

    private void save()
    {
        ObservableList<Account> accounts = table.getItems();
        accounts.sort(BY_NAME);
        try
        {
            session.save(List.copyOf(accounts));
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Could not save vault", e);
            Dialogs.info("Saving failed", "Your changes could not be saved:\n" + e.getMessage());
        }
        updateCounter();
    }

    private void updateCounter()
    {
        accountCounter.setText("Accounts saved: " + table.getItems().size());
    }

    @FXML
    private void addAccount()
    {
        AccountDialog.add().ifPresent(account ->
        {
            table.getItems().add(account);
            save();
        });
    }

    @FXML
    private void editAccount()
    {
        int row = table.getSelectionModel().getSelectedIndex();
        if (row == -1)
        {
            return;
        }

        AccountDialog.edit(table.getItems().get(row)).ifPresent(edited ->
        {
            table.getItems().set(row, edited);
            save();
        });
    }

    @FXML
    private void removeAccount()
    {
        int row = table.getSelectionModel().getSelectedIndex();
        if (row == -1)
        {
            return;
        }

        if (RemoveAccount.confirm(table.getItems().get(row).name()))
        {
            table.getItems().remove(row);
            save();
        }
    }

    @FXML
    private void about()
    {
        Hyperlink link = new Hyperlink(GITHUB_URL);
        link.setOnAction(e -> App.openInBrowser(GITHUB_URL));

        VBox content = new VBox(new Label("Find more of my projects here:"), link);
        content.setAlignment(Pos.CENTER);
        Dialogs.show("About", content, 265, 115);
    }

    @FXML
    private void locateDatabase()
    {
        Text text1 = new Text("The location of the database is:\n");
        text1.setFill(Color.WHITE);
        Text text2 = new Text(session.getRepository().getFile().toString());
        text2.setFont(Font.font("System", FontWeight.BOLD, 12));
        text2.setFill(Color.WHITE);
        TextFlow textFlow = new TextFlow(text1, text2);
        textFlow.setTextAlignment(TextAlignment.CENTER);

        Dialogs.show("Database Location", textFlow, 265, 115);
    }
}
