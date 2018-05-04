package projectRadish;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.Route.Self;
import java.util.List;
import java.util.ArrayList;

import java.util.Random;

public class MessageListener extends ListenerAdapter {
    /**
     * NOTE THE @Override!
     * This method is actually overriding a method in the ListenerAdapter class! We place an @Override annotation
     *  right before any method that is overriding another to guarantee to ourselves that it is actually overriding
     *  a method from a super class properly. You should do this every time you override a method!
     *
     * As stated above, this method is overriding a hook method in the
     * {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter} class. It has convenience methods for all JDA events!
     * Consider looking through the events it offers if you plan to use the ListenerAdapter.
     *
     * In this example, when a message is received it is printed to the console.
     *
     * @param event
     *          An event containing information about a {@link net.dv8tion.jda.core.entities.Message Message} that was
     *          sent in a channel.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!shouldIgnore(event)) {
            handleMessage(event);
        }
    }

    /**
     * Ignore the message if...
     * @param event MessageReceivedEvent
     * @return Whether to ignore the message (bool)
     */
    private boolean shouldIgnore(MessageReceivedEvent event) {
        boolean ignore = false;
        User author = event.getAuthor();
        String myId = event.getJDA().getSelfUser().getId();

        // We sent the message (don't talk to yourself)
        if (author.getId().equals(myId)) { ignore = true; }

        // Message was sent by a bot
        if (author.isBot()) { ignore = true; }

        return ignore;
    }

    private void handleMessage(MessageReceivedEvent event) {

        //Event specific information
        User author = event.getAuthor();                //The user that sent the message
        Message message = event.getMessage();           //The message that was received.
        MessageChannel channel = event.getChannel();    //This is the MessageChannel that the message was sent to.
        //  This could be a TextChannel, PrivateChannel, or Group!

        String msg = message.getContentDisplay();              //This returns a human readable version of the Message. Similar to
        // what you would see in the client.

        if (event.isFromType(ChannelType.TEXT))         //If this message was sent to a Guild TextChannel
        {
            //Because we now know that this message was sent in a Guild, we can do guild specific things
            // Note, if you don't check the ChannelType before using these methods, they might return null due
            // the message possibly not being from a Guild!

            Guild guild = event.getGuild();             //The Guild that this message was sent in. (note, in the API, Guilds are Servers)
            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.
            Member member = event.getMember();          //This Member that sent the message. Contains Guild specific information about the User!

            String name;
            if (message.isWebhookMessage())
            {
                System.out.println("Webhook detected! A rare specimen indeed.");
                name = author.getName();                //If this is a Webhook message, then there is no Member associated
            }                                           // with the User, thus we default to the author for name.
            else
            {
                name = member.getEffectiveName();       //This will either use the Member's nickname if they have one,
            }                                           // otherwise it will default to their username. (User#getName())

            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
        }
        else if (event.isFromType(ChannelType.PRIVATE)) //If this message was sent to a PrivateChannel
        {
            //The message was sent in a PrivateChannel.
            //In this example we don't directly use the privateChannel, however, be sure, there are uses for it!

            System.out.printf("[PRIV]<%s>: %s\n", author.getName(), msg);
        }
        else if (event.isFromType(ChannelType.GROUP))   //If this message was sent to a Group. This is CLIENT only!
        {
            //The message was sent in a Group. It should be noted that Groups are CLIENT only.
            Group group = event.getGroup();
            String groupName = group.getName() != null ? group.getName() : "";  //A group name can be null due to it being unnamed.

            System.out.printf("[GRP: %s]<%s>: %s\n", groupName, author.getName(), msg);
        }


        //Remember, in all of these .equals checks it is actually comparing
        // message.getContentDisplay().equals, which is comparing a string to a string.
        // If you did message.equals() it will fail because you would be comparing a Message to a String!
        if (msg.equals("!info"))
        {
            String reply = "Commands: !alldocs, !doc, !say, !roll";
            channel.sendMessage(reply).queue();
        }
        else if (msg.startsWith("!say ")) {
            String reply = msg.replaceFirst("!say ", "");
            channel.sendMessage(reply).queue();
        }
        else if (msg.startsWith("!abbreviate ")) {
            String reply = msg.replaceFirst("!abbreviate ", "");
            channel.sendMessage(DidYouMean.abbreviate(reply)).queue();
        }
        else if (msg.equals("!doc")) {
            channel.sendMessage(Constants.getCurrentDoc()).queue();
        }


        else if (msg.startsWith("!doc ")) {
            String input = msg.replaceFirst("!doc ", "");
            input = input.toLowerCase();
            String game = null;

            for (String doc : Constants.getDocs().keySet()) { // If full name matches
                if (input.equals(doc.toLowerCase())) {
                    game = doc;
                }
            }

            if (game == null) { // no luck with full name
                for (String doc : Constants.getDocs().keySet()) { // check if abbreviation matches
                    String abbr = DidYouMean.abbreviate(doc).toLowerCase();
                    if (input.equals(abbr)) {
                        game = doc;
                    }
                }
            }

            String prefix = "";
            if (game == null) {    // still no match found
                game = DidYouMean.getBest(input);
                prefix = "No match found. My best guess is...\n";
            }
            String abbr = DidYouMean.abbreviate(game);
            String link = Constants.getDocs().get(game);
            String reply = prefix + String.format("%s [%s]:\n%s", game, abbr, link);
            channel.sendMessage(reply).queue();
        }


        else if (msg.equals("!roll"))
        {
            Random rand = new Random();
            int roll = rand.nextInt(6) + 1; //This results in 1 - 6 (instead of 0 - 5)
            channel.sendMessage("Your roll: " + roll).queue();
        }
        else if (msg.equals("!alldocs"))
        {
            channel.sendMessage("http://twitchplays.wikia.com/wiki/Game_Documents_(Mobile)").queue();
        }

        else if (msg.startsWith("!status ")) {
            if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                String status = msg.replaceFirst("!status ", "").toLowerCase();
                try {
                    event.getJDA().getPresence().setStatus(OnlineStatus.fromKey(status));
                } catch (IllegalArgumentException e) {
                    channel.sendMessage("Invalid Status. Must be Online, Idle, DND, Invisible, or Offline.").queue();
                }
            } else {
                channel.sendMessage("You're not allowed to use this command").queue();
            }
        }

        else if (msg.startsWith("!game ")) {
        	if (Constants.getRadishAdmin().contains(event.getAuthor().getId())) {
        	    String game = msg.replaceFirst("!game ", "");
        	    if (!game.toLowerCase().equals("none")) {
                    event.getJDA().getPresence().setGame(Game.of(GameType.DEFAULT, game));
                } else {
                    event.getJDA().getPresence().setGame(null);
                }
        	} else {
                channel.sendMessage("You're not allowed to use this command").queue();
            }
        }
        
        
        else if (msg.startsWith("!streaming ")) {
        	
        	
        	if ((Constants.getTPEAdmin().contains(event.getAuthor().getId())) || Constants.getRadishAdmin().contains(event.getAuthor().getId())) {
        		String game = msg.replaceFirst("!streaming ", "");
        		if (!game.toLowerCase().equals("none")) {
        			event.getJDA().getPresence().setGame(Game.of(GameType.STREAMING, game, "https://twitch.tv/twitchplays_everything"));
        			
        		} else {
        			event.getJDA().getPresence().setGame(null);
        		}
        	}
        	else {
        		channel.sendMessage("You're not allowed to use this command").queue();
        	}
        }
        else if (msg.startsWith("!listening to ")) {
        	if ((Constants.getRadishAdmin().contains(event.getAuthor().getId()))) {
        		String game = msg.replaceFirst("!listening to ", "");
        		if (!game.toLowerCase().equals("none")) {
        			event.getJDA().getPresence().setGame(Game.of(GameType.LISTENING, game));
        		}
        		else {
        			event.getJDA().getPresence().setGame(null);
        		}
        	}
        }
        else if (msg.startsWith("!watching ")) {
        	if ((Constants.getRadishAdmin().contains(event.getAuthor().getId()))) {
        		String game = msg.replaceFirst("!watching ", "");
        		if (!game.toLowerCase().equals("none")) {
        			event.getJDA().getPresence().setGame(Game.of(GameType.WATCHING, game));
        		}
        		else {
        			event.getJDA().getPresence().setGame(null);
        		}
        	}
        }
    }
}
        