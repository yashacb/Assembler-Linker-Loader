package SimulatorComponents;

public class RegisterFile
{
    String[] registers ;
    public RegisterFile()
    {
        registers = new String[16] ;
    }
    public String read(int number)
    {
        return registers[number] ;
    }
    public void write(int number , String data)
    {
        registers[number] = data ;
    }
}
