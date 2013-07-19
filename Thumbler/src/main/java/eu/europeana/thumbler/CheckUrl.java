package eu.europeana.thumbler;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMessages;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ErrorMsg;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class CheckUrl {
    protected String uri;
    private Integer redirectDepth;
    protected LinkStatus state;
    private String ErrorMsg;
    protected ByteArrayOutputStream orgFileConent = null; // storage for downloaded item, not used when just linkchecking


    public CheckUrl(String uri) {
        this.uri = uri;
        initiateParams(5);
    }

    public CheckUrl(String uri, Integer redirectDepth) {
        this.uri = uri;
        initiateParams(redirectDepth);
    }


    public LinkStatus getState() {
        return state;
    }

    public String getErrorMessage() {
        return ErrorMsg; // might be empty if no specific info was available
    }




    public boolean isResponding() {
        return doConnection(this.redirectDepth, false);
    }
    public boolean isResponding(boolean saveItem) {
        return doConnection(this.redirectDepth, saveItem);
    }
    public boolean isResponding(Integer redirectDepth) {
        return doConnection(redirectDepth, false);
    }



    private boolean doConnection(Integer redirectDepth, boolean saveItem) {
        HttpURLConnection urlConnection;
        URL url;
        BufferedInputStream in;

        if (redirectDepth < 0) {
            return setState(LinkStatus.REDIRECT_DEPTH_EXCEEDED);
        }

        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return setState(LinkStatus.BAD_URL);
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return setState(LinkStatus.FAILED_TO_OPEN_CONNECTION);
        }
        //urlConnection.setRequestMethod("HEAD");
        urlConnection.setConnectTimeout(5000); /* timeout after 5s if can't connect */

        try {
            urlConnection.connect();
        } catch (IOException e) {
            return setState(LinkStatus.FAILED_TO_CONNECT);
        }

        String redirectLink = urlConnection.getHeaderField("Location");
        if (redirectLink != null && !uri.equals(redirectLink)) {
            return isResponding(redirectDepth - 1);
        }

        Integer respCode;
        try {
            respCode = urlConnection.getResponseCode();
        } catch (IOException e) {
            return setState(LinkStatus.NO_RESPONSE_CODE);
        }
        if (respCode != HttpURLConnection.HTTP_OK) {
            return setState(LinkStatus.HTTP_ERROR, respCode.toString());
        }


        if (saveItem) {
            // since the link is open read it now and save the item into orgFile to avoid additional connects
            try {
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException e) {
                return setState(LinkStatus.FAILED_TO_BIND_TO_INPUT_STREAM); // we just checked the link
            }
            orgFileConent = new ByteArrayOutputStream();
            int c;
            try {
                while ((c = in.read()) != -1) {
                    orgFileConent.write(c);
                }
                orgFileConent.close();
            } catch (IOException e) {
                orgFileConent = null;
                return setState(LinkStatus.FAILED_TO_READ_ORIG);
            }
        }
        urlConnection.disconnect();
        return setState(LinkStatus.LINK_OK); // we just checked the link
    }






    private void initiateParams(Integer redirectDepth) {
        this.redirectDepth = redirectDepth;
        setState(LinkStatus.UNKNOWN);
    }

    protected boolean setState(LinkStatus state){  // param lazy shorthand
        return setState(state,"");
    }
    protected boolean setState(LinkStatus state, String msg){
        boolean b = (state == LinkStatus.CACHE_OK || state == LinkStatus.LINK_OK);
        this.state = state;
        ErrorMsg = msg;
        return b;
    }
}
