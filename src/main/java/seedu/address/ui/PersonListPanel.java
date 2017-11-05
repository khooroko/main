package seedu.address.ui;

import static seedu.address.logic.ListObserver.BLACKLIST_NAME_DISPLAY_FORMAT;
import static seedu.address.logic.ListObserver.MASTERLIST_NAME_DISPLAY_FORMAT;
import static seedu.address.logic.ListObserver.OVERDUELIST_NAME_DISPLAY_FORMAT;
import static seedu.address.logic.ListObserver.WHITELIST_NAME_DISPLAY_FORMAT;
import static seedu.address.logic.commands.SelectCommand.MESSAGE_SELECT_PERSON_SUCCESS;

import java.util.logging.Logger;

import org.fxmisc.easybind.EasyBind;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.ChangeInternalListEvent;
import seedu.address.commons.events.ui.EmptyListEvent;
import seedu.address.commons.events.ui.JumpToListRequestEvent;
import seedu.address.commons.events.ui.NearbyPersonNotInCurrentListEvent;
import seedu.address.commons.events.ui.NearbyPersonPanelSelectionChangedEvent;
import seedu.address.commons.events.ui.NewResultAvailableEvent;
import seedu.address.commons.events.ui.PersonPanelSelectionChangedEvent;
import seedu.address.logic.commands.BlacklistCommand;
import seedu.address.logic.commands.OverdueListCommand;
import seedu.address.logic.commands.WhitelistCommand;
import seedu.address.model.person.ReadOnlyPerson;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private final String currentListName;

    @FXML
    private ListView<PersonCard> personListView;

    public PersonListPanel(ObservableList<ReadOnlyPerson> personList, String currentListName) {
        super(FXML);
        setConnections(personList);
        this.currentListName = currentListName;
        registerAsAnEventHandler(this);
    }

    private void setConnections(ObservableList<ReadOnlyPerson> personList) {
        ObservableList<PersonCard> mappedList = EasyBind.map(
                personList, (person) -> new PersonCard(person, personList.indexOf(person) + 1));
        personListView.setItems(mappedList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        setEventHandlerForSelectionChangeEvent();
    }

    private void setEventHandlerForSelectionChangeEvent() {
        personListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        logger.fine("Selection in person list panel changed to : '" + newValue + "'");
                        raise(new PersonPanelSelectionChangedEvent(newValue));
                        raise(new NewResultAvailableEvent(getSelectionMessage(personListView.getSelectionModel()
                                .getSelectedIndex() + 1), false));
                    }
                });
    }

    private String getSelectionMessage(int oneBasedIndex) {
        switch (currentListName) {
        case BlacklistCommand.COMMAND_WORD:
            return BLACKLIST_NAME_DISPLAY_FORMAT + String.format(MESSAGE_SELECT_PERSON_SUCCESS, oneBasedIndex);

        case WhitelistCommand.COMMAND_WORD:
            return WHITELIST_NAME_DISPLAY_FORMAT + String.format(MESSAGE_SELECT_PERSON_SUCCESS, oneBasedIndex);

        case OverdueListCommand.COMMAND_WORD:
            return OVERDUELIST_NAME_DISPLAY_FORMAT + String.format(MESSAGE_SELECT_PERSON_SUCCESS, oneBasedIndex);

        default:
            return MASTERLIST_NAME_DISPLAY_FORMAT + String.format(MESSAGE_SELECT_PERSON_SUCCESS, oneBasedIndex);
        }
    }

    /**
     * Scrolls to the {@code PersonCard} at the {@code index} and selects it.
     */
    private void scrollTo(int index) {
        Platform.runLater(() -> {
            personListView.scrollTo(index);
            personListView.getSelectionModel().clearAndSelect(index);
        });
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        scrollTo(event.targetIndex);
    }

    //@@author khooroko
    /**
     * When there is a change in selection in nearby panel, scroll to the selected person in this person list panel,
     * if it exists. If it does not exist, a {@code NearbyPersonNotInCurrentListEvent} is raised and this panel is
     * unregistered as an event handler as it will be replaced by a new {@code PersonListPanel} in the
     * {@code MainWindow}.
     */
    @Subscribe
    private void handleNearbyPersonPanelSelectionChangedEvent(NearbyPersonPanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        boolean currentListContainsPerson = false;
        for (PersonCard pc : personListView.getItems()) {
            if (pc.person.equals(event.getNewSelection().person)) {
                scrollTo(personListView.getItems().indexOf(pc));
                currentListContainsPerson = true;
            }
        }
        if (!currentListContainsPerson) {
            raise(new NearbyPersonNotInCurrentListEvent(event.getNewSelection()));
            unregisterAsAnEventHandler(this);
        }
    }

    @Subscribe
    private void handleEmptyListEvent(EmptyListEvent event) {
        personListView.getSelectionModel().clearSelection();
    }

    @Subscribe
    private void handleChangeInternalListEvent(ChangeInternalListEvent event) {
        unregisterAsAnEventHandler(this);
    }

    //@@author
    /**
     * Custom {@code ListCell} that displays the graphics of a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<PersonCard> {

        @Override
        protected void updateItem(PersonCard person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(person.getRoot());
            }
        }
    }

}
