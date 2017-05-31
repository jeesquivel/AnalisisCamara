package pruebacamara.proyecto.analisis.analisiscamara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */

public class MainActivity extends AppCompatActivity {

    //Variables de códigos para obtener permisos por parte del usuario
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;

    //Variables de códigos para los intents creados
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SELECT_PICTURE = 2;

    //Constantes para archivos
    private static final String ID_PHOTOS = "P3_";
    private static final String SUFFIX_PHOTO = ".jpg";

    //Permisos obtenidos
    private boolean WRITE_PERMISSION = false;
    private boolean READ_PERMISSION = false;
    private boolean CAMERA_PERMISSION = false;

    //Variables para ejecución
    //Constantes
    private static final int SIZE_HASH_PIXELS = 16;
    //Data
    private LSHP hashForPixels;
    private String currentPhotoPath;
    //UI
    private Spinner sppinerGroupType;

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
        initVariables();
        createDirBuckets();
    }

    /**
     * Verifica permisos necesarios para que la aplicación funcione
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,                                                 //Verifica si es posible escribir en la memoria del teléfono
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {                                             //Se solicita el permiso en caso de no tenerlo
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {                                                                                    //Si el permiso estaba disponible habilita la opción
            WRITE_PERMISSION = true;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            READ_PERMISSION = true;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            CAMERA_PERMISSION = true;
        }
    }

    /**
     * Inicia variables por el find en un xml y asigna trabajos
     */
    private void initComponents() {
        initSpinnerGroupType();
    }

    /**
     * Inicia variables
     */
    private void initVariables() {
        hashForPixels = new LSHP(getApplicationContext(), SIZE_HASH_PIXELS);
    }

    /**
     * Se encarga de iniciar el sppiner que muestra los métodos de agrupamiento de imágenes
     */
    private void initSpinnerGroupType() {
        sppinerGroupType = (Spinner) findViewById(R.id.sppiner_group_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.group_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sppinerGroupType.setAdapter(adapter);
        configSpinnerGroupType();
    }

    /*----------------------*
     *  Funciones para UI   *
     *----------------------*/

    /**
     * Da la opción de elegir una imagen desde la galería prefereida por el usuario
     *
     * @param view Requerido para ligar este método al botón desde el xml
     */
    public void choosePic(View view) {
        if (WRITE_PERMISSION && READ_PERMISSION) {
            Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,                                   //Action pick: lanzalanzar una actividad que muestre una liste de objetos a seleccionar para que el usuario elija uno de ellos
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureIntent.setType("image/*");                                                       //Muestra las imagenes de cualquier extensión
            startActivityForResult(
                    Intent.createChooser(pickPictureIntent, "Abrir con"),
                    REQUEST_SELECT_PICTURE);
        }
    }

    /**
     * Función que se llama al presionar el botón CÁMARA
     * Crea el intent para solicitar usar una aplicación externa como cámara y crea un File para su
     * posterior almacenamiento.
     *
     * @param view Requerido para poder ligar este método al botón desde el XML
     */
    public void openCamera(View view) {
        if (WRITE_PERMISSION && READ_PERMISSION && CAMERA_PERMISSION) {                                                                     //Solo si hay permiso
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

    /**
     * Coloca el listener al sppiner con los métodos de agrupamiento de imágenes, el listener se
     * encarga de reaccionar cuando el usuario cambia el elemento seleccioando
     */
    private void configSpinnerGroupType() {
        sppinerGroupType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),
                        parent.getItemAtPosition(position).toString(),
                        Toast.LENGTH_SHORT).show();
                try {
                    leer();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            }
        });
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
        String absolutePath = storageDir + "/" + imageFileName + SUFFIX_PHOTO;                      //Se crea el path absoluto (directorios junto al nombre) para guardar la imagen
        File image = new File(absolutePath);                                                        //Se crea el archivo
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Verifica si el directorio de trabajo de la aplicación se encuentra disponible
     *
     * @return valor booleano, true: existe/fue creado false: error
     */
    private boolean createDirBuckets() {
        File dir = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buckets");
        return dir.exists() || dir.mkdir();
    }

    /**
     * Dado un Uri, obtiene su path, crea dos archivos un fuente desde el Uri y uno nuevo para
     * copiar los datos a este desde el Uri
     *
     * @param sourceUri Uri del archivo que desea ser copiado a un nuevo archivo
     */
    private void newFileFromUri(Uri sourceUri) {
        String path = getPath(sourceUri);
        try {
            File fuente = new File(path);
            File destino = createImageFile();

            copyFile(fuente, destino);

        } catch (IOException e) {
            e.printStackTrace();
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
                        Scanner.scanFile(this, currentPhotoPath);
                        hashForPixels.processImage(currentPhotoPath);
                    }
                }
                break;
            case REQUEST_SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    Uri path = data.getData();
                    newFileFromUri(path);
                    if (currentPhotoPath != null) {
                        Scanner.scanFile(this, currentPhotoPath);
                        hashForPixels.processImage(currentPhotoPath);
                    }
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
                WRITE_PERMISSION =
                        grantResults.length > 0 &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED;               //Se asigna al valor administrador del permiso el resultado del usuario
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                READ_PERMISSION =
                        grantResults.length > 0 &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                CAMERA_PERMISSION =
                        grantResults.length > 0 &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }
        }
    }


    public void leer() throws IOException {
        ListView ListView = (ListView) findViewById(R.id.listview);
        List<String> listado = new ArrayList<>();
        String linea;
        InputStream archivo = this.getResources().openRawResource(R.raw.hashes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(archivo));
        if (archivo != null) {
            while ((linea = reader.readLine()) != null) {
                listado.add(linea);
            }
        }
        archivo.close();
        Toast.makeText(this, "cargado", Toast.LENGTH_LONG).show();
        String datos[] = listado.toArray(new String[listado.size()]);
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, datos);
        ListView.setAdapter(adaptador);
    }


}
