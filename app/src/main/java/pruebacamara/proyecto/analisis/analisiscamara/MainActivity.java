package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Variables de códigos para obtener permisos por parte del usuario
    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    //Permisos obtenidos
    boolean WRITE_PERMISSION = false;

    //Variables de códigos para los intents creados
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SELECT_PICTURE = 2;

    //Constantes para archivos
    static final String ID_PHOTOS = "P3_";
    static final String SUFFIX_PHOTO = ".jpg";

    //Variables para ejecución
    String currentPhotoPath;
    ImageView imageView;

    private ImageView vistaImagen;
    private Button botonOpciones;




    /**
     * Se ejecuta cada vez que la aplicación es lanzada:
     *      Agrega la UI user interface
     *      Inicia variables por búsqueda de ID en los xml
     *      Verifica los permisos disponibles para la aplicación
     * @param savedInstanceState Bundle con el savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vistaImagen = (ImageView) findViewById(R.id.imageView);
        botonOpciones = (Button) findViewById(R.id.buttonOpciones);
        checkPermissions();
        initComponents();

        //agrega metodo al boton de opciones
        botonOpciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptions();
            }
        });


    }

    /** Muestra  las opciones al presionar el boton de opciones
     *
     */
    private void showOptions() {
        final CharSequence[] option = {"Abrir Camara", "Elegir de galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Eleige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Abrir Camara"){
                    openCamera(findViewById(R.id.ok));
                }else if(option[which] == "Elegir de galeria"){
                    Intent intent = new Intent(Intent.ACTION_PICK,                                  // action pick: lanzalanzar una actividad que muestre una liste de objetos a seleccionar para que el usuario elija uno de ellos
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");                                                      // muestra las imagenes de cualquier extensión
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"),
                            SELECT_PICTURE);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }


    /** Método sobre-escrito de Andriod
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){                                                                // verifica que la respuesta sea la indicada
            switch (requestCode){
                case SELECT_PICTURE:
                    Uri path = data.getData();
                    vistaImagen.setImageURI(path);                                                  // asigna la imagen al imageView
                    break;
            }
        }
    }


    /**
     * Verifica permisos necesarios para que la aplicación funcione
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,                                                 // Verifica si es posible escribir en la memoria del teléfono
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {                                                                                           //Se solicita el permiso en caso de no tenerlo
            ActivityCompat.requestPermissions(this,
                    new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {                                                                                    //Si el permiso estaba disponible habilita la opción
            WRITE_PERMISSION = true;
        }
    }

    /**
     * Inicia variables por el find en un xml
     */
    private void initComponents() {
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    /**
     * Función que se llama al presionar el botón CÁMARA
     *      Crea el intent para solicitar usar una aplicación externa como cámara y crea un File pa-
     * ra su posterior almacenamiento.
     * @param view Requerido para poder ligar este método al botón desde el XML
     * Importante que esta función al ser asignada a un button (view) debe cumplir SIEMPRE con:
     *      Ser pública.
     *      Retornar vacio.
     *      Únicamente poseer un parámetro view.
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
                            "pruebacamara.proyecto.analisis.analisiscamara.fileprovider", photoFile);              //Requiere la actividad que lo lanza, el PROVIDER del permiso que lo concede y el archivo al asociar el URI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);                  //Se envía al Intent de la captura como una salida extra el URI para que pueda guardar la foto en él.
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);               //Inicia la actividad y espera el resultado (sin uso actualmente puede ser cambiado por stratActivity())
                }

            }
        }
    }

    /**
     * Crear un archivo para almacenar la imagen y asegura de que el directorio exista previamente
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
            currentPhotoPath = image.getAbsolutePath();                                             //Se almacena el path
            return image;                                                                           //Se regresa la referencia al archivo
        }
        else {
            throw new IOException();                                                                //La función que se encuentra sobre esta maneja la excepción.
        }
    }

    /**
     * Verifica si el directorio dado por parámetro exizte.
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
     * Administra si el permiso fue concedido por el usuario
     * @param requestCode constante creada por el app para identificar cual permiso se otorgo
     * @param permissions necesario para completar el override
     * @param grantResults aquí vienen los permisos obtenidos, GRANTED OR DENIED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {                                   //Casos constantes genereados por la aplicación
                // If request is cancelled, the result arrays are empty.
                WRITE_PERMISSION =
                        grantResults.length > 0 &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED;                       //Se asigna al valor administrador del permiso el resultado del usuario
                break;
            }
        }
    }
}
