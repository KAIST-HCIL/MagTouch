package kaist.hcil.magtouchlibrary.log;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.ml.MLNode;

public class LogReader {
    private File externalStorage;
    private String absoluteDir;
    private String subDir;

    public LogReader(File externalStorage, int type)
    {
        this.externalStorage = externalStorage;
        subDir = LogWriter.typeToSubDir(type);
    }

    public ArrayList<MLNode> readMLNodes()
    {
        absoluteDir = externalStorage.getAbsolutePath() + LogWriter.dataDir + subDir;

        File dir = new File(absoluteDir);
        ArrayList<MLNode> nodes = new ArrayList<>();
        for(final File fileEntry: dir.listFiles())
        {
            String filePath = absoluteDir + "/" + fileEntry.getName();
            ArrayList<MLNode> fileNodes = readFileAsMLNode(filePath);
            nodes.addAll(fileNodes);
        }
        return nodes;
    }

    public ArrayList<MLNode> readFileAsMLNode(String filePath)
    {
        ArrayList<MLNode> nodes = new ArrayList<>();
        try
        {
            FileInputStream inputStream = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
                nodes.add(MLNode.parseLog(line));
            }
        }
        catch (Exception e)
        {
            Log.e("FingMag", e.toString());
        }
        return nodes;
    }
}
