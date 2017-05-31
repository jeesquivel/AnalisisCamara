package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

final class Scanner {

    /**
     * Función que agrega al índice un archivo nuevo sin necesidad de escanear toda la memoria
     *
     * @param filePath Absolute path del archivo a agregar
     */
    static void scanFile(Context context, String filePath) {
        String[] paths = new String[1];
        paths[0] = filePath;
        MediaScannerConnection.scanFile(
                context, paths, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                }
        );
    }

}
