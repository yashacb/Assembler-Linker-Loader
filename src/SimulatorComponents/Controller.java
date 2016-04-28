package SimulatorComponents;

import HelperClasses.ObjectModule;
import HelperClasses.SymTableRow;

import java.util.Collection;
import java.util.HashMap;

public class Controller
{
    RegisterFile registers = new RegisterFile() ;
    RAM ram ;
    int start ;
    public Controller(int start , RAM ram)
    {
        this.start = start ;
        this.ram = ram ;
    }
    public RAM start(HashMap<String , Integer> ntable)
    {
        int pc = start ;
        Collection<Integer> values = ntable.values();
        System.out.println(values);
        while(!ram.read(pc).equals("11111111111111111111111111111111"))
        {
            if(! values.contains(pc))
            {
                System.out.println(pc);
                String instruction = ram.read(pc) ;
                if(instruction.startsWith("S"))
                {
                    ram.write(pc, "00000000000000000000000000000000");
                    pc++ ;
                    continue;
                }
                int opcode = Integer.parseInt(instruction.substring(0,4) , 2) ;
                int register , register2 , result ;
                String resString , address ;
                int val1 , val2 ;
                switch (opcode)
                {
                    case 3 :
                        register = Integer.parseInt(instruction.substring(4 , 8) , 2) ;
                        registers.write(register , ram.read(Integer.parseInt(instruction.substring(8) , 2)));
                        pc = pc + 1 ;
                        break ;
                    case 4 :
                        register = Integer.parseInt(instruction.substring(4 , 8) , 2) ;
                        String value = instruction.substring(8) ;
                        while(value.length() < 32)
                            value = "0" +  value ;
                        registers.write(register , value);
                        pc = pc + 1 ;
                        break ;
                    case 5 :
                        register = Integer.parseInt(instruction.substring(4 , 8) , 2) ;
                        registers.write(Integer.parseInt(instruction.substring(8,12) , 2) , registers.read(register));
                        pc = pc + 1 ;
                        break ;
                    case 6 :
                        register = Integer.parseInt(instruction.substring(4 , 8)) ;
                        ram.write(Integer.parseInt(instruction.substring(12) , 2) , registers.read(register));
                        pc = pc + 1 ;
                        break ;
                    case 7 :
                        register = Integer.parseInt(instruction.substring(4,8) , 2) ;
                        register2 = Integer.parseInt(instruction.substring(8,12) , 2) ;
                        result = Integer.parseInt(registers.read(register) , 2) + Integer.parseInt(registers.read(register2) , 2) ;
                        resString = Integer.toBinaryString(result) ;
                        while (resString.length() < 32)
                            resString = "0" + resString ;
                        registers.write(register , resString);
                        pc = pc + 1 ;
                        break ;
                    case 8 :
                        register = Integer.parseInt(instruction.substring(4,8) , 2) ;
                        register2 = Integer.parseInt(instruction.substring(8,12) , 2) ;
                        result = Integer.parseInt(registers.read(register) , 2) - Integer.parseInt(registers.read(register2) , 2) ;
                        resString = Integer.toBinaryString(result) ;
                        while (resString.length() < 32)
                            resString = "0" + resString ;
                        registers.write(register , resString);
                        pc = pc + 1 ;
                        break ;
                    case 9 :
                        register = Integer.parseInt(instruction.substring(4,8) , 2) ;
                        register2 = Integer.parseInt(instruction.substring(8,12) , 2) ;
                        result = Integer.parseInt(registers.read(register) , 2) * Integer.parseInt(registers.read(register2) , 2) ;
                        resString = Integer.toBinaryString(result) ;
                        while (resString.length() < 32)
                            resString = "0" + resString ;
                        registers.write(register , resString);
                        pc = pc + 1 ;
                        break ;
                    case 10 :
                        register = Integer.parseInt(instruction.substring(4,8) , 2) ;
                        register2 = Integer.parseInt(instruction.substring(8,12) , 2) ;
                        result = Integer.parseInt(registers.read(register) , 2) / Integer.parseInt(registers.read(register2) , 2) ;
                        resString = Integer.toBinaryString(result) ;
                        while (resString.length() < 32)
                            resString = "0" + resString ;
                        registers.write(register , resString);
                        pc = pc + 1 ;
                        break ;
                    case 11 :
                        val1 = Integer.parseInt(registers.read(0), 2) ;
                        val2 = Integer.parseInt(registers.read(1), 2) ;
                        if(val1 == val2)
                            registers.write(0 , "00000000000000000000000000000001");
                        else
                            registers.write(0 , "00000000000000000000000000000000");
                        pc = pc + 1 ;
                        break ;
                    case 12 :
                        val1 = Integer.parseInt(registers.read(0), 2) ;
                        val2 = Integer.parseInt(registers.read(1), 2) ;
                        if(val1 < val2)
                            registers.write(0 , "00000000000000000000000000000001");
                        else
                            registers.write(0 , "00000000000000000000000000000000");
                        pc = pc + 1 ;
                        break ;
                    case 13 :
                        System.out.println("REgisters : " + registers.read(0) + " : " + registers.read(1));
                        val1 = Integer.parseInt(registers.read(0), 2) ;
                        val2 = Integer.parseInt(registers.read(1), 2) ;
                        if(val1 > val2)
                            registers.write(0 , "00000000000000000000000000000001");
                        else
                            registers.write(0 , "00000000000000000000000000000000");
                        pc++ ;
                        break ;
                    case 14 :
                        address = instruction.substring(4) ;
                        if(registers.read(0).endsWith("0"))
                            pc = Integer.parseInt(address , 2) ;
                        else
                            pc = pc + 1 ;
                        break ;
                    case 15 :
                        pc = Integer.parseInt(instruction.substring(4) , 2) ;
                        break ;
                    default:
                        pc++ ;
                }
            }
            else
                pc++ ;
        }
        return ram ;
    }
}
