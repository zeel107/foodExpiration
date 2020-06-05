package com.example.foodtracker3;

/*
    Class to represent a Unit.
 */
public class Unit
{
    private int id;            // consider changing to short int, place cap on max custom units
    private String name;
    private String abbrev;

    // constructors
    public Unit() { }

    public Unit(int id, String name, String abbrev)
    {
        this.id = id;
        this.name = name;
        this.abbrev = abbrev;
    }

    // getters & setters
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getAbbrev() { return abbrev; }

    public void setAbbrev(String abbrev) { this.abbrev = abbrev; }
}
