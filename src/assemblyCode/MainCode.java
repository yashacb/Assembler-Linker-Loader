package assemblyCode;

import HelperClasses.ObjectModule;
import sample.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainCode
{
    List<String> allFiles ;
    List<SecondPass> secondPassList ;
    List<FirstPass> firstPassList ;
    List<ObjectModule> modules ;
    public HashMap<String , Integer> ntable ;
    public List<String> linker(Main main)
    {
        Collections.reverse(modules);
        Linker linker = new Linker(modules , 700 , 0) ;
        linker.programRelocation();
        boolean done = linker.programLinking(main);
        if(done)
        {
            List<String> machines = new ArrayList<>() ;
            int i = 700 ;
            for(ObjectModule module : modules)
            {
                machines.addAll(module.getMachines()) ;
            }
            ntable = linker.ntable ;
            return machines ;
        }
        return null ;
    }
    public List<String> toAssemblyCode(Main main , String fname) throws Exception
    {
        allFiles = new ArrayList<>() ;
        List<String> files = new ArrayList<>() ;
        files.add(fname) ;
        allFiles.add(fname) ;
        List<String> convertedFiles = new ArrayList<>() ;
        while(files.size() != 0)
        {
            String file = files.get(0) ;
            System.out.println("File being processed : " + file);
            files.remove(0) ;
            convertedFiles.add(file) ;
            ToAssemblyCode toAssemblyCode = new ToAssemblyCode();
            List<String> filesToAdd = toAssemblyCode.convertToAssembly(main , file , 500) ;
            files.addAll(filesToAdd) ;
            allFiles.addAll(filesToAdd) ;
        }
//        List<ObjectModule> modules = new ArrayList<>() ;
//        while(convertedFiles.size() != 0)
//        {
//            FirstPass firstPass = new FirstPass();
//            String file = convertedFiles.get(0) ;
//            SecondPass secondPass = firstPass.firstPass(main , file + ".assembly") ;
//            ObjectModule module = secondPass.secondPass(file + ".assembly.inter") ;
//            modules.add(module) ;
//            convertedFiles.remove(0) ;
//        }
//        Collections.reverse(modules) ;
//        System.out.println("-----------------------------------------------------------------------------------------") ;
//        modules.stream().forEach(e -> {e.printMachines();e.printReloctab();e.printSymbols();System.out.println(e.getStart_location());});
//        System.out.println("-----------------------------------------------------------------------------------------") ;
//        Linker linker = new Linker(modules , 700 , 500) ;
//        System.out.println("Linker logs :") ;
//        linker.programRelocation();
//        System.out.println("-----------------------------------------------------------------------------------------") ;
//        modules.stream().forEach(e -> {e.printMachines();e.printReloctab();e.printSymbols();});
//        System.out.println("-----------------------------------------------------------------------------------------") ;
//        System.out.println("Program linking :");
//        linker.programLinking();
//        System.out.println("-----------------------------------------------------------------------------------------") ;
//        modules.stream().forEach(e -> {e.printMachines();e.printReloctab();e.printSymbols();});
//        System.out.println("-----------------------------------------------------------------------------------------") ;
        return allFiles ;
    }
    public Object firstPass(Main main)
    {
        firstPassList = new ArrayList<>() ;
        secondPassList = new ArrayList<>() ;
        secondPassList = new ArrayList<>() ;
        try {
            for (String file : allFiles)
            {
                FirstPass firstPass = new FirstPass();
                SecondPass res = firstPass.firstPass(main , file + ".assembly") ;
                if(res != null)
                    secondPassList.add(res);
                else
                    return null ;
                firstPassList.add(firstPass) ;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace() ;
            return e.toString() ;
        }
        return firstPassList ;
    }
    public List<ObjectModule> secondPass(Main main)
    {
        modules = new ArrayList<>() ;
        try
        {
            if (secondPassList.size() > 0)
            {
                for (SecondPass sp : secondPassList)
                {
                    ObjectModule module = sp.secondPass(main, sp.getFileName());
                    System.out.println(module);
                    if(module == null)
                        return null ;
                    else
                        modules.add(module) ;
                }
            }
            else
                return null;
            return modules ;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return  null ;
        }
    }
}
