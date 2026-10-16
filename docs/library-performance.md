# Library loading

New PDF catalogs should open books through `HadithPdfStore.open`, save them through
`HadithPdfStore.download`, and use the shared `FastNet` client for network requests.
Do not download an entire PDF before displaying an online book or parse its full
bookmark tree before showing the first page.

On Android 8 and later, a seekable proxy descriptor serves PDF reads using 1 MiB
HTTP ranges. Complete blocks are cached by source URL, file size and ETag (or
Last-Modified). Servers without validators get a separate cache per session.
Changed source versions and malformed/incomplete ranges are rejected. Old range
cache directories are trimmed when a book opens, with a 256 MiB budget; the
current book can exceed that budget. Closing the reader releases its native
renderer, descriptor, callback thread and bitmap cache.

Android 7 and sources without byte ranges fall back to the existing parallel,
validated PDF download. Explicit downloads remain complete files for offline
reading. Streaming partial blocks do not count as an offline download. Opening
the chapter index of an online book fetches the full PDF to extract bookmarks.

World Library search/volume responses are reused for 60 seconds (up to 32
responses). Canceled searches cancel their HTTP calls. Daily removal checks still
read the source directly. PDF validation and parsing run off the UI thread;
normal pages render at screen resolution, with higher resolution requested on
zoom.

Run `:app:testDebugUnitTest --tests com.example.core.net.RangeFileTest --tests
com.example.hadith.WorldLibraryTest` to check exact cross-block/EOF reads, cache
reuse, source changes, range fallback and catalog parsing. Native proxy descriptor
performance still needs measurement on an Android device and the user's network;
throughput depends on the source server, PDF structure and connection.
