package HelperClasses;

import java.util.HashSet;
import java.util.List;

public class ObjectModule
{
    String name ;
    List<String> machines = null ;
    List<Integer> reloctab = null ;
    HashSet<SymTableRow> symbols = new HashSet<>() ;
    int start_location = 0 ;
    public ObjectModule(List<String> machines , List<Integer> reloctab , HashSet<SymTableRow> symbols , int start_location , String name)
    {
        this.machines = machines ;
        this.reloctab = reloctab ;
        this.symbols = symbols ;
        this.start_location = start_location ;
        this.name = name ;
    }
    public List<String> getMachines()
    {
        return machines ;
    }
    public List<Integer> getReloctab()
    {
        return reloctab ;
    }
    public HashSet<SymTableRow> getSymbols()
    {
        return symbols ;
    }
    public int getStart_location()
    {
        return start_location ;
    }
    public String getName()
    {
        return name ;
    }
    public void printMachines()
    {
        machines.stream().forEach(e -> System.out.println(e + " : " + interpretMachineInstruction(e)));
    }
    public void printReloctab()
    {
        reloctab.stream().forEach(System.out::println);
    }
    public void printSymbols()
    {
        symbols.stream().forEach(System.out::println);
    }
    public static String interpretMachineInstruction(String instruction)
    {
        if(instruction.charAt(0) == '0' || instruction.charAt(0) == '1')
        {
            int opcode = Integer.parseInt(instruction.substring(0, 4), 2);
            if(opcode == 14 || opcode == 15)
            {
                int last = Integer.parseInt(instruction.substring(4 , 8) , 2) ;
                return opcode + " " + last ;
            }
            int register1 = Integer.parseInt(instruction.substring(4, 8), 2);
            int last = 0;
            if(opcode == 5 || opcode == 7 || opcode == 8 || opcode == 9)
                last = Integer.parseInt(instruction.substring(8 , 12) , 2) ;
            else
                last = Integer.parseInt(instruction.substring(8) , 2) ;
            return opcode + " " + register1 + " " + last ;
        }
        else
        {
            return "" ;
        }
    }
}
