package app.jeankn.view.persistentsearch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class PersistentSearch extends MaterialCardView {

    TextInputEditText input;
    AppCompatImageButton backButton;
    AppCompatImageButton clearButton;
    RecyclerView list;

    ArrayList<String> suggestions;
    SearchAdapter adapter;

    OnSearchListener searchListener;

    private int shortAnimationDuration;
    private boolean clearVisible = false;

    public PersistentSearch(Context context) {
        super(context);
        init();
    }

    public PersistentSearch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

        setAttrs(context, attrs);

    }

    public PersistentSearch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        setAttrs(context, attrs);
    }

    private void init() {
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        inflate(getContext(), R.layout.persistentsearch, this);

        input = findViewById(R.id.input);
        backButton = findViewById(R.id.back);
        clearButton = findViewById(R.id.clear);
        list = findViewById(R.id.list);

        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                input.setText("");
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if ((charSequence.length() > 0) != clearVisible) {
                    if (charSequence.length() > 0) {
                        clearVisible = true;

                        clearButton.setAlpha(0f);
                        clearButton.setVisibility(View.VISIBLE);
                        clearButton.animate()
                                .alpha(1f)
                                .setDuration(shortAnimationDuration)
                                .setListener(null);
                    } else {
                        clearVisible = false;

                        clearButton.animate()
                                .alpha(0f)
                                .setDuration(shortAnimationDuration)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        clearButton.setVisibility(View.GONE);
                                    }
                                });
                    }
                }
                updateSuggestions(input.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        input.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    doSearch();
                    return true;
                }
                return false;
            }
        });

        suggestions = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(layoutManager);
        adapter = new SearchAdapter(getContext());
        list.setAdapter(adapter);
        adapter.setOnClickListener(new SearchAdapter.OnClickListener() {
            @Override
            public void onClick(String value) {
                input.setText(value);
                doSearch();
            }
        });
        updateSuggestions("");
    }

    private void setAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PersistentSearch,
                0, 0);
        String hint = "";
        try {
            hint = a.getString(R.styleable.PersistentSearch_hint);
        } finally {
            a.recycle();
        }

        input.setHint(hint);
    }

    public void setSuggestions(ArrayList<String> suggestions) {
        this.suggestions = suggestions;
        updateSuggestions(input.getText().toString());
    }

    private void updateSuggestions(String filter) {
        ArrayList<String> items = new ArrayList<String>();

        if (!filter.equals("")) {
            for (String str : suggestions) {
                if (str.toUpperCase().contains(filter.toUpperCase())) {
                    items.add(str);
                }
            }
        }
        adapter.setItems(items);

        invalidate();
        requestLayout();
    }

    public void hideSuggestions() {
        updateSuggestions("");
    }

    public void setBackButtonClickListener(OnClickListener listener) {
        backButton.setOnClickListener(listener);
    }

    private void doSearch() {
        hideSuggestions();
        if (searchListener != null) {
            searchListener.searchResult(input.getText().toString());
        }
    }

    public void setText(String text) {
        input.setText(text);
        doSearch();
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.searchListener = listener;
    }

    public interface OnSearchListener {
        void searchResult(String value);
    }
}
