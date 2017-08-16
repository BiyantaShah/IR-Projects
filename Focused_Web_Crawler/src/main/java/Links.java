import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Biyanta on 14/06/17.
 */
public class Links {

    private String rawUrl;
    private String canonicalizedUrl;
    private String authority;
    private int depth;
    private int relevance;
    private Long waitTime;



    public Links(String url, String canonicalized) {
        super();
        this.rawUrl = url;
        this.canonicalizedUrl = canonicalized;
        try {

            authority = (new URI(url)).getAuthority();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Links(String url) {
        super();
        this.rawUrl = url;
        WebCrawl wc = new WebCrawl();
        try {
            this.canonicalizedUrl = wc.canonicalizedURL(url);
            authority = (new URI(url)).getAuthority();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    public String getCanonicalizedUrl() {
        return canonicalizedUrl;
    }

    public void setCanonicalizedurl(String canonicalizedUrl) {
        this.canonicalizedUrl = canonicalizedUrl;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }


    public int isRelevant() {
        return relevance;
    }

    public void setRelevance(int relevant) {
        relevance = relevant;
    }

    public Long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Long waitTime) {
        this.waitTime = waitTime;
    }
}
