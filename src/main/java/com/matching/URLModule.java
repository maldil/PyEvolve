package com.matching;

import com.utils.Assertions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Objects;

public class URLModule {
    private final URL url;
    public URLModule(String url) {
        this.url = getURL(url);
    }

    public URLModule(URL url) {
        this.url = url;
    }

    public InputStream getInputStream(String fileName) {
        try {
            return getURL(fileName).openConnection().getInputStream();
        } catch (IOException e) {
            assert false : "cannot find stream for " + url;
            return null;
        }
    }
    public String getName() {
        try {
            URLConnection con = url.openConnection();
            if (con instanceof JarURLConnection) return ((JarURLConnection) con).getEntryName();
            else return filePathFromURL(url);
        } catch (IOException e) {
            Assertions.UNREACHABLE();
            return null;
        }
    }

    public String filePathFromURL(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url is null");
        }
        try {
            URI uri = new File(URLDecoder.decode(url.getPath(), "UTF-8")).toURI();
            return uri.getPath();
        } catch (UnsupportedEncodingException e) {
            // this really shouldn't happen
            Assertions.UNREACHABLE();
            return null;
        }
    }

    protected URL getURL(String name)  {
        try {
            File f = new File(name);
            if (f.exists()) {
                return f.toURI().toURL();
            } else {
                return  new URL(name);
            }
        } catch (IOException e) {
            return getClass().getClassLoader().getResource(name);
        }
    }

}
