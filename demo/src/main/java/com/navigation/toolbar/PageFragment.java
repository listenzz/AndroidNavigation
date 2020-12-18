package com.navigation.toolbar;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.TestNavigationFragment;
import com.navigation.androidx.FragmentHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Listen on 2018/2/1.
 */

public class PageFragment extends BaseFragment {

    private static final String ARG_TITLE = "title";

    public static PageFragment newInstance(String title) {
        PageFragment fragment = new PageFragment();
        Bundle args = FragmentHelper.getArguments(fragment);
        args.putString(ARG_TITLE, title);
        return fragment;
    }

    private PageAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        RecyclerView recyclerView =  view.findViewById(R.id.list);
        adapter = new PageAdapter(getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((position, view1) -> {
            requireNavigationFragment().pushFragment(new TestNavigationFragment());
        });

        dataAsyncTask = new DataAsyncTask();
        dataAsyncTaskListener = items -> adapter.setData(items);
        dataAsyncTask.setListener(dataAsyncTaskListener);
        Bundle args = FragmentHelper.getArguments(PageFragment.this);
        String title = args.getString(ARG_TITLE);
        dataAsyncTask.execute(title);
    }

    DataAsyncTask dataAsyncTask;
    DataAsyncTask.DataAsyncTaskListener dataAsyncTaskListener;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dataAsyncTask.setListener(null);
        dataAsyncTask.cancel(true);
    }

    static class DataAsyncTask extends AsyncTask<String, Void, List<String>> {

        interface DataAsyncTaskListener {
            void onResult(List<String> items);
        }

        DataAsyncTaskListener listener;

        public void setListener(DataAsyncTaskListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            List<String> items = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                items.add(strings[0]);
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            if (listener != null) {
                listener.onResult(strings);
            }
        }

    }

    static class PageAdapter extends RecyclerView.Adapter<PageAdapter.MyViewHolder> {

        private List<String> items = new ArrayList<>();
        private LayoutInflater inflater;

        private OnItemClickListener itemClickListener;

        public PageAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        public void setData(List<String> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_page, parent, false);
            final MyViewHolder holder = new MyViewHolder(view);
            holder.itemView.setOnClickListener(v -> {
                int position = holder.getAdapterPosition();
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position, v);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            String item = items.get(position);
            holder.titleTextView.setText(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setOnItemClickListener(OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        static class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView titleTextView;

            MyViewHolder(View itemView) {
                super(itemView);
                titleTextView =  itemView.findViewById(R.id.title);
            }
        }
    }

}
