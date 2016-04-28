package HelperClasses;

public class SymTableRow
{
    String name , type;
    int address , length ;
    boolean resolved ;
    public SymTableRow(String name , String type , int address , boolean resolved)
    {
        this.name = name ;
        this.address = address ;
        this.length = 1 ;
        this.type = type ;
        this.resolved = resolved ;
    }
    public String getName()
    {
        return this.name ;
    }
    public int getAddress()
    {
        return this.address ;
    }
    public boolean getResolved()
    {
        return this.resolved ;
    }
    public int getLength()
    {
        return this.length ;
    }
    public void setResolved()
    {
        this.resolved = true ;
    }
    public String getType()
    {
        return this.type ;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SymTableRow that = (SymTableRow) o;

        return  name.equals(that.name) ;

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "SymTableRow{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", address=" + address +
                ", length=" + length +
                '}';
    }
}
