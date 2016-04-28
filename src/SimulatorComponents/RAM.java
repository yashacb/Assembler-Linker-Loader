package SimulatorComponents;

public class RAM
{
    String[] ram ;
    public RAM()
    {
        ram = new String[1024] ;
        for(int i = 700 ; i < 775 ; i++)
            ram[i] = new String("00000000000000000000000000000000") ;
    }
    public String read(int location)
    {
        return ram[location] ;
    }
    public void write(int location , String data)
    {
        ram[location] = data ;
    }
    public void printRam()
    {
        for(int i = 700 ; i <750 ; i++)
        {
            if (!ram[i].startsWith("S") && !ram[i].equals("11111111111111111111111111111111"))
                System.err.println(ram[i] + " : " + Integer.parseInt(ram[i].substring(10), 2));
            else
                System.err.println(ram[i]);
        }
    }
}
