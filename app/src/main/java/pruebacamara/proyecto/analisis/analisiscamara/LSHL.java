package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */

class LSHL {

    //Referencia a la actividad central
    private Context context;

    //Constantes para archivos
    private static final String PREFIX_HP_FILE = "HP_L_";
    private static final String PREFIX_SB_FILE = "BUCKETS_LPB_";
    private static final String SUFFIX_FILE = ".txt";
    private static final File BUCKETS_PATH = new File(
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buckets");

    //Variables para ejecución
    //Constantes
    private static final int DIMENSION = 256;
    //Data
    private int[][] hiperplanos;
    private int cantidadHiperplanos;
    private ArrayList<String[]> buckets;

    /**
     * Builder de la clase, llamado al instanciar un nuevo objeto, requiere el contexto de la
     * aplicación principal para mantenerlas ligadas y la cantidad de hashes que estára creando
     *
     * @param context             Contexto de la aplicación que instancia a esta
     * @param cantidadHiperplanos Cantidad de hiperplanos que cortaran el vector
     */
    LSHL(Context context, int cantidadHiperplanos) {
        this.context = context;
        this.cantidadHiperplanos = cantidadHiperplanos;
        if (checkData()) {
            cargarHiperplanos();
        } else {
            crearHiperplanos();
        }
        loadBucketsData();
    }

    /**
     * Dado un hash y un nombre como parámetro asigna ese nombre al hash
     *
     * @param hash    Hash al que se le cambiará el nombre
     * @param newName nombre que tendrá el hash
     */
    void nameHash(String hash, String newName) {
        String name[];
        for (String[] bucketName : buckets) {
            name = bucketName[0].split(",");
            if (hash.equals(name[0])) {
                bucketName[0] = name[0] + "," + newName;
                break;
            }
        }
        saveBucketsData();
    }

    /**
     * Función que aplica un resize, degrada a grises y calcula el hash para una imagen dada
     *
     * @param source path fuente de donde se encuentra la imagen
     */
    void processImage(String source) {
        Bitmap bitImage = BitmapFactory.decodeFile(source);
        bitImage = toGrayscale(bitImage);
        int width = bitImage.getWidth();
        int height = bitImage.getHeight();
        int[] linearPixeles = new int[width * height];
        int[][] pixeles = new int[width][height];
        bitImage.getPixels(linearPixeles, 0, width, 0, 0, width, height);
        int desplazamiento;
        for (int i = 0; i < width; i++) {
            desplazamiento = i * width;
            for (int j = 0; j < height; j++) {
                pixeles[i][j] = Math.abs(linearPixeles[desplazamiento + j]) % 256;
            }
        }
        String hash = hashFromImage(pixeles);
        String name = source.substring(source.lastIndexOf("/") + 1);
        Log.i("DEBUG", "Hash = " + hash + " image " + name);
        //guardarHash(name, hash);
    }

    /**
     * Retorna los buckets existentes
     *
     * @return ArrayList<String>, con los nombres de los buckets que actualmente existen.
     */
    ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        if (buckets.isEmpty()) {
            loadBucketsData();
        }
        for (String[] bucket : buckets) {
            names.add(bucket[0]);
        }
        return names;
    }

    /**
     * Retorna todas las imágenes asociadas a un hash dado por parámetro
     * @param hash Hash del que se desea obtener las imágenes
     * @return String, con todos los nombres de las imágenes separadas por un caracter ';'
     */
    String getImagesOfHash (String hash) {
        for (String[] bucket : buckets) {
            if (hash.equals(bucket[0])) {
                return bucket[1];
            }
        }
        return null;
    }

    /**
     * Dado un bitmap, retorna una copia de este en escala de grises
     *
     * @param image imagen fuente que será colocada en escala de grises
     * @return Bitmap, el mismo bitmap en escala de grises
     */
    private Bitmap toGrayscale(Bitmap image) {
        int height = image.getHeight();
        int width = image.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(f);
        canvas.drawBitmap(image, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * Calcula el LSH para una imagen con previos hiperplanos creados
     *
     * @param pixeles vector unidimensional con los pixeles de la imagen
     * @return String, hash obtenido para la imagen
     */
    private String hashFromImage(int[][] pixeles) {
        return "helloWorld!";
    }

    /**
     * Almacena la información necesaria para ligar la imagen dada junto al hash dado por parámetro
     *
     * @param imagen nombre de la imagen que se asocia a ese hash
     * @param hash   hash de la imagen a almacenar
     */
    private void guardarHash(String imagen, String hash) {
        boolean alreadyExists = false;
        String[] newBucket = {hash, imagen};
        if (checkBucketData()) {
            if (buckets.isEmpty())
                loadBucketsData();
            for (String[] bucket : buckets) {
                String onlyHash = bucket[0].split(",")[0];
                if (hash.equals(onlyHash)) {
                    bucket[1] = bucket[1] + ";" + imagen;
                    alreadyExists = true;
                    break;
                }
            }
            if (!alreadyExists) {
                buckets.add(newBucket);
            }
            saveBucketsData();
        } else {
            buckets = new ArrayList<>();
            buckets.add(newBucket);
            saveBucketsData();
        }
    }

    /**
     * Carga a memoria los datos de los buckets desde el archivo en caso de existir
     */
    private void loadBucketsData() {
        File toLoad = new File(BUCKETS_PATH, PREFIX_SB_FILE + cantidadHiperplanos + SUFFIX_FILE);
        ArrayList<String[]> tempBuckets = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(toLoad);
            String[] temp;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                temp = line.split("/");
                tempBuckets.add(temp);
            }
        } catch (FileNotFoundException e) {
            Log.e("FileError", "Error al cargar los buckets | Datos no creados");
        }
        buckets = tempBuckets;
    }

    /**
     * Almacena en un archivo txt las imágenes asociadas a un bucket
     */
    private void saveBucketsData() {
        MainActivity.createDirBuckets();
        File toSave = new File(BUCKETS_PATH, PREFIX_SB_FILE + cantidadHiperplanos + SUFFIX_FILE);
        try {
            PrintWriter printWriter = new PrintWriter(toSave);
            for (String[] bucket : buckets) {
                String dato = bucket[0] + "/" + bucket[1];
                printWriter.println(dato);
            }
            printWriter.flush();
            printWriter.close();
            ScannerMedia.scanFile(context, toSave.getAbsolutePath());
        } catch (FileNotFoundException e) {
            Log.e("FileError", "Error al guardar los buckets", e);
        }
    }

    /**
     * En caso de no existir hiperplanos creados para la cantidad deseada, se encarga de crearlos
     */
    private void crearHiperplanos() {
        hiperplanos = new int[cantidadHiperplanos][DIMENSION];
        Random randomGenerator = new Random(SystemClock.currentThreadTimeMillis());
        for (int i = 0; i < cantidadHiperplanos; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                hiperplanos[i][j] = randomGenerator.nextInt(513) - 256;
            }
        }
        guardarHiperplanos();
    }

    /**
     * Guarda en un archivo de texto los hiperplanos creados, se encarga de crear el estandar
     * para el nombre del archivo
     */
    private void guardarHiperplanos() {
        MainActivity.createDirBuckets();
        File toSave = new File(BUCKETS_PATH, PREFIX_HP_FILE + cantidadHiperplanos + SUFFIX_FILE);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(toSave));
            oos.writeObject(hiperplanos);
            oos.flush();
            oos.close();
            ScannerMedia.scanFile(context, toSave.getAbsolutePath());
            Log.i("File", "Hiperplanos guardados exitosamente");
        } catch (IOException e) {
            Log.e("FileError", "Error guardado hiperplanos", e);
        }
    }

    /**
     * En caso de existir un archivo con hiperplanos para la cantidad requerida, procede a cargarlos
     */
    private void cargarHiperplanos() {
        File toLoad = new File(BUCKETS_PATH, PREFIX_HP_FILE + cantidadHiperplanos + SUFFIX_FILE);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(toLoad));
            Object aux = ois.readObject();
            hiperplanos = (int[][]) aux;
            ois.close();
            Log.i("File", "Hiperplanos cargados correctamente");
        } catch (ClassNotFoundException | IOException e) {
            Log.e("FileError", "Error cargando hiperplanos", e);
        }
    }

    /**
     * Verifica la existencia del archivo necesario para guardar los hash
     *
     * @return Boolean, existe ? true | false
     */
    private boolean checkData() {
        File data = new File(BUCKETS_PATH, PREFIX_HP_FILE + cantidadHiperplanos + SUFFIX_FILE);
        return data.exists();
    }

    /**
     * Verifica la existencia del archivo necesario para guardar los hashes de imágenes
     *
     * @return Boolean, existe ? true | false
     */
    private boolean checkBucketData() {
        File data = new File(BUCKETS_PATH, PREFIX_SB_FILE + cantidadHiperplanos + SUFFIX_FILE);
        return data.exists();
    }

}