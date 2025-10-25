package com.example.login_signup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DocumentsFragment extends Fragment {

    public DocumentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout fragment_documents.xml cho Fragment này
        View view = inflater.inflate(R.layout.fragment_documents, container, false);

        // TODO: Thêm findViewById và logic cho các nút trong fragment_documents.xml ở đây
        // Ví dụ: ImageButton menuButton = view.findViewById(R.id.btn_menu);
        //        menuButton.setOnClickListener(...);

        return view;
    }
}