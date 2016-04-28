package SimulatorComponents;

import java.util.List;

public class Loader
{
    int start ;
    List<String> instructions ;
    public Loader(int start , List<String> instructions)
    {
        this.start = start ;
        this.instructions = instructions ;
    }
    public RAM load()
    {
        RAM ram = new RAM() ;
        for(int i = 0 ; i < instructions.size() ; i++)
        {
            ram.write(start + i ,instructions.get(i));
        }
//        System.err.println("-----------");
//        ram.printRam();
//        System.err.println("-----------");
        return ram ;
    }
}
