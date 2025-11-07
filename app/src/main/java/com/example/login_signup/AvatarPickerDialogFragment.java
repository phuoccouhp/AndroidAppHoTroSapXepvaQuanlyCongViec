package com.example.login_signup;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AvatarPickerDialogFragment extends DialogFragment {

    public interface AvatarPickerListener {
        void onAvatarSelected(String avatarId);
    }

    private AvatarPickerListener listener;
    private static final String[] AVATAR_IDS = {
        "ic_avatar_1", "ic_avatar_2", "ic_avatar_3", "ic_avatar_4", "ic_avatar_5"
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_avatar_picker, null);

        GridView gridView = view.findViewById(R.id.gv_avatars);
        gridView.setAdapter(new AvatarAdapter(getContext()));

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            if (listener != null) {
                listener.onAvatarSelected(AVATAR_IDS[position]);
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AvatarPickerListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AvatarPickerListener");
        }
    }

    private static class AvatarAdapter extends BaseAdapter {
        private Context context;

        public AvatarAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return AVATAR_IDS.length;
        }

        @Override
        public Object getItem(int position) {
            return AVATAR_IDS[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = (ImageView) LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
            } else {
                imageView = (ImageView) convertView;
            }

            int resourceId = context.getResources().getIdentifier(AVATAR_IDS[position], "drawable", context.getPackageName());
            imageView.setImageResource(resourceId);
            return imageView;
        }
    }
}
