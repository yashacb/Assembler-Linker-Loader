package assemblyCode;

import HelperClasses.ObjectModule;
import HelperClasses.OpTableRow;
import HelperClasses.SymTableRow;
import sample.Main;

import java.io.*;
import java.util.*;

public class FirstPass
{
    int loc_ctr = 0 ;
    public String fileName ;
    HashSet<SymTableRow> symbols = new LinkedHashSet<>() ;
    public static HashSet<OpTableRow> operations = new HashSet<>() ;
    ArrayList<String> interCodes = new ArrayList<>() ;
    int t_origin = 0 ;
    HashMap<String , Integer> registers = new HashMap<>() ;
    static
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
    public HashSet<SymTableRow> getSymbols()
    {
        return symbols ;
    }
    public HashSet<OpTableRow> getOperations()
    {
        return operations ;
    }
    public ArrayList<String> getInterCodes()
    {
        return interCodes ;
    }
    public  void initialize()
    {
        registers.put("_Acc" , 0) ;
        registers.put("_R1" , 1) ;
        registers.put("_R2" , 2) ;
        registers.put("_R3" , 3) ;
        for(int i = 4 ; i < 16 ; i++)
            registers.put("_helper" + (i-4) , i) ;
    }
    public boolean run(Main main , String fname) throws Exception
    {
        File file = new File(fname) ;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))) ;
        String str = null ;
        int line = 0 ;
        while( (str = br.readLine()) != null)
        {
            line ++ ;
            System.out.println(str + " : " + loc_ctr);
            if(str.startsWith("START"))
            {
                loc_ctr = Integer.parseInt(str.substring(6)) ;
                t_origin = loc_ctr ;
                interCodes.add("AD-1-" + loc_ctr) ;
                System.out.println("AD-1-" + loc_ctr) ;
                loc_ctr += 1 ;
            }
            else if(str.startsWith("DS"))
            {
                String[] fields = str.split(" ") ;
                try
                {
                    boolean inserted = symbols.add(new SymTableRow(fields[1], fields[2] , loc_ctr , false));
                    if(!inserted)
                    {
                        main.errorOccurred("Duplicate declaration for : " + fields[1] + "\non line " + line);
                        return false ;
                    }
                }
                catch(Exception e)
                {
                    boolean inserted = symbols.add(new SymTableRow(fields[1] , "PB"  , loc_ctr , true));
                    if(!inserted)
                    {
                        main.errorOccurred("Duplicate declaration for  \'" + fields[1] + "\'\none line " + line);
                        return false ;
                    }
                }
                interCodes.add("DS-03-" + fields[1]  + (str.contains("extern") ? "--extern" : "")) ;
                loc_ctr += 1 ;
            }
            else if(str.startsWith(".L"))
            {
                symbols.add(new SymTableRow(str , "label" , loc_ctr , true)) ;
            }
            else if(str.equals("END"))
            {
                interCodes.add("AD-02") ;
                System.out.println("AD-02");
            }
            else {
                String[] fields = str.split(" ");
                try
                {
                    if (fields.length == 1)
                    {
                        interCodes.add("IS-" + operations.stream().filter(e -> e.getOpcode().equals(fields[0])).findFirst().get().getOpnum());
                        System.out.println("IS-" + operations.stream().filter(e -> e.getOpcode().equals(fields[0])).findFirst().get().getOpnum());
                    }
                    else if (fields.length == 2)
                    {
                        interCodes.add("IS-" + operations.stream().filter(e -> e.getOpcode().equals(fields[0])).findFirst().get().getOpnum() + "-V-" +
                                fields[1]);
                        System.out.println("IS-" + operations.stream().filter(e -> e.getOpcode().equals(fields[0])).findFirst().get().getOpnum() + "-V-" +
                                fields[1]);
                    }
                    else
                    {
                        String toAdd = "IS-" + operations.stream().filter(e -> e.getOpcode().equals(fields[0])).findFirst().get().getOpnum() + "-";
                        toAdd = toAdd + "R-" +  registers.get(fields[1]) + "-";
                        if (fields[2].startsWith("$")) {
                            toAdd = toAdd + "C-" + fields[2].substring(1);
                        }
                        else if(fields[2].startsWith("_"))
                            toAdd = toAdd + "R-" +  registers.get(fields[2]);
                        else
                            toAdd = toAdd + "V-" + fields[2];
                        interCodes.add(toAdd);
                        System.out.println(toAdd);
                    }
                    loc_ctr += 1;
                }
                catch (Exception e)
                {
                    System.out.println("No variable defined .");
                }
            }

        }
        PrintWriter pw = new PrintWriter(new FileOutputStream(fname + ".inter") , true) ;
        symbols.stream().forEach(System.out::println);
        interCodes.stream().forEach(System.out::println);
        interCodes.stream().forEach(pw::println);
        pw.close();
        br.close();
        return true ;
    }
    public SecondPass firstPass(Main main , String name) throws Exception
    {
        fileName = name ;
        initialize();
        return !run(main, name) ? null : new SecondPass(name + ".inter" , symbols , operations , registers) ;
    }
}