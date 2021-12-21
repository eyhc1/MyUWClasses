package me.eyhc.utils;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import com.google.gson.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class ParseClasses {

    public ParseClasses(String netid, String password) throws IOException {
        String content = getUW(netid, password,
                "https://my.uw.edu/api/v1/visual_schedule/current").body().text();
        parseNExport(content, "CurrentSchedule_" + netid);
    }

    public ParseClasses() {
    }

    public void parseNExport(String content, String fileName) throws IOException {
        Ical calendar = new Ical(fileName);
        JsonObject contentJson = new JsonParser().parse(content).getAsJsonObject();
        JsonArray period = contentJson.get("periods").getAsJsonArray();
        JsonObject classes = period.get(period.size() - 1).getAsJsonObject();  // Probably safe to assume

        JsonArray sections = classes.get("sections").getAsJsonArray();
        for (JsonElement j : sections) {
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
                loc = locations(building);
            }
            calendar.event(className + " " + sectionType,
                    startDate,
                    endingDate,
                    startTime,
                    endTime,
                    days.toString().replace("[", "").replace("]", ""),
                    loc[0],
                    loc[1],
                    "<a href=\"http://www.washington.edu/students/maps/map.cgi?" + building +"\">" + building + "</a>   " + room);
            JsonObject finals = classInfo.get("final_exam").getAsJsonObject();
            String confirmed = "";
            if (!finals.get("is_confirmed").getAsBoolean()) {
                confirmed = "Note: The date, time, and location is not yet confirmed. Please verify with instructor";
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
            String loginURL = contents.getElementsByTag("form").attr("action");
            Response m = Jsoup.connect("https://idp.u.washington.edu" + loginURL)
                    .data("j_username", username)
                    .data("j_password", password)
                    .data("_eventId_proceed", "Sign in")
                    .cookies(re.cookies())
                    .execute();
            Document s = m.parse();
            System.out.println(s.getElementsByAttributeValue("id", "firstWait").text());

            Element dFrame = s.getElementById("duo_iframe");
            if (dFrame != null) {
                String sigRequest = dFrame.attr("data-sig-request");
                String postAction = dFrame.attr("data-post-action");
                String duoHost = "https://" + s.getElementById("duo_iframe").attr("data-host");
                String duoDirect = duoHost + "/frame/web/v1/auth?tx=" + sigRequest.split(":")[0] + "&parent=" + postAction + "&v=2.6";
                Response d = Jsoup.connect(duoDirect).cookies(m.cookies()).method(Method.GET).execute();
                s = d.parse();

                d = Jsoup.connect(duoDirect)
                        .data("tx", s.getElementsByAttributeValue("name", "tx").attr("value"))
                        .data("parent", s.getElementsByAttributeValue("name", "parent").attr("value"))
                        .method(Method.POST)
                        .execute();
                s = d.parse();
                Elements device = s.getElementsByTag("option");
                String phone = device.eachText().get(0);

                System.out.println("A 2FA push has been sent to your default device " + phone + ". Please check your device to allow it");
                String sid = s.getElementsByAttributeValue("name", "sid").attr("value");
                d = Jsoup.connect(duoHost + "/frame/prompt")
                        .data("sid", sid)
                        .data("device", device.val())
                        .data("factor", s.getElementsByAttributeValue("name", "factor").attr("value"))
                        .data("out_of_date", s.getElementsByAttributeValue("name", "out_of_date").attr("value"))
                        .data("days_out_of_date", s.getElementsByAttributeValue("name", "days_out_of_date").attr("value"))
                        .data("days_to_block", s.getElementsByAttributeValue("name", "days_to_block").attr("value"))
                        .ignoreContentType(true)
                        .method(Method.POST)
                        .execute();
                s = d.parse();
                String txid = new JsonParser().parse(s.body().text()).getAsJsonObject().get("response").getAsJsonObject().get("txid").getAsString();
                JOptionPane.showMessageDialog(null,
                        "A 2FA push has been sent to your default device " + phone + "\nPress OK to continue",
                        "Check your " + phone + " for 2FA",
                        JOptionPane.INFORMATION_MESSAGE);
                d = Jsoup.connect(duoHost + "/frame/status")
                        .data("sid", sid)
                        .data("txid", txid)
                        .ignoreContentType(true)
                        .method(Method.POST)
                        .execute();
                d = Jsoup.connect(duoHost + "/frame/status")
                        .data("sid", sid)
                        .data("txid", txid)
                        .ignoreContentType(true)
                        .method(Method.POST)
                        .execute();
                s = d.parse();
                JsonObject pushData = new JsonParser().parse(s.body().text()).getAsJsonObject().get("response").getAsJsonObject();
                System.out.println(pushData.get("status").getAsString());
                d = Jsoup.connect(duoHost + pushData.get("result_url").getAsString())
                        .data("sid", sid)
                        .ignoreContentType(true)
                        .method(Method.POST)
                        .execute();
                s = d.parse();
                pushData = new JsonParser().parse(s.body().text()).getAsJsonObject().get("response").getAsJsonObject();
                Response r = Jsoup.connect("https://idp.u.washington.edu" + pushData.get("parent").getAsString())
                        .data("_eventId", "proceed")
                        .data("sig_response", pushData.get("cookie").getAsString() + ":" + sigRequest.split(":")[1])
                        .cookies(re.cookies())
                        .ignoreContentType(true)
                        .method(Method.POST)
                        .execute();
                s = r.parse();
            }

            String relayState = s.getElementsByAttributeValue("name", "RelayState").attr("value");
            String samlResponse = s.getElementsByAttributeValue("name", "SAMLResponse").attr("value");
            url = Objects.requireNonNull(s.getElementsByAttributeValue("method", "post")).attr("action");
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
