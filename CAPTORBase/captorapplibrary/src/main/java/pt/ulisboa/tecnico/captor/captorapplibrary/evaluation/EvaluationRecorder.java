package pt.ulisboa.tecnico.captor.captorapplibrary.evaluation;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EvaluationRecorder {
    private static final String TAG = "EvaluationRecorder";
    private static final String FILE_NAME = "STOPEvaluationFile.txt";

    private static EvaluationRecorder instance = null;
    private File evaluationFile;

    protected EvaluationRecorder() {}

    public static EvaluationRecorder getInstance() {
        if (instance == null) {
            instance = new EvaluationRecorder();
        }
        return instance;
    }

    private boolean setEvaluationFile() {
        if (! isExternalStorageWritable()) {
            return false;
        }
        if (evaluationFile != null) {
            return true;
        }
        evaluationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
        if (! evaluationFile.exists()) {
            try {
                if (!evaluationFile.getParentFile().mkdirs() && !evaluationFile.createNewFile()) {
                    Log.e(TAG, "Directory or file not created");
                    return false;
                }
            } catch (IOException e) {
                Log.e(TAG, "File not created");
                return false;
            }
        }
        return true;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean writeLine(String line) {
        if (! setEvaluationFile()) {
            return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(evaluationFile, true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            //Log: STOPLOG,Timestamp,LINE
            line = "STOPLOG," + new SimpleDateFormat("yyyyMMddHH:mm:ss:SSS").format(new Date()) + "," + line;
            writer.append(line);
            writer.append(System.getProperty("line.separator"));
            writer.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found");
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Could not write to file");
            return false;
        }
        return true;
    }

}
