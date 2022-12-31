package com.example.cvdatabase.Controller;

import com.example.cvdatabase.Application;
import com.example.cvdatabase.Controller.AddControllers.AddDialogController;
import com.example.cvdatabase.Controller.AddControllers.AddTagController;
import com.example.cvdatabase.Controller.EditControllers.EditDialogController;
import com.example.cvdatabase.Export;
import com.example.cvdatabase.Helpers.Config;
import com.example.cvdatabase.Helpers.DataManager;
import com.example.cvdatabase.Helpers.DatabaseConnector;
import com.example.cvdatabase.Model.Person;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.base.AbstractMFXTreeItem;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.materialfx.filter.IntegerFilter;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.font.MFXFontIcon;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class Controller implements Initializable {
    public static ObservableList<Person> personListSelection;
    public static Person rootPerson;
    public static Object rootPersonEdit;
    private static ObservableMap<Integer, Person> listValuesSelection;
    public Stage stage;
    @FXML
    public MFXTableView<Person> table;
    @FXML
    public MFXTreeView<String> treeView;
    private ObservableList<Person> personList = FXCollections.observableArrayList();
    @FXML
    private MFXButton addButton;
    @FXML
    private MFXButton removeButton;
    @FXML
    private MFXButton editButton;
    @FXML
    private MFXButton exportButton;
    @FXML
    private MFXButton helpButton;
    @FXML
    private MFXButton displayButton;
    @FXML
    private MFXFontIcon closeIcon;
    @FXML
    private MFXFontIcon minimizeIcon;
    @FXML
    private MFXFontIcon alwaysOnTopIcon;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private HBox windowHeader;
    private double x;
    private double y;

    public Controller() {

    }

    public Controller(Stage stage) {
        this.stage = stage;
    }

    public static void createAlert(String content, String header) {
        MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build().setContentText(content).get();
        MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent).toStageDialogBuilder().initModality(Modality.APPLICATION_MODAL).setDraggable(true).setTitle("Dialog").setScrimPriority(ScrimPriority.WINDOW).setScrimOwner(true).get();

        dialogContent.setMaxSize(400, 200);

        MFXFontIcon infoIcon = new MFXFontIcon("mfx-info-circle-filled", 18);
        dialogContent.setHeaderIcon(infoIcon);

        dialogContent.setHeaderText(header);
        dialog.showDialog();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void createPerson(String name, String surname, String dateOfBirth, String email, String phone, String interests, String skills) {

        ArrayList<String> interestsList = new ArrayList<String>(Arrays.asList(interests.split(",")));
        ArrayList<String> skillsList = new ArrayList<String>(Arrays.asList(skills.split(",")));

        Person person = new Person(name, surname, dateOfBirth, email, phone, interestsList, skillsList);
        personList.add(person);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        closeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Platform.exit());
        minimizeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((Stage) rootPane.getScene().getWindow()).setIconified(true));
        alwaysOnTopIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean newVal = !stage.isAlwaysOnTop();
            alwaysOnTopIcon.pseudoClassStateChanged(PseudoClass.getPseudoClass("always-on-top"), newVal);
            stage.setAlwaysOnTop(newVal);
        });

        windowHeader.setOnMousePressed(event -> {
            x = stage.getX() - event.getScreenX();
            y = stage.getY() - event.getScreenY();
        });
        windowHeader.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + x);
            stage.setY(event.getScreenY() + y);
        });

        addButton.setOnAction(actionEvent -> onAdd());
        editButton.setOnAction(actionEvent -> onEdit());
        helpButton.setOnAction(actionEvent -> onHelp());
        exportButton.setOnAction(actionEvent -> onExport(table));
        removeButton.setOnAction(actionEvent -> onRemove());
        displayButton.setOnAction(actionEvent -> handleRowSelection());


        createTable();

    }

    private void handleRowSelection() {
        ObservableMap<Integer, Person> listValues = table.getSelectionModel().getSelection();
        ObservableList<Person> personList = FXCollections.observableArrayList(listValues.values());
        rootPerson = personList.listIterator().next();
        if (table.getSelectionModel().getSelectedValues().size() > 0) {

            treeView.setRoot(createTreeView(personList));

        }
    }

    private MFXTreeItem<String> createTreeView(ObservableList<Person> personList) {

        String name = personList.listIterator().next().getName();
        String surname = personList.listIterator().next().getSurname();

        MFXTreeItem<String> root = new MFXTreeItem<>(name + " " + surname);

        MFXTreeItem<String> educations = new MFXTreeItem<>("Educations");

        if (personList.listIterator().next().getEducation() != null) {
            for (int i = 0; i < personList.listIterator().next().getEducation().size(); i++) {

                String educationName = personList.listIterator().next().getEducation().get(i).getName();
                String educationStartDate = personList.listIterator().next().getEducation().get(i).getStartDate();
                String educationEndDate = personList.listIterator().next().getEducation().get(i).getEndDate();

                MFXTreeItem<String> educationsName = new MFXTreeItem<>(educationName);
                educationsName.getItems().addAll(List.of(new MFXTreeItem<>("Start Date: " + educationStartDate), new MFXTreeItem<>("End Date: " + educationEndDate)));
                educations.getItems().add(educationsName);

            }
        }

        MFXTreeItem<String> experiences = new MFXTreeItem<>("Experiences");

        if (personList.listIterator().next().getExperiences() != null) {
            for (int i = 0; i < personList.listIterator().next().getExperiences().size(); i++) {


                String title = personList.listIterator().next().getExperiences().get(i).getTitle();
                String experienceStartDate = personList.listIterator().next().getExperiences().get(i).getStartDate();
                String experienceEndDate = personList.listIterator().next().getExperiences().get(i).getEndDate();

                MFXTreeItem<String> experienceTitle = new MFXTreeItem<>(title);
                experienceTitle.getItems().addAll(List.of(new MFXTreeItem<>("Start Date: " + experienceStartDate), new MFXTreeItem<>("End Date: " + experienceEndDate)));
                experiences.getItems().add(experienceTitle);


            }
        }

        MFXTreeItem<String> publications = new MFXTreeItem<>("Publications");
        if (personList.listIterator().next().getPublications() != null) {
            for (int i = 0; i < personList.listIterator().next().getPublications().size(); i++) {


                String title = personList.listIterator().next().getPublications().get(i).getTitle();
                String publisher = personList.listIterator().next().getPublications().get(i).getPublisher();
                String publicationDate = personList.listIterator().next().getPublications().get(i).getPublicationDate();

                MFXTreeItem<String> publicationTitle = new MFXTreeItem<>(title);
                publicationTitle.getItems().addAll(List.of(new MFXTreeItem<>("Publisher: " + publisher), new MFXTreeItem<>("Publication date: " + publicationDate)));
                publications.getItems().add(publicationTitle);


            }
        }

        MFXTreeItem<String> interests = new MFXTreeItem<>("Interests");

        for (int i = 0; i < personList.listIterator().next().getInterests().size(); i++) {
            if (personList.listIterator().next().getInterests() != null) {

                String interestName = personList.listIterator().next().getInterests().get(i);
                MFXTreeItem<String> interestsItem = new MFXTreeItem<>(interestName);
                interests.getItems().add(interestsItem);
            }
        }

        MFXTreeItem<String> skills = new MFXTreeItem<>("Skills");

        for (int i = 0; i < personList.listIterator().next().getSkills().size(); i++) {
            if (personList.listIterator().next().getSkills() != null) {

                String skillName = personList.listIterator().next().getSkills().get(i);
                MFXTreeItem<String> skillsItem = new MFXTreeItem<>(skillName);
                skills.getItems().add(skillsItem);
            }
        }

        MFXTreeItem<String> tags = new MFXTreeItem<>("Tags");

        for (int i = 0; i < personList.listIterator().next().getTags().size(); i++) {
            if (personList.listIterator().next().getTags() != null) {

                String tagName = personList.listIterator().next().getTags().get(i).getName();
                MFXTreeItem<String> tagItem = new MFXTreeItem<>(tagName);
                tags.getItems().add(tagItem);
            }
        }


        root.getItems().addAll(List.of(educations, experiences, publications, interests, skills,tags));


        return root;
    }

    public void createTable() {

        table.getSelectionModel().setAllowsMultipleSelection(false);

        MFXTableColumn<Person> idColumn = new MFXTableColumn<>("ID", true, Comparator.comparing(Person::getId));
        MFXTableColumn<Person> nameColumn = new MFXTableColumn<>("Name", true, Comparator.comparing(Person::getName));
        MFXTableColumn<Person> surnameColumn = new MFXTableColumn<>("Surname", true, Comparator.comparing(Person::getSurname));
        MFXTableColumn<Person> dateOfBirthColumn = new MFXTableColumn<>("Date of birth", true, Comparator.comparing(Person::getBirthdate));
        MFXTableColumn<Person> emailColumn = new MFXTableColumn<>("Email", true, Comparator.comparing(Person::getEmail));
        MFXTableColumn<Person> phoneColumn = new MFXTableColumn<>("Phone", true, Comparator.comparing(Person::getPhone));
        MFXTableColumn<Person> tagsColumn = new MFXTableColumn<>("Tags", true, Comparator.comparing(Person::getTagsAsString));


        idColumn.setRowCellFactory(person -> new MFXTableRowCell<>(Person::getId));
        nameColumn.setRowCellFactory(person -> new MFXTableRowCell<>(Person::getName));
        surnameColumn.setRowCellFactory(person -> new MFXTableRowCell<>(Person::getSurname));
        dateOfBirthColumn.setRowCellFactory(person -> new MFXTableRowCell<>(Person::getBirthdate));
        emailColumn.setRowCellFactory(person -> new MFXTableRowCell<>(Person::getEmail));
        phoneColumn.setRowCellFactory(person -> new MFXTableRowCell<>(Person::getPhone));
        tagsColumn.setRowCellFactory(person -> new MFXTableRowCell<>(Person::getTagsAsString));


        table.getTableColumns().addAll(idColumn, nameColumn, surnameColumn, dateOfBirthColumn, emailColumn, phoneColumn,tagsColumn);
        table.getFilters().addAll(new IntegerFilter<>("ID", Person::getId), new StringFilter<>("Name", Person::getName), new StringFilter<>("Surname", Person::getSurname), new StringFilter<>("Date of birth", Person::getBirthdate), new StringFilter<>("Email", Person::getEmail), new StringFilter<>("Phone", Person::getPhone),new StringFilter<>("Tag", Person::getTagsAsString));

        personList = FXCollections.observableArrayList(DataManager.getInstance().PullPersons());

        table.setItems(personList);
        table.update();
    }

    private void onAdd() {
        Parent root;
        FXMLLoader loader;
        AbstractMFXTreeItem<String> treeItem = treeView.getSelectionModel().getSelectedItem();
        try {
            if (treeItem != null && treeItem.getData().equals("Educations")) {

                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.addEducationDialogPath)));
                root = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();
            } else if (treeItem != null && treeItem.getData().equals("Experiences")) {
                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.addExperienceDialogPath)));
                root = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();
            } else if (treeItem != null && treeItem.getData().equals("Publications")) {
                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.addPublicationDialogPath)));
                root = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();
            } else if (treeItem != null && treeItem.getData().equals("Interests")) {
                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.addInterestDialogPath)));
                root = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();
            } else if (treeItem != null && treeItem.getData().equals("Skills")) {
                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.addSkillDialogPath)));
                root = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();
            } else if (treeItem != null && treeItem.getData().equals("Tags")) {
                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.addTagDialogPath)));
                root = loader.load();

                AddTagController a = loader.getController();
                Stage tag_stage = new Stage();
                tag_stage.setScene(new Scene(root));
                tag_stage.initStyle(StageStyle.TRANSPARENT);
                a.setStage(stage);
                tag_stage.show();
            }else {
                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.addDialogPath)));
                root = loader.load();

                AddDialogController a = loader.getController();
                Stage add_stage = new Stage();
                add_stage.setScene(new Scene(root));
                add_stage.initStyle(StageStyle.TRANSPARENT);
                a.setStage(stage);
                add_stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onEdit() {
        listValuesSelection = table.getSelectionModel().getSelection();
        personListSelection = FXCollections.observableArrayList(listValuesSelection.values());

        AbstractMFXTreeItem<String> treeItem = treeView.getSelectionModel().getSelectedItem();

        Parent root;
        FXMLLoader loader;
        try {

            if (treeItem == null || treeItem.getData().equals(rootPerson.getName() + " " + rootPerson.getSurname())) {
                loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.editDialogPath)));
                root = loader.load();

                EditDialogController e = loader.getController();
                e.setStage(stage);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.show();
            } else {
                AbstractMFXTreeItem<String> parent = treeItem.getItemParent();
                rootPersonEdit = treeItem.getData();

                if (treeItem != null && parent != null && parent.getData().equals("Educations")) {
                    loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.editEducationDialogPath)));
                    root = loader.load();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initStyle(StageStyle.TRANSPARENT);
                    stage.show();
                } else if (treeItem != null && parent != null && parent.getData().equals("Experiences")) {
                    loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.editExperienceDialogPath)));
                    root = loader.load();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initStyle(StageStyle.TRANSPARENT);
                    stage.show();
                } else if (treeItem != null && parent != null && parent.getData().equals("Publications")) {
                    loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.editPublicationDialogPath)));
                    root = loader.load();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initStyle(StageStyle.TRANSPARENT);
                    stage.show();
                } else if (treeItem != null && parent != null && parent.getData().equals("Interests") || treeItem.getData().equals("Interests")) {
                    loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.editDialogPath)));
                    root = loader.load();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initStyle(StageStyle.TRANSPARENT);
                    stage.show();
                } else if (treeItem != null && parent != null && parent.getData().equals("Skills") || treeItem.getData().equals("Skills")) {
                    loader = new FXMLLoader(Objects.requireNonNull(Application.class.getResource(Config.editDialogPath)));
                    root = loader.load();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initStyle(StageStyle.TRANSPARENT);
                    stage.show();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onRemove() {
        AbstractMFXTreeItem<String> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null || treeItem.getData().equals(rootPerson.getName() + " " + rootPerson.getSurname())) {
            Person p = table.getSelectionModel().getSelectedValues().iterator().next();

            String q = "delete from Person where id = ?";
            try {
                PreparedStatement ps = DatabaseConnector.getInstance().prepareStatement(q);
                ps.setInt(1, p.getId());

                if (ps.executeUpdate() > 0) {

                    Controller.createAlert("Selected CV deleted successfully.", "");
                    table.getItems().remove(p);

                } else {

                    Controller.createAlert("Selected CV could not be deleted, something went wrong.", "");
                    table.getItems().remove(p);

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            AbstractMFXTreeItem<String> parent = treeItem.getItemParent();
            rootPersonEdit = treeItem.getData();

            if (treeItem != null && parent != null && parent.getData().equals("Educations")) {
                for (int i = 0; i < rootPerson.getEducation().size(); i++) {
                    if (rootPerson.getEducation().get(i).getName().equals(treeItem.getData())) {
                        rootPerson.getEducation().remove(i);
                        createAlert("Education deleted successfully", "");
                    }
                }
            } else if (treeItem != null && parent != null && parent.getData().equals("Experiences")) {
                for (int i = 0; i < rootPerson.getExperiences().size(); i++) {
                    if (rootPerson.getExperiences().get(i).getTitle().equals(treeItem.getData())) {
                        rootPerson.getExperiences().remove(i);
                        createAlert("Experience deleted successfully", "");
                    }
                }
            } else if (treeItem != null && parent != null && parent.getData().equals("Publications")) {
                for (int i = 0; i < rootPerson.getPublications().size(); i++) {
                    if (rootPerson.getPublications().get(i).getTitle().equals(treeItem.getData())) {
                        rootPerson.getPublications().remove(i);
                        createAlert("Publication deleted successfully", "");
                    }
                }
            } else if (treeItem != null && parent != null && parent.getData().equals("Interests")) {
                for (int i = 0; i < rootPerson.getInterests().size(); i++) {
                    if (rootPerson.getInterests().get(i).equals(treeItem.getData())) {
                        rootPerson.getInterests().remove(i);
                        createAlert("Interest deleted successfully", "");
                    }
                }
            } else if (treeItem != null && parent != null && parent.getData().equals("Skills")) {
                for (int i = 0; i < rootPerson.getSkills().size(); i++) {
                    if (rootPerson.getSkills().get(i).equals(treeItem.getData())) {
                        rootPerson.getSkills().remove(i);
                        createAlert("Skill deleted successfully", "");
                    }
                }
            }
        }


        ///////////////


    }

    private void onExport(MFXTableView<Person> table) {
        Export.buildCV(table);
    }

    private void onHelp() {
        Parent root;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(Application.class.getResource(Config.helpDialogPath)));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}