import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Biyanta on 14/06/17.
 */
public class RobotCheck {

    static Map<String, Boolean> visited = new HashMap<String, Boolean>();

    public boolean isRobotPermitted(String url) {

        URL tempUrl = null;

        try {
            tempUrl = new URL(url.trim());

            if(visited.containsKey(tempUrl.getAuthority())){

                return visited.get(tempUrl.getAuthority());
            }

            StringBuilder sb = new StringBuilder();
            sb.append(tempUrl.getProtocol()+"://");
            sb.append(tempUrl.getAuthority()+"/");
            sb.append("robots.txt");


            Document doc = Jsoup.connect(sb.toString()).get();
//            if (doc.body().text().contains("Crawl-delay")) {
//                String str = doc.body().toString().split("Crawl-delay:")[1].trim();
//
//                String str1 = str.split(" ")[0];
//
//                Long delay = Long.parseLong(str1) * 1000;
//                WebCrawl.sleepTime.put(tempUrl.getAuthority(), delay);
//
//            }
            if(doc.body().text().contains("User-agent: * Disallow: /")) {

                visited.put(tempUrl.getAuthority(), false);
                return false;
            }

        } catch (Exception e) {
            if(tempUrl!= null){
                visited.put(tempUrl.getAuthority(), true);
            }
            return true;
        }

        visited.put(tempUrl.getAuthority(), true);

        return true;
    }
}
