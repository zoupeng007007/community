package com.zoupeng.community.util;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ImageUtil {
    public static String getImageUrl() {
        String url = "https://api.btstu.cn/sjtx/api.php?lx=c1";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = null;
        String absUrl = null;
        try {
            response = httpclient.execute(httpget, context);
            HttpHost target = context.getTargetHost();
            List<URI> redirectLocations = context.getRedirectLocations();
            URI location = URIUtils.resolve(httpget.getURI(), target, redirectLocations);
            absUrl = location.toASCIIString();
        }catch(IOException e){
            e.printStackTrace();
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }finally {
            try {
                httpclient.close();
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return absUrl;
    }

}
