package pruebacamara.proyecto.analisis.analisiscamara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

class LocalitySensitiveHashingPixels extends LocalitySensitiveHashing {

    //Constantes para archivos
    private static final String PREFIX_HP_FILE = "HP_P_";
    private static final String PREFIX_SB_FILE = "BUCKETS_PIXEL";

    //Variables para ejecución
    private static final int WIDTH_PIC = 256;
    private static final int HEIGHT_PIC = 256;
    private static final int SIZE_PIC = 256 * 256;

    /**
     * Builder de la clase, llamado al instanciar un nuevo objeto, requiere el contexto de la
     * aplicación principal para mantenerlas ligadas y la cantidad de hashes que estára creando
     *
     * @param context             Contexto de la aplicación que instancia a esta
     * @param cantidadHiperplanos Cantidad de hiperplanos que cortaran el vector
     */
    LocalitySensitiveHashingPixels(Context context, int cantidadHiperplanos) {
        super(context, cantidadHiperplanos);
        if (checkData()) {
            cargarHiperplanos();
        } else {
            crearHiperplanos();
        }
        loadBucketsData();
    }

    /**
     * Función que aplica un resize, degrada a grises y calcula el hash para una imagen dada
     *
     * @param source path fuente de donde se encuentra la imagen
     */
    @Override
    void processImage(String source) {
        Bitmap bitImage = BitmapFactory.decodeFile(source);
        bitImage = resizeImage(bitImage);
        bitImage = toGrayscale(bitImage);
        int[] pixeles = new int[SIZE_PIC];
        bitImage.getPixels(pixeles, 0, WIDTH_PIC, 0, 0, WIDTH_PIC, HEIGHT_PIC);
        String hash = hashFromImage(pixeles);
        String name = source.substring(source.lastIndexOf("/") + 1);
        Log.i("DEBUG", "Hash = " + hash + " image " + name);
        guardarHash(name, hash);
    }

    /**
     * Calcula el LocalitySensitiveHashingHandler para una imagen con previos hiperplanos creados
     *
     * @param pixeles vector unidimensional con los pixeles de la imagen
     * @return String, hash obtenido para la imagen
     */
    private String hashFromImage(int[] pixeles) {
        String hash = "";
        long suma = 0;
        for (int i = 0; i < SIZE_PIC; i++) {
            pixeles[i] = Math.abs(pixeles[i]) % 256;
        }
        for (int i = 0; i < cantidadHiperplanos; i++) {
            for (int j = 0; j < SIZE_PIC; j++) {
                suma += hiperplanos[i][j] * pixeles[j];
            }
            if (suma > 0)
                hash += "1";
            else
                hash += "0";
            suma = 0;
        }
        return hash;
    }

    /**
     * Carga a memoria los datos de los buckets desde el archivo en caso de existir
     */
    @Override
    void loadBucketsData() {
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
    @Override
    void saveBucketsData() {
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
    @Override
    void crearHiperplanos() {
        hiperplanos = new int[cantidadHiperplanos][SIZE_PIC];
        Random randomGenerator = new Random(SystemClock.currentThreadTimeMillis());
        for (int i = 0; i < cantidadHiperplanos; i++) {
            for (int j = 0; j < SIZE_PIC; j++) {
                hiperplanos[i][j] = randomGenerator.nextInt(513) - 256;
            }
        }
        guardarHiperplanos();
    }

    /**
     * Guarda en un archivo de texto los hiperplanos creados, se encarga de crear el estandar
     * para el nombre del archivo
     */
    @Override
    void guardarHiperplanos() {
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
    @Override
    void cargarHiperplanos() {
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
    @Override
    boolean checkData() {
        File data = new File(BUCKETS_PATH, PREFIX_HP_FILE + cantidadHiperplanos + SUFFIX_FILE);
        return data.exists();
    }

    /**
     * Verifica la existencia del archivo necesario para guardar los hashes de imágenes
     *
     * @return Boolean, existe ? true | false
     */
    @Override
    boolean checkBucketData() {
        File data = new File(BUCKETS_PATH, PREFIX_SB_FILE + cantidadHiperplanos + SUFFIX_FILE);
        return data.exists();
    }

}
