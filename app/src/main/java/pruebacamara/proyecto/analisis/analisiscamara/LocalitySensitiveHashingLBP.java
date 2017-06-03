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

class LocalitySensitiveHashingLBP extends LocalitySensitiveHashing {

    //Constantes para archivos
    private static final String PREFIX_HP_FILE = "HP_L_";
    private static final String PREFIX_SB_FILE = "BUCKETS_LPB_";

    //Variables para ejecución
    private static final int DIMENSION = 256;

    /**
     * Builder de la clase, llamado al instanciar un nuevo objeto, requiere el contexto de la
     * aplicación principal para mantenerlas ligadas y la cantidad de hashes que estára creando
     *
     * @param context             Contexto de la aplicación que instancia a esta
     * @param cantidadHiperplanos Cantidad de hiperplanos que cortaran el vector
     */
    LocalitySensitiveHashingLBP(Context context, int cantidadHiperplanos) {
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
        bitImage = toGrayscale(bitImage);
        bitImage = resizeImage(bitImage);
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
        guardarHash(name, hash);
    }

    /**
     * Calcula el LocalitySensitiveHashingHandler para una imagen con previos hiperplanos creados
     *
     * @param pixeles vector unidimensional con los pixeles de la imagen
     * @return String, hash obtenido para la imagen
     */
    private String hashFromImage(int[][] pixeles) {
        int[] LBP = LBP(pixeles);
        normalizar(LBP);
        String hash = "";
        long suma = 0;/*
        for (int i = 0; i < DIMENSION; i++) {
            LBP[i] = Math.abs(LBP[i]) % 256;
        }*/
        for (int i = 0; i < cantidadHiperplanos; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                suma += hiperplanos[i][j] * LBP[j];
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
     * Funcion: Calculation of LBP values of una celda especifica.
     *
     * @param matriz es la matriz de la imagen en escala de grises
     * @param i      posicion del a fila de la celda central de la matrix de 3x3
     * @param j      posicion del a fila de la celda central de la matrix de 3x3
     * @return el LBPH
     */
    private int sumarValoresIntermedios(int[][] matriz, int i, int j) {
        int res = 0;
        int verto9[] = {0, 0, 0, 0, 0, 0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int l = 0; l < 3; l++) {
                if (l % 2 == 0 || k % 2 == 0) {
                    try {
                        int valor = matriz[i][j] - matriz[i - 1 + k][j - 1 + l];
                        if (valor >= 0) {
                            switch (k) {
                                case 0:
                                    verto9[7 - l] = (int) Math.pow(2, 7 - l);
                                    break;
                                case 1:
                                    if (l == 0)
                                        verto9[0] = 1;
                                    else {
                                        if (l != j)
                                            verto9[4] = (int) Math.pow(2, 4);
                                    }
                                    break;
                                case 2:
                                    verto9[l + 1] = (int) Math.pow(2, l + 1);
                                    break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        for (int aVerto9 : verto9) {
            res += aVerto9;
        }
        return res;
    }

    /**
     * Retorna un vector de 256 relacionada con valores calculados de LBP
     *
     * @param matriz es la matriz de la imagen
     * @return un vector de tamaño de 256
     */
    private int[] LBP(int[][] matriz) {
        int escalar;
        int[] normal = new int[256];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                escalar = sumarValoresIntermedios(matriz, i, j);
                normal[escalar] = 1 + normal[escalar];
            }
        }
        return normal;
    }

    /**
     * Dado un set de datos, normaliza estos al rango de 0-256
     *
     * @param vector Vector con el set de datos
     */
    private void normalizar(int[] vector) {
        long tempPromedio = 0;
        double desviaciónEstandar = 0;
        for (int escalar : vector) {
            tempPromedio += escalar;
        }
        Double promedio = (double) tempPromedio / vector.length;
        for (int escalar : vector) {
            desviaciónEstandar += Math.pow(escalar - promedio, 2);
        }
        desviaciónEstandar /= vector.length;
        double temp;
        int tempInt;
        for (int i = 0; i < vector.length; i++) {
            temp = vector[i] - promedio;
            temp /= desviaciónEstandar;
            temp = Math.abs(temp);
            temp *= 256;
            tempInt = (int) Math.floor(temp);
            vector[i] = tempInt;
        }
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
