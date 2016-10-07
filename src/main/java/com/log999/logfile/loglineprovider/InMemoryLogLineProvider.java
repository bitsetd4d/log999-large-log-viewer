package com.log999.logfile.loglineprovider;

import com.log999.logfile.api.LogLineProvider;
import com.log999.logfile.api.StreamingLogLineProvider;

import java.util.ArrayList;
import java.util.List;

public class InMemoryLogLineProvider implements LogLineProvider {

    private final List<String> lines = new ArrayList<>();;

    public InMemoryLogLineProvider(StreamingLogLineProvider streamingProvider) {
        loadFromStreaming(streamingProvider);
    }

    private void loadFromStreaming(StreamingLogLineProvider streamingProvider) {
        String line;
        while ((line = streamingProvider.readLine()) != null) {
            lines.add(line);
        }
    }

    @Override
    public String getLine(int line) {
        if (line < 0) throw new IllegalArgumentException("line cannot be less than 0");
        if (line >= lines.size()) return null;
        return lines.get(line);
    }
}
