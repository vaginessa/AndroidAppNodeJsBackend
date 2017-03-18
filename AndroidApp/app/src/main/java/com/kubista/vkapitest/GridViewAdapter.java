package com.kubista.vkapitest;

/**
 * Created by wilek on 2017-03-13.
 */
        import android.content.Context;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import com.squareup.picasso.Picasso;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;

        import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class GridViewAdapter extends BaseAdapter {
    private final Context context;
    private  List<String> urls;
    private  List<String> bigPhotosUrls;
    private  List<Integer> ids;
    public GridViewAdapter(Context context,List<String> urls,List<String> bigPhotosUrls,List<Integer> ids) {
        this.context = context;
        this.bigPhotosUrls = bigPhotosUrls;
        this.ids = ids;
        this.urls = urls;
        // Triple up the list.
       /* ArrayList<String> copy = new ArrayList<>(urls);
        for(int i = 0 ; i < 200 ; i ++) {
            urls.addAll(copy);
            urls.addAll(copy);
        } */
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
       // String url = getItem(position);
        String url =   urls.get(position);
        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                .placeholder(R.drawable.placeholder) //
                .error(R.drawable.error) //
                .fit() //
                .tag(context) //
                .into(view);

        return view;
    }

    @Override public int getCount() {
        return urls.size();
    }

    @Override public String getItem(int position) {
        return bigPhotosUrls.get(position);

    }

    @Override public long getItemId(int position) {
        return ids.get(position);
    }
}