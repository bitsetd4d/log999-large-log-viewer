package com.log999.deprecated.logchunk.internal;

import com.log999.deprecated.chunkloader.LoadableLogChunk;
import com.log999.deprecated.chunkloader.LogChunkLoader;
import com.log999.deprecated.chunkloader.LogChunks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LogChunkLoaderImpl implements LogChunkLoader {

    private static Logger logger = LoggerFactory.getLogger(LogChunksImpl.class);

    private static final int DEFAULT_MAX_BYTES_PER_CHUNK = 1024 * 1024 * 50;

    private final int maxBytesPerChunk;

    private int chunkCount;
    private List<LoadableLogChunk> chunks = new ArrayList<>();
    private LoadableLogChunk currentChunk;

    public LogChunkLoaderImpl() {
        this(DEFAULT_MAX_BYTES_PER_CHUNK);
    }

    public LogChunkLoaderImpl(int maxBytesPerChunk) {
        this.maxBytesPerChunk = maxBytesPerChunk;
        currentChunk = new LogChunkImpl(chunkCount++, maxBytesPerChunk);
        chunks.add(currentChunk);
    }

    public void acceptLine(String line) {
        boolean ok = currentChunk.acceptLineIfRoom(line);
        if (!ok) {
            createNewLoadableLogChunk();
            if (!currentChunk.acceptLineIfRoom(line)) {
                logger.error("Could not insert line into fresh chunk = {}", line);
                throw new RuntimeException("Could not insert into fresh chunk");
            }
            logger.info("Started new chunk {}", chunks.size());
        }
    }

    private void createNewLoadableLogChunk() {
        LoadableLogChunk chunk = new LogChunkImpl(chunkCount++, maxBytesPerChunk);
        currentChunk.linkToNextChunk(chunk);
        currentChunk.finishChunk();
        currentChunk = chunk;
        chunks.add(chunk);
    }

    public void fullyLoaded() {
        currentChunk.finishChunk();
        long idx = 0;
        for (LoadableLogChunk c : chunks) {
            c.setLogLineStartIndex(idx);
            idx += c.getLines().size();
        }
    }

    @Override
    public LogChunks getLogChunks() {
        return new LogChunksImpl(chunks);
    }
}
