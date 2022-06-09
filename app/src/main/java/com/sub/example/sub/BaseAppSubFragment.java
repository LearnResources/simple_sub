package com.sub.example.sub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.petprojects.sub.PurchaseHelper;
import com.petprojects.sub.SubConfigPrefs;
import com.petprojects.sub.base.BaseSubFragment;
import com.petprojects.sub.util.SubLogUtils;
import com.sub.example.R;
import com.sub.example.sub.PurchasePack;
import com.sub.example.sub.Sub;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import me.relex.circleindicator.CircleIndicator;


public abstract class BaseAppSubFragment extends BaseSubFragment {

    private final Handler handler = new Handler();
    private final long AUTO_SCROLL_PAGER_DELAY = 5000;

    protected String selectedPack = Sub.getPackDefault();
    private int countDownTime = 5;
    private View btClose;
    private TextView tvTime;
    private ViewPager vpCover;
    private CircleIndicator coverIndicator;
    private AtomicInteger page;
    private Runnable autoScrollRunnable;

    private final Map<String, String> textChangeOnPackUpdateMap = new HashMap<>();

    @Override
    protected void initViews(View rootView) {
        textChangeOnPackUpdateMap.put(Sub.PACK_SUB_WEEK, "Get Your 3-Days Free");
        textChangeOnPackUpdateMap.put(Sub.PACK_SUB_YEAR, "Try it FREE for 3 days!");
        this.vpCover = rootView.findViewById(R.id.vp_cover);
        this.coverIndicator = rootView.findViewById(R.id.sub_indicator);

        this.initActionButton(rootView);
        if (!isTrialFragment()) {
            this.initPackList(rootView);
        }
        this.initSubDesc(rootView);
        this.initTextFeature(rootView);
        this.initContinueLimitVersionButton(rootView);
        this.initRestoreButton(rootView);
        this.initMediaView(rootView);
        this.initTimerAndCloseButton(rootView);
    }

    private void initRestoreButton(View rootView) {
        Context context = rootView.getContext();
        TextView btRestorePurchase = (TextView) findViewByName(rootView, "bt_restore_purchase");
        if (btRestorePurchase == null) {
            return;
        }
        btRestorePurchase.setText(fromHtml(context, R.string.restore));
        btRestorePurchase.setOnClickListener(view -> {
//            FireBaseLogEvents.getInstance().log("SUB_RESTORE");
            if (PurchaseHelper.getInstance().isRemovedAds(context)) {
                PurchaseHelper.getInstance().purchaseSuccess();
            } else {
                Toast.makeText(context, R.string.sub_not_vip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViewPagerCoverIfPossible() {
        if (this.vpCover == null || this.coverIndicator == null) {
            return;
        }
        List<Integer> coverImages = getCoverImages();
        vpCover.setVisibility(View.VISIBLE);
        coverIndicator.setVisibility(View.VISIBLE);
        vpCover.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return coverImages.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @SuppressLint("ClickableViewAccessibility")
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                LayoutInflater inflater = LayoutInflater.from(container.getContext());
                View layout = inflater.inflate(R.layout.sub_item_cover, container, false);
                layout.setOnTouchListener((v, event) -> {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        handler.removeCallbacks(autoScrollRunnable);
                    } else if (action == MotionEvent.ACTION_UP
                            || action == MotionEvent.ACTION_CANCEL) {
                        startAutoScrollCoverPager(coverImages);
                    }
                    return true;
                });
                ImageView ivCover = layout.findViewById(R.id.iv_cover);
                ivCover.setImageResource(coverImages.get(position));
                container.addView(layout);
                return layout;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        });
        this.startAutoScrollCoverPager(coverImages);
        coverIndicator.setViewPager(vpCover);
    }

    private void startAutoScrollCoverPager(List<Integer> coverImages) {
        if (page == null) {
            page = new AtomicInteger();
        }
        autoScrollRunnable = new Runnable() {
            public void run() {
                if (coverImages.size() == page.get()) {
                    page.set(0);
                } else {
                    page.getAndIncrement();
                }
                vpCover.setCurrentItem(page.get(), true);
                handler.postDelayed(this, AUTO_SCROLL_PAGER_DELAY);
            }
        };
        handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_PAGER_DELAY);
    }

    private void initTimerAndCloseButton(View rootView) {
        this.tvTime = rootView.findViewById(R.id.tv_time);
        if (this.tvTime != null) {
            this.tvTime.setBackgroundResource(getTimeBackgroundResourceId());
            this.tvTime.setTextColor(ContextCompat.getColor(rootView.getContext(), getTimeTextColorResourceId()));
        }

        btClose = rootView.findViewById(R.id.tv_close);
        if (btClose != null) {
            btClose.setBackgroundResource(getCloseButtonResourceId());
            btClose.setOnClickListener(view -> {
                try {
                    if (!close()) {
                        Log.i("superman", "initTimerAndCloseButton: close");
                        requireActivity().finish();
                    }
                    Log.i("superman", "initTimerAndCloseButton: 1");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
        startCountDownTimeIfPossible();
    }

    private void startCountDownTimeIfPossible() {
        if (this.countDownTime == 0) {
            this.tvTime.setVisibility(View.GONE);
            this.btClose.setVisibility(View.VISIBLE);
            return;
        }
        boolean enableSubShowCountDown = SubConfigPrefs.get().isEnableSubShowCountDown();
        if (enableSubShowCountDown) {
            this.tvTime.setVisibility(View.VISIBLE);
            this.tvTime.setText(String.valueOf(countDownTime));
        }
        this.btClose.setVisibility(View.INVISIBLE);
        handler.postDelayed(new Runnable() {
            public void run() {
                countDownTime--;
                if (countDownTime < 1) {
                    btClose.setVisibility(View.VISIBLE);
                    if (enableSubShowCountDown) {
                        tvTime.setText("X");
                        tvTime.setVisibility(View.GONE);
                    }
                    return;
                }
                tvTime.setText(String.valueOf(countDownTime));
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    protected boolean isTrialFragment() {
        return false;
    }

    protected int getTimeBackgroundResourceId() {
        return R.drawable.sub_vector_time;
    }

    protected int getTimeTextColorResourceId() {
        return R.color.white;
    }

    protected int getCloseButtonResourceId() {
        return R.drawable.sub_vector_close;
    }

    private void initTextFeature(View rootView) {

        List<String> featureTexts = getSubFeatureTexts();
        for (int i = 0; i < featureTexts.size(); i++) {
            int viewOrder = i + 1;
            TextView tvFeature = (TextView) findViewByName(rootView, "tv_feature_" + viewOrder);
            if (tvFeature != null) {
                tvFeature.setText(fromHtml(rootView.getContext(), featureTexts.get(i)));
            }
        }
    }

    private void initMediaView(View rootView) {
        VideoView videoView = rootView.findViewById(R.id.video_view);
        ImageView ivSubCover = rootView.findViewById(R.id.iv_sub_cover);
        List<Integer> coverImages = getCoverImages();
        if (videoView != null && getVideoResourceId() > 0) {
            if (coverImages == null || coverImages.isEmpty()) {
                ivSubCover.setVisibility(View.GONE);
            } else {
                ivSubCover.setVisibility(View.VISIBLE);
                ivSubCover.setBackgroundResource(coverImages.get(0));
            }

            String uriPath = "android.resource://" + rootView.getContext().getPackageName() + "/" + getVideoResourceId();
            Uri uri = Uri.parse(uriPath);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(uri);
            videoView.setOnCompletionListener(mp -> {
                ivSubCover.setVisibility(View.VISIBLE);
            });
            videoView.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.setLooping(true);
                if (!SubConfigPrefs.get().isEnableSubSound()) {
                    mediaPlayer.setVolume(0, 0);
                }
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (currentPosition > 0) {
                    mediaPlayer.seekTo(currentPosition);
                }
                mediaPlayer.start();
                new Handler().postDelayed(() -> ivSubCover.setVisibility(View.GONE), 200);
            });
            videoView.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    ivSubCover.setVisibility(View.GONE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    ivSubCover.setVisibility(View.VISIBLE);
                }
                return false;
            });
            videoView.start();
            return;
        }
        if (videoView != null) {
            videoView.setVisibility(View.GONE);
        }

        if (coverImages == null || coverImages.isEmpty()) {
            return;
        }

        if (coverImages.size() == 1) {
            ivSubCover.setVisibility(View.VISIBLE);
            ivSubCover.setBackgroundResource(coverImages.get(0));
            return;
        }
        initViewPagerCoverIfPossible();
    }

    protected int getVideoResourceId() {
        return -1;
    }

    protected List<Integer> getCoverImages() {
        return Collections.emptyList();
    }

    protected List<PurchasePack> getPurchasePacks() {
        return Collections.emptyList();
    }

    private void initPackList(View rootView) {
        View layoutListPack = rootView.findViewById(R.id.layout_list_pack);
        if (layoutListPack == null) {
            return;
        }

        List<PurchasePack> purchasePacks = getPurchasePacks();
        if (purchasePacks.isEmpty()) {
            purchasePacks = Sub.getPurchasePacks();
        }
        int size = purchasePacks.size();
        for (int i = 0; i < size; i++) {
            try {
                PurchasePack purchasePack = purchasePacks.get(i);
                int viewOrder = i + 1;

                View layoutPack = findViewByName(rootView, "layout_pack_" + viewOrder);
                String pack = purchasePack.getPack();
                layoutPack.setTag(pack);
                layoutPack.setOnClickListener(view -> {
                    resetSelected(rootView, size);
                    view.setSelected(true);
                    Object viewTag = view.getTag();
                    if (viewTag != null) {
                        selectedPack = (String) viewTag;
                    }
                    updateCtaText(rootView, pack);
//                    FireBaseLogEvents.getInstance().log("SUB_SCR_CHANGE_PACK");
                    makePurchase();
                });

                String packDefault = Sub.getPackDefault();
                if (packDefault.equalsIgnoreCase(pack)) {
                    layoutPack.setSelected(true);
                    selectedPack = packDefault;
                    updateCtaText(rootView, selectedPack);
                }

                View viewBestOffer = findViewByName(rootView, String.format(Locale.US,
                        "iv_pack_%d_best_offer", viewOrder));
                if (viewBestOffer != null) {
                    viewBestOffer.setVisibility(Sub.PACK_BEST_OFFER.equalsIgnoreCase(pack) ? View.VISIBLE : View.GONE);
                }

                String titleViewIdString = String.format(Locale.US, "tv_pack_%d_title", viewOrder);
                TextView tvPackTitle = (TextView) findViewByName(rootView, titleViewIdString);
                tvPackTitle.setAllCaps(isPackAllCap() || pack.equalsIgnoreCase(packDefault));
                tvPackTitle.setSelected(layoutPack.isSelected());
                tvPackTitle.setText(purchasePack.getPackTitle());

                String priceViewIdString = String.format(Locale.US, "tv_pack_%d_price", viewOrder);
                TextView tvPrice = (TextView) findViewByName(rootView, priceViewIdString);
                tvPrice.setSelected(layoutPack.isSelected());
                tvPrice.setText(purchasePack.getDescString(layoutPack.getContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCtaText(View rootView, String pack) {
        TextView btAction = rootView.findViewById(R.id.bt_action);
        String ctaText = textChangeOnPackUpdateMap.get(pack);
        if (!TextUtils.isEmpty(ctaText)) {
            btAction.setText(ctaText);
        } else {
            btAction.setText(getActionButtonText());
        }
    }

    protected boolean isPackAllCap() {
        return false;
    }

    protected String getPackChangeCtaButtonText() {
        return Sub.PACK_SUB_YEAR;
    }

    protected String getCtaTextOnChangePack() {
        try {
            return requireActivity().getString(R.string.sub_try_3_days);
        } catch (Exception e) {
            e.printStackTrace();
            return "Try it FREE for 3 days!";
        }
    }

    private void resetSelected(View rootView, int size) {
        for (int i = 0; i < size; i++) {
            int viewOrder = i + 1;
            View layoutPack = findViewByName(rootView, "layout_pack_" + viewOrder);
            layoutPack.setSelected(false);
        }
    }

    private View findViewByName(View parentView, String viewIdString) {
        return parentView.findViewById(parentView.getResources()
                .getIdentifier(viewIdString, "id", parentView.getContext().getPackageName()));
    }

    private void initContinueLimitVersionButton(View rootView) {
        Context context = rootView.getContext();
        TextView btContinueLimitVersion = (TextView) findViewByName(rootView, "bt_continue_limit_version");
        if (btContinueLimitVersion == null) {
            return;
        }
        if (hideContinuaLimitedButton()) {
            btContinueLimitVersion.setVisibility(View.INVISIBLE);
            return;
        }
        btContinueLimitVersion.setVisibility(View.VISIBLE);
        btContinueLimitVersion.setVisibility(isTrialFragment() ? View.GONE : View.VISIBLE);
        btContinueLimitVersion.setText(fromHtml(context, R.string.sub_continue_with_limited_version));
        btContinueLimitVersion.setOnClickListener(view -> {
            try {
//                FireBaseLogEvents.getInstance().log("SUB_SCR_CONTINUE_LIMITED_VERSION");
                requireActivity().finish();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    private boolean hideContinuaLimitedButton() {
        return true;
    }

    private Spanned fromHtml(Context context, @StringRes int stringResId) {
        return HtmlCompat.fromHtml(context.getResources().getString(stringResId), HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    private Spanned fromHtml(Context context, String text) {
        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    private void initSubDesc(View rootView) {
        TextView tvSubDesc = rootView.findViewById(R.id.tv_sub_desc);
        if (tvSubDesc == null) {
            return;
        }
        PurchaseHelper purchaseHelper = getPurchaseHelper();
        String format = String.format(rootView.getContext().getString(R.string.sub_desc),
                purchaseHelper.getPrice(Sub.PACK_SUB_WEEK), purchaseHelper.getPrice(Sub.PACK_SUB_MONTH), purchaseHelper.getPrice(Sub.PACK_SUB_YEAR));
        String privacyTextHexColor = getPrivacyTextHexColor();
        if (!TextUtils.isEmpty(privacyTextHexColor)) {
            format = format.replaceAll(Sub.DEFAULT_PRIVACY_COLOR, privacyTextHexColor);
        }
        tvSubDesc.setText(fromHtml(getContext(), format));
        initSubDescClickEvent(tvSubDesc);
    }

    protected String getPrivacyTextHexColor() {
        return null;
    }

    private void initSubDescTrial(View rootView) {
        TextView tvSubDesc = rootView.findViewById(R.id.tv_sub_desc);
        if (tvSubDesc == null) {
            return;
        }
        PurchaseHelper purchaseHelper = getPurchaseHelper();
        tvSubDesc.setText(fromHtml(getContext(), rootView.getContext().getString(R.string.sub_desc_trial)));
        initSubDescClickEvent(tvSubDesc);
    }

    private void initSubDescClickEvent(TextView tvSubDesc) {
        tvSubDesc.setOnClickListener(v -> {
//            FireBaseLogEvents.getInstance().log("SUB_SCR_OPEN_POLICY");
//            AppUtil.openPolicy(tvSubDesc.getContext());
        });
    }

    private void initActionButton(View rootView) {
        TextView btAction = rootView.findViewById(R.id.bt_action);
        if (btAction == null) {
            return;
        }
        int actionButtonText = getActionButtonText();
        if (actionButtonText > 0) {
            btAction.setText(actionButtonText);
        }
        btAction.setOnClickListener(view -> makePurchase());
    }

    private void makePurchase() {
        try {
//            FireBaseLogEvents.getInstance().log("SUB_" + selectedPack);
            SubLogUtils.logD(selectedPack);
            if (isSub(selectedPack)) {
                subscription(selectedPack);
            } else {
                buyLifetime(selectedPack);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public abstract int getActionButtonText();

    @Override
    protected int onLayout() {
        return 0;
    }

    @Override
    public Fragment getFragment(Context context) {
        return null;
    }
}
