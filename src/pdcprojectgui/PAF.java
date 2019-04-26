package pdcprojectgui;

import java.util.ArrayList;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PAF extends LifeLine
{
    @Override
    public int use(Question question) 
    {
       if (super.getUsed())
       {
           JOptionPane.showMessageDialog(null, "Phone a Friend has already been used! Select a different lifeline or answer the question.");
           return -1;
       }
       
       String intro1 = "You called your friend! They said...";
       
       Random rand = new Random();
       
       ArrayList<String> responses = new ArrayList();
       responses.add("I'm pretty sure the answer is number ");
       responses.add("I don't know! If I had to guess, I'd say number ");
       responses.add("Uhhhh.... probably number ");
       responses.add("I know the answer! It's number ");
       responses.add("Why did you call me??? We're not even friends. The answer is number ");
       responses.add("I think the answer is number ");
       
       // randomly select one of the responses from the list
       String reply = "\"";
       reply = reply + responses.get(rand.nextInt(responses.size()));
       
       // the answer the "friend" will give
       int answer = -1;
       
       // chance of giving correct answer is 70%
       // 1-7 = correct answer, 8-10 = random false answer
       int correct = rand.nextInt(10);
       
       if (correct < 7)
       {
           answer = question.getCorrectAns();
       }
       else
       {
           while (answer == -1) // generate random answer
           {
               answer = rand.nextInt(4) + 1; // adjust for 0-3 != 1-4
               
               // check that the answer generated was not correct
               // if it is, repeat
               if (answer == question.getCorrectAns()) 
               {
                   answer = -1;
               }
           }
       }
       
       reply = reply + answer + "\"";
       
       JPanel display = new JPanel();
       display.setLayout(new BoxLayout(display,BoxLayout.Y_AXIS));
        
       display.add(new JLabel(intro1));
       display.add(new JLabel(reply));
        
       JOptionPane.showMessageDialog(null, display);
       
       super.setUsed();
       return -1; // returning -1 triggers re-scan for answer
    }  
}
