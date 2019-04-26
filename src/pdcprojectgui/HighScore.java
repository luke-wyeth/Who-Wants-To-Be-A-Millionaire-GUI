package pdcprojectgui;

import java.io.*;

public class HighScore implements Serializable, Comparable
{
    private String name;
    private int score;
    
    public HighScore()
    {
        name = "";
        score = 0;
    }
    
    public HighScore(String name, int score)
    {
        this.name = name;
        this.score = score;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getScore()
    {
       return score;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setScore(int score)
    {
        this.score = score;
    }
    
    @Override
    public String toString()
    {
        return name + " " + score;
    }

    @Override
    public int compareTo(Object o)
    {
        // negative if this is less than supplied 
        // 0 if equal
        // positive if this is greater than supplied
        
        return score - ((HighScore)o).getScore();
    }
    
}