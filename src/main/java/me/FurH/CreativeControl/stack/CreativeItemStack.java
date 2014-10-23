package me.FurH.CreativeControl.stack;

public class CreativeItemStack
{
    public int type;
    public int data;
    
    public CreativeItemStack(final int type, final int data) {
        super();
        this.type = type;
        this.data = data;
    }
    
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof CreativeItemStack)) {
            return false;
        }
        final CreativeItemStack stack = (CreativeItemStack)object;
        return stack.type == this.type && (this.data == -1 || this.data == stack.data);
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.type;
        hash = 17 * hash + this.data;
        return hash;
    }
}
