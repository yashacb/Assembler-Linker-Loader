package HelperClasses;

public class NTableRow
{
    String name ;
    int address ;
    public NTableRow(String name , int address)
    {
        this.name = name ;
        this.address = address ;
    }

    public String getName() {
        return name;
    }

    public int getAddress() {
        return address;
    }
}
