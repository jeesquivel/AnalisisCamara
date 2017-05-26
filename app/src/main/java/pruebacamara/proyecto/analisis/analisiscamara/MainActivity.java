package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */


public class MainActivity extends AppCompatActivity {

    //Variables de códigos para obtener permisos por parte del usuario
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    //Permisos obtenidos
    private boolean WRITE_PERMISSION = false;

    //Variables de códigos para los intents creados
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SELECT_PICTURE = 2;

    //Constantes para archivos
    private static final String ID_PHOTOS = "P3_";
    private static final String SUFFIX_PHOTO = ".jpg";

    //Variables para ejecución
    //Data
    private String currentPhotoPath;
    //UI

    /*--------------------------------------------------*
     *  Ejecución inmediata por acceso a la aplicación  *
     *--------------------------------------------------*/

    /**
     * Se ejecuta cada vez que la aplicación es lanzada:
     * Agrega la UI user interface
     * Inicia variables por búsqueda de ID en los xml
     * Verifica los permisos disponibles para la aplicación
     *
     * @param savedInstanceState Bundle con el savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        initComponents();
    }

    /**
     * Inicia variables por el find en un xml y asigna trabajos
     */
    private void initComponents() {
    }

    /**
     * Verifica permisos necesarios para que la aplicación funcione
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,                                                 // Verifica si es posible escribir en la memoria del teléfono
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {                                             //Se solicita el permiso en caso de no tenerlo
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {                                                                                    //Si el permiso estaba disponible habilita la opción
            WRITE_PERMISSION = true;
        }
    }

    /*--------------------------*
     *  Funciones para botones  *
     *--------------------------*/

    /**
     * Da la opción de elegir una imagen desde la galería prefereida por el usuario
     *
     * @param view Requerido para ligar este método al botón desde el xml
     */
    public void choosePic(View view) {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,                                              //Action pick: lanzalanzar una actividad que muestre una liste de objetos a seleccionar para que el usuario elija uno de ellos
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPictureIntent.setType("image/*");                                                                  //Muestra las imagenes de cualquier extensión
        startActivityForResult(
                Intent.createChooser(pickPictureIntent, "Abrir con"),
                REQUEST_SELECT_PICTURE);
    }

    /**
     * Función que se llama al presionar el botón CÁMARA
     * Crea el intent para solicitar usar una aplicación externa como cámara y crea un File para su
     * posterior almacenamiento.
     *
     * @param view Requerido para poder ligar este método al botón desde el XML
     */
    public void openCamera(View view) {
        if (WRITE_PERMISSION) {                                                                     //Solo si hay permiso
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);                 //Nuevo Intent para capturar una imagen.
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {                   //Se verifica que hayan aplicaciones en el sistema capacez de recibir el intent.
                File photoFile = null;                                                              //Puntero a un File, que luego se intenta crear para manejar la exepción de error.
                try {
                    photoFile = createImageFile();                                                  //Se intenta crear el archivo
                } catch (IOException e) {
                    System.out.println("Error al crear el archivo.");
                }
                if (photoFile != null) {                                                            //Si el archivo fue creado con éxito
                    Uri photoUri = FileProvider.getUriForFile(this,                                 //Se crea un URI con el anterior archivo creado, un URI es una especie de identificador de archivo universal
                            "pruebacamara.proyecto.analisis.analisiscamara.fileprovider",
                            photoFile);                                                             //Requiere la actividad que lo lanza, el PROVIDER del permiso que lo concede y el archivo al asociar el URI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);                  //Se envía al Intent de la captura como una salida extra el URI para que pueda guardar la foto en él.
                    startActivityForResult(
                            Intent.createChooser(takePictureIntent, "Tomar con"),
                            REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    /*--------------------------*
     *  Funciones del programa  *
     *--------------------------*/

    /**
     * Crear un archivo para almacenar la imagen y asegura de que el directorio exista previamente
     *
     * @return referencia a un File donde fue creado el archivo
     * @throws IOException Lanzado en caso de que no se pueda crear el directorio
     */
    private File createImageFile() throws IOException {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);              //Se obtiene una máscara para la fecha en la region de US (soportada en todos los dispositivos)
        String timeStamp = sdfDate.format(new Date());                                              //String final con la fecha
        String imageFileName = ID_PHOTOS + timeStamp;                                               //Nombre de la imagen

        File storageDir =
                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buckets"); //Directorio donde se almacenan las imágenes
        boolean state = checkDir(storageDir);                                                       //Se consulta si el directorio existe

        if (state) {                                                                                //State es false solo en el caso de que se presente un error
            String absolutePath = storageDir + "/" + imageFileName + SUFFIX_PHOTO;                  //Se crea el path absoluto (directorios junto al nombre) para guardar la imagen
            File image = new File(absolutePath);                                                    //Se crea el archivo
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } else {
            currentPhotoPath = null;
            throw new IOException();                                                                //La función que se encuentra sobre esta maneja la excepción.
        }
    }

    /**
     * Verifica si el directorio dado por parámetro exizte.
     *
     * @param fileToCheck Archivo que se desea verificar
     * @return valor booleano, true: existe/fue creado false: error
     */
    private boolean checkDir(File fileToCheck) {
        boolean sucess = true;
        if (!fileToCheck.exists()) {
            sucess = fileToCheck.mkdir();
        }
        return sucess;
    }

    /**
     * Función que agrega al índice un archivo nuevo sin necesidad de escanear toda la memoria
     *
     * @param filePath Absolute path del archivo a agregar
     */
    private void scanFile(String filePath) {
        String[] paths = new String[1];
        paths[0] = filePath;
        MediaScannerConnection.scanFile(
                this, paths, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                }
        );
    }

    /**
     * Dado un Uri, obtiene su path, crea dos archivos un fuente desde el Uri y uno nuevo para
     * copiar los datos a este desde el Uri
     *
     * @param sourceUri Uri del archivo que desea ser copiado a un nuevo archivo
     */
    private void newFileFromUri(Uri sourceUri) {
        try {
            String path = getPath(sourceUri);
            File source = new File(path);
            File destination = createImageFile();
            copyFile(source, destination);
        } catch (IOException e) {
            Log.e("FileError", "Archivo no creado | Error copiando", e);
        }
    }

    /**
     * Dado un Uri determina el path exacto del archivo proveniente del Uri, utiliza la base de datos
     * del media store
     *
     * @param uri Uri fuente del archivo
     * @return Retorna un string con el path del archivo proveniente del Uri
     */
    private String getPath(Uri uri) {

        String path;
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            path = uri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    /**
     * Dado un fuente y un destino copia los datos del archivo, no maneja excepciones la función
     * que llame debe manejar el error
     *
     * @param source      Archivo fuente de los datos a copiar
     * @param destination Archivo destino de los datos a copiar
     * @throws IOException Lanza una excepción en canso de obtener un error al leer archivo o copiarlo
     */
    private void copyFile(File source, File destination) throws IOException {

        FileChannel in = new FileInputStream(source).getChannel();
        FileChannel out = new FileOutputStream(destination).getChannel();

        in.transferTo(0, in.size(), out);
        in.close();
        out.close();

    }

    /*----------------------------------*
     *  Eventos generados por Android   *
     *----------------------------------*/

    /**
     * Switch encargado de recibir los resultados de intents llamados
     *
     * @param requestCode Solicitud pedida al intent
     * @param resultCode  Codígo del resultado obtenido por el intent
     * @param data        Intent que alberga el resultado obtenido
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);                                      //Verifica que la respuesta sea la indicada
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    if (currentPhotoPath != null) {
                        scanFile(currentPhotoPath);
                    }
                }
                break;
            case REQUEST_SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    Uri path = data.getData();
                    newFileFromUri(path);
                }
                break;
        }
    }

    /**
     * Administra si el permiso fue concedido por el usuario
     *
     * @param requestCode  constante creada por el app para identificar cual permiso se otorgo
     * @param permissions  necesario para completar el override
     * @param grantResults aquí vienen los permisos obtenidos, GRANTED OR DENIED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {                                   //Casos constantes genereados por la aplicación
                // If request is cancelled, the result arrays are empty.
                WRITE_PERMISSION =
                        grantResults.length > 0 &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED;               //Se asigna al valor administrador del permiso el resultado del usuario
                break;
            }
        }
    }

}
