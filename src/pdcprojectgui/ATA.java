package pdcprojectgui;

import java.util.ArrayList;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ATA extends LifeLine
{    
    @Override
    public int use(Question question) 
    {
        if (super.getUsed())
        {
            JOptionPane.showMessageDialog(null, "Ask the Audience has already been used! Select a different lifeline or answer the question.");
            return -1;
        }
        
        String intro1 = "You have used the Ask the Audience lifeline!";
        String intro2 = "Here's what the audience voted for:";
        
        int[] percentages =  new int[4]; // 0-3 represents answer nums 1-4
        int corrAns = question.getCorrectAns()-1; 
        Random random = new Random();
        ArrayList<String> output = new ArrayList();
        
        // give correct answer a base vote of 40%
        percentages[corrAns] = 40;
        
        int remaining = 60; // percent remaining to randomise over answers
        
        for (int i = 0; i < 4; i++)
        {
            String line = "";
            int gen = 0;
                    
            if (remaining > 0)
            {
               gen = random.nextInt(remaining) + 1; 
            }
            
            percentages[i] += gen;
            remaining -= gen;
            
            line = line + (i+1) + ") ";
            //System.out.print((i+1) + ") ");
            
            for (int k = 0; k < percentages[i]; k++)
            {
               // System.out.print("-");
                line = line + "-";
            }
            
           // System.out.println(percentages[i] + "%");
            line = line + percentages[i] + "%";
            
            output.add(line);
        }
        
        //System.out.println("Enter your answer now: ");
        JPanel display = new JPanel();
        display.setLayout(new BoxLayout(display,BoxLayout.Y_AXIS));
        
        display.add(new JLabel(intro1));
        display.add(new JLabel(intro2));
        
        for (int i = 0; i < output.size(); i++)
        {
            display.add(new JLabel(output.get(i)));
        }
        
        JOptionPane.showMessageDialog(null, display);
        super.setUsed();
        
        return 1; // successfully completed
    }
    
}
