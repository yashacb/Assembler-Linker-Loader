package sample;

import HelperClasses.NTableRow;
import HelperClasses.ObjectModule;
import HelperClasses.OpTableRow;
import HelperClasses.SymTableRow;
import SimulatorComponents.Controller;
import SimulatorComponents.Loader;
import SimulatorComponents.RAM;
import assemblyCode.FirstPass;
import assemblyCode.MainCode;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main extends Application
{
    List<OpTableRow> operations = new ArrayList<>() ;
    TextField file  ;
    Button choose ;
    List<String> machines ;
    static MainCode mainCode = new MainCode();
    public Main()
    {
        operations.add(new OpTableRow("START" , "AD" , 0)) ;
        operations.add(new OpTableRow("END" , "AD" , 1)) ;
        operations.add(new OpTableRow("DS" , "DS" , 2)) ;
        operations.add(new OpTableRow("LOAD" , "IS" , 3)) ;
        operations.add(new OpTableRow("LOADIM" , "IS" , 4)) ;
        operations.add(new OpTableRow("MOVE" , "IS" , 5)) ; // both operands are registers
        operations.add(new OpTableRow("STORE" , "IS" , 6)) ;
        operations.add(new OpTableRow("ADD" , "IS" , 7)) ;// both operands are registers
        operations.add(new OpTableRow("SUB" , "IS" , 8)) ;// both operands are registers
        operations.add(new OpTableRow("MUL" , "IS" , 9)) ;// both operands are registers
        operations.add(new OpTableRow("DIV" , "IS" , 10)) ;// both operands are registers
        operations.add(new OpTableRow("CMPEQ" , "IS" , 11)) ;
        operations.add(new OpTableRow("CMPLT" , "IS" , 12)) ;
        operations.add(new OpTableRow("CMPGT" , "IS" , 13)) ;
        operations.add(new OpTableRow("JZERO" , "IS" , 14)) ;
        operations.add(new OpTableRow("JMP" , "IS" , 15)) ;
    }
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        file = new TextField() ;
        file.setPromptText("Enter complete path of C file .");
        file.setFocusTraversable(false);
        file.setPrefSize(300 , 30);
        choose = new Button("Choose file") ;
        choose.setPrefSize(150,30);
        choose.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser() ;
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("C files" , "*.c")) ;
            fileChooser.setInitialDirectory(new File("J:/"));
            fileChooser.setTitle("Open C file");
            File f = fileChooser.showOpenDialog(primaryStage) ;
            if(f != null)
            {
                file.setText(f.toString());
            }
        });
        HBox fileChoosing = new HBox(15) ;
        fileChoosing.getChildren().addAll(file , choose) ;
        fileChoosing.setPadding(new Insets(7,7,7,7));
        fileChoosing.setAlignment(Pos.CENTER);

        GridPane buttons = new GridPane();
        Button convertToAssembly = new Button("To Assembly") ;
        Button pass1 = new Button("Assembler-P1") ;
        Button pass2 = new Button("Assembler-P2") ;
        Button linker = new Button("Link") ;
        Button load = new Button("Load to RAM") ;
        Button run = new Button("Run") ;
        GridPane.setConstraints(convertToAssembly , 0 , 0);
        GridPane.setConstraints(pass1 , 1 , 0);
        GridPane.setConstraints(pass2 , 0 , 1);
        GridPane.setConstraints(linker , 1 , 1);
        GridPane.setConstraints(load , 0 , 2);
        GridPane.setConstraints(run , 1 , 2);
        convertToAssembly.setMaxSize(200 , 30);
        pass1.setMaxSize(200 , 30);
        pass2.setMaxSize(200 , 30);
        linker.setMaxSize(200 , 30);
        load.setMaxSize(200 , 30);
        run.setMaxSize(200 , 30);
        pass1.setDisable(true);
        pass2.setDisable(true);
        linker.setDisable(true);
        load.setDisable(true);
        run.setDisable(true);
        buttons.getChildren().addAll(convertToAssembly , pass1 , pass2 , linker , load , run) ;
        buttons.setAlignment(Pos.CENTER);
        buttons.setHgap(25);
        buttons.setVgap(25);
        convertToAssembly.setOnAction(event -> {
            if(checkInput(file.getText()))
            {
                try
                {
                    List<String> allfiles = mainCode.toAssemblyCode(this , file.getText());
                    if(allfiles != null)
                    {
                        showAssembly(allfiles);
                        pass1.setDisable(false);
                        file.setDisable(true);
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Exception occurred . ");
                }
            }
            else
                errorOccurred("Chosen file is not a c file .");
        });
        pass1.setOnAction(event -> {if(firstPass()) pass2.setDisable(false);});
        pass2.setOnAction(event -> {if(secondPass()) linker.setDisable(false);});
        linker.setOnAction(event -> {if(linker()) load.setDisable(false);} );
        load.setOnAction(event -> {loader() ; run.setDisable(false);});
        run.setOnAction(event -> run());
        VBox container = new VBox(15) ;
        VBox.setMargin(fileChoosing , new Insets(15,0,0,0));
        container.setPadding(new Insets(15));
        container.getChildren().addAll(fileChoosing , buttons) ;

        VBox opcodes = new VBox(5) ;
        Label title = new Label("Opcodes used :") ;
        title.setAlignment(Pos.CENTER);
        title.setFont(Font.font(23));
        TableView table = new TableView() ;
        ObservableList<OpTableRow> ops = FXCollections.observableArrayList(operations) ;
        table.setItems(ops);
        TableColumn opcode = new TableColumn() ;
        opcode.setPrefWidth(150);
        opcode.setCellValueFactory(new PropertyValueFactory<>("opcode"));
        opcode.setText("Opcode Name");
        TableColumn opnum = new TableColumn() ;
        opnum.setPrefWidth(150);
        opnum.setText("Opcode Number");
        opnum.setCellValueFactory(new PropertyValueFactory<>("opnum"));
        TableColumn opclass = new TableColumn() ;
        opclass.setPrefWidth(150);
        opclass.setText("Opcode class");
        opclass.setCellValueFactory(new PropertyValueFactory<>("opclass"));
        table.getColumns().setAll(opcode , opclass , opnum) ;
        opcodes.getChildren().add(table) ;
        table.setPrefSize(700 , 500);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Separator separator = new Separator(Orientation.HORIZONTAL) ;
        separator.setPrefWidth(650);
        separator.setPadding(new Insets(17 , 17 , 0 , 17));

        VBox container2 = new VBox(15) ;
        container2.getChildren().addAll(container , separator , title , opcodes) ;
        VBox.setMargin(table , new Insets(0,17,0,17));
        VBox.setMargin(title , new Insets(0,17,0,17));

        Scene scene = new Scene(container2 , 550 , 900) ;
        primaryStage.setTitle("Assembler Linker Loader");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("file:///J:/asm.png")) ;
        primaryStage.show();
    }

    public boolean checkInput(String file)
    {
        return file.endsWith(".c") ;
    }

    public void errorOccurred(String errorOcc)
    {
        Stage incorrect = new Stage() ;
        Text error = new Text(errorOcc) ;
        Button close = new Button("Close") ;
        close.setAlignment(Pos.CENTER);
        error.setFont(Font.font(20));
        close.setOnAction(e -> incorrect.close());
        VBox container = new VBox(30) ;
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(7));
        container.getChildren().addAll(error , close) ;
        Scene scene = new Scene(container , 600 , 200) ;
        incorrect.setTitle("Error");
        incorrect.setScene(scene);
        incorrect.getIcons().add(new Image("file:///J:/error.png")) ;
        incorrect.show();
    }

    public void showAssembly(List<String> files)
    {
        Stage assembly = new Stage();
        assembly.setTitle("Assembly Codes");
        VBox mt = new VBox() ;
        mt.setAlignment(Pos.CENTER);
        Label mainTitle = new Label("Assembly codes of all involved files :") ;
        VBox.setMargin(mainTitle , new Insets(5));
        mainTitle.setFont(Font.font(25));
        mt.getChildren().addAll(mainTitle) ;
        Label label ;
        Label title ;
        String assemblyCode = "";
        VBox codes = new VBox(3) ;
        codes.setPadding(new Insets(15));
        for(String file : files)
        {
            assemblyCode = "" ;
            label = new Label() ;

            title = new Label("File name : " + file) ;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file + ".assembly"))))
            {
                String str;
                while ((str = br.readLine()) != null)
                    assemblyCode = assemblyCode + "\n\n" + str;

            }
            catch (Exception e)
            {
                errorOccurred("File Not found .");
            }
            label.setText(assemblyCode);
            title.setFont(Font.font(22));
            label.setFont(Font.font("Georgia" , 17));
            codes.getChildren().addAll(title , label) ;
            Separator separator = new Separator(Orientation.HORIZONTAL) ;
            separator.setPrefWidth(850);
            separator.setPadding(new Insets(15,0,15,0));
            codes.getChildren().addAll(separator) ;
        }
        VBox total = new VBox() ;
        ScrollPane scrollPane = new ScrollPane(codes);
        scrollPane.setPadding(new Insets(10));
        total.getChildren().addAll(mt , scrollPane) ;
        Scene scene = new Scene(total, 900, 500);
        assembly.getIcons().add(new Image("file:///J:/code.png")) ;
        assembly.setScene(scene);
        assembly.show();
    }

    public boolean firstPass()
    {
        Object res = mainCode.firstPass(this) ;
        if(res instanceof String)
        {
            errorOccurred(res.toString());
            return false ;
        }
        else if(res == null)
            return false ;
        else if(res != null)
        {
            Label title = new Label("Output of Pass 1 :") ;
            title.setFont(Font.font(23));
            List<FirstPass> firstPassList = (ArrayList)res ;
            Stage firstPassResults = new Stage() ;
            VBox container = new VBox(5) ;
            for(FirstPass firstPass : firstPassList)
            {
                String fname = firstPass.fileName ;
                Label filename = new Label("File name : " + fname) ;
                filename.setFont(Font.font("Verdana" , 20));
                VBox.setMargin(filename , new Insets(10 , 0 , 10 , 15));
                filename.setAlignment(Pos.CENTER);
                HashSet<SymTableRow> symbols = firstPass.getSymbols() ;
                List<String> interCodes = firstPass.getInterCodes() ;
                ObservableList<SymTableRow> symbolsList = FXCollections.observableArrayList(symbols) ;
                TableView table = new TableView() ;
                table.setPrefWidth(450);
                TableColumn name = new TableColumn("Symbol Name") ;
                name.setCellValueFactory(new PropertyValueFactory<>("name"));
                TableColumn address = new TableColumn("Address") ;
                address.setCellValueFactory(new PropertyValueFactory<>("address"));
                TableColumn type = new TableColumn("Type") ;
                type.setCellValueFactory(new PropertyValueFactory<>("type"));
                table.getColumns().addAll(name , address , type) ;
                table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                table.setItems(symbolsList);
                table.setTooltip(new Tooltip("Symbols table"));
                ListView<String> inters = new ListView<>(FXCollections.observableArrayList(interCodes)) ;
                inters.setPrefWidth(450);
                inters.setTooltip(new Tooltip("Intermediate representation"));
                Separator separator = new Separator(Orientation.VERTICAL) ;
                HBox results = new HBox(15) ;
                results.getChildren().addAll(table , separator , inters) ;
                VBox.setMargin(results , new Insets(10));
                Separator sp = new Separator(Orientation.HORIZONTAL) ;
                sp.setPrefWidth(900);
                VBox.setMargin(sp , new Insets(10));
                VBox.setMargin(separator , new Insets(0,10,0,10));
                container.getChildren().addAll(filename , results , sp) ;
                VBox.setMargin(results , new Insets(23));
            }
            container.setAlignment(Pos.CENTER);
            ScrollPane scrollPane = new ScrollPane(container) ;
            VBox container2 = new VBox(10) ;
            container2.getChildren().addAll(title , scrollPane) ;
            container2.setAlignment(Pos.CENTER);
            VBox.setMargin(title , new Insets(10));
            Scene scene = new Scene(container2 , 1000 , 950) ;
            firstPassResults.setScene(scene);
            firstPassResults.setTitle("Output of pass 1");
            firstPassResults.getIcons().add(new Image("file:///J:/results.png")) ;
            firstPassResults.setResizable(false);
            firstPassResults.show();
        }
        return true ;
    }

    public boolean secondPass() {
        List<ObjectModule> modules = mainCode.secondPass(this);
        if (modules == null)
            return false;
        else {
            Stage secondPassStage = new Stage();
            VBox container = new VBox(15);
            container.setAlignment(Pos.CENTER);
            Label title = new Label("Output of pass 2 :") ;
            title.setFont(Font.font(20));
            Separator sptr = new Separator(Orientation.HORIZONTAL) ;
            sptr.setPrefWidth(900);
            for (ObjectModule module : modules) {
                Label fname = new Label(module.getName());
                fname.setFont(Font.font(20));
                VBox.setMargin(fname, new Insets(10, 0, 10, 0));
                List<String> machineCodes = module.getMachines();
                List<String> complementary = new ArrayList<>();
                machineCodes.stream().forEach(e -> complementary.add(e + " : " + ObjectModule.interpretMachineInstruction(e)));
                ListView<String> machines = new ListView<>(FXCollections.observableList(complementary));
                ListView<Integer> reloc = new ListView<>(FXCollections.observableArrayList(module.getReloctab()));
                machines.setPrefWidth(400);
                machines.setTooltip(new Tooltip("Machine Instructions ."));
                reloc.setPrefWidth(400);
                reloc.setTooltip(new Tooltip("Relocation table"));
                HBox results = new HBox(30);
                Separator sp1 = new Separator(Orientation.VERTICAL);
                results.getChildren().addAll(machines, sp1, reloc);
                Separator sp2 = new Separator(Orientation.HORIZONTAL);
                sp2.setPrefWidth(490);
                VBox.setMargin(sp2, new Insets(0, 0, 0, 5));
                VBox.setMargin(results, new Insets(20));
                container.getChildren().addAll(fname, results, sp2);
            }
            ScrollPane scrollPane = new ScrollPane(container);
            VBox cont2 = new VBox(15) ;
            cont2.setAlignment(Pos.CENTER);
            cont2.getChildren().addAll(title , scrollPane) ;
            secondPassStage.setScene(new Scene(cont2, 920, 900));
            secondPassStage.setResizable(false);
            secondPassStage.setTitle("Output of pass 2");
            VBox.setMargin(title , new Insets(20,0,15,0));
            secondPassStage.getIcons().add(new Image("file:///J:/results.png"));
            secondPassStage.show();
        }
        return true;
    }

    public boolean linker()
    {
        machines = mainCode.linker(this) ;
        if(machines != null)
        {
            machines.add("11111111111111111111111111111111") ;
            List<String> modMac = new ArrayList<>() ;
            int base = 700 ;
            for(String instr : machines)
            {
                if(instr.equals("11111111111111111111111111111111"))
                    continue;
                instr = base + " : " + instr + " : " + ObjectModule.interpretMachineInstruction(instr) ;
                modMac.add(instr) ;
                base++ ;
            }
            HashMap<String , Integer> ntable = mainCode.ntable ;
            List<NTableRow> ntlist = new ArrayList<>() ;
            Set<Map.Entry<String , Integer>> entries = ntable.entrySet() ;
            for(Map.Entry entry : entries)
                ntlist.add(new NTableRow((String) entry.getKey() , (Integer) entry.getValue())) ;
            Stage linkerStage = new Stage() ;
            Label title = new Label("Output of Linker :") ;
            title.setFont(Font.font(20));
            TableView table = new TableView() ;
            TableColumn name = new TableColumn("Name of the variable") ;
            name.setCellValueFactory(new PropertyValueFactory<>("name"));
            TableColumn address = new TableColumn("Name of the variable") ;
            address.setCellValueFactory(new PropertyValueFactory<>("address"));
            table.setItems(FXCollections.observableList(ntlist));
            table.getColumns().addAll(name , address) ;
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            ListView<String> modMachines = new ListView<>(FXCollections.observableList(modMac)) ;
            modMachines.setTooltip(new Tooltip("Final Machine Instructions ."));
            modMachines.setPrefHeight(550);
            table.setPrefWidth(550);
            table.setTooltip(new Tooltip("All variables table"));
            modMachines.setPrefWidth(500);
            VBox vbox = new VBox(20) ;
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(15));
            vbox.setMaxWidth(Double.MAX_VALUE);
            Separator sptr = new Separator(Orientation.HORIZONTAL) ;
            sptr.setPrefWidth(580);
            vbox.getChildren().addAll(table , sptr , modMachines) ;
            ScrollPane sp = new ScrollPane(vbox) ;
            VBox top = new VBox(20) ;
            top.getChildren().addAll(title , sp) ;
            top.setAlignment(Pos.CENTER);
            VBox.setMargin(title , new Insets(20,0,15,0));
            VBox.setMargin(modMachines , new Insets(15,0,15,0));
            VBox.setMargin(table , new Insets(15,0,15,0));
            linkerStage.setScene(new Scene(top , 625,800));
            linkerStage.setTitle("Output of Linker .");
            linkerStage.getIcons().addAll(new Image("file:///J:/link.png")) ;
            linkerStage.show();
            return true ;
        }
        return false ;
    }
    RAM loaded ;
    public void loader()
    {
        Loader loader = new Loader(700 , machines) ;
        loaded = loader.load() ;
        List<String> list = new ArrayList<>() ;
        int base = 700 ;
        for(int i = 0 ; i < 75 ; i++)
        {
            list.add((base + i) + " - " + loaded.read(base + i)) ;
        }
        ListView<String> ramContents = new ListView<>(FXCollections.observableList(list)) ;
        ramContents.setPrefSize(500,700);
        Stage afterLoading = new Stage() ;
        afterLoading.setTitle("After Loading ");
        Label title = new Label("Contents of RAM after loading :") ;
        VBox.setMargin(title , new Insets(15,0,15,0));
        title.setFont(Font.font(23));
        VBox box = new VBox(10) ;
        box.setPadding(new Insets(10,15,10,15));
        Separator sptr = new Separator(Orientation.HORIZONTAL) ;
        VBox.setMargin(sptr , new Insets(0,0,15,0));
        box.getChildren().addAll(title , sptr , ramContents) ;
        box.setAlignment(Pos.CENTER);
        Scene scene = new Scene(box , 600,725) ;
        afterLoading.setScene(scene);
        afterLoading.getIcons().add(new Image("file:///J:/ram.png")) ;
        afterLoading.show() ;
    }

    public void run()
    {
        Controller controller = new Controller(700 , loaded) ;
        RAM loaded = controller.start(mainCode.ntable) ;
        loaded.printRam();
        loaded.printRam();
        List<String> list = new ArrayList<>() ;
        int base = 700 ;
        for(int i = 0 ; i < 75 ; i++)
        {
            System.err.println((base + i) + " " + loaded.read(base + i));
            try {
                list.add((base + i) + " - " + loaded.read(base + i) + " - " + Integer.parseInt(loaded.read(base + i).substring(10), 2));
            }
            catch(NumberFormatException e)
            {
                continue;
            }
        }
        ListView<String> ramContents = new ListView<>(FXCollections.observableList(list)) ;
        ramContents.setPrefSize(500,700);
        Stage afterLoading = new Stage() ;
        afterLoading.setTitle("After executing ");
        Label title = new Label("Contents of RAM after executing :") ;
        VBox.setMargin(title , new Insets(15,0,15,0));
        title.setFont(Font.font(23));
        VBox box = new VBox(10) ;
        box.setPadding(new Insets(10,15,10,15));
        Separator sptr = new Separator(Orientation.HORIZONTAL) ;
        VBox.setMargin(sptr , new Insets(0,0,15,0));
        box.getChildren().addAll(title , sptr , ramContents) ;
        box.setAlignment(Pos.CENTER);
        Scene scene = new Scene(box , 600,725) ;
        afterLoading.setScene(scene);
        afterLoading.getIcons().add(new Image("file:///J:/ram.png")) ;
        afterLoading.show() ;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
