package projectRadish.Commands.VoiceCommands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import projectRadish.Commands.BaseCommand;
import projectRadish.LavaPlayer.QueueItem;
import projectRadish.MessageListener;
import projectRadish.Utilities;

public class VoiceSeekCommand extends BaseCommand
{
    //Used for conversions; in order, Hours, Minutes, Seconds
    public static long[] timeConstants = new long[] { ((60L * 60L) * 1000L), (60L * 1000L), 1000L };

    private final String usageMsg = "Usage: \"Track position in time `hhh:mm:ss`\"";

    @Override
    public boolean canBeUsedViaPM()
    {
        return false;
    }

    @Override
    public String getDescription()
    {
        return "Seeks to a certain position in the current track.";
    }

    @Override
    protected void ExecuteCommand(String contents, MessageReceivedEvent event)
    {
        if (MessageListener.vp.isPlayingTrack(event.getTextChannel()) == false)
        {
            event.getChannel().sendMessage("There is no track being played!").queue();
            return;
        }

        QueueItem item = MessageListener.vp.getItem(event.getTextChannel());

        long trackLength = item.getLength();
        long curPos = item.getPosition();

        //If not seekable for a reason, then we can't use this command
        if (item.isSeekable() == false)
        {
            event.getChannel().sendMessage("The current track is not seekable.").queue();
            return;
        }

        //Split by colon for time (Ex. 1:57:22)
        String[] args = contents.split(":");

        //Only allow parsing hours, minutes, and seconds
        if (args.length <= 0 || args.length > 3)
        {
            event.getChannel().sendMessage(usageMsg).queue();
            return;
        }

        long finalAmount = 0L;

        //Kimimaru: This offset helps with conversions if the user specified only minutes or seconds
        int timeOffset = timeConstants.length - args.length;

        for (int i = 0; i < args.length; i++)
        {
            int timeInd = i + timeOffset;
            long val = Utilities.TryParse(args[i], -1L);

            //Invalid data. In the first case, we couldn't parse the text into a number
            //In the second, the minute or seconds value was 60 or over
            if (val <= -1L
                || (timeInd > 0 && val >= 60L))
            {
                event.getChannel().sendMessage(usageMsg).queue();
                return;
            }

            finalAmount += val * timeConstants[timeInd];
        }

        //System.out.println("Final: " + finalAmount + " Dur: " + trackLength);

        //Kimimaru: Don't let them seek further than the track length, as that ends the track
        //"skip" is for skipping tracks, so they should use that instead if they want to do so

        //First, we need to account for the difference in the track length and the calculated position to seek to
        //We need to do this because the remaining duration can be under a second (Ex. 170021 rather than 170000 in a 2m 50s track)
        long trackDiff = trackLength - finalAmount;

        //If the difference is less than a second, then we are effectively skipping the track, so prevent this
        if (trackDiff < 1000L)
        {
            event.getChannel().sendMessage("Specified position is at or past the end of the track!").queue();
            return;
        }

        item.setPosition(finalAmount);
        event.getChannel().sendMessage("Track seeked from `" + Utilities.getTimeStringFromMs(curPos) +
                "` to `" + Utilities.getTimeStringFromMs(finalAmount) + "`").queue();
    }
}
