package com.neosolusi.expresslingua.features.flashcard;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.MainActivity;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.features.flashcard.MainAdapter.CardType;
import com.neosolusi.expresslingua.features.stories.StoriesActivity;

public class MainFragment extends Fragment
        implements MainAdapter.OnClickListener, View.OnClickListener {

    public static final String TAG = MainFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private RecyclerView mListMenu;
    private MainAdapter mAdapter;
    private Button mButtonFlashcard, mButtonReading;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MainAdapter(this);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard_main, container, false);

        initComponent(rootView);
        initListener(rootView);
        configureLayout(rootView);

        return rootView;
    }

    private void initComponent(View rootView) {
        mListMenu = rootView.findViewById(R.id.recycler_flashcard);
        mButtonFlashcard = rootView.findViewById(R.id.button_flashcard);
        mButtonReading = rootView.findViewById(R.id.button_reading);
    }

    private void initListener(View rootView) {
        rootView.findViewById(R.id.button_reading).setOnClickListener(this);
        rootView.findViewById(R.id.button_exit).setOnClickListener(this);
    }

    private void configureLayout(View rootView) {
        if (getActivity() == null) return;

        ((AppCompatActivity) getActivity()).setSupportActionBar(rootView.findViewById(R.id.toolbar));
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            setHasOptionsMenu(true);
        }

        mButtonFlashcard.setBackgroundColor(getResources().getColor(R.color.button_indicator));
        mButtonFlashcard.setTextColor(getResources().getColor(R.color.white));
        mButtonReading.setBackgroundColor(getResources().getColor(R.color.white));
        mButtonReading.setTextColor(getResources().getColor(R.color.color_text));

        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        Drawable dividerImage = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
        if (dividerImage != null) divider.setDrawable(dividerImage);

        mListMenu.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListMenu.addItemDecoration(divider);
        mListMenu.setHasFixedSize(true);
        mListMenu.setAdapter(mAdapter);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() == null) return;

        getActivity().getMenuInflater().inflate(R.menu.app_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint("Search Episode...");
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) {
//                mAdapter.update(mDataEpisodes.where().contains("title", s, Case.INSENSITIVE).findAllAsync());
                return true;
            }

            @Override public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        EditText editTextSearch = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editTextSearch.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorFooterPrimary));
        editTextSearch.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.colorFooterPrimary));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null) return false;

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(getActivity());
                if (parentIntent == null) break;
                if (NavUtils.shouldUpRecreateTask(getActivity(), parentIntent)) {
                    TaskStackBuilder.create(getActivity())
                            .addNextIntentWithParentStack(parentIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(getActivity(), parentIntent);
                }
//                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override public void onClick(View view, CardType cardType) {
        mListener.onClick(cardType);
    }

    @Override public void onMasteringClick(View view, CardType cardType, int mastering) {
        mListener.onMasterLevelClick(cardType, mastering);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reading:
                AppUtils.startActivity(getContext(), StoriesActivity.class);
                break;
            case R.id.button_exit:
                AppUtils.startActivity(getContext(), MainActivity.class);
                break;
        }
        mListener.onFinish();
    }

    public interface OnFragmentInteractionListener {
        void onClick(CardType cardType);

        void onMasterLevelClick(CardType cardType, int level);

        void onFinish();
    }
}
