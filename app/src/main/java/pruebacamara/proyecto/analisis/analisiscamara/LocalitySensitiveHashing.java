package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by:
 * Pablo Brenes
 * Jeison Esquivel
 */

abstract class LocalitySensitiveHashing {

    //Referencia a la actividad central
    Context context;

    //Constantes para archivos
    static final String SUFFIX_FILE = ".txt";
    static final File BUCKETS_PATH = new File(
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/Buckets");

    //Variables para ejecución
    int[][] hiperplanos;
    int cantidadHiperplanos;
    ArrayList<String[]> buckets;

    /**
     * Builder de la clase, llamado al instanciar un nuevo objeto, requiere el contexto de la
     * aplicación principal para mantenerlas ligadas y la cantidad de hashes que estára creando
     *
     * @param context             Contexto de la aplicación que instancia a esta
     * @param cantidadHiperplanos Cantidad de hiperplanos que cortaran el vector
     */
    LocalitySensitiveHashing(Context context, int cantidadHiperplanos) {
        this.context = context;
        this.cantidadHiperplanos = cantidadHiperplanos;
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
    abstract void processImage(String source);

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
     *
     * @param hash Hash del que se desea obtener las imágenes
     * @return String, con todos los nombres de las imágenes separadas por un caracter ';'
     */
    String getImagesOfHash(String hash) {
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
    Bitmap toGrayscale(Bitmap image) {
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
     * Almacena la información necesaria para ligar la imagen dada junto al hash dado por parámetro
     *
     * @param imagen nombre de la imagen que se asocia a ese hash
     * @param hash   hash de la imagen a almacenar
     */
    void guardarHash(String imagen, String hash) {
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
    abstract void loadBucketsData();

    /**
     * Almacena en un archivo txt las imágenes asociadas a un bucket
     */
    abstract void saveBucketsData();

    /**
     * En caso de no existir hiperplanos creados para la cantidad deseada, se encarga de crearlos
     */
    abstract void crearHiperplanos();

    /**
     * Guarda en un archivo de texto los hiperplanos creados, se encarga de crear el estandar
     * para el nombre del archivo
     */
    abstract void guardarHiperplanos();

    /**
     * En caso de existir un archivo con hiperplanos para la cantidad requerida, procede a cargarlos
     */
    abstract void cargarHiperplanos();

    /**
     * Verifica la existencia del archivo necesario para guardar los hash
     *
     * @return Boolean, existe ? true | false
     */
    abstract boolean checkData();

    /**
     * Verifica la existencia del archivo necesario para guardar los hashes de imágenes
     *
     * @return Boolean, existe ? true | false
     */
    abstract boolean checkBucketData();
}
