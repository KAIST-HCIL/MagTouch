package kaist.hcil.magtouchlibrary.fragment.demo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kaist.hcil.magtouchlibrary.R;

public class ContactsAdapter extends BaseAdapter {
    private ArrayList<Contact> contacts;
    private Context context;
    private LayoutInflater mInflater;

    private int green = Color.parseColor("#388E3C");
    private int blue = Color.parseColor("#0091EA");
    private int black = Color.parseColor("#000000");
    public ContactsAdapter(Context context, ArrayList<Contact> contacts)
    {
        this.context = context;
        this.contacts = contacts;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = mInflater.inflate(R.layout.contacts_list_item, parent, false);
        TextView nameTextView = rowView.findViewById(R.id.contacts_item_text_view);
        Contact contact = (Contact)getItem(position);
        nameTextView.setText(contact.getDisplayText());

        if(contact.getDisplayType() == Contact.DisplayType.CALL)
        {
            nameTextView.setTextColor(black);
            rowView.setBackgroundColor(green);
        }
        else if (contact.getDisplayType() == Contact.DisplayType.MESSAGE)
        {
            nameTextView.setTextColor(black);
            rowView.setBackgroundColor(blue);
        }

        return rowView;
    }
}
