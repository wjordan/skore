/*
    Copyright (c) 2008, Interactive Pulp, LLC
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of Interactive Pulp, LLC nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/

package net.tetromi.pulpcore.net;

import net.tetromi.log.LOG;
import util.ByteArray;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
    The Upload class represents name/value pairs for sending to an HTTP server
    via the POST method (multipart form). Values can be either plain text or files.
*/
public class Upload implements Runnable {

    private static final int BUFFER_SIZE = 1024;
    private static final String NEW_LINE = "\r\n";

    private String formBoundary = "upload" + System.currentTimeMillis();
    private Properties requestFields = new Properties();
    private List<byte[]> fields = new ArrayList<byte[]>();
    private URL url;
    private String response;
    private Map responseFields;
    private boolean completed;
    private IOException uncaughtException;
    private int responseCode;

    /**
        Creates a new Upload object.
        @param url the URL to send the POST to.
    */
    public Upload(URL url) {
        this.url = url;
    }

    public void addRequestFields(Properties props) {
        requestFields.putAll(props);
    }

    /**
        Adds plaintext form fields to this form.
    */
    public void addFields(Map fields) {
        for (Object o : fields.keySet()) {
            String name = o.toString();
            addField(name, fields.get(name).toString());
        }
    }

    /**
        Add a plain text form field to this form.
    */
    public void addField(String name, String value) {

        String field = "--" + formBoundary + NEW_LINE +
            "Content-Disposition: form-data; " +
            "name=\"" + name + "\"" + NEW_LINE + NEW_LINE +
            value + NEW_LINE;

        fields.add(getBytes(field));
    }

    /**
        Add a data file to this form.
    */
    public void addFile(String name, String fileName, String mimeType, byte[] fileData) {

        String header = "--" + formBoundary + NEW_LINE +
            "Content-Disposition: form-data; " +
            "name=\"" + name + "\"; " +
            "filename=\"" + fileName + "\"" + NEW_LINE +
            "Content-Type: " + mimeType + NEW_LINE +
            "Content-Transfer-Encoding: binary" + NEW_LINE + NEW_LINE;

        ByteArray out = new ByteArray(header.length() + fileData.length + NEW_LINE.length());
        out.write(getBytes(header));
        out.write(fileData);
        // is this newline needed?
        out.write(getBytes(NEW_LINE));

        fields.add(out.getData());
    }

    private byte[] getBytes(String field) {
        try {
            return field.getBytes("ISO-8859-1");
        }
        catch (UnsupportedEncodingException ex) {
            return field.getBytes();
        }
    }

    public void run() {
        try {
            send();
        }
        catch (IOException ex) {
            LOG.debug("Upload.sendNow()", ex);
            response = null;
            completed = true;
            uncaughtException = ex;
        }
    }

    /**
        Write the form to an URL via the POST method.
        The form is sent using the multipart/form-data encoding.
        The response, if any, can be retrieved using getResponse().
        This method makes the request asynchronously (returns immediately).
    */
    public void start() {
        completed = false;
        new Thread(this, "File-Upload").start();
    }

    /**
        Write the form to an URL via the POST method.
        The form is sent using the multipart/form-data encoding.
        The response, if any, can be retrieved using getResponse().
        @param asynchronous true for asynchronous operation
        (the call returns immediately) or false for synchronous operation (the call
        blocks until the method is complete or an error occurs).
    */
    public void start(boolean asynchronous) {
        if (asynchronous) {
            start();
        }
        else {
            run();
        }
    }

    /**
        Write the form to an URL via the POST method.
        The form is sent using the multipart/form-data encoding.
        The response, if any, can be retrieved using getResponse().
        This method blocks until the call is complete or an error occurs.
        @deprecated Use send(false);
        @throws IOException if an error occurs.
    */
    public void sendNow() throws IOException {
        run();
        if (uncaughtException != null) {
            throw uncaughtException;
        }
    }

    private void send() throws IOException {
        completed = false;
        URLConnection connection = url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        addRequestProperties(connection);
        connection.setRequestProperty("Content-Type",
            "multipart/form-data; boundary=" + formBoundary);

        int length = 0;

        // Send form data
        OutputStream out = connection.getOutputStream();
        for (byte[] field : fields) {
            out.write(field);
            length += field.length;
        }

        final byte[] b = getBytes("--" + formBoundary + "--");
        length += b.length;

        out.write(b);
        out.flush();
        out.close();

        // Get response data.
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer responseBuffer = new StringBuffer();

        char[] buffer = new char[BUFFER_SIZE];
        while (true) {
            int charsRead = in.read(buffer);
            if (charsRead == -1) {
                break;
            }
            responseBuffer.append(buffer, 0, charsRead);
        }

        in.close();
        this.responseCode = ((HttpURLConnection)connection).getResponseCode();
        this.response = responseBuffer.toString();
        this.responseFields = connection.getHeaderFields();
        this.completed = true;
    }

    /** Adds the request Properties to the connection. */
    private void addRequestProperties(URLConnection connection) {
        for(Object o : requestFields.entrySet()) {
            final Map.Entry entry = (Map.Entry) o;
            connection.addRequestProperty((String)entry.getKey(),(String)entry.getValue());
        }
    }

    public String getResponse() {
        return response;
    }

    public Map getResponseFields() {
        return responseFields;
    }

    /**
        @return true if a call to start() has completed.
    */
    public boolean isCompleted() {
        return completed;
    }

    /**
        @return the uncaught exception during upload, if any.
    */
    public IOException getUncaughtException() {
        return uncaughtException;
    }

    /**
     * @return the response code returned from the connection, eg. 200 for "OK".
     */
    public int getResponseCode() {
        return responseCode;
    }
}
