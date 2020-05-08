package kaist.hcil.magtouchlibrary.fragment.demo;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kaist.hcil.magtouchlibrary.R;
import kaist.hcil.magtouchlibrary.datamodel.CameData;
import kaist.hcil.magtouchlibrary.datamodel.MagTouchRequestPacket;
import kaist.hcil.magtouchlibrary.datamodel.TapData;
import kaist.hcil.magtouchlibrary.fragment.MessageFragment;
import kaist.hcil.magtouchlibrary.fragment.core.MagTouchFragment;
import kaist.hcil.magtouchlibrary.util.NowTimestamp;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsAppFragment extends MagTouchFragment {

    protected ProgressBar progressBar;
    ListView mListView;
    ContactsAdapter contactsAdapter;

    Contact selectedContact = null;

    Handler delayRunHandler;

    public ContactsAppFragment() {
        // Required empty public constructor
    }

    public static ContactsAppFragment newInstance() {
        ContactsAppFragment fragment = new ContactsAppFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        delayRunHandler = new Handler();

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_contacts_app, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        mListView = view.findViewById(R.id.contacts_list_view);

        loadSvmModule();
        setContentsInListView();
        setItemClickListener();
        return view;
    }

    private void loadSvmModule()
    {
        // Load SVM Module
        File storageDir = Environment.getExternalStorageDirectory();
        magTouch.loadModels(storageDir);
    }

    private void setContentsInListView()
    {
        ArrayList<Contact> contactList = generateContacts();
        contactsAdapter = new ContactsAdapter(getActivity(), contactList);
        mListView.setAdapter(contactsAdapter);
    }

    private ArrayList<Contact> generateContacts()
    {
        ArrayList<Contact> contactList = new ArrayList<>();
        contactList.add(new Contact("Amirah", "Kerr"));
        contactList.add(new Contact("Gabrielle", "Collier"));
        contactList.add(new Contact("Laiba", "Franco"));
        contactList.add(new Contact("David", "Jones"));
        contactList.add(new Contact("Olivia", "Shaw"));
        contactList.add(new Contact("Mia", "Hughes"));
        contactList.add(new Contact("Hollie", "Thompson"));
        contactList.add(new Contact("Alice", "Johnson"));
        contactList.add(new Contact("Ethan", "Woods"));
        contactList.add(new Contact("Archer", "Garrison"));
        contactList.add(new Contact("Seamus", "Greene"));
        contactList.add(new Contact("Bella", "Cunningham"));
        contactList.add(new Contact("Eloise", "Wood"));
        contactList.add(new Contact("Zayne", "Gallegos"));
        contactList.add(new Contact("Dylan", "Hunter"));
        contactList.add(new Contact("Lucille", "Dickerson"));
        contactList.add(new Contact("Chaya", "Fitzpatrick"));
        contactList.add(new Contact("Rylee", "Rose"));
        contactList.add(new Contact("Jodie", "Gallegos"));
        contactList.add(new Contact("Dylan", "Armstrong"));
        contactList.add(new Contact("Abby", "Riley"));

        return contactList;
    }

    private void setItemClickListener()
    {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int[] viewPos = new int[2];
                view.getLocationOnScreen(viewPos);
                int width = view.getWidth();
                int height = view.getHeight();
                int x = viewPos[0] + width/2;
                int y = viewPos[1] + height/2;

                MagTouchRequestPacket packet = new MagTouchRequestPacket(NowTimestamp.inSec(), x, y, TapData.Finger.DONT_KNOW, true);
                runMagTouch(packet);
                selectedContact = (Contact)contactsAdapter.getItem(position);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected void handleCameData(CameData cameData) {

    }

    @Override
    protected void handleClassified(MagTouchRequestPacket packet, CameData cameData, String predictedFinger) {
        if (selectedContact == null)
        {
            return;
        }

        int displayType = -1;

        if(TapData.Finger.INDEX.equals(predictedFinger))
        {
            displayType = Contact.DisplayType.NORMAL;
        }
        else if (TapData.Finger.MIDDLE.equals(predictedFinger))
        {
            displayType = Contact.DisplayType.CALL;
        }
        else if (TapData.Finger.RING.equals(predictedFinger))
        {
            displayType = Contact.DisplayType.MESSAGE;
        }

        selectedContact.setDisplayType(displayType);

        delayRunHandler.post(new Runnable() {
            @Override
            public void run() {
                contactsAdapter.notifyDataSetChanged();
            }
        });

        final String msg = selectedContact.getDisplayText();


        delayRunHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openMessageFragment(msg);
                selectedContact.setDisplayType(Contact.DisplayType.NORMAL);
                contactsAdapter.notifyDataSetChanged();
                selectedContact = null;
            }
        }, 750);


    }

    @Override
    protected void handleStabilized() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void handleStateChangedTo(int state) {

    }

    @Override
    protected void respondCameDataAfter(MagTouchRequestPacket packet, CameData cameData) {

    }

    public void openMessageFragment(String msg)
    {
        MessageFragment msgFragment = MessageFragment.newInstance(msg);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_container, msgFragment)
                .addToBackStack(null)
                .commit();
    }

}
