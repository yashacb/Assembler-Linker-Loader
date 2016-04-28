package HelperClasses;

public class OpTableRow
{
    String opcode , opclass ;
    int opnum ;
    public OpTableRow(String opcode , String op_class , int op_num)
    {
        this.opcode = opcode ;
        this.opclass = op_class ;
        this.opnum = op_num ;
    }

    public String getOpclass() {
        return opclass;
    }

    public void setOpclass(String opclass) {
        this.opclass = opclass;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public int getOpnum() {
        return opnum;
    }

    public void setOpnum(int opnum) {
        this.opnum = opnum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpTableRow that = (OpTableRow) o;

        return opnum == that.opnum && opcode.equals(that.opcode) && opclass.equals(that.opclass);

    }

    @Override
    public int hashCode() {
        int result = opcode.hashCode();
        result = 31 * result + opclass.hashCode();
        result = 31 * result + opnum;
        return result;
    }
}
