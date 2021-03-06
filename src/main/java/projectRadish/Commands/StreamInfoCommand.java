package projectRadish.Commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.time.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import projectRadish.Constants;
import projectRadish.Twitch.*;
import projectRadish.Utilities;

public class StreamInfoCommand extends BaseCommand
{
    @Override
    public String getDescription() {
        return "Displays current status of TPE's twitch stream.";
    }

    @Override
    public boolean canBeUsedViaPM() { return true; }

    private static final int thumbnailWidth = 284;
    private static final int thumbnailHeight = 160;

    private static final String thumbnailWidthStr = Integer.toString(thumbnailWidth);
    private static final String thumbnailHeightStr = Integer.toString(thumbnailHeight);

    private static final Color embedColor = new Color(100, 65, 164);

    private StreamData streamInfo = new StreamData();
    private GameData gameData = new GameData();
    private UserData userData = new UserData();

    private ObjectMapper objMapper = new ObjectMapper();
    private EmbedBuilder embedBuilder = new EmbedBuilder();

    private StringBuilder strBuilder = new StringBuilder(40);

    @Override
    public void Initialize()
    {
        super.Initialize();

        //Map the object to the class
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
    }

    @Override
    public void ExecuteCommand(String contents, MessageReceivedEvent event)
    {
        GetStreamInfo();

        StreamResponse response = null;
        String uptime = "N/A";
        String gameName = null;
        String profileImgURL = null;

        //Not live, as the information returned empty data
        if (streamInfo == null || streamInfo.data == null || streamInfo.data.length == 0)
        {
            event.getChannel().sendMessage("TPE is currently not live").queue();
            return;
        }
        else
        {
            response = streamInfo.data[0];

            gameName = response.game_id;

            try
            {
                //Get the uptime from the difference of the current UTC time and the stream's time (which is in UTC)
                LocalDateTime now = LocalDateTime.now(Constants.UTC_ZoneID);

                //Parse stream time from the string given
                LocalDateTime streamDateTime = Utilities.parseDateTimeFromString(response.started_at);

                //Get the duration between
                Duration duration = Duration.between(now, streamDateTime);

                //Define these constants to convert the seconds to minutes, hours, and days
                final long secondsPerMinute = 60;
                final long secondsPerHour = 60 * 60;
                final long secondsPerDay = secondsPerHour * 24;

                long totalSeconds = Math.abs(duration.getSeconds());

                //Convert
                long days = totalSeconds / secondsPerDay;
                long hours = totalSeconds / secondsPerHour;
                int minutes = (int) ((totalSeconds % secondsPerHour) / secondsPerMinute);
                int seconds = (int) (totalSeconds % secondsPerMinute);

                //Kimimaru: The compiler uses a StringBuilder for string concatenation anyway, so reduce garbage here by reusing one
                strBuilder.setLength(0);

                strBuilder.append(days);
                strBuilder.append(" days,");
                strBuilder.append(hours);
                strBuilder.append(" hrs, ");
                strBuilder.append(minutes);
                strBuilder.append(" min, ");
                strBuilder.append(seconds);
                strBuilder.append("sec");
                uptime = strBuilder.toString();

                //Get game name
                GetGameData(response.game_id);
                if (gameData != null && gameData.data != null && gameData.data.length > 0)
                    gameName = gameData.data[0].name;

                //Get profile image URL
                GetUserData(response.user_id);
                if (userData != null && userData.data != null && userData.data.length > 0)
                    profileImgURL = userData.data[0].profile_image_url;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            //Get our embed
            MessageEmbed embed = GetEmbed(response, uptime, gameName, profileImgURL);

            event.getChannel().sendMessage(embed).queue();
        }
    }

    private MessageEmbed GetEmbed(StreamResponse response, String uptime, String gameName, String profileImgURL)
    {
        //Replace these strings in the thumbnail URL to get an image with the size we want
        String thumbnail = response.thumbnail_url.replace("{width}", thumbnailWidthStr);
        thumbnail = thumbnail.replace("{height}", thumbnailHeightStr);

        //Build the embed
        embedBuilder.clear();
        embedBuilder.setTitle(response.title, "https://twitch.tv/twitchplays_everything");
        embedBuilder.setImage(thumbnail);
        embedBuilder.setThumbnail(profileImgURL);
        embedBuilder.setAuthor("TwitchPlaysEverything", "https://www.twitch.tv/twitchplays_everything/collections", profileImgURL);
        embedBuilder.addField("**GAME**", gameName, false);
        embedBuilder.addField("**VIEWERS**", Integer.toString(response.viewer_count), true);
        embedBuilder.addField("**UPTIME**", uptime, true);

        //Twitch Purple
        embedBuilder.setColor(embedColor);
        return embedBuilder.build();
    }

    public void GetStreamInfo()
    {
        try
        {
            //URL to Twitch stream information
            URL url = new URL(Constants.STREAM_ENDPOINT + Constants.STREAM_NAME);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            //Add the client ID as the header
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Client-ID", Constants.CLIENT_ID);

            //Read information
            BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
            String inputLine = br.readLine();
            br.close();

            objMapper.readerForUpdating(streamInfo).readValue(inputLine);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void GetGameData(String gameID)
    {
        try
        {
            //URL to Twitch game information
            URL url = new URL(Constants.GAME_ENDPOINT + gameID);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            //Add the client ID as the header
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Client-ID", Constants.CLIENT_ID);

            //Read information
            BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
            String inputLine = br.readLine();
            br.close();

            objMapper.readerForUpdating(gameData).readValue(inputLine);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void GetUserData(String userID)
    {
        try
        {
            //URL to Twitch user information
            URL url = new URL(Constants.USER_ENDPOINT + userID);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            //Add the client ID as the header
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Client-ID", Constants.CLIENT_ID);

            //Read information
            BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ));
            String inputLine = br.readLine();
            br.close();

            objMapper.readerForUpdating(userData).readValue(inputLine);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
