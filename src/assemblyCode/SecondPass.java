package assemblyCode;

import HelperClasses.ObjectModule;
import HelperClasses.OpTableRow;
import HelperClasses.SymTableRow;
import sample.Main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.stream.Stream;

public class SecondPass
{
    String fileName ;
    HashSet<SymTableRow> symbols = new HashSet<>() ;
    HashSet<OpTableRow> operations = new HashSet<>() ;
    HashMap<String , Integer> registers = new HashMap<>() ;
    List<Integer> reloctab = new ArrayList<>() ;
    List<String> machines = new ArrayList<>() ;
    int start ;
    int loc_ctr = 0 ;
    SecondPass(String fileName , HashSet<SymTableRow> symbols , HashSet<OpTableRow> operations, HashMap<String , Integer> registers)
    {
        this.symbols = symbols ;
        this.operations = operations ;
        this.registers = registers ;
        this.fileName = fileName ;
    }
    public String getFileName(){return this.fileName ;}
    public ObjectModule secondPass(Main main , String file) throws Exception
    {
        System.err.println(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))) ;
        String str = null ;
        System.out.println("---------------------------------------------------------------------------------------");
        boolean error = false ;
        try {
            while ((str = br.readLine()) != null)
            {
                System.out.println(str);
                if (str.startsWith("AD"))
                {
                    String[] fields = str.split("-");
                    if (fields[1].equals("1"))
                    {
                        loc_ctr = Integer.parseInt(fields[2]);
                        start = loc_ctr + 1 ;
                    }
                    System.out.println(loc_ctr) ;
                }
                else if (str.startsWith("DS"))
                {
                    machines.add("Storage for variable : " + str.substring(6).trim());
                }
                else if (str.startsWith("IS"))
                {
                    String[] fields = str.split("-");
                    String machine_instruction = "";
                    String opcode = Integer.toBinaryString(Integer.parseInt(fields[1]));
                    if(Integer.parseInt(fields[1]) == 3 || Integer.parseInt(fields[1]) == 6 || Integer.parseInt(fields[1]) == 14 || Integer.parseInt(fields[1]) == 15)
                        reloctab.add(loc_ctr) ;
                    int temp = 4 - opcode.length();
                    for (int i = 0; i < temp; i++)
                        opcode = "0" + opcode;
                    if (fields.length == 2)
                    {
                        machines.add(opcode + "0000000000000000000000000000");
                        System.out.println(opcode + "0000000000000000000000000000");
                    }
                    else if (fields.length == 4)
                    {
                        System.err.println("HDBAIDSBHASBDUABSDU : " + symbols);
                        System.out.println(symbols);
                        String last = "" ;
                        for(SymTableRow row : symbols)
                        {
                            System.err.println("CHECK : " + row.getName() + " : " + fields[3]);
                            if(row.getName().equals(fields[3]))
                            {
                                last = Integer.toBinaryString(row.getAddress()) ;
                                break ;
                            }
                        }
                        while (last.length() < 28)
                            last = "0" + last;
                        machines.add(opcode + last);
                        System.out.println(opcode + last + " : " + ObjectModule.interpretMachineInstruction(opcode + last));
                    }
                    else
                    {
                        String register = Integer.toBinaryString(Integer.parseInt(fields[3]));
                        while(register.length() < 4)
                            register = "0" + register ;
                        String last = "";
                        if (fields[4].equals("R"))
                        {
                            last = Integer.toBinaryString(Integer.parseInt(fields[5]));
                            while(last.length() < 4)
                                last = "0" + last ;
                            while (last.length() < 24)
                                last = last + "0" ;
                            machine_instruction = opcode + register + last;
                        }
                        else if (fields[4].equals("C"))
                        {
                            last = Integer.toBinaryString(Integer.parseInt(fields[5]));
                            while(last.length() < 24)
                                last = "0" + last ;
                            machine_instruction = opcode + register + last;
                        }
                        else
                        {
                            Stream<SymTableRow> syms = symbols.stream().filter(e -> e.getName().trim().equals(fields[5])) ;
                            System.err.println(symbols);
                            long count = syms.count() ;
                            if(count == 1)
                                last = Integer.toBinaryString(symbols.stream().filter(e -> e.getName().trim().equals(fields[5])).findFirst().get().getAddress());
                            else if(count > 1)
                                throw new DuplicateVariableDeclaration(fields[5]) ;
                            else
                                throw new VariableNotDeclaredException(fields[5]) ;
                            while(last.length() < 24)
                                last = "0" + last ;
                            machine_instruction = opcode + register + last;
                        }
                        machines.add(machine_instruction);
                        System.out.println(machine_instruction + " : " + ObjectModule.interpretMachineInstruction(machine_instruction));
                    }
                }
                loc_ctr = loc_ctr + 1 ;
            }
        }
        catch(VariableNotDeclaredException | DuplicateVariableDeclaration e)
        {
            System.err.println(e);
            error = true ;
            main.errorOccurred("Undeclared variable : \'" + e + "\' in file " + this.fileName);
        }
        br.close() ;
        ObjectModule module = new ObjectModule(machines , reloctab , symbols , start , file) ;
        if(!error)
        {

        }
        else
            return null ;
        return module ;
    }
}
class VariableNotDeclaredException extends Exception
{
    String undefined ;
    VariableNotDeclaredException(String variable)
    {
        this.undefined = variable ;
    }

    @Override
    public String toString() {
        return undefined ;
    }
}
class DuplicateVariableDeclaration extends Exception
{
    String undefined ;
    DuplicateVariableDeclaration(String variable)
    {
        this.undefined = variable ;
    }

    @Override
    public String toString() {
        return undefined ;
    }
}
