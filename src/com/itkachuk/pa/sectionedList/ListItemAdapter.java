package com.itkachuk.pa.sectionedList;

import java.util.ArrayList;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListItemAdapter extends ArrayAdapter<SectionedListItem> {

	private Context context;
	private ArrayList<SectionedListItem> items;
	private LayoutInflater vi;

	public ListItemAdapter(Context context, ArrayList<SectionedListItem> items) {
		super(context,0, items);
		this.context = context;
		this.items = items;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		final SectionedListItem i = items.get(position);
		if (i != null) {
			if (i.isSection()) {
				SectionItem si = (SectionItem)i;
				v = vi.inflate(R.layout.section_row, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
			} else {
				if (i instanceof Account) {
					Account account = (Account) i;
					v = vi.inflate(R.layout.account_row, null);
					fillText(v, R.id.accountName, account.getName());
					fillText(v, R.id.accountCurrency, account.getCurrency());
					fillText(v, R.id.accountDescription, account.getDescription());	
				} else if (i instanceof Category) {
					Category category = (Category) i;
					v = vi.inflate(R.layout.category_row, null);
					fillText(v, R.id.categoryName, category.getName());
					if (category.isExpense()) {
						fillText(v, R.id.categoryGroup, context.getResources().getString(R.string.expenses_text));
						setTextColor(v, R.id.categoryGroup, context.getResources().getColor(R.color.expense_amount_color));
					} else {
						fillText(v, R.id.categoryGroup, context.getResources().getString(R.string.incomes_text));
						setTextColor(v, R.id.categoryGroup, context.getResources().getColor(R.color.income_amount_color));
					}	
				}
			}
		}
		return v;
	}

	private void fillText(View v, int id, String text) {
		TextView textView = (TextView) v.findViewById(id);
		textView.setText(text == null ? "" : text);
	}
	
	private void setTextColor(View v, int id, int colorCode) {
		TextView textView = (TextView) v.findViewById(id);
		textView.setTextColor(colorCode);
	}
}
