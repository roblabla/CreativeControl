package me.FurH.CreativeControl.manager;

import java.util.*;

public class CreativeBlockData
{
    public String owner;
    public int type;
    public HashSet<String> allowed;
    public String date;
    
    public CreativeBlockData(final int type) {
        super();
        this.type = type;
    }
    
    public CreativeBlockData(final String owner, final int type, final HashSet<String> allowed) {
        super();
        this.owner = owner;
        this.type = type;
        this.allowed = allowed;
    }
    
    public CreativeBlockData(final String owner, final int type, final HashSet<String> allowed, final String date) {
        super();
        this.owner = owner;
        this.type = type;
        this.allowed = allowed;
        this.date = date;
    }
}
