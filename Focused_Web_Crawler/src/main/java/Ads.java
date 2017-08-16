/**
 * Created by Biyanta on 14/06/17.
 */
public class Ads {

    String[] ads = {"facebook",
            "instagram",
            "twitter",
            "shop",
            "wikimedia",
            "ads",
            "foursquare",
            "mediawiki",
            "aenetworks",
            "contact_us",
            "license",
            "plus.google.com",
            "fyi.tv",
            "email",
            "support",
            "emails",
            "wiki/special:",
            "portal:featured_content",
            "portal:current_events",
            "special:random",
            "help:contents",
            "wikipedia:about",
            "wikipedia:community_portal",
            "special:recentchanges",
            "wikipedia:file_upload_wizard",
            "special",
            "wikipedia:general_disclaimer",
            "en.m.",
            "action=edit",
            "Help:Category",
            "international_standard_book_number",
            ".pdf",
            "file:",
            "youtube",
            "\\.tv",
            "mylifetime",
            "intellectualproperty",
            "integrated_authority",
            "citation",
            ".jpg",
            ".php",
            ".aspx",
            ".asp"};


    public boolean isAdUrl(final String url){
        for (String ad : ads) {
            if(url.toLowerCase().contains(ad)){
                return true;
            }
        }
        return false;
    }
}
