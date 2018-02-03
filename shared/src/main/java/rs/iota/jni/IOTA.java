package rs.iota.jni;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class IOTA {
    private enum LibraryState {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    private static AtomicReference<LibraryState> libraryLoaded
            = new AtomicReference<>(LibraryState.NOT_LOADED);

    static {
        IOTA.loadLibrary();
    }

    /**
     * Loads the necessary library files.
     * Calling this method twice will have no effect.
     * By default the method extracts the shared library for loading at
     * java.io.tmpdir, however, you can override this temporary location by
     * setting the environment variable IOTA_SHAREDLIB_DIR.
     */
    public static void loadLibrary() {
        if (libraryLoaded.get() == LibraryState.LOADED) {
            return;
        }

        if (libraryLoaded.compareAndSet(LibraryState.NOT_LOADED,
                LibraryState.LOADING)) {
            final String tmpDir = System.getenv("IOTA_SHAREDLIB_DIR");
            try {
                NativeLibraryLoader.getInstance().loadLibrary(tmpDir);
            } catch (IOException e) {
                libraryLoaded.set(LibraryState.NOT_LOADED);
                throw new RuntimeException("Unable to load the IOTA shared library"
                        + e);
            }

            libraryLoaded.set(LibraryState.LOADED);
            return;
        }

        while (libraryLoaded.get() == LibraryState.LOADING) {
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                //ignore
            }
        }
    }
}
