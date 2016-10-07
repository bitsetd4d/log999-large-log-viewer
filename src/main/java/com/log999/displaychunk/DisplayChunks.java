package com.log999.displaychunk;

public interface DisplayChunks {
}
        /*
          Change this so that LogChunks are just the physical log lines and have no
          display aspect.
          The Gui should talk to something that uses LogChunks and maps that to
          display row things. A new layer that doesn't need the LogChunks to support them.
          Then the logchunks can just deal with caching/searching etc.
          The GUI layer can deal with mapping multiple display rows to 1 real row.
         */
