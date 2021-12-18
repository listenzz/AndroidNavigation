package androidx.fragment.app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InternalFragment extends DialogFragment {

    public int getContainerId() {
        return mContainerId;
    }

    private boolean dismissed;

    public boolean isDismissed() {
        return dismissed;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        dismissed = true;
    }

    @Override
    public void dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss();
        dismissed = true;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dismissed = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dismissed = true;
    }

    @Override
    protected void performViewCreated() {
        super.performViewCreated();
    }

    @Override
    protected void performCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.performCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void performDestroyView() {
        super.performDestroyView();
    }
}
