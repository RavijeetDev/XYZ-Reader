package com.ravijeet.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ravijeet.xyzreader.data.ArticleLoader;
import com.ravijeet.xyzreader.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    final PaletteTransformation paletteTransformation = PaletteTransformation.instance();
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private int[] position;
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private FloatingActionButton shareButton;



    private CollapsingToolbarLayout collapsingToolbarLayout;

    private ImageView mPhotoView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }


        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) mRootView.findViewById(R.id.toolbar));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);



        shareButton = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });






        return mRootView;
    }


    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        Toolbar titleView = (Toolbar) mRootView.findViewById(R.id.toolbar);
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        final TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);

        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {

            String snackBarMessage = getString(R.string.storyLoadedMessage);
            int snackbarDisplayLength = Snackbar.LENGTH_LONG;
            int snackbarColor = getResources().getColor(R.color.colorPrimary);

            Snackbar informationalSnackbar = Snackbar.make(this.mRootView, snackBarMessage, snackbarDisplayLength);
            informationalSnackbar.getView().setBackgroundColor(snackbarColor);
            informationalSnackbar.show();

            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

            collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            } else {

                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                        + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));

            }
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)
                    .replaceAll("(\r\n|\n)", "<br />")));

            String url = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            Picasso.with(getActivity())
                    .load(url)
                    .placeholder(R.drawable.photo_background_protection)
                    .transform(paletteTransformation)
                    .into(mPhotoView
                            , new Callback.EmptyCallback() {
                                @Override public void onSuccess() {

                                    try {
                                        Bitmap bitmap = ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap(); // Ew!
                                        Palette palette = PaletteTransformation.getPalette(bitmap);

                                        int primary = getResources().getColor(R.color.colorPrimary);

                                        bylineView.setBackgroundColor(palette.getMutedColor(primary));
                                    }catch (Exception e){
                                        
                                    }
                        }
                            });



        } else {

            String snackBarErrorMessage = getString(R.string.unableToLoadStory);
            int snackbarDisplayLength = Snackbar.LENGTH_INDEFINITE;
            int snackbarColor = getResources().getColor(R.color.colorPrimary);
            String actionLabelMessage = getString(R.string.dismissActionMessage);

            final Snackbar errorSnackbar = Snackbar.make(this.mRootView, snackBarErrorMessage,snackbarDisplayLength);
            errorSnackbar.getView().setBackgroundColor(snackbarColor);
            errorSnackbar.setAction(actionLabelMessage, new View.OnClickListener (){

                        @Override
                public void onClick(View view) {
                    errorSnackbar.dismiss();
                        }
                    });
            errorSnackbar.show();

            mRootView.setVisibility(View.GONE);
            titleView.setTitle("N/A");
            titleView.setSubtitle("N/A" );
            bodyView.setText("N/A");
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }




}
