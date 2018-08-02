package projectRadish.Commands.MetaCommands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import projectRadish.Commands.AdminCommand;
import projectRadish.Configuration;

import java.util.HashMap;

public final class NewCmdCommand extends AdminCommand
{
    @Override
    public String getDescription() {
        return "Adds a new command, using the name and Java Class you input (in that order). Currently doesn't work for command names containing spaces.";
    }

    @Override
    public void ExecuteCommand(String contents, MessageReceivedEvent event)
    {
        String[] args = contents.split(" ");

        //Ignore messages without exactly two arguments
        if (args.length != 2)
        {
            event.getChannel().sendMessage("Usage: \"command\" \"ClassName\"").queue();
            return;
        }

        HashMap<String, String> cmds = Configuration.getCommands();
        String cmdToLower = args[0].toLowerCase();

        //Check if it already exists and return if so
        if (cmds.containsKey(cmdToLower) == true)
        {
            event.getChannel().sendMessage("Command " + cmdToLower + " already exists").queue();
            return;
        }

        //Kimimaru: Don't allow adding another NewCmdCommand, since you can't remove it
        if (args[1].equals(NewCmdCommand.class.getSimpleName()))
        {
            event.getChannel().sendMessage("You can't add another NewCmdCommand since it can't be removed!").queue();
            return;
        }

        cmds.put(cmdToLower, args[1]);
        Configuration.saveConfiguration();
        Configuration.loadConfiguration();

        event.getChannel().sendMessage("Added command " + "\"" + cmdToLower + "\"").queue();
    }
}
