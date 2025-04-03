package com.example.campusexpensemanagerse06304.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asm_app_se06304.R;
import com.example.asm_app_se06304.model.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdaper extends BaseAdapter implements Filterable {

    public List<Products> products;
    public List<Products> searchPd;
    public Context context;
    public ProductListAdaper(Context myContext, List<Products> items) {
        super();
        this.products = items;
        this.context = myContext;
    }
    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return products.get(position).get_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewProductList;
        if(convertView == null){
            viewProductList = View.inflate(parent.getContext(), R.layout.product_item_list, null);
        }else{
            viewProductList = convertView;
        }
        //xử lý đổ dữ liệu ra ngoài list view
        Products pd = products.get(position);
        ImageView img = viewProductList.findViewById(R.id.imgProduct);
        TextView tvNamepd = viewProductList.findViewById(R.id.tvProductName);
        TextView tvPricepd = viewProductList.findViewById(R.id.tvProductPrice);

        tvNamepd.setText(pd.get_name());
        tvPricepd.setText(String.valueOf(pd.get_price()));
        Picasso.get().load(pd.get_image()).into(img);

        return viewProductList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oResults = new FilterResults();
                final List<Products> results = new ArrayList<>();
                if (searchPd == null) {
                    searchPd = products;
                }
                if (constraint != null) {

                    if (searchPd != null && !searchPd.isEmpty()) {
                        for (final Products p : searchPd) {
                            if (p.get_name().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                results.add(p);
                            }
                        }
                    }
                    oResults.values = results;
                }
                return oResults;
            }
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                products = (List<Products>) results.values;
                notifyDataSetChanged();

            }
        };
    }
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }
}
