package com.log999.logfile.loglineprovider;

import com.log999.loading.api.StreamingLogLineProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamLogLineProvider implements StreamingLogLineProvider {

    private BufferedReader reader;
    private String readAhead;

    public InputStreamLogLineProvider(InputStream stream) {
        reader = new BufferedReader(new InputStreamReader(stream));
    }

    @Override
    public boolean hasMoreLines() {
        if (reader == null) return false;
        if (readAhead != null) return true;
        readAhead = readLineFromStream();
        return readAhead != null;
    }

    @Override
    public String readLine() {
        if (reader == null) return null;
        if (readAhead != null) {
            String line = readAhead;
            readAhead = null;
            return line;
        }
        return readLineFromStream();
    }

    private String readLineFromStream() {
        try {
            String line = reader.readLine();
            if (line == null) {
                reader.close();
                reader = null;
            }
            return line;
        } catch (IOException e) {
            throw new RuntimeException("IO Error reading input stream");
        }
    }

}
