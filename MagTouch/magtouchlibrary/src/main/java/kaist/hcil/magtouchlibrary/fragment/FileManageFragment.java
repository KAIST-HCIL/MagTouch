package kaist.hcil.magtouchlibrary.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.log.LogWriter;

/*
    Shows files related to MagTouch
 */
public class FileManageFragment extends Fragment {


    TextView fileTextView;
    Button resetButton;

    public FileManageFragment() {
        // Required empty public constructor
    }

    public static FileManageFragment newInstance() {
        FileManageFragment fragment = new FileManageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_manage, container, false);

        fileTextView = view.findViewById(R.id.file_text_view);
        fileTextView.setMovementMethod(new ScrollingMovementMethod());

        String fileNames = getFileNames();
        fileTextView.setText(fileNames);
        resetButton = view.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseAll();
            }
        });
        return view;
    }

    private String getFileNames()
    {

        File rootFile = getRootFile();
        if(!rootFile.exists())
        {
            return "";
        }

        return getFileNamesInDir(rootFile, "");
    }

    private String getFileNamesInDir(File parent, String prefix)
    {
        String fileNames = parent.getName() + "\n";
        prefix += ">";

        File[] files = parent.listFiles();
        if(files == null)
        {
            return "";
        }
        for(File child : files)
        {
            if(child.isDirectory())
            {
                fileNames += (prefix + " " + getFileNamesInDir(child, prefix));
            }
            else
            {
                fileNames += (prefix + " " + child.getName() + "\n");
            }
        }

        return fileNames;
    }

    private File getRootFile()
    {
        File storageDir = Environment.getExternalStorageDirectory();
        String absoluteDir = storageDir.getAbsolutePath() + LogWriter.dataDir;
        File file = new File(absoluteDir);
        return file;
    }

    public void eraseAll()
    {
        File rootFile = getRootFile();
        if(!rootFile.exists())
        {
            return;
        }

        deleteDir(rootFile);
        finish();
    }

    private void deleteDir(File parent)
    {
        File[] files = parent.listFiles();
        for(File child : files)
        {
            if(child.isDirectory())
            {
                deleteDir(child);
            }
            else
            {
                child.delete();
            }
        }
        parent.delete();
    }

    private void finish()
    {
        getActivity().finish();
    }

}
