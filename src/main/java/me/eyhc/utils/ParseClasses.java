package me.eyhc.utils;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import com.google.gson.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class ParseClasses {
    public final Logger LOGGER = LogManager.getLogger();

    public ParseClasses(String netid, String password) throws IOException {
        String content = getUW(netid, password,
                "https://my.uw.edu/api/v1/visual_schedule/current").body().text();
        parseNExport(content, netid);
    }

    public ParseClasses() {
    }

    public void parseNExport(String content, String fileName) throws IOException {
        Ical calendar = new Ical(fileName);
        JsonObject contentJson = new JsonParser().parse(content).getAsJsonObject();
        JsonArray period = contentJson.get("periods").getAsJsonArray();

        for (JsonElement i : period) {
            JsonObject classes = i.getAsJsonObject();
            if (!classes.get("id").getAsString().contains("finals")) {
                JsonArray sections = classes.get("sections").getAsJsonArray();
                for (JsonElement j : sections) {
                    LOGGER.info("getting classes information");
                    JsonObject classInfo = j.getAsJsonObject();
                    String className = classInfo.get("curriculum_abbr")
                            .getAsString() + " " + classInfo.get("course_number")
                            .getAsString() + classInfo.get("section_id")
                            .getAsString();
                    String startDate = classInfo.get("start_date").getAsString().replaceAll("-","");
                    String endingDate = classInfo.get("end_date").getAsString().replaceAll("-","");
                    String sectionType = classInfo.get("section_type").getAsString();
                    JsonObject meetDetails = classInfo.get("meetings").getAsJsonArray().get(0).getAsJsonObject();

                    if (meetDetails.get("no_meeting").getAsBoolean()) {
                        continue;
                    }

                    String startTime = meetDetails.get("start_time").getAsString().replaceAll(":","");
                    String endTime = meetDetails.get("end_time").getAsString().replaceAll(":","");
                    String building = meetDetails.get("building").getAsString();
                    String room = meetDetails.get("room").getAsString();
                    Set<Map.Entry<String, JsonElement>> meetDays = meetDetails.get("meeting_days").getAsJsonObject()
                            .entrySet();
                    List<String> days = new ArrayList<>();
                    for (Map.Entry<String, JsonElement> dContents : meetDays){
                        if (dContents.getValue() instanceof JsonNull){
                            continue;
                        }
                        if (dContents.getValue().getAsBoolean()){
                            days.add(dContents.getKey().substring(0, 2).toUpperCase(Locale.ROOT));
                        }
                    }
                    float[] loc = new float[]{0f, 0f};
                    if (!meetDetails.get("building_tbd").getAsBoolean()) {
                        LOGGER.info("Getting building location");
                        loc = locations(building);
                    }
                    LOGGER.info("writing class information on calendar file");
                    calendar.event(className + " " + sectionType,
                            startDate,
                            endingDate,
                            startTime,
                            endTime,
                            days.toString().replace("[", "").replace("]", ""),
                            loc[0],
                            loc[1],
                            building + " " + room);
                }
            }
        }
        calendar.close();
    }

    public Document getUW(String username, String password, String url) throws IOException {
        Response re = Jsoup.connect(url)
                .method(Method.GET)
                .execute();
        Document contents = re.parse();
        if (contents.getElementsByTag("h1").eachText().contains("UW NetID sign-in")) {
            LOGGER.info("logging in...");
            String loginURL = contents.getElementsByTag("form").attr("action");
            Connection.Response m = Jsoup.connect("https://idp.u.washington.edu" + loginURL)
                    .data("j_username", username)
                    .data("j_password", password)
                    .data("_eventId_proceed", "Sign in")
                    .cookies(re.cookies())
                    .execute();
            Document s = m.parse();
            url = Objects.requireNonNull(s.getElementsByTag("form")).attr("action");
            LOGGER.info(s.getElementsByAttributeValue("id", "firstWait").text());
            String relayState = s.getElementsByAttributeValue("name", "RelayState").attr("value");
            String samlResponse = s.getElementsByAttributeValue("name", "SAMLResponse").attr("value");
            if (s.getElementsByAttributeValue("id", "uwsignin").text().length() > 1) {
                throw new InputMismatchException(s.getElementsByAttributeValue("id", "uwsignin").text());
            }
            Response pageR = Jsoup.connect(url)
                    .data("RelayState", relayState)
                    .data("SAMLResponse", samlResponse)
                    .cookies(m.cookies())
                    .ignoreContentType(true)
                    .method(Method.POST)
                    .execute();
            contents = pageR.parse();
            LOGGER.debug(contents.getElementsByTag("h1").text());
        }
        return contents;
    }


    public float[] locations (String building) throws IOException {
        // https://stackoverflow.com/a/43330690
        URLConnection c = new URL("https://www.washington.edu/maps/?json=campusmap.get_locations")
                .openConnection();
        Scanner s = new Scanner(c.getInputStream());

        JsonObject m = new JsonParser().parse(s.useDelimiter("\\A").next()).getAsJsonObject();
        JsonArray locations = m.get("posts").getAsJsonArray();
        float lng = 0f;
        float lat = 0f;
        for (JsonElement i : locations) {
            JsonObject buildingInfo = i.getAsJsonObject();
            if (buildingInfo.get("code").getAsString().equalsIgnoreCase(building)) {
                lng = buildingInfo.get("lng").getAsFloat();
                lat = buildingInfo.get("lat").getAsFloat();
            }
        }
        return new float[]{lat, lng};
    }
}
