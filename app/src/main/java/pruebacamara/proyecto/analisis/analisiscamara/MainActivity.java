package pruebacamara.proyecto.analisis.analisiscamara;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */

public class MainActivity extends AppCompatActivity {

    //Para Intents
    public final static String EXTRA_HASH_SELECTED =
            "pruebacamara.proyecto.analisis.analisiscamara.HASH_SELECTED";
    public final static String EXTRA_TYPE_SELECTED =
            "pruebacamara.proyecto.analisis.analisiscamara.TYPE_SELECTED";
    public final static String EXTRA_IMAGES =
            "pruebacamara.proyecto.analisis.analisiscamara.IMAGES";

    //Variables de códigos para obtener permisos por parte del usuario
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2;

    //Variables de códigos para los intents creados
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SELECT_PICTURE = 2;

    //Constantes para archivos
    private static final String ID_PHOTOS = "P3_";
    private static final String SUFFIX_PHOTO = ".jpg";

    //Permisos obtenidos
    private boolean WRITE_PERMISSION = false;
    private boolean CAMERA_PERMISSION = false;

    //Constante de localización de archivos
    private static final File BUCKETS_PATH =
            new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buckets");

    //Variables para ejecución
    //Constantes
    private static final int SIZE_HASH_PIXELS = 10;
    static final int PIXEL_SPINNER_ID = 0;
    static final int LBP_SPINNER_ID = 1;
    //Data
    private LocalitySensitiveHashingHandler hashManipulator;
    private String currentPhotoPath;
    private int currentMethod;
    private String newName;
    ArrayList<String> hashNames;
    //UI
    private Spinner sppinerGroupType;
    private ListView listViewHash;

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
        checkPermissionsWriteExternalStorage();
        initVariables();
        initComponents();
    }

    /**
     * Verifica permiso para la escritura y lectura del espacio externo de memoria
     */
    private void checkPermissionsWriteExternalStorage() {
        if (ContextCompat.checkSelfPermission(this,                                                 //Verifica si es posible escribir en la memoria del teléfono
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {                                             //Se solicita el permiso en caso de no tenerlo
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {                                                                                    //Si el permiso estaba disponible habilita la opción
            WRITE_PERMISSION = true;
        }
    }

    /**
     * Verifica permiso para utilizar la camara
     */
    private void checkPermissionsCAMERA() {
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
        listViewHash = (ListView) findViewById(R.id.listViewHash);
        configClickOnList();                                                                        //Agrega el evento de click a el list view
    }

    /**
     * Inicia variables
     */
    private void initVariables() {
        if (WRITE_PERMISSION) {
            hashManipulator = new LocalitySensitiveHashingHandler(getApplicationContext(), SIZE_HASH_PIXELS);
        }
        newName = null;
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

    /**
     * Da la opción de elegir una imagen desde la galería prefereida por el usuario
     *
     * @param view Requerido para ligar este método al botón desde el xml
     */
    public void choosePic(View view) {
        if (WRITE_PERMISSION) {
            Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,                                   //Action pick: lanzalanzar una actividad que muestre una liste de objetos a seleccionar para que el usuario elija uno de ellos
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPictureIntent.setType("image/*");                                                       //Muestra las imagenes de cualquier extensión
            startActivityForResult(
                    Intent.createChooser(pickPictureIntent, "Abrir con"),
                    REQUEST_SELECT_PICTURE);
        } else {
            checkPermissionsWriteExternalStorage();
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
        if (WRITE_PERMISSION) {
            checkPermissionsCAMERA();
            if (CAMERA_PERMISSION) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);             //Nuevo Intent para capturar una imagen.
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {               //Se verifica que hayan aplicaciones en el sistema capacez de recibir el intent.
                    File photoFile = null;                                                          //Puntero a un File, que luego se intenta crear para manejar la exepción de error.
                    try {
                        photoFile = createImageFile();                                              //Se intenta crear el archivo
                    } catch (IOException e) {
                        System.out.println("Error al crear el archivo.");
                    }
                    if (photoFile != null) {                                                        //Si el archivo fue creado con éxito
                        Uri photoUri = FileProvider.getUriForFile(this,                             //Se crea un URI con el anterior archivo creado, un URI es una especie de identificador de archivo universal
                                "pruebacamara.proyecto.analisis.analisiscamara.fileprovider",
                                photoFile);                                                         //Requiere la actividad que lo lanza, el PROVIDER del permiso que lo concede y el archivo al asociar el URI
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);              //Se envía al Intent de la captura como una salida extra el URI para que pueda guardar la foto en él.
                        startActivityForResult(
                                Intent.createChooser(takePictureIntent, "Tomar con"),
                                REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        }
    }

    /**
     * Configura el evento de click para el ListView con los hash, crea una nueva actividad para ver las
     * imágenes para ese método de cálculo con el hash seleccionado
     */
    private void configClickOnList() {
        listViewHash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                String item = hashNames.get(pos);
                String images = hashManipulator.getImagesFromType(item, currentMethod);
                Intent intent = new Intent(getApplicationContext(), ViewImagesActivity.class);
                intent.putExtra(EXTRA_HASH_SELECTED, item);
                intent.putExtra(EXTRA_TYPE_SELECTED, currentMethod);
                intent.putExtra(EXTRA_IMAGES, images);
                startActivity(intent);
            }
        });
        listViewHash.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                String item = hashNames.get(pos);
                captureNameFromUser(item);
                return true;
            }
        });
    }

    /**
     * Crea un dialogo con un edittext para cambiar el nombre a un bucket
     */
    private void captureNameFromUser(String item) {
        String[] forSplit = item.split(",");
        final String hash = forSplit[0];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_changeName);

        final EditText input = new EditText(this);
        input.setHint(R.string.textedit_name);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if (forSplit.length > 1) {
            input.setText(forSplit[1]);
            input.setSelection(forSplit[1].length());
        }
        builder.setView(input);

        builder.setPositiveButton(R.string.button_okChange, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newName = input.getText().toString();
                hashManipulator.nameHash(hash, newName, currentMethod);
                switch (currentMethod) {
                    case PIXEL_SPINNER_ID:
                        setListView();
                        break;
                    case LBP_SPINNER_ID:
                        setListView();
                        break;
                }
                newName = null;
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newName = null;
                dialog.cancel();
            }
        });

        builder.show();
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
                currentMethod = position;
                setListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Actualiza el list view con los hash de píxeles
     */
    private void setListView() {
        if (WRITE_PERMISSION) {
            hashNames = hashManipulator.getNames(currentMethod);
            ArrayList<String> hashForList = new ArrayList<>();
            for (String name : hashNames) {
                String[] names = name.split(",");
                if (names.length > 1) {
                    hashForList.add(names[1]);
                } else {
                    hashForList.add(names[0]);
                }
            }
            ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, hashForList);
            listViewHash.setAdapter(adaptador);
        }
    }

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

        createDirBuckets();
        File image = new File(BUCKETS_PATH, imageFileName + SUFFIX_PHOTO);                          //Se crea el archivo
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Dado un Uri, obtiene su path, crea dos archivos un fuente desde el Uri y uno nuevo para
     * copiar los datos a este desde el Uri
     *
     * @param sourceUri Uri del archivo que desea ser copiado a un nuevo archivo
     */
    private void newFileFromUri(Uri sourceUri) {
        String path = getPath(sourceUri);
        compressImage(path, false);
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
     * Método de compresión incríblemente bueno, comparado al que utiliza whatsapp
     *
     * @param imagePath  Path donde se encuentra la imagen fuente
     * @param withDelete En caso de que se desee borrar la imagen fuente
     */
    public void compressImage(String imagePath, boolean withDelete) {
        Bitmap scaledBitmap = null;
        float maxHeight = 1280.0f;
        float maxWidth = 1280.0f;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        if (scaledBitmap != null) {
            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

            bmp.recycle();

            ExifInterface exif;
            try {
                exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out;
            try {
                out = new FileOutputStream(createImageFile());

                //write the compressed bitmap at the destination specified by filename.
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.flush();
                out.close();

                if (withDelete) {
                    File toDelete = new File(imagePath);
                    boolean deleted = toDelete.delete();
                    if (!deleted) {
                        Log.e("FileError", "No se pudo borrar el archivo");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Auxiliar para compressImage, calcula un tamaño en base al radio
     *
     * @param options Optiones del bitmap a crear
     * @param reqWidth width calculado por el compress image
     * @param reqHeight height calculador por el compress image
     * @return Int, tamaño calculado para la imagen
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * Verifica si el directorio de trabajo de la aplicación se encuentra disponible
     *
     * @return valor booleano, true: existe/fue creado false: error
     */
    static boolean createDirBuckets() {
        return BUCKETS_PATH.exists() || BUCKETS_PATH.mkdir();
    }

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
                        compressImage(currentPhotoPath, true);
                        ScannerMedia.scanFile(this, currentPhotoPath);
                        hashManipulator.processImage(currentPhotoPath);
                    }
                }
                break;
            case REQUEST_SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    Uri path = data.getData();
                    newFileFromUri(path);
                    if (currentPhotoPath != null) {
                        ScannerMedia.scanFile(this, currentPhotoPath);
                        hashManipulator.processImage(currentPhotoPath);
                    }
                }
                break;
        }
        if (resultCode == RESULT_OK) {
            setListView();
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
                initVariables();
                configSpinnerGroupType();
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


}
