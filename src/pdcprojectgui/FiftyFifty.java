package pdcprojectgui;

import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class FiftyFifty extends LifeLine
{
    @Override
    public int use(Question question) 
    {
        
        if (super.getUsed())
        {
            JOptionPane.showMessageDialog(null, "50/50 has already been used! Select a different lifeline or answer the question.");
            return -1;
        }

        Random rand = new Random();
        String intro1 = "You have used the 50/50 lifeline!";
        String intro2 = "2 incorrect answers have been removed.";

        int remove1 = -1;
        int remove2 = -1;

        boolean valid1 = false;
        boolean valid2 = false;

        // select first answer to remove
        // if randomly generated num is correct, generate again
        while (valid1 == false)
        {
            remove1 = rand.nextInt(4);
            if ((remove1 + 1) != question.getCorrectAns())
            {
                valid1 = true;
            }
        }

        // select second answer to remove
        // if generated num is correct OR same as first num to remove, gen again
        while (valid2 == false)
        {
            remove2 = rand.nextInt(4);
            if ((remove2 + 1) != question.getCorrectAns() && remove2 != remove1)
            {
                valid2 = true;
            }
        }

        // replace selected questions with blank space 
        question.setAnswers(remove1, " ");
        question.setAnswers(remove2, " ");

        JPanel display = new JPanel();
        display.setLayout(new BoxLayout(display,BoxLayout.Y_AXIS));
        
        display.add(new JLabel(intro1));
        display.add(new JLabel(intro2));
        
        JOptionPane.showMessageDialog(null, display);

        super.setUsed();
        return 1; // successfully completed
    }

 }
