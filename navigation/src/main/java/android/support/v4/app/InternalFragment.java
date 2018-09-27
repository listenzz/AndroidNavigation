package android.support.v4.app;

public class InternalFragment extends DialogFragment {

    public int getContainerId() {
        return mContainerId;
    }

    @Override
    protected void dismissInternal(boolean allowStateLoss) {
        super.dismissInternal(allowStateLoss);
    }
}
