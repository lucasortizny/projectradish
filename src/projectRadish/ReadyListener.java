package projectRadish;



import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.exceptions.*;
import net.dv8tion.jda.core.hooks.*;
public class ReadyListener implements EventListener
{
    public static void main(String[] args)
            throws LoginException, RateLimitedException, InterruptedException
    {
        // Note: It is important to register your ReadyListener before building
        JDA jda = new JDABuilder(AccountType.BOT)
            .setToken("NDMxNjcwNDI3NDg0Njg0MzEx.DctCUg.ziZXxz5YefmK_qGFyssiU8_qLP4")
            .addEventListener(new ReadyListener())
            .buildBlocking();
        System.out.println("done");
    }

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

    
}