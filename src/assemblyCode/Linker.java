package assemblyCode;

import HelperClasses.ObjectModule;
import HelperClasses.SymTableRow;
import sample.Main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Linker
{
    List<ObjectModule> modules = null ;
    int linkerOrigin ;
    int addressOfWorkArea ;
    HashMap<String , Integer> ntable = new HashMap<>() ;
    enum state { relocated , nope }
    state s ;
    public Linker(List<ObjectModule> modules , int linker_origin , int addressOfWorkArea)
    {
        this.modules = modules ;
        this.linkerOrigin = linker_origin ;
        this.addressOfWorkArea = addressOfWorkArea ;
        s = state.nope ;
    }
    public void programRelocation()
    {
        int programLinkedOrigin = this.linkerOrigin ;
        for(ObjectModule module : modules )
        {
            int translatedOrigin = module.getStart_location() ;
            int moduleSize = module.getMachines().size() ;
            for(Integer address : module.getReloctab())
            {
                int entry = address - translatedOrigin ;
                int relocationFactor = programLinkedOrigin - translatedOrigin ;
                String instruction = module.getMachines().get(entry) ;
                int opcode = Integer.parseInt(instruction.substring(0 , 4)) ;
                String modInstruction ;
                if(opcode == 14 || opcode == 15)
                {
                    String modified = Integer.toBinaryString(Integer.parseInt(instruction.substring(4) , 2) + relocationFactor) ;
                    while(modified.length() < 28)
                        modified = "0" + modified ;
                    modInstruction = instruction.substring(0 , 4) + modified ;
                }
                else
                {
                    String modified = Integer.toBinaryString(Integer.parseInt(instruction.substring(8) , 2) + relocationFactor) ;
                    while(modified.length() < 24)
                        modified = "0" + modified ;
                    modInstruction = instruction.substring(0 , 8) + modified ;
                }
                module.getMachines().set(entry , modInstruction) ;
            }
            programLinkedOrigin += moduleSize ;
        }
        s = state.relocated ;
    }
    public boolean programLinking(Main main)
    {
        int programLinkOrigin = linkerOrigin ;
        for(ObjectModule module : modules)
        {
            int tOrigin = module.getStart_location() ;
            int omSize = module.getMachines().size() ;
            int relocationFactor = programLinkOrigin - tOrigin ;
            HashSet<SymTableRow> symbols = module.getSymbols() ;
            symbols.stream().filter(e -> e.getType().trim().equals("PB")).forEach(e -> ntable.put(e.getName() , e.getAddress() + relocationFactor));
            ntable.put(module.getName() , relocationFactor) ;
            programLinkOrigin += omSize ;
        }
        for(ObjectModule module : modules)
        {
            List<SymTableRow> externs = module.getSymbols().stream().filter(e -> e.getType().equals("extern")).collect(Collectors.toList());
            System.out.println(externs);
            for(SymTableRow extern : externs)
            {
                for(Integer address : module.getReloctab()) {
                    int entry = address - module.getStart_location();
                    try
                    {
                        if (module.getMachines().get(entry).substring(4).contains(Integer.toBinaryString(extern.getAddress() + ntable.get(module.getName()))))
                        {
                            extern.setResolved();
                            String mods = Integer.toBinaryString(ntable.get(extern.getName()));
                            while (mods.length() < 24)
                                mods = "0" + mods;
                            String modified = module.getMachines().get(entry).substring(0, 8) + mods;
                            module.getMachines().set(entry, modified);
                        }
                    }
                    catch (Exception e)
                    {
                        main.errorOccurred("No external definition for \'" + extern.getName() + "\'");
                        return false ;
                    }
                }
            }
        }
        return true ;
    }
}
