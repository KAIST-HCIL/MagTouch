package kaist.hcil.magtouchlibrary.log;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogWriter {
    public static final int TYPE_RAW = 100;
    public static final int TYPE_ML = 101;
    public static final int TYPE_EXP = 102;
    public static final int TYPE_SITU = 104;
    private File externalStorage;
    private LogWriteTask backgroundTask;

    public static final String dataDir = "/magtouch_data";
    public static final String rawDir = "/raw";
    public static final String mlDir = "/ml";
    public static final String expDir = "/exp";
    public static final String situDir = "/situ";

    private String subDir;

    private String absoluteDir;
    private File logFile;
    ConcurrentLinkedQueue<String> logQueue;
    private LogWriterCallback callback;

    public LogWriter(File externalStorage, int type)
    {
        this.externalStorage = externalStorage;
        subDir = typeToSubDir(type);
    }

    public static String typeToSubDir(int type)
    {
        if(type == TYPE_RAW)
        {
            return rawDir;
        }
        else if(type == TYPE_ML)
        {
            return mlDir;
        }
        else if(type == TYPE_EXP)
        {
            return expDir;
        }
        else if(type == TYPE_SITU)
        {
            return situDir;
        }
        return "";
    }

    public void init(LogWriterCallback callback)
    {
        absoluteDir = externalStorage.getAbsolutePath() + dataDir + subDir;

        // you can pull the logs by >adb pull /sdcard/magtouch_data
        // or /storage/emulated/0/magtouch_data
        makeDirectory(absoluteDir);
        logFile = createFile();
        logQueue = new ConcurrentLinkedQueue<>();
        backgroundTask = new LogWriteTask(logQueue);
        backgroundTask.setCallback(callback);
        backgroundTask.execute();
    }

    private File makeDirectory(String dirPath)
    {
        File dir = new File(dirPath);

        if(!dir.exists())
        {
            boolean result = dir.mkdirs();
        }
        return dir;
    }

    private File createFile()
    {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss");
        String formattedDate = df.format(currentTime);
        String logFileName = formattedDate + ".txt";
        File file = new File(absoluteDir, logFileName);
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            Log.d("FingMagError", e.toString());
            Log.d("FingMagError", absoluteDir);
            Log.d("FingMagError", logFileName);
        }
        return file;
    }

    public boolean isLogRemaining()
    {
        return !logQueue.isEmpty();
    }

    public void writeLog(String log)
    {
        logQueue.add(log);
    }

    public void writeLogChunk(ArrayList<String> logs)
    {
        for (String log: logs)
        {
            writeLog(log);
        }
        if(logs.isEmpty())
        {
            backgroundTask.getCallback().doneWriting();
        }
    }

    private void flush()
    {
        backgroundTask.flush();
    }

    public void terminate()
    {
        backgroundTask.stop();
    }

    private class LogWriteTask extends AsyncTask<Void, Void, Void>
    {
        LogWriterCallback callback;
        String inputText;
        FileOutputStream outputStream;
        OutputStreamWriter writer;
        ConcurrentLinkedQueue<String> logQueue;
        boolean isRunning;

        public LogWriteTask(ConcurrentLinkedQueue logQueue)
        {
            try
            {
                outputStream = new FileOutputStream(logFile, true);
                writer = new OutputStreamWriter(outputStream);
            }
            catch (Exception e)
            {
                Log.d("FingMagError", e.toString());
            }

            this.logQueue = logQueue;
            isRunning = true;

        }
        public void setCallback(LogWriterCallback callback)
        {
            this.callback = callback;
        }
        public LogWriterCallback getCallback(){ return callback; }
        public void stop() { isRunning = false; }
        public void setInputText(String input)
        {
            this.inputText = input;
        }
        public void flush()
        {
            try
            {
                outputStream.flush();
                writer.flush();
            }
            catch (Exception e)
            {
                Log.d("FingMagError", e.toString() );
            }
        }
        @Override
        protected Void doInBackground(Void... params) {

            while(isRunning)
            {

                while(!logQueue.isEmpty())
                {
                    String lineToWrite = logQueue.poll() + "\n";
                    try
                    {
                        writer.append(lineToWrite);
                        outputStream.flush();
                        writer.flush();
                        if(callback != null)
                        {
                            callback.doneWriting();

                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("FingMagError", e.toString() );
                    }
                }
            }
            return null;
        }
    }
}
