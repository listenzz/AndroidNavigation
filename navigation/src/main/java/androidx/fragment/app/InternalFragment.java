package androidx.fragment.app;

public class InternalFragment extends DialogFragment {

    public int getContainerId() {
        return mContainerId;
    }

    @Override
    protected void dismissInternal(boolean allowStateLoss, boolean fromOnDismiss) {
        super.dismissInternal(allowStateLoss, fromOnDismiss);
    }

//    @Override
//    protected void dismissInternal(boolean allowStateLoss) {
//        super.dismissInternal(allowStateLoss);
//    }
}
