package Tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pdcprojectgui.ATA;
import pdcprojectgui.Question;

/**
 *
 * @author Luke
 */
public class ATATest
{
    public ATA ata;
    public Question testQuestion;
    
    @Before
    public void setUp()
    {
        ata = new ATA();
        testQuestion = new Question(); 
    }
    
    /*
        test functionality of trying to use unused lifeline
        - checking used state
    */
    @Test
    public void testUse()
    {
        boolean expectedUsedState = false;
        boolean actualUsedState = ata.getUsed();
        
        Assert.assertEquals(expectedUsedState, actualUsedState);
        
        // lifelines return 1 after successfully completing
        // they return -1 if lifeline had already been  used, didn't successfully complete
        int expectedReturn = 1;
        int actualReturn = ata.use(testQuestion);
        
        Assert.assertEquals(expectedReturn, actualReturn);
    }
    
    /*
        test functionality of trying to use already used lifeline
        - checking used state
        - checking lifeline can't be used again
    */
    @Test
    public void testAlreadyUsed()
    {
        ata.setUsed(); // set to already used
        
        boolean expectedUsedState = true;
        boolean actualUsedState = ata.getUsed();
        
        Assert.assertEquals(expectedUsedState, actualUsedState);
        
        // lifelines return 1 after successfully completing
        // they return -1 if lifeline had already been  used, didn't successfully complete
        int expectedReturn = -1;
        int actualReturn = ata.use(testQuestion);
        
        Assert.assertEquals(expectedReturn, actualReturn);
    }
}
